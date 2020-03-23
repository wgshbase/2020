package com.mss.crawler.spiderjson.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mss.crawler.spiderjson.JSONSpider;
import com.mss.crawler.spiderjson.config.JSONExtractorConfig;
import com.mss.crawler.spiderjson.config.SpiderConfig;
import com.mss.crawler.spiderjson.extractor.ObjectModelExtractor;
import com.mss.crawler.spiderjson.scheduler.FileCacheDoubleRemvoeFilter;

import net.minidev.json.JSONObject;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Selector;

/**
 * json模型的页面解析处理器
 * 支持json配置文件的页面解析处理器
 * @author wangdw
 *
 */
public class IPTestModelPageProcessor implements IConfigAblePageProcessor {

    private Map<String,ObjectModelExtractor> jsonModelExtractors = new HashMap<String,ObjectModelExtractor>();

    private Site site;
    
    private SpiderConfig config;
    
    private JSONSpider spider;
    
    private FileCacheDoubleRemvoeFilter fileCacheDoubleRemvoeFilter;
    
    private String rootPath;

    public IPTestModelPageProcessor(Site site ,SpiderConfig config) {
        this.site = site;
        this.config = config;
        this.iniPageModel(config.getExtractorCfgs());
        this.rootPath = config.getRootPath();
        this.fileCacheDoubleRemvoeFilter = new FileCacheDoubleRemvoeFilter(this.rootPath+"/"+this.site.getDomain());
    }
    
    public void iniPageModel(Map<String,JSONExtractorConfig> jsonMap) {
    	
    	 for(Map.Entry<String,JSONExtractorConfig> exCfg:config.getExtractorCfgs().entrySet()){
    		 ObjectModelExtractor extractor = ObjectModelExtractor.create(exCfg.getValue());
    		 jsonModelExtractors.put(exCfg.getKey(), extractor);
         }
    }

    @Override
    public void process(Page page) {
    	
    	List<String> objectNames = new ArrayList<String>();
    	
    	Map<String,Object> context = new HashMap<String,Object>();
    	 
    	context.put("domain", site.getDomain());
    	page.putField("domain", site.getDomain());
        for (Map.Entry<String,ObjectModelExtractor> extractor : jsonModelExtractors.entrySet()) {
        	
        	//抽取链接
        	if(extractor.getValue().getHelpUrlPatterns()!=null&&extractor.getValue().getHelpUrlPatterns().size()>0){
       	 		extractLinks(page, extractor.getValue().getHelpUrlRegionSelector(), extractor.getValue().getHelpUrlPatterns(), false);
        	}
        	if(!config.isSinglePage()&&extractor.getValue().getTargetUrlPatterns()!=null&&extractor.getValue().getTargetUrlPatterns().size()>0){
        		extractLinks(page, extractor.getValue().getTargetUrlRegionSelector(), extractor.getValue().getTargetUrlPatterns(), true);
        	}
           
        	//将成功解析好的对象，doubleKey入栈
        	if(!this.fileCacheDoubleRemvoeFilter.isContains(page.getRequest().getUrl())){
        	this.fileCacheDoubleRemvoeFilter.push(page.getRequest().getUrl());
        	}
            //解析页面
            Object process = extractor.getValue().process(page,context);
            if (process == null || (process instanceof List && ((List) process).size() == 0)) {
                continue;
            }
            objectNames.add(extractor.getValue().getObjectName());
            page.putField(extractor.getValue().getObjectName(), process);
            System.out.println("---------------------------------------------------------------------");
            System.out.println(extractor.getValue().getObjectName());
            System.out.println(process);
        }
        
        if(objectNames.size()>0){
        	 page.putField("objectNames", objectNames);
        }
        
       
        
        if (page.getResultItems().getAll().size() == 0) {
            page.getResultItems().setSkip(true);
        }
    }

    //抽取链接方法
    private void extractLinks(Page page, Selector urlRegionSelector, List<Pattern> urlPatterns, boolean isTargetUrl ) {
        List<String> links;
        if (urlRegionSelector == null) {
            links = page.getHtml().links().all();
        } else {
            links = page.getHtml().selectList(urlRegionSelector).links().all();
        }
        for (String link : links) {
            for (Pattern targetUrlPattern : urlPatterns) {
                Matcher matcher = targetUrlPattern.matcher(link);
                if (matcher.find()) {
                	//去重处理
        			if(!isTargetUrl || !this.fileCacheDoubleRemvoeFilter.isContains(matcher.group(1))){
        				page.addTargetRequest(new Request(matcher.group(1)));
                    
        			}
                }
            }
        }
    }

    

    @Override
    public Site getSite() {
        return site;
    }
    
	@Override
	public void setSpider(JSONSpider spider) {
		this.spider = spider;
	}
}
