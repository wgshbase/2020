package com.mss.crawler.spiderjson.model;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mss.crawler.spiderjson.util.HtmlFormatter;
import io.netty.util.internal.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mss.crawler.spiderjson.JSONSpider;
import com.mss.crawler.spiderjson.config.JSONExtractorConfig;
import com.mss.crawler.spiderjson.config.SpiderConfig;
import com.mss.crawler.spiderjson.extractor.ObjectModelExtractor;
import com.mss.crawler.spiderjson.scheduler.FileCacheDoubleRemvoeFilter;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.selector.Html;
import us.codecraft.webmagic.selector.PlainText;
import us.codecraft.webmagic.selector.Selectable;


public class ListDetailPageProcessor implements IConfigAblePageProcessor {
	protected Logger logger = LoggerFactory.getLogger(getClass());
	private final static String jsonRegex = "\\{\"page\":\\[\\{.*?\\]\\}";
	public final static String TYPE_LABEL = "type";
	public final static String LIST_OBJECT_LABEL = "listObject";
	private Map<String, ObjectModelExtractor> jsonModelExtractors = new HashMap<String, ObjectModelExtractor>();
	private Site site;
	private JSONSpider spider;
	private SpiderConfig config;
	private PagingModel pagingModel;
	private FileCacheDoubleRemvoeFilter fileCacheDoubleRemvoeFilter;
	private String rootPath;
	private List<ListContentModel> listContentModels;

	public ListDetailPageProcessor(Site site, SpiderConfig config) {
		this.site = site;
		this.config = config;
		this.pagingModel = config.getPagingModel();
		this.listContentModels = new ArrayList<>();
		this.iniPageModel(config.getExtractorCfgs());
		if(StringUtils.isEmpty(config.getRootPath())) {
			this.rootPath = "D:/AppData/data/store/upload/";
		} else {
			this.rootPath = config.getRootPath();
		}
		this.fileCacheDoubleRemvoeFilter = new FileCacheDoubleRemvoeFilter(this.rootPath + "/" + this.site.getDomain());
	}

	public void iniPageModel(Map<String, JSONExtractorConfig> jsonMap) {
		for (Map.Entry<String, JSONExtractorConfig> exCfg : config.getExtractorCfgs().entrySet()) {
			ObjectModelExtractor extractor = ObjectModelExtractor.create(exCfg.getValue());
			jsonModelExtractors.put(exCfg.getKey(), extractor);
		}
	}

	@Override
	public Site getSite() {
		return site;
	}

	@Override
	public void process(Page page) {

		//获取页面抽取类型
		Request req = page.getRequest();
		Object type = req.getExtra(TYPE_LABEL);
		Html html = page.getHtml();

		//处理文章列表
		if (type == null) {
			//处理翻页
			this.processPageTurning(page);
			//处理文章页
		} else if ("article_list".equals(type)) {
			//处理列表详情页			
			parseListSeed(page);
			//处理文章页
		} else if ("article".equals(type)) {
			//设置运行环境变量
			Map<String, Object> context = new HashMap<String, Object>();
			context.put("domain", site.getDomain());
			List<String> objectNames = new ArrayList<String>();
			page.putField("url", page.getRequest().getUrl());
			page.putField("rawText", page.getRawText());
			page.putField(LIST_OBJECT_LABEL, req.getExtra(LIST_OBJECT_LABEL));

			// 添加发布时间
			if(listContentModels.size() > 0) {
				for(ListContentModel model : listContentModels) {
					if(page.getRequest().getUrl().equals(model.getUrl())) {
						if(!StringUtils.isEmpty(model.getPubdate())) {
							page.putField("pubdate", model.getPubdate());
						}
						if(!StringUtils.isEmpty(model.getTitle())) {
							page.putField("title", model.getTitle());
						}

						break;
					}
				}
			}

			for (Map.Entry<String, ObjectModelExtractor> extractor : jsonModelExtractors.entrySet()) {
				//解析页面
				Object process = extractor.getValue().process(page, context);
				if (process == null || (process instanceof List && ((List) process).size() == 0)) {
					continue;
				}
				objectNames.add(extractor.getValue().getObjectName());
				page.putField(extractor.getValue().getObjectName(), process);
			}

			if (objectNames.size() > 0) {
				page.putField("objectNames", objectNames);
			}
			page.putField("domain", site.getDomain());

			// 添加 Solr 需要的额外的信息
			page.putField("news_category", config.getNewsCategory());
			page.putField("dbType", config.getDbType());
			page.putField("src", config.getSrc());

			// 添加不同的项目的标识符
			if (!StringUtils.isEmpty(config.getSslm())) {
				page.putField("sslm", config.getSslm());
			}

			//将成功解析好的对象，doubleKey入栈
			this.fileCacheDoubleRemvoeFilter.push(page.getRequest().getUrl());
		}

		if (page.getResultItems().getAll().size() == 0) {
			page.getResultItems().setSkip(true);
		}

	}

	/**
	 * 解析列表頁的url
	 * 通过 XPath/Regex/JSONPath 进行定位, 通过 Regex 进行定位
	 *
	 * @param page
	 */
	private void parseListSeed(Page page) {
		Html html = page.getHtml();

		List<Selectable> links = new ArrayList<>();
		if (this.config.getListHrefRegex() != null) {
			String json = html.toString();
			String regex = this.config.getListHrefRegex();
			Matcher matcher = Pattern.compile(regex).matcher(json);
			while (matcher.find()) {
				String group = matcher.group();
				group = group.replace("\"url\":", "").replace("\\", "").replace("\"", "").replace("docpuburl:", "").replace("link:", "").replace("SourceUrl:", "").replace(",Status:", "").replace("id:", "");
				// 将提取到的 url 放入到 links 中
				links.add(new PlainText(group));
			}
		} else if (this.config.getListHrefJsonPath() != null) {
			links = html.jsonPath(this.config.getListHrefJsonPath()).nodes();
		} else if (this.config.getListHrefOnclick() != null) {
			String onclickStr = this.config.getListHrefOnclick();
			links = html.xpath(onclickStr).nodes();
			links = parseLinks(links, page.getRequest().getUrl());
		} else {
			links = html.xpath(this.config.getListHrefXPath()).nodes();
		}
		for (Selectable node : links) {
			String detailurl = this.getUrl(site.getDomain(), node.toString(), this.config.getStartURL()[0]);
			logger.info("detailurl=" + detailurl);

			//去重处理
			if (!this.fileCacheDoubleRemvoeFilter.isContains(detailurl)) {
				page.addTargetRequest(new Request(detailurl).putExtra(TYPE_LABEL, "article"));
				/* 
				for(int i = 2; i < 112; i ++) {
					String tempUrl = detailurl + "0_0_0_" + i;
					page.addTargetRequest(new Request(tempUrl).putExtra(TYPE_LABEL, "article"));
				}
				*/
			}
			/*
			JSONExtractorConfig jsonExtractorConfig = this.config.getExtractorCfgs().get("News");
			// 详情页有分页, 需要做特殊的处理
			if(jsonExtractorConfig.getPagingModel() != null) {
				 PagingModel detailPagingModel = jsonExtractorConfig.getPagingModel();
	        	 List<Selectable> selectables = page.getHtml().xpath(detailPagingModel.getPagerXpath()).nodes();
	        	 for(Selectable select:selectables){
	        		 String string = select.get();
	        		 String maxPage = string.substring(string.lastIndexOf("_") + 1, string.length());
	        		 int max = Integer.parseInt(maxPage);
	        		 for(int i = 1; i <= max; i++) {
	        			 String tempUrl = string.replaceAll(maxPage, 1 + "");
	        			 page.addTargetRequest(new Request(tempUrl).putExtra(TYPE_LABEL, "article"));
	        		 }
	        	 }
			}
			*/

		}
		// 列表页添加额外的信息
		if(null != this.config.getListContentModel()) {
			List<Selectable> titles = null;
			List<Selectable> urls = null;
			List<Selectable> pubdates = null;
			if(!StringUtils.isEmpty(this.config.getListContentModel().getTitle())) {
				titles = html.xpath(this.config.getListContentModel().getTitle()).nodes();
			}
			if(!StringUtils.isEmpty(this.config.getListContentModel().getUrl())) {
				urls = html.xpath(this.config.getListContentModel().getUrl()).nodes();
			}
			if(!StringUtils.isEmpty(this.config.getListContentModel().getPubdate())) {
				pubdates = html.xpath(this.config.getListContentModel().getPubdate()).nodes();
			}

			if((titles != null && urls != null) || (pubdates != null && urls != null)) {
				for(int i = 0; i < titles.size(); i++) {
					ListContentModel m = new ListContentModel();
					m.setTitle(titles.get(i).toString());
					m.setUrl(getUrl(this.config.getDomain(), urls.get(i).toString(), this.config.getStartURL()[0]));
//					m.setUrl(urls.get(i).toString());
					try {
						m.setPubdate(HtmlFormatter.convertPubDate(pubdates.get(i).toString()));
					} catch (Exception e) {
						e.printStackTrace();
					}
					listContentModels.add(m);
				}
			}


		}


	}

	/**
	 * 拼接请求参数, 将超链接中的参数分步拼装到目标的地址当中
	 *
	 * @param links 解析到的链接
	 * @param url   主 url, 即为了获取地址的域名...
	 * @return 例: url --> http://aerospace.ckcest.cn/txcbController.do?method=getTxcbList&cnName=&categoryno=&pinyinS=&strokes=&pageNo=1
	 * 目标链接 --> http://aerospace.ckcest.cn/txcbController.do?method=getXcbXq&id=12863&cnName=%E5%AF%BC%E5%BC%B9%E5%AE%9A%E5%9E%8B&enName=Guided%20missile%20finalization&categoryno=0101
	 * <p>
	 * searchXq('34778','总体失稳','General instability','0101')
	 */
	private List<Selectable> parseLinks(List<Selectable> links, String url) {
		List<Selectable> newLinks = new ArrayList<>();
		for (Selectable selectable : links) {
			//System.out.println(selectable.getClass());
			// http://aerospace.ckcest.cn/txcbController.do?method=getTxcbList&cnName=&categoryno=&pinyinS=&strokes=&pageNo=1
			// http://aerospace.ckcest.cn/txcbController.do?method=getXcbXq&id=12863&cnName=%E5%AF%BC%E5%BC%B9%E5%AE%9A%E5%9E%8B&enName=Guided%20missile%20finalization&categoryno=0101
			// searchXq('34778','总体失稳','General instability','0101')
			String select = selectable.toString();
			List<String> params = new ArrayList<>();
			Matcher m = Pattern.compile("'(.*?)'").matcher(select);
			while (m.find()) {
				String temp = m.group().replace("'", "");
				try {
					temp = URLEncoder.encode(temp, "UTF-8");
					temp = temp.replace(" ", "%20");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				params.add(temp);
			}
			String detailUrl = url.substring(0, url.indexOf("?") + 1) + "method=getXcbXq&id=" + params.get(0) + "&cnName=" + params.get(1) + "&enName=" + params.get(2) + "&categoryno=" + params.get(3);
			newLinks.add(new PlainText(detailUrl));
		}
		return newLinks;
	}

	private String getUrl(String domain, String url, String startURL) {
		if (url.toLowerCase().startsWith("http://") || url.toLowerCase().startsWith("https://")) {
			return url;
		}
		if(url.contains("articleId") && startURL.contains("chinare.mnr.gov.cn")) {
			String prefixUrl = startURL.substring(0, startURL.lastIndexOf("/"));
			String suffixUrl = url.substring(url.indexOf("articleId")+10);
			return prefixUrl + "/detail?id=" + suffixUrl;
		}
		/*String http = "http://";
		String regEx= "(HTTP|http)s?://[^,， ]+";
		 // 编译正则表达式
	    Pattern pattern = Pattern.compile(regEx);
	    // 忽略大小写的写法
	    // Pattern pat = Pattern.compile(regEx, Pattern.CASE_INSENSITIVE);
	    Matcher matcher = pattern.matcher(url);
	    
		if(matcher.matches()){
			return url;
		}else{
			return "http://"+domain+url;
		}*/
		if(url.startsWith("/url?q=")) {
			return url.substring(7, url.length());
		}
		if (url.toLowerCase().startsWith("//")) {
			return "http:" + url;
		}
		if (url.startsWith("./")) {
			startURL = startURL.substring(0, startURL.lastIndexOf("/"));
			String result = startURL + url.replace("./", "/");
			return result;

		}
		if (!url.startsWith("/") && !url.startsWith("./")) {
			String prefix = "";
			if(startURL.startsWith("https")) {
				prefix = "https://" + domain + "/";
			} else {
				prefix = "http://" + domain + "/";
			}
			if (startURL.endsWith("/")) {
				startURL = startURL + "?1";
			}
			if(startURL.contains(prefix)) {
				startURL = startURL.subSequence(startURL.indexOf(prefix) + prefix.length(), startURL.length()).toString();
			}
			if (startURL == null || startURL.equals("")) {
				return prefix + url;
			}
			File file = new File(startURL);
			File parentUrl = file.getParentFile();
			if (parentUrl != null) {
				if (!url.contains("..")) {
					return prefix + parentUrl.toString().replace("\\", "/") + "/" + url;
				}
				String[] temp = url.split("/");
				int count = 0;
				for (int i = 0; i < temp.length; i++) {
					if (temp[i].equals("..")) {
						count++;
					}
				}
				while (count > 0) {
					parentUrl = parentUrl.getParentFile();
					count--;
				}
				url = url.replace("../", "");
				if (parentUrl == null) {
					return prefix + url;
				}
				String midUrl = parentUrl.getPath();
				if(parentUrl.getPath().contains("\\")) {
					midUrl = midUrl.replace("\\", "/");
				}
				if(midUrl.startsWith("/")) {
					midUrl = "";
				}
				if(midUrl.endsWith("/")) {
					return prefix + midUrl + url;
				} else {
					return prefix + midUrl + "/" + url;
				}
			} else {
				return "http://" + domain + "/" + url;
			}

		}


		return "http://" + domain + url;

	}

	/**
	 * 处理翻页链接-处理下一页
	 *
	 * @param page
	 */
	private void processPageTurning(Page page) {

		Html html = page.getHtml();

		//当前页下翻下一页
		if (this.pagingModel.getPagingType().equals(PagingModel.PAGINGTYPE_URL)) {
			String url = pagingModel.getPageNumRules();
			for (int i = pagingModel.getStartPage(); i <= pagingModel.getEndPage(); i++) {
				if (pagingModel.getStep() != 1) {
					if(url.contains("${page}") && url.endsWith("${page}")) {
						// 开始 URL 与  翻页 url 一致不能爬取, -- 为 翻页 url 后面加点东西
						page.addTargetRequest(new Request(url.replace("${page}", (i * pagingModel.getStep()) + "?" + 1)).putExtra(TYPE_LABEL, "article_list"));
						logger.info(url.replace("${page}", (i) * pagingModel.getStep() + "" + 1));
					} else {
						page.addTargetRequest(new Request(url.replace("${page}", (i * pagingModel.getStep() + ""))).putExtra(TYPE_LABEL, "article_list"));
						logger.info(url.replace("${page}", i * pagingModel.getStep() + ""));
					}

				} else if (pagingModel.getParam() != "") {
					//  
					page.addTargetRequest(new Request(url.replace("${page}", i + "").replace("${param}", Long.parseLong(pagingModel.getParam()) + i + "")).putExtra(TYPE_LABEL, "article_list"));
					logger.info(url.replace("${page}", (i - 1) * pagingModel.getStep() + "" + 1));
				} else {
					page.addTargetRequest(new Request(url.replace("${page}", i + "")).putExtra(TYPE_LABEL, "article_list"));
					logger.info(url.replace("${page}", i + ""));

				}
			}
		} else {
			String pagerXpath = this.pagingModel.getPagerXpath();
			List<Selectable> links = html.xpath(pagerXpath).nodes();
			String href = "";
			for (Selectable node : links) {
				href = node.toString();
				//判断是不是 http://开头
				if (href.indexOf("http://") == -1 && href.indexOf("https://") == -1) {
					//拼接分页Url
					href = "http://" + site.getDomain() + href;
				}
				//加入到
				page.addTargetRequest(new Request(href).putExtra(TYPE_LABEL, "article_list"));

			}
		}
	}

	@Override
	public void setSpider(JSONSpider spider) {
		this.spider = spider;
	}


}
