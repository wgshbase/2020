package io.renren.modules.spider.service.impl;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.management.JMException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.hash.Hashing;
import com.mss.crawler.spiderjson.JSONSpider;
import com.mss.crawler.spiderjson.SpiderBuilder;
import com.mss.crawler.spiderjson.config.SpiderConfig;
import com.mss.crawler.spiderjson.pipeline.ImgPipeline;
import com.mss.crawler.spiderjson.pipeline.JSONPipeline;

import io.renren.modules.job.entity.ScheduleJobEntity;
import io.renren.modules.job.service.ScheduleJobService;
import io.renren.modules.spider.entity.SpiderInfo;
import io.renren.modules.spider.gather.AsyncGather;
import io.renren.modules.spider.gather.model.State;
import io.renren.modules.spider.gather.model.Task;
import io.renren.modules.spider.model.SchedulerInfo;
import io.renren.modules.spider.model.SpiderRuntimeInfo;
import io.renren.modules.spider.model.Webpage;
import io.renren.modules.spider.service.ISpiderInfoService;
import io.renren.modules.spider.service.ISpiderManager;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.pipeline.Pipeline;
import us.codecraft.webmagic.pipeline.ResultItemsCollectorPipeline;
import us.codecraft.webmagic.scheduler.QueueScheduler;

@Service("defaultSpiderManager")
public class DefaultSpiderManager extends AsyncGather implements ISpiderManager,Serializable {

	private static final long serialVersionUID = 400524272685070043L;

	private static final Logger LOG = LogManager.getLogger(DefaultSpiderManager.class);

	private final String QUARTZ_JOB_GROUP_NAME = "webpage-spider-job";
	private final String QUARTZ_TRIGGER_GROUP_NAME = "webpage-spider-trigger";
	private final String QUARTZ_TRIGGER_NAME_SUFFIX = "-hours";
	private static final String SPIDER_INFO = "spiderInfo";
	private Map<String, JSONSpider> spiderMap = new HashMap<>();
	
	@Autowired
	private ScheduleJobService scheduleJobService;
	
	@Autowired
	private ISpiderInfoService spiderInfoService;
	
	private SpiderBuilder spiderBuilder = new SpiderBuilder();
	
	@Override
	public String start(SpiderConfig info) {
		 
		 boolean running = taskManager.findTaskById(info.getId());
	     Preconditions.checkArgument(!running, "已经提交了这个任务,模板编号%s,请勿重复提交", info.getId());
	        
	     final String uuid = UUID.randomUUID().toString();
	     //初始化任务
	     Task task = taskManager.initTask(uuid, info.getDomain(), info.getCallbackURL(), "spiderInfoId=" + info.getId() + "&spiderUUID=" + uuid);
	     task.addExtraInfo(SPIDER_INFO, info);
        
         JSONSpider spider = (JSONSpider) makeSpider(info, task);
     
         //慎用爬虫监控,可能导致内存泄露
         //spiderMonitor.register(spider);
         spiderMap.put(uuid, spider);
         spider.start();
         taskManager.getTaskById(uuid).setState(State.RUNNING);
         return uuid;
	}
	
	/**
     * 生成爬虫
     *
     * @param info 抓取模板
     * @param task 任务实体
     * @return
     */
    private JSONSpider makeSpider(SpiderConfig info, Task task) {
    	JSONSpider spider = (JSONSpider)spiderBuilder.builder(info);
		spider.setUUID(task.getTaskId());
		return spider;
    }
    
	@Override
	public void stop(String uuid) {
		Preconditions.checkArgument(spiderMap.containsKey(uuid), "找不到UUID为%s的爬虫,请检查参数", uuid);
		spiderMap.get(uuid).stop();
		spiderMap.get(uuid).close();
	    taskManager.getTaskById(uuid).setState(State.STOP);
		
	}

	@Override
	public void delete(String uuid) {
		Preconditions.checkArgument(spiderMap.containsKey(uuid) || taskManager.getTaskById(uuid) != null, "找不到UUID为%s的爬虫,请检查参数", uuid);
	    Preconditions.checkArgument(taskManager.getTaskById(uuid).getState() == State.STOP, "爬虫" + uuid + "尚未停止,不能删除任务");
	    deleteTaskById(uuid);
	    spiderMap.remove(uuid);
		
	}

	@Override
	public void deleteAll() {
		for(Map.Entry<String,JSONSpider> entry:spiderMap.entrySet()){
			if(entry.getValue().getStatus() == Spider.Status.Stopped){
				try {
					
	                deleteTaskById(entry.getKey());
	                spiderMap.remove(entry.getKey());
	            } catch (Exception e) {
	                LOG.error("删除任务ID:{}出错,{}", entry.getKey(), e.getLocalizedMessage());
	            }
			}
			
		}
        taskManager.deleteTasksByState(State.STOP);
		
	}

	@Override
	public List<Webpage> testSpiderInfo(SpiderConfig info) throws JMException {
		  final ResultItemsCollectorPipeline resultItemsCollectorPipeline = new ResultItemsCollectorPipeline();
	      final String uuid = UUID.randomUUID().toString();
	      Task task = taskManager.initTask(uuid, info.getDomain(), info.getCallbackURL(), "spiderInfoId=" + info.getId() + "&spiderUUID=" + uuid);
	      task.addExtraInfo("spiderInfo", info);
	      QueueScheduler queueScheduler = new QueueScheduler();
	      JSONSpider spider = (JSONSpider) makeSpider(info, task)
	                .addPipeline(resultItemsCollectorPipeline)
	                .setScheduler(queueScheduler);

	      spiderMap.put(uuid, spider);
	      taskManager.getTaskById(uuid).setState(State.RUNNING);
	      spider.run();
	      List<Webpage> webpageList = Lists.newLinkedList();
	      resultItemsCollectorPipeline.getCollected().forEach(resultItems -> webpageList.add(convertResultItems2Webpage(resultItems)));
	      return webpageList;
	}
	
	 /**
     * 将webmagic的resultItems转换成webpage对象
     *
     * @param resultItems
     * @return
     */
    public static Webpage convertResultItems2Webpage(ResultItems resultItems) {
        Webpage webpage = new Webpage();      
        webpage.setUrl(resultItems.get("url"));
        webpage.setId(Hashing.md5().hashString(webpage.getUrl(), Charset.forName("utf-8")).toString());
        webpage.setDomain(resultItems.get("domain"));
        webpage.setSpiderInfoId(resultItems.get("spiderInfoId"));
        webpage.setGathertime(resultItems.get("gatherTime"));
        webpage.setSpiderUUID(resultItems.get("spiderUUID"));        
        webpage.setCategory(resultItems.get("category"));
        webpage.setRawHTML(resultItems.get("rawHTML"));              
        webpage.setAttachmentList(resultItems.get("attachmentList"));
        webpage.setImageList(resultItems.get("imageList"));
        webpage.setProcessTime(resultItems.get("processTime"));        
        webpage.setJsonData(resultItems.get("objects"));
        return webpage;
    }

	@Override
	public List<SpiderRuntimeInfo> listAllSpiders(boolean containsExtraInfo) {
		List<SpiderRuntimeInfo> result = new ArrayList<>();
		 for(Map.Entry<String, JSONSpider> spiderEntry:spiderMap.entrySet()){
			 JSONSpider spider = spiderEntry.getValue();
			 result.add(makeSpiderRuntimeInfo(spider, containsExtraInfo));
		 }
	     return result;
	}
	
	/**
     * 根据ID获取爬虫对象
     *
     * @param uuid
     * @return
     */
    public JSONSpider getSpiderById(String uuid) {
        Preconditions.checkArgument(spiderMap.containsKey(uuid), "找不到UUID为%s的爬虫,请检查参数", uuid);
        return spiderMap.get(uuid);
    }

	@Override
	public SpiderRuntimeInfo getSpiderRuntimeInfo(String uuid, boolean containsExtraInfo) {
		return makeSpiderRuntimeInfo(getSpiderById(uuid), containsExtraInfo);
	}
	 /**
     * 获取爬虫运行时信息
     *
     * @param spider
     * @return
     */
    private SpiderRuntimeInfo makeSpiderRuntimeInfo(JSONSpider spider, boolean containsExtraInfo) {
    	SpiderRuntimeInfo infoMap = new SpiderRuntimeInfo();
    	
    	Task task =getTaskById(spider.getUUID(), true);
    	infoMap.setTaskName(task.getName());
    	infoMap.setId(task.getTaskId());
    	infoMap.setPageCount(spider.getPageCount());
    	infoMap.setStartTime(spider.getStartTime());
    	infoMap.setThreadAlive(spider.getThreadAlive());
    	infoMap.setStatus(spider.getStatus());
    	infoMap.setSpiderInfo(spider.getConfig());
       
        if (containsExtraInfo) {
           // infoMap.put("Links", getTaskById(spider.getUUID(), true).getExtraInfoByKey(LINK_KEY));
        }
        return infoMap;
    }

    
    /**
     * 创建定时任务
     *
     * @param spiderInfoId  爬虫模板id
     * @param hoursInterval 每几小时运行一次
     */
    public String createQuartzJob(String spiderInfoId, String cronExp) {
        SpiderInfo spiderInfo = spiderInfoService.getById(spiderInfoId);
        ScheduleJobEntity scheduleJob = new ScheduleJobEntity();
        scheduleJob.setBeanName("CrawlerTask");
        scheduleJob.setMethodName("executeInternal");
        scheduleJob.setParams(spiderInfoId);
        scheduleJob.setCronExpression(cronExp);
        scheduleJob.setRemark(spiderInfo.getSiteName());
        scheduleJobService.save(scheduleJob);
        return spiderInfoId;
    }

	@Override
	public List<SchedulerInfo> listAllQuartzJobs() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String removeQuartzJob(String spiderInfoId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String checkQuartzJob(String spiderInfoId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String exportQuartz() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void importQuartz(String json) {
		// TODO Auto-generated method stub
		
	}

   

}
