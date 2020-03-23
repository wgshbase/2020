package com.mss.crawler.spiderjson.model;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mss.crawler.common.CrawlerCommonUtils;
import com.mss.crawler.common.CrawlerConstant;
import com.mss.crawler.common.FileUtils;
import com.mss.crawler.spiderjson.Constants;
import com.mss.crawler.spiderjson.JSONSpider;
import com.mss.crawler.spiderjson.config.JSONExtractorConfig;
import com.mss.crawler.spiderjson.config.SpiderConfig;
import com.mss.crawler.spiderjson.extractor.ObjectModelExtractor;
import com.mss.crawler.spiderjson.scheduler.FileCacheDoubleRemvoeFilter;
import com.mss.crawler.spiderjson.util.HtmlFormatter;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.selector.Selectable;

/**
 * google主题检索模型的页面解析处理器
 * 支持json配置文件的页面解析处理器
 * @author wangdw
 *
 */
public class GoogleSubjectModelPageProcessor implements IConfigAblePageProcessor {
	private static final Logger LOG = LogManager.getLogger(GoogleSubjectModelPageProcessor.class);
	private Map<String,ObjectModelExtractor> jsonModelExtractors = new HashMap<String,ObjectModelExtractor>();
	private FileCacheDoubleRemvoeFilter fileCacheDoubleRemvoeFilter;
    private Site site;
    
    private SpiderConfig config;

	private JSONSpider spider;
    
    public GoogleSubjectModelPageProcessor(Site site ,SpiderConfig config) {
        this.site = site;
        this.config = config;
        if(!StringUtils.isEmpty(config.getIndexFile())){
        	List<String> indexs = FileUtils.fileToList(new File(config.getIndexFile()));
        	config.setIndexs(indexs.toArray(new String[]{}));
        }
        this.iniPageModel(config.getExtractorCfgs());
        this.fileCacheDoubleRemvoeFilter = new FileCacheDoubleRemvoeFilter(config.getRootPath()+"/"+this.site.getDomain());
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
    	 
    	//1)构建检索主题词链接
    	if(requestType == null || Constants.REQUEST_SEARCH.equals(requestType)) { //搜索页面
    		String searchText_Ui = "";
    		for(String searchText:config.getIndexs()){
				try {
					searchText_Ui = URLEncoder.encode(searchText, "UTF-8");
					page.putField("searchText", searchText);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
    			String url = CrawlerCommonUtils.replaceUrlParam(page.getUrl().get(),"word", searchText_Ui);
    			for(int i=1;i<=3;i++){
	    			url = CrawlerCommonUtils.replaceUrlParam(url,"pn", (i-1)*10+"");
	    			//StringUtils.replace(config.getSeedRex(), "${index}",searchText_Ui);
	    			Request seedUrl = new Request(url).putExtra(CrawlerConstant.REQUEST_TYPE,
						CrawlerConstant.REQUEST_DETAIL_LIST).putExtra("searchText", searchText);
	    			page.addTargetRequest(seedUrl);
    			}
    		}
    			
    	//2)抽取检索结果列表链接
    	}else if(Constants.REQUEST_DETAIL_LIST.equals(requestType)){
    		
    		List<Selectable> list = page.getHtml().xpath(config.getListHrefXPath()).nodes();
    		for(int i=0; i<list.size(); i++) {
				String href = list.get(i).get();
				String domain = HtmlFormatter.getDomain(href);
				//只解析已配置的网站
				if(jsonModelExtractors.containsKey(domain)){
					 //判断是否重复
		            if(!this.fileCacheDoubleRemvoeFilter.isContains(href)){
		            	Request req = new Request(href).putExtra(CrawlerConstant.REQUEST_TYPE,CrawlerConstant.REQUEST_DETAIL).putExtra("searchText", page.getRequest().getExtras().get("searchText"));
						//req.putExtra("accountInfo", page.getRequest().getExtra("accountInfo"));
						page.addTargetRequest(req);
		            }
					
				}
				
			}
    		
    		
    	//3) 抽取内容页
    	}else if(Constants.REQUEST_DETAIL.equals(requestType)){
    		String url = page.getRequest().getUrl();
    		
    		 // 添加 Solr 需要的额外的信息
			page.putField("news_category", config.getNewsCategory());
			page.putField("dbType", config.getDbType());
			page.putField("src", config.getSrc());

			// 加入当前 搜索的关键字
			page.putField("searchText", page.getRequest().getExtras().get("searchText"));
			
    		//循环抽取对象
        	ObjectModelExtractor extractor = jsonModelExtractors.get(HtmlFormatter.getDomain(url));
            //解析页面
            Object process = extractor.process(page,context);
            if (process == null || (process instanceof List && ((List) process).size() == 0)) {
                //continue;
            }else{
            	 objectNames.add(extractor.getObjectName());
                 page.putField(extractor.getObjectName(), process);
            }
            
            if(objectNames.size()>0){
            	 page.putField("objectNames", objectNames);
            }
            
            if (page.getResultItems().getAll().size() == 0) {
                page.getResultItems().setSkip(true);
            }
            page.putField("domain", site.getDomain());

          //将成功解析好的对象，doubleKey入栈
          this.fileCacheDoubleRemvoeFilter.push(url);
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
