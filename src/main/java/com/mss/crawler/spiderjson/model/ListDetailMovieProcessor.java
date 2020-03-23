package com.mss.crawler.spiderjson.model;

import com.mss.crawler.spiderjson.JSONSpider;
import com.mss.crawler.spiderjson.config.JSONExtractorConfig;
import com.mss.crawler.spiderjson.config.SpiderConfig;
import com.mss.crawler.spiderjson.extractor.ObjectModelExtractor;
import com.mss.crawler.spiderjson.scheduler.FileCacheDoubleRemvoeFilter;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.selector.Html;
import us.codecraft.webmagic.selector.PlainText;
import us.codecraft.webmagic.selector.Selectable;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ListDetailMovieProcessor implements IConfigAblePageProcessor {
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

	public ListDetailMovieProcessor(Site site, SpiderConfig config) {
		this.site = site;
		this.config = config;
		this.pagingModel = config.getPagingModel();
		this.iniPageModel(config.getExtractorCfgs());
		this.rootPath = config.getRootPath();
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

			page.putField(LIST_OBJECT_LABEL, req.getExtra(LIST_OBJECT_LABEL));

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

		// html 标签的格式化
		/*if(html.toString().contains("&lt;")) {
			html = new Html(html.toString().replace("&lt;", "<"));
		}
		if(html.toString().contains("&gt;")) {
			html = new Html(html.toString().replace("&gt;", ">"));
		}
		if(html.toString().contains("&quot;")) {
			html = new Html(html.toString().replace("&quot;", "\""));
		}*/

		List<Selectable> links = new ArrayList<>();
		if (this.config.getListHrefRegex() != null) {
			String json = html.toString();
			String regex = this.config.getListHrefRegex();
			Matcher matcher = Pattern.compile(regex).matcher(json);
			while (matcher.find()) {
				String group = matcher.group();
				group = group.replace("\"url\":", "").replace("\\", "").replace("\"", "");
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
		if (url.toLowerCase().startsWith("//")) {
			return "http:" + url;
		}
		if (url.startsWith("./")) {
			startURL = startURL.substring(0, startURL.lastIndexOf("/"));
			String result = startURL + url.replace("./", "/");
			return result;

		}
		if (!url.startsWith("/") && !url.startsWith("./")) {
			String prefix = "http://" + domain + "/";
			if (startURL.endsWith("/")) {
				startURL = startURL + "?1";
			}
			startURL = startURL.subSequence(startURL.indexOf(prefix) + prefix.length(), startURL.length()).toString();
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
				return prefix + parentUrl + "/" + url;
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
					// 开始 URL 与  翻页 url 一致不能爬取, -- 为 翻页 url 后面加点东西
					page.addTargetRequest(new Request(url.replace("${page}", ((i - 1) * pagingModel.getStep()) + "?" + 1)).putExtra(TYPE_LABEL, "article_list"));
					logger.info(url.replace("${page}", (i - 1) * pagingModel.getStep() + "" + 1));
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
