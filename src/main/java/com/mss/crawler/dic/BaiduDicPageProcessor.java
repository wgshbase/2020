package com.mss.crawler.dic;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mss.crawler.dic.pipline.TxtDataFilePipeline;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.downloader.HttpClientDownloader;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Html;
import us.codecraft.webmagic.selector.Selectable;



public class BaiduDicPageProcessor  implements PageProcessor{
	private Logger logger = LoggerFactory.getLogger(BaiduDicPageProcessor.class);
	private Site site = Site.me();
	private Map<String,String> class1Map ;
	//private Map<String,Integer> classesType ;
	private String[] parentClass = {"医学","文学","语言","网络游戏","单机游戏","旅游","影视","明星","球类"};
	private String crawlerRootPath;
	
	
	public BaiduDicPageProcessor(){
		class1Map = new HashMap<String,String>();
		class1Map.put("https://shurufa.baidu.com/dict_list?cid=179","城市区划");
		//class1Map.put("https://shurufa.baidu.com/dict_list?cid=158","理工行业");
/*		class1Map.put("https://shurufa.baidu.com/dict_list?cid=317","人文社会");
		class1Map.put("https://shurufa.baidu.com/dict_list?cid=162","电子游戏"); 
		class1Map.put("https://shurufa.baidu.com/dict_list?cid=159","生活百科"); 
		class1Map.put("https://shurufa.baidu.com/dict_list?cid=163","娱乐休闲");
		class1Map.put("https://shurufa.baidu.com/dict_list?cid=165","人名专区");
		class1Map.put("https://shurufa.baidu.com/dict_list?cid=160","文化艺术");
		class1Map.put("https://shurufa.baidu.com/dict_list?cid=161","体育运动");*/
		
		
		//classesType = new HashMap<String,Integer>();
		
		

		
	}
	
	public String getCrawlerRootPath() {
		return crawlerRootPath;
	}
	public void setCrawlerRootPath(String crawlerRootPath) {
		this.crawlerRootPath = crawlerRootPath;
	}
	
    public Site getSite() {
	        return site;
	}
	
	private boolean isHasChild(String className){
		for(String name:this.parentClass){
			if(name.equals(className)){
				return true;
			}
		}
		return false;
	}
	@Override
	public void process(Page page) {
		
		//网站爬取Url
		String url = page.getUrl().toString();
		
		String reqType = (String)page.getRequest().getExtra("type");
		
		if("https://shurufa.baidu.com/".equals(url)){
			reqType = "class1";
		}
		switch(reqType){
		
		case "class1":
			this.processClass1(page);
			break;
		case "class2":
			this.processClass2(page);
			break;
		case "pageTurning":
			this.processPageTurning(page);
			break;
		case "list":
			this.processList(page);
			break;
	/*	case "detail":
			this.processDetail(page,downloader);
			break;*/
		
		}
		
		
		
		
		
	}
	
	private void createDir(String absPath){
		String filePath =this.trimAll(this.crawlerRootPath+"dic\\"+absPath);
		File fileDir = new File(filePath);
		if(!fileDir.exists()){
			fileDir.mkdir();
			logger.info("create dir :"+filePath);
		}
	}
	
	private void processClass1(Page page){
		
		logger.info("processClass1----------------------------------------------------------------------begin");
		for(Entry<String,String> entry :class1Map.entrySet()){
			Request req = new Request(entry.getKey());
			
			String class1 = entry.getValue();
			Map<String,Object> extras = new HashMap<String,Object>();
			extras.put("class1", class1);
			req.setExtras(extras);
			
			
			req.putExtra("type","class2");
			page.addTargetRequest(req);
			
			
			
			logger.info("class1 is "+class1);
			logger.info("class2 url is "+req.getUrl());
			this.createDir(class1);
		}
		logger.info("processClass1----------------------------------------------------------------------end");
		
		
		
	
	}
	private void processClass2(Page page){
		
		logger.info("processClass2----------------------------------------------------------------------begin");
		Html html = page.getHtml();
		
		String class1 = page.getRequest().getExtra("class1").toString();
		
		String class2;
		String class3 = null;
		String seedUrl;
		String class2Id;
		
		// 处理没有下级分类的  div[@id='cid_']/a
		//String pagerXpath = "//div[@id='cid_']/a";
		String pagerXpath = "//div[@class='popup_list']/a";
		
		List<Selectable> links = html.xpath(pagerXpath).nodes();
		for(Selectable node:links){		
			Html newHtml = Html.create(node.toString());
			String className = newHtml.xpath("//a/text()").toString();
			//boolean isHasChild = this.isHasChild(className);
			
			//if(!isHasChild){
				class2 = className;
				seedUrl = newHtml.xpath("//a/@href").toString();
				Map<String,Object> extras1 = new HashMap<String,Object>();
				extras1.put("class1", class1);
				extras1.put("class2", class2);
				this.createDir(class1+"\\"+class2);
				Request req = new Request(seedUrl);
				req.setExtras(extras1);
				req.putExtra("type","pageTurning");
				page.addTargetRequest(req);	
				
				logger.info("----------------------------------------------------------------------");
				logger.info("class is "+class1+"-"+class2);
				logger.info("pageTurning url is "+seedUrl);
			//}
			

			
		}
		
		// 处理含有下级分类的  div[@id='cid_']/a
		
		pagerXpath = "//div[@id='cid_']/div[@class='tag_list']";
		links = html.xpath(pagerXpath).nodes();
		
		for(Selectable node:links){		
			Html newHtml = Html.create(node.toString());
			
			class2 = newHtml.xpath("//div/a/text()").toString();
			class2Id = newHtml.xpath("//div/a/@cid").toString();
			this.createDir(class1+"\\"+class2);		
			List<Selectable> class3Links = newHtml.xpath("//div/div[@id='l3_cid_"+class2Id+"']/p/a").nodes();
			for(Selectable class3Node:class3Links){	
					Html class3Html = Html.create(class3Node.toString());
					class3 = class3Html.xpath("//a/text()").toString();
					seedUrl = class3Html.xpath("//a/@href").toString();
					
					this.createDir(class1+"\\"+class2+"\\"+class3);
					
					Map<String,Object> extras2 = new HashMap<String,Object>();
					extras2.put("class1", class1);
					extras2.put("class2", class2);
					extras2.put("class3", class3);
					extras2.put("type", "pageTurning");
					Request req = new Request(seedUrl);
					req.setExtras(extras2);
					page.addTargetRequest(req);	
					
					logger.info("----------------------------------------------------------------------");
					logger.info("class is "+class1+"-"+class2+"-"+class3);
					logger.info("pageTurning url is "+seedUrl);
			}
		}
		
		logger.info("processClass2----------------------------------------------------------------------end");
	}
	
	
	
	/**
	 * 处理翻页链接-处理下一页
	 * @param page
	 */
	private void processPageTurning(Page page){
		logger.info("processPageTurning----------------------------------------------------------------------begin");
		Map<String,Object> classInfo = page.getRequest().getExtras();
		
		
		
		if(classInfo.containsKey("class3")){
			logger.info("class is "+classInfo.get("class1")+"-"+classInfo.get("class2")+"-"+classInfo.get("class3"));
		}else{
			logger.info("class is "+classInfo.get("class1")+"-"+classInfo.get("class2"));
		}
		
		String curUrl = page.getUrl().toString();
		
		Html html = page.getHtml();
		String pagerXpath = "//div[@class='pages']/a/text()";
		List<Selectable> pagerNums = html.xpath(pagerXpath).nodes();
		int maxPager = 1;
		for(Selectable node:pagerNums){			
			String pagerNum = node.toString();
			if(!"确定".equals(pagerNum)&&!">".equals(pagerNum)){
				if(Integer.parseInt(pagerNum)>maxPager){
					maxPager = Integer.parseInt(pagerNum);
				}
			}
			
		}
		
		logger.info("max page is "+maxPager);
		
		
		for(int i=1;i<=maxPager;i++){
			String nextPageUrl = curUrl+"&page="+i;
			logger.info("Page Num "+i+" ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			logger.info("list url is "+nextPageUrl);
			
			Map<String,Object> listExtras = this.clone(classInfo);
			listExtras.put("pageNum", i);
			listExtras.put("type", "list");
			Request req = new Request(nextPageUrl);
			req.setExtras(listExtras);
			
			page.addTargetRequest(req);	
		}
		
		logger.info("processPageTurning----------------------------------------------------------------------end");	
		
		
	}
	
	/**
	 * 处理抽取页面数据
	 * @param page
	 */
	private void processList(Page page){
		
		logger.info("processList----------------------------------------------------------------------begin");	
		Html html = page.getHtml();
		
		
		Map<String,Object> listExtras = page.getRequest().getExtras();
		
		/*if(listExtras.containsKey("class3")){
			logger.info("class is "+listExtras.get("class1")+"-"+listExtras.get("class2")+"-"+listExtras.get("class3"));
		}else{
			logger.info("class is "+listExtras.get("class1")+"-"+listExtras.get("class2"));
		}
		
		logger.info("Page Num is"+listExtras.get("pageNum"));*/
		
		String pagerXpath = "//div[@class='dict-list-info']/table/tbody/tr";
		List<Selectable> links = html.xpath(pagerXpath).nodes();
		int i=0;
		String seedUrl = "https://shurufa.baidu.com/dict_innerid_download?innerid=";
		for(Selectable node:links){			
			Html newHtml = Html.create(node.toString());// /@dict-innerid
			
			String dictName = newHtml.xpath("//a[1]/text()").toString();
			String dictInnerid = newHtml.xpath("//a[2]/@dict-innerid").toString();
			
			Request req = new Request(seedUrl+dictInnerid);			
			logger.info("detail url is "+req.getUrl());
			
			Map<String,Object> detailExtras = this.clone(listExtras);
			
			//req.setRequestType("detail");
			req.setExtras(detailExtras);
			detailExtras.put("fileName", dictName);
			this.processDetail(req);
			//page.addTargetRequest(req);	
		}
		
		logger.info("processList----------------------------------------------------------------------end");
			
	}
	
	private void processDetail(Request req){
		logger.info("processDetail----------------------------------------------------------------------begin");	
		
		
		
		Map<String,Object> detailExtras = req.getExtras();
		
		String class1 = detailExtras.get("class1").toString();
		String class2 = detailExtras.get("class2").toString();
		
		String filePath;
		String filePathName = "";
		
		if(detailExtras.containsKey("class3")){
			logger.info("class is "+detailExtras.get("class1")+"-"+detailExtras.get("class2")+"-"+detailExtras.get("class3"));
			filePath = this.crawlerRootPath+"\\dic\\"+class1+"\\"+class2+"\\"+detailExtras.get("class3").toString();
			logger.info("class is "+detailExtras.get("class1")+"-"+detailExtras.get("class2")+"-"+detailExtras.get("class3"));
			//page.putField("text", req.getUrl()+"|"+filePathName+"|"+class1+"|"+class2+"|"+detailExtras.get("class3"));
			
		}else{
			filePath = this.crawlerRootPath+"\\dic\\"+class1+"\\"+class2;
			logger.info("class is "+detailExtras.get("class1")+"-"+detailExtras.get("class2"));
			//page.putField("text", seedUrl+"|"+filePathName+"|"+class1+"|"+class2);
		}
		logger.info("down Url is "+req.getUrl());
		filePathName = StringUtils.trim(filePath)+"\\"+detailExtras.get("fileName")+".bdict";
		filePathName = this.trimAll(filePathName);
		logger.info("filePathName="+filePathName);				
		File fileDir = new File(filePathName);
		if(!fileDir.exists()){
			//req.putExtra(HttpClientDownloader.req_type, filePathName);
			//downloader.download(req, this.getTask());
		}
		
		logger.info("processDetail----------------------------------------------------------------------end");	
			
	}
	
	private String trimAll(String str){
		return str.replaceAll(" +","");
	}
	
	
	public Map<String,Object> clone(Map<String,Object> map){
		
		Map<String,Object> resultMap = new HashMap<String,Object>();
		for(Entry<String,Object> entry :map.entrySet()){
			resultMap.put(entry.getKey(), entry.getValue());
		}
		
		return resultMap;
		
	}

	
	public static void main(String[] args){
		
		
		String startUrl ="https://shurufa.baidu.com/" ; //"https://shurufa.baidu.com/dict_list?cid=208&page=1";//"https://shurufa.baidu.com/" ;
		
		BaiduDicPageProcessor spp = new BaiduDicPageProcessor();
		spp.setCrawlerRootPath("E:\\dic\\baidu\\");
		
		Spider spider  = Spider.create(spp)
			        		  .thread(1)
			        		  .addPipeline(new TxtDataFilePipeline(spp.getCrawlerRootPath())).addUrl(startUrl);        
        spider.run();
        
    }

}
