package com.mss.crawler.spiderjson.model;

import java.util.Map;

import com.mss.crawler.spiderjson.JSONSpider;
import com.mss.crawler.spiderjson.config.JSONExtractorConfig;

import us.codecraft.webmagic.processor.PageProcessor;

/**
 * 可配置的页面解析类模型
 * @author wangdawei
 *
 */
public interface IConfigAblePageProcessor extends PageProcessor {
	
	/**
	 * 初始化页面解析模型
	 * @param json
	 */
	public void iniPageModel(Map<String,JSONExtractorConfig> jsonMap);
	
	/**
	 * 设置爬虫
	 * @param spider
	 */
	public void setSpider(JSONSpider spider);

}
