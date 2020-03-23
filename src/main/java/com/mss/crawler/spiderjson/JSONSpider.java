package com.mss.crawler.spiderjson;
import java.util.Map;

import com.mss.crawler.spiderjson.config.JSONExtractorConfig;
import com.mss.crawler.spiderjson.config.SpiderConfig;
import com.mss.crawler.spiderjson.model.IConfigAblePageProcessor;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Spider;

/**
 * json爬虫
 * @author wangdw
 *
 */
public class JSONSpider extends Spider {

	private SpiderConfig config;
	private IConfigAblePageProcessor pageProcessor;
    public JSONSpider(IConfigAblePageProcessor pageProcessor) {
        super(pageProcessor);
        this.pageProcessor = pageProcessor;
        this.pageProcessor.setSpider(this);
    }

    public static JSONSpider create(IConfigAblePageProcessor pageProcessor) {
        return new JSONSpider(pageProcessor);
    }

    public JSONSpider addJSONModel(IConfigAblePageProcessor jsonPageProcessor, Map<String,JSONExtractorConfig> eConfig) {
    	this.pageProcessor = jsonPageProcessor;
    	this.pageProcessor.setSpider(this);
    	pageProcessor.iniPageModel(eConfig);
        return this;
    }
    
    public Page download(Request request){
    	return this.downloader.download(request, this);
    }

	public SpiderConfig getConfig() {
		return config;
	}

	public void setConfig(SpiderConfig config) {
		this.config = config;
	}
}
