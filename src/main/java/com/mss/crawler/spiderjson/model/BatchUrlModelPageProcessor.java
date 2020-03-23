package com.mss.crawler.spiderjson.model;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mss.crawler.common.CrawlerConstant;
import com.mss.crawler.common.FileUtils;
import com.mss.crawler.spiderjson.Constants;
import com.mss.crawler.spiderjson.JSONSpider;
import com.mss.crawler.spiderjson.config.JSONExtractorConfig;
import com.mss.crawler.spiderjson.config.SpiderConfig;
import com.mss.crawler.spiderjson.extractor.ObjectModelExtractor;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;

/**
 * json模型的页面解析处理器
 * 支持json配置文件的页面解析处理器
 * @author wangdw
 *
 */
public class BatchUrlModelPageProcessor implements IConfigAblePageProcessor {

    private Map<String,ObjectModelExtractor> jsonModelExtractors = new HashMap<String,ObjectModelExtractor>();

    private Site site;
    
    private JSONSpider spider;
    
    private SpiderConfig config;

    public BatchUrlModelPageProcessor(Site site ,SpiderConfig config) {
        this.site = site;
        this.config = config;
        this.iniPageModel(config.getExtractorCfgs());
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
    	String requestType = (String)page.getRequest().getExtra(Constants.REQUEST_TYPE);
	   	//1)构建url检索链接
	   	if(requestType == null) { //搜索页面
	   		List<String> urls = FileUtils.fileToList(new File(config.getIndexFile()));
	   		for(String url:urls){
	   			Request seedUrl = new Request(url).putExtra(CrawlerConstant.REQUEST_TYPE,
						CrawlerConstant.REQUEST_DETAIL);
	   			page.addTargetRequest(seedUrl);
	
	   		}
	   	//2) 抽取内容页
	   	}else if(Constants.REQUEST_DETAIL.equals(requestType)){
	   		
		   	 for (Map.Entry<String,ObjectModelExtractor> extractor : jsonModelExtractors.entrySet()) {
	             //解析页面
	             Object process = extractor.getValue().process(page,context);
	             if (process == null || (process instanceof List && ((List) process).size() == 0)) {
	                 continue;
	             }
	             objectNames.add(extractor.getValue().getObjectName());
	             if(objectNames.size()>0){
	            	 page.putField("objectNames", objectNames);
	             }
	             page.putField(extractor.getValue().getObjectName(), process);
	             page.putField("domain", site.getDomain());
	             page.putField("rawText", page.getRawText());
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
