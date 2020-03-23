package com.mss.crawler.custom.weapon;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.jackson.JsonObjectSerializer;

import com.alibaba.fastjson.JSONObject;
import com.mss.crawler.dic.pipline.TxtDataFilePipeline;
import com.mss.crawler.spiderjson.util.HtmlFormatter;
import com.mss.utils.JsonFormatUtil;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Html;
import us.codecraft.webmagic.selector.Selectable;



public class HuanQiuJunShiPageProcessor  implements PageProcessor{
	private Logger logger = LoggerFactory.getLogger(HuanQiuJunShiPageProcessor.class);
	private Site site = Site.me();
	private String crawlerRootPath;
	
	public String getCrawlerRootPath() {
		return crawlerRootPath;
	}
	public void setCrawlerRootPath(String crawlerRootPath) {
		this.crawlerRootPath = crawlerRootPath;
	}
	
    public Site getSite() {
	        return site;
	}
	
	
	@Override
	public void process(Page page) {
		//网站爬取Url
		String url = page.getUrl().toString();
		String reqType = (String)page.getRequest().getExtra("type");
		this.processDetail(page);
	}
	
	
	
	private void processDetail(Page page){
		logger.info("processDetail----------------------------------------------------------------------begin");	
		
		Html html = new Html("");
		
		WeaponBean entity = new WeaponBean();
		
		String contentXpath = "//div[@class='detail']";
		Selectable content = page.getHtml().xpath(contentXpath);
		
		
		entity.setName(content.xpath("//span[@class='name']/text()").get());
		entity.setCover(content.xpath("//div[@class='maxPic']/img/@src").get());
		entity.setCountry(content.xpath("//span[@class='country']/b/a/text()").get());
		entity.setSummary(content.xpath("//div[@class='intron']/div/p/text()").get());
		
		Map<String,String> mainContent = new HashMap<>();
		
		
		
		
		List<Selectable> contentNodes = content.xpath("//div[@class='otherList']").nodes();
		for(Selectable div:contentNodes){		
			String key = div.xpath("//h3[@class='title_']/text()").get();
			String divContent = div.xpath("//div[@class='textInfo']").get();
			if(!StringUtils.isEmpty(divContent)){
				mainContent.put(key, HtmlFormatter.html2text(divContent));
			}
		}
		
		Map<String,Object> infos = new HashMap<>();
		
		List<Selectable> infoNodes = content.xpath("//div[@class='dataInfo']/").nodes();
		
		infos.putAll(formatDataList(infoNodes.get(0)));
		
		String key = "";
		for(int i=1;i<infoNodes.size();i++){	
			if(i%2!=0){
				key = HtmlFormatter.html2text(infoNodes.get(i).get());
			}else{

				Map<String,String> value = formatDataList(infoNodes.get(i));
				System.out.println(value);
				infos.put(key, value);
				key="";
			}
			
			
			
		}
		/*entity.setContent(mainContent);
		entity.setInfos(infos);*/
		
		String jsonStr = JSONObject.toJSONString(entity) ;
		Map<String,String> keys = JSONFinder.analysisJson(JSONObject.parseObject(jsonStr),"");  		
		entity.setKeys(keys);
		
		jsonStr = JSONObject.toJSONString(entity) ;
		System.out.println("----------------------------------------------------------------------------------");
		System.out.println(JsonFormatUtil.formatJson(jsonStr));
		
		
			
	}
	
	
	private Map<String,String> formatDataList(Selectable list){
		//System.out.println("formatDataList"+"---------------------------------------------------------------");
		Map<String,String> result = new HashMap<>();
		for(Selectable node:list.xpath("//ul/li").nodes()){	
			String li_Str = HtmlFormatter.html2text(node.get());
			//System.out.println(li_Str);
			/*if(li_Str.indexOf("：")!=-1){
				String[] splits = StringUtils.split(li_Str,"：");
				result.put(splits[0], splits[1]);
				System.out.println(result);
			}*/
			result.putAll(str2Map(li_Str));
			
		}
		return result;
	}
	
	private Map<String,String> str2Map(String str){
		
		Map<String,String> result = new HashMap<>();
		if(str.indexOf("；")!=-1){
			String[] list = StringUtils.split(str,"；");
			for(String line :list){
				if(line.indexOf("：")!=-1){
					String[] splits = StringUtils.split(line,"：");
					result.put(HtmlFormatter.removeNum(splits[0]), splits[1]);
				}
			}
		}else{
			if(str.indexOf("：")!=-1){
				String[] splits = StringUtils.split(str,"：");
				result.put(splits[0], splits[1]);
			}
			
		}
		
		
		return result;
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
		
		
		String startUrl ="http://weapon.huanqiu.com/j_16" ; //"https://shurufa.baidu.com/dict_list?cid=208&page=1";//"https://shurufa.baidu.com/" ;
		String crawlerRootPath = "d:\\data";
		HuanQiuJunShiPageProcessor spp = new HuanQiuJunShiPageProcessor();		
		spp.setCrawlerRootPath(crawlerRootPath);
		Spider spider  = Spider.create(spp)
			        		  .thread(1)
			        		  .addPipeline(new TxtDataFilePipeline(spp.getCrawlerRootPath())).addUrl(startUrl);        
        spider.run();
        
    }

}
