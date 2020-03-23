package io.renren.modules.job.task;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.mss.crawler.spiderjson.config.SpiderConfig;

import io.renren.SpringContextUtil;
import io.renren.modules.spider.entity.SpiderInfo;
import io.renren.modules.spider.service.ISpiderManager;

/**
 * Created by gaoshen on 2017/1/18.
 */
@DisallowConcurrentExecution
public class WebpageSpiderJob extends QuartzJobBean {
    private Logger LOG = LogManager.getLogger(WebpageSpiderJob.class);
    private SpiderInfo spiderInfo; 			
    private ISpiderManager spiderManager = SpringContextUtil.getBean(ISpiderManager.class);
	public WebpageSpiderJob setSpiderInfo(SpiderInfo spiderInfo) {
		this.spiderInfo = spiderInfo;
		return this;
	}


	@Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        LOG.info("开始定时网页采集任务，网站：{}，模板ID：{}", spiderInfo.getSiteName(), spiderInfo.getId());
        String uuid = spiderManager.start(SpiderConfig.create(spiderInfo.getJsonData()));
        LOG.info("定时网页采集任务完成，网站：{}，模板ID：{},任务ID：{}", spiderInfo.getSiteName(), spiderInfo.getId(), uuid);
    }
}
