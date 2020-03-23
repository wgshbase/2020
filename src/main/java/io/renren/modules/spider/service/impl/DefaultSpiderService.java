package io.renren.modules.spider.service.impl;

import java.util.List;
import java.util.Map;

import javax.management.JMException;

import org.apache.commons.lang3.tuple.Triple;
import org.quartz.JobKey;
import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mss.crawler.spiderjson.config.SpiderConfig;

import io.renren.modules.spider.entity.SpiderInfo;
import io.renren.modules.spider.model.SchedulerInfo;
import io.renren.modules.spider.model.SpiderRuntimeInfo;
import io.renren.modules.spider.model.Webpage;
import io.renren.modules.spider.service.ISpiderInfoService;
import io.renren.modules.spider.service.ISpiderManager;
import io.renren.modules.spider.service.ISpiderService;

/**
 * 缺省爬虫管理服务类
 * 
 * @author wangdw
 * @email dawei.happy@gmail.com
 * @date 2018年2月14日 上午10:17:09
 */
@Service("defaultSpiderService")
public class DefaultSpiderService implements ISpiderService {
	
	@Autowired
	private ISpiderManager spiderManager;
	@Autowired
	private ISpiderInfoService spiderInfoService;

	@Override
	public String start(String spiderInfoId) {
		
		SpiderInfo spiderInfo = spiderInfoService.getById(spiderInfoId);
		return this.spiderManager.start(SpiderConfig.create(spiderInfo.getJsonData()));
	}

	@Override
	public void stop(String uuid) {
		this.spiderManager.stop(uuid);;
	}

	@Override
	public void delete(String uuid) {
		this.spiderManager.delete(uuid);;
	}

	@Override
	public void deleteAll() {
		this.spiderManager.deleteAll();
	}

	@Override
	public SpiderRuntimeInfo runtimeInfo(String uuid, boolean containsExtraInfo) {
		return this.spiderManager.getSpiderRuntimeInfo(uuid, containsExtraInfo);
	}

	@Override
	public List<SpiderRuntimeInfo> listRunSpider(boolean containsExtraInfo) {
		return this.spiderManager.listAllSpiders(containsExtraInfo);
	}

	@Override
	public List<Webpage> testSpiderInfo(String spiderInfoJson) throws JMException {
		return this.spiderManager.testSpiderInfo(SpiderConfig.create(spiderInfoJson));
	}

	@Override
	public void validateSpiderInfo(String spiderInfo) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String startAll(List<String> spiderInfos) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String createQuartzJob(String spiderInfoId, String cronExp) {
		return this.spiderManager.createQuartzJob(spiderInfoId, cronExp);
	}

	@Override
	public List<SchedulerInfo> listAllQuartzJobs() {
		return this.spiderManager.listAllQuartzJobs();
	}

	@Override
	public String removeQuartzJob(String spiderInfoId) {
		return this.spiderManager.removeQuartzJob(spiderInfoId);
	}

	@Override
	public String checkQuartzJob(String spiderInfoId) {
		return this.spiderManager.checkQuartzJob(spiderInfoId);
	}

	@Override
	public String exportQuartz() {
		return this.spiderManager.exportQuartz();
	}

	@Override
	public void importQuartz(String json) {
		 this.spiderManager.importQuartz(json);
		
	}

}
