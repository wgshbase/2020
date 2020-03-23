package com.mss.crawler.spiderjson.extractor;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mss.crawler.spiderjson.model.WeiBoModelPageProcessor;
import com.mss.crawler.spiderjson.util.*;
import io.renren.modules.spider.utils.CSVUtils;
import io.renren.modules.spider.utils.ZipCompressor;
import org.apache.commons.codec.digest.DigestUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.alibaba.druid.support.json.JSONUtils;
import com.alibaba.fastjson.JSON;
import com.mss.crawler.common.HttpClientPoolUtil;
import com.mss.crawler.spiderjson.ResourceFile;
import com.mss.crawler.spiderjson.config.JSONExtractorConfig;
import com.mss.crawler.spiderjson.config.JSONFieldExtractorConfig;
import com.mss.crawler.spiderjson.model.PagingModel;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.selector.Html;
import us.codecraft.webmagic.selector.Selectable;
import us.codecraft.webmagic.selector.Selector;
import us.codecraft.webmagic.selector.XpathSelector;


/**
 * json模型文件抽取器
 *
 * @author wangdw
 */
public class ObjectModelExtractor implements IModelExtractor {

	/**
	 * 主内容抽取页url匹配规则
	 */
	private List<Pattern> targetUrlPatterns = new ArrayList<Pattern>();

	/**
	 * 主内容抽取区域选择器
	 */
	private Selector targetUrlRegionSelector;

	/**
	 * 辅助url抽取页 url匹配规则
	 */
	private List<Pattern> helpUrlPatterns = new ArrayList<Pattern>();
	/**
	 * 辅助url抽取页区域选择器
	 */
	private Selector helpUrlRegionSelector;

	/**
	 * 主对象抽取器
	 */
	private Extractor objectExtractor;

	/**
	 * 对象名称
	 */
	private String objName;

	/**
	 * 文件名生成项
	 */
	private String[] fileNameRules;

	/**
	 * 域名
	 */
	private String domain;

	/**
	 * 对象Xpath路径
	 */
	private String objXpath;

	/**
	 * 是否抽取多个
	 */
	private boolean isMulti = false;

	/**
	 * 对象列抽取器
	 */
	private List<FieldExtractor> fieldExtractors = new ArrayList<FieldExtractor>();

	/**
	 * 对象分页器
	 */
	private PagingModel pagingModel;

	private Logger logger = LoggerFactory.getLogger(getClass());

	public static ObjectModelExtractor create(JSONExtractorConfig exCfg) {
		ObjectModelExtractor pageModelExtractor = new ObjectModelExtractor();
		pageModelExtractor.setDomain(exCfg.getDomain());
		pageModelExtractor.init(exCfg);
		return pageModelExtractor;
	}

	private void init(JSONExtractorConfig exCfg) {
		//初始化实体抽取类
		initEntityExtractor(exCfg);
		//初始化字段抽取类
		initEntityFieldsExtractors(exCfg);
	}

	private void initEntityExtractor(JSONExtractorConfig exCfg) {
		this.domain = exCfg.getDomain();
		this.objName = exCfg.getObjName();
		this.isMulti = exCfg.isMulti();
		this.objXpath = exCfg.getObjXpath();
		this.pagingModel = exCfg.getPagingModel();
		this.fileNameRules = exCfg.getFileNameRules();

		if (exCfg.getTargetUrlPatterns() == null) {
			targetUrlPatterns.add(Pattern.compile("(.*)"));
		} else {
			for (String s : exCfg.getTargetUrlPatterns()) {
				targetUrlPatterns.add(Pattern.compile("(" + s.replace(".", "\\.").replace("*", "[^\"'#]*") + ")"));
			}
		}

		if (!StringUtils.isEmpty(exCfg.getTargetUrlXPath())) {
			targetUrlRegionSelector = new XpathSelector(exCfg.getTargetUrlXPath());
		}

		if (exCfg.getHelpUrlPatterns() != null) {

			for (String s : exCfg.getHelpUrlPatterns()) {
				helpUrlPatterns.add(Pattern.compile("(" + s.replace(".", "\\.").replace("*", "[^\"'#]*") + ")"));
			}
		}

		if (!StringUtils.isEmpty(exCfg.getHelpUrlXPath())) {
			helpUrlRegionSelector = new XpathSelector(exCfg.getHelpUrlXPath());
		}
		objectExtractor = new Extractor(new XpathSelector(objXpath), Extractor.Source.Html, true, isMulti);

	}

	private void initEntityFieldsExtractors(JSONExtractorConfig exCfg) {

		List<JSONFieldExtractorConfig> fieldConfigs = exCfg.getFieldList();
		for (JSONFieldExtractorConfig fieldCfg : fieldConfigs) {
			FieldExtractor fieldExtractor = getExtractBy(fieldCfg);
			if (fieldExtractor != null) {
				fieldExtractor.setDomain(exCfg.getDomain());
				fieldExtractors.add(fieldExtractor);
			}
		}
	}

	private FieldExtractor getExtractBy(JSONFieldExtractorConfig fieldCfg) {

		FieldExtractor fieldExtractor = null;
		if (fieldCfg != null) {
			Selector selector = ExtractorUtils.getSelector(fieldCfg.getSourceType(), fieldCfg.getFieldExtractorExp());
			FieldExtractor.Source source = null;

			// 获取视频的来源类型
			String videoType = fieldCfg.getVideoType();

			// 获取视频的标题
			String videoTitle = fieldCfg.getVideoTitle();

			// 获取资源的表达式
			String fieldExtractorExp = fieldCfg.getFieldExtractorExp();

			// 获取视频文件的保存路径
			String videoStorePath = fieldCfg.getVideoStorePath();

			// 获取视频的封面图片
			String videoHeadimg = fieldCfg.getVideoHeadimg();

			// 获取视频的打包路径
			String zippath = fieldCfg.getZippath();

			if (Extractor.Source.RawText.name().equalsIgnoreCase(fieldCfg.getSourceType())) {
				source = FieldExtractor.Source.RawText;
			} else if (Extractor.Source.RawHtml.name().equalsIgnoreCase(fieldCfg.getSourceType())) {
				source = FieldExtractor.Source.RawHtml;
			} else if (Extractor.Source.Html.name().equalsIgnoreCase(fieldCfg.getSourceType())) {
				source = FieldExtractor.Source.Html;
			} else if (Extractor.Source.NewsText.name().equalsIgnoreCase(fieldCfg.getSourceType())) {
				source = FieldExtractor.Source.NewsText;
			} else if (Extractor.Source.DateText.name().equalsIgnoreCase(fieldCfg.getSourceType())) {
				source = FieldExtractor.Source.DateText;
			} else if (Extractor.Source.Regex.name().equalsIgnoreCase(fieldCfg.getSourceType())) {
				source = FieldExtractor.Source.Regex;
			} else if (Extractor.Source.Json.name().equalsIgnoreCase(fieldCfg.getSourceType())) {
				source = FieldExtractor.Source.Json;
			} else if(Extractor.Source.HtmlText.name().equalsIgnoreCase(fieldCfg.getSourceType())) {
				source = FieldExtractor.Source.HtmlText;
			} else if(Extractor.Source.movie.name().equalsIgnoreCase(fieldCfg.getSourceType())) {
				source = FieldExtractor.Source.movie;
			} else {
				source = FieldExtractor.Source.Html;
			}
			fieldExtractor = new FieldExtractor(fieldCfg.getFieldName(), selector, source,
					fieldCfg.isNotNull(), fieldCfg.isMulti(), fieldCfg.getRemoveDom(), fieldCfg.getExcludeRegionExp(), fieldCfg.getInnerFileConfigs());
			if(null != videoType) {
				fieldExtractor.setVideoType(videoType);
			}
			if(null != videoTitle) {
				fieldExtractor.setVideoTitle(videoTitle);
			}
			if(null != videoStorePath) {
				fieldExtractor.setVideoStorePath(videoStorePath);
			}
			if(null != videoHeadimg) {
				fieldExtractor.setVideoHeadimg(videoHeadimg);
			}
			if(null != zippath) {
				fieldExtractor.setZippath(zippath);
			}
			fieldExtractor.setFieldExtractExp(fieldExtractorExp);

		}
		return fieldExtractor;
	}

	public String getObjectName() {
		return this.objName;
	}

	/**
	 * 解析页面方法
	 */
	public Object process(Page page, Map<String, Object> context) {

		boolean matched = false;
		for (Pattern targetPattern : targetUrlPatterns) {
			if (targetPattern.matcher(page.getUrl().toString()).matches()) {
				matched = true;
			}
		}
		if (!matched) {
			return null;
		}

		if (isMulti) {
			List<Object> os = new ArrayList<Object>();
			List<String> list = this.objectExtractor.getSelector().selectList(page.getRawText());
			for (String s : list) {
				Object o = processSingle(page, s, false, context);
				if (o != null) {
					os.add(o);
				}
			}

			return os;
		} else {
			String select = this.objectExtractor.getSelector().select(page.getRawText());
			Object o = processSingle(page, select, false, context);
			return o;
		}

	}

	private Map<String, Object> processSingle(Page page, String html, boolean isRaw, Map<String, Object> context) {
		List<String> pagingUrls = new ArrayList<>();
		//需要分页
		if (this.pagingModel != null) {
			// 设置 chrome 浏览器的 webdriver 的驱动程序
			System.setProperty("webdriver.chrome.marionette", "chromedriver.exe");

			// 无弹窗的方式进行调试界面, 感受好得多...
			ChromeOptions chromeOptions = new ChromeOptions();
			chromeOptions.addArguments("--headless");
			WebDriver driver = new ChromeDriver(chromeOptions);
			//WebDriver driver = new ChromeDriver();
			driver.get(page.getRequest().getUrl());
			Html h = new Html(driver.getPageSource());
			//driver.close();
			driver.quit();
			List<Selectable> selectables = h.xpath(pagingModel.getPagerXpath()).nodes();
			if (selectables != null && selectables.size() > 0) {
				for (Selectable selectable : selectables) {
					String node = selectable.get();
					if (node != null) {
						if (!pagingUrls.contains(node)) {
							pagingUrls.add(node);
						}
					}
				}
			}
		}

		Map<String, Object> map = new HashMap<String, Object>();
		// title, pubdate 通过请求传递过来
		String extraTitle = (String) page.getRequest().getExtra("title");
		String pubdate = (String) page.getRequest().getExtra("pubdate");
		String copyright = (String) page.getRequest().getExtra("copyright");
		if (extraTitle != null) {
			map.put("title", extraTitle);
		}
		if (pubdate != null) {
			map.put("pubdate", pubdate);
		}
		if (copyright != null) {
			map.put("copyright", copyright);
		}
		for (FieldExtractor fieldExtractor : fieldExtractors) {
			if (fieldExtractor.isMulti()) {
				List<String> value = this.processFieldForList(page, html, fieldExtractor, context);
				if ((value == null || value.size() == 0) && fieldExtractor.isNotNull()) {
					return null;
				}
				map.put(fieldExtractor.getFieldName(), value);

			} else {
				String value = this.processField(page, html, fieldExtractor, context, pagingUrls);
				if (value == null && fieldExtractor.isNotNull()) {
					return null;
				}
				map.put(fieldExtractor.getFieldName(), value);
			}
		}

		if(!StringUtils.isEmpty(page.getResultItems().get("title"))) {
			map.put("title", page.getResultItems().get("title"));
		}

		//自动生成记录唯一标识，用于去重
		if (!map.containsKey("id") && !page.getRequest().getUrl().contains("weibo.com")) {
			String title = (String) map.get("title");
        	/* 临时方法, 用于采集关键字的相关内容需要的设定
        	 * if(null == title)
        		title = (String) map.get("thesaurusCn") + (String) map.get("thesaurusEn");*/
			if (title != null && title != "") {
				// 按照标题作为 id
				String id = DigestUtils.md5Hex(JSON.toJSONString(title.trim()));
				map.put("id", id);
			} else {
				logger.error("The news you wanna download has no title!!!");
				// 此处抛出异常的原因是对于部分的新闻没有标题不进行插入数据库等的后续操作
				throw new RuntimeException("----> The news you wanna download has no title!!!");
			}

		} else if (!map.containsKey("id") && page.getRequest().getUrl().contains("weibo.com")) {
			String id = UUID.randomUUID().toString().replace("-", "").substring(0, 32);
			map.put("id", id);
		}

		StringBuilder fileName = new StringBuilder();
		//处理文件名
		if (this.fileNameRules != null && this.fileNameRules.length > 0) {

			for (int i = 0; i < this.fileNameRules.length; i++) {
				if (i > 0) {
					fileName.append("-");
				}
				fileName.append(filenameFilter((String) map.get(fileNameRules[i])));
			}
		}

		if (StringUtils.isEmpty(fileName.toString())) {
			fileName.append(map.get("id"));
		}

		map.put("filename", fileName.toString());

		return map;
	}

	private static Pattern FilePattern = Pattern.compile("[\\\\/:*?\"<>|]");

	public String filenameFilter(String str) {
		return str == null ? null : FilePattern.matcher(str).replaceAll("").trim();
	}

	/**
	 * 字段列表处理
	 *
	 * @param page
	 * @param html
	 * @param fieldExtractor
	 * @return
	 */
	private List<String> processFieldForList(Page page, String html, FieldExtractor fieldExtractor, Map<String, Object> context) {
		List<String> value;
		switch (fieldExtractor.getSource()) {
			case RawHtml:
				value = page.getHtml().selectDocumentForList(fieldExtractor.getSelector());
				break;
			case Html:
				value = fieldExtractor.getSelector().selectList(html);
				break;
			case Url:
				value = fieldExtractor.getSelector().selectList(page.getUrl().toString());
				break;
			case RawText:
				value = fieldExtractor.getSelector().selectList(page.getRawText());
				break;
			case NewsText:
				value = fieldExtractor.getSelector().selectList(page.getRawText());
				break;
			case Json:
				value = fieldExtractor.getSelector().selectList(page.getRawText());
				break;
			case HtmlText:
				value = fieldExtractor.getSelector().selectList(page.getRawText());
				break;
			default:
				value = fieldExtractor.getSelector().selectList(html);
		}
		return value;
	}

	/**
	 * 字段字符串处理
	 *
	 * @param page
	 * @param html
	 * @param fieldExtractor
	 * @return
	 */
	private String processField(Page page, String html, FieldExtractor fieldExtractor, Map<String, Object> context, List<String> pagingUrls) {
		String value = "";

		// 微博的特殊处理
		if(page.getRequest().getUrl().contains("weibo.com") && html.contains("<script>FM.view")) {
			String urlTemp = page.getRequest().getUrl();
			if(urlTemp.contains("from") && urlTemp.contains("weibotime")) {
				urlTemp = urlTemp.substring(urlTemp.indexOf("from"), urlTemp.indexOf("weibotime"));
				System.out.println("===================================================================== " + urlTemp);
			}
			String[] ss = html.split("<script>FM.view");
			StringBuilder sb = new StringBuilder();
			for(String x : ss) {
				if (x.contains("\"html\":\"") && x.contains(urlTemp)) {
					String realContent = WeiBoModelPageProcessor.getHtml(x);
					sb.append(realContent);
				}
			}
			html = sb.toString();
		}

		switch (fieldExtractor.getSource()) {

			case RawHtml:
				value = page.getHtml().selectDocument(fieldExtractor.getSelector());
				//value = parseHtml2String(value, fieldExtractor.getFieldName());
				break;
			case Html:
				value = fieldExtractor.getSelector().select(html);
				break;
			case Url:
				value = fieldExtractor.getSelector().select(page.getUrl().toString());
				break;
			case RawText:
				value = fieldExtractor.getSelector().select(html);
				//value = trimValue(value);
				break;
			case DateText:
				// value = fieldExtractor.getSelector().select(page.getRawText());
				value = fieldExtractor.getSelector().select(html);
				value = processDateField(value);
				break;
			case NewsText:
				value = fieldExtractor.getSelector().select(html);
				value = processNewContent(page, value, fieldExtractor, context);
				//处理详情页分页
				if (pagingUrls != null && pagingUrls.size() > 0) {
					for (String url : pagingUrls) {
						String fromUrl = page.getRequest().getUrl();
						String pageHtml = HttpClientPoolUtil.httpGetRequest(fromUrl.substring(0, fromUrl.lastIndexOf("/") + 1) + url, null, null);
						if (!StringUtils.isEmpty(pageHtml)) {
							String pageValue = fieldExtractor.getSelector().select(pageHtml);
							pageValue = processNewContent(page, pageValue, fieldExtractor, context);
							value = value + pageValue;
						}
					}
				}
				break;
			case Regex:
				break;
			case Json:
				value = fieldExtractor.getSelector().select(page.getRawText());
				value = translateHtml2Json(value);
				break;
			case HtmlText:
				value = fieldExtractor.getSelector().select(html);
				value = extraAllText(page, value, context);
				break;
			case movie:
				String videoType = fieldExtractor.getVideoType();
				switch (videoType) {
					case "ifeng":
						downloadIFengVideo(page, fieldExtractor);
						break;
					case "cctv":
						downloadCCTVVideo(page, fieldExtractor);
						break;
				}
				value = "";
				break;
			default:
				value = fieldExtractor.getSelector().select(html);
		}
		return value;
	}

	// 下载 cctv 的视频
	private void downloadCCTVVideo(Page page, FieldExtractor fieldExtractor) {
		String videoList = CCTVVideoUtils.getVideolistRequest(page.getRequest().getUrl());
		String result = ChromeDriverUtils.getSourceByUrl(videoList);
		String targetVideoUrl = CCTVVideoUtils.getTargetVideoUrl(page.getRequest().getUrl(), result);
		if("".equals(targetVideoUrl)) {
			throw new RuntimeException("没有对应的文件视频");
		}
		String videoResult = ChromeDriverUtils.getSourceByUrl(targetVideoUrl);
		Map<String, Object> downloadInfo = CCTVVideoUtils.getTargetVideoDownUrl(videoResult);
		String zippath = fieldExtractor.getZippath();
		// 下载对应的文件
		CCTVVideoUtils.downFile2Local(downloadInfo, fieldExtractor.getVideoStorePath(), zippath);

	}

	// 下载凤凰视频
	private void downloadIFengVideo(Page page, FieldExtractor fieldExtractor) {
		String zippath = fieldExtractor.getZippath();
		String targetZip = "";
		WebDriver driver = ChromeDriverUtils.getHeadlessChromeDrver();
		driver.get(page.getRequest().getUrl());

		driver.findElement(By.xpath("//ul[@id='js_scrollList']/li[@class='current']")).click();

		WebElement element = driver.findElement(By.xpath(fieldExtractor.getFieldExtractExp()));
		String downloadUrl = element.getAttribute("src");
		String title = driver.findElement(By.xpath(fieldExtractor.getVideoTitle())).getText();
		driver.manage().window().maximize();
		String headimg = driver.findElement(By.xpath(fieldExtractor.getVideoHeadimg())).getAttribute("src");
		if(!StringUtils.isEmpty(headimg) && headimg.contains("w204_h115")) {
			// 封面的大小调整...
			headimg = headimg.replace("w204_h115", "q100");
		}
		String suffix = headimg.substring(headimg.lastIndexOf("."), headimg.length());
		if(!StringUtils.isEmpty(downloadUrl)) {
			try {
				String targetPath = fieldExtractor.getVideoStorePath();
				File file = new File(targetPath);
				if(!file.exists()) {
					file.mkdirs();
				}
				List<String> movies = Arrays.asList(file.list());
				String imgZipPath = "";
				if(!movies.contains(title)) {
					try {
						String imgPath = targetPath + title + File.separator + title + suffix;
						HttpFileUtil.getInstance().getVideoFileTo(headimg, imgPath);
						imgZipPath = zippath + title + File.separator + title + suffix;
						// 文件复制
						CSVUtils.copySingleFile(imgPath, imgZipPath);
					} catch (IOException e) {
						System.out.println("下载封面失败...");
						e.printStackTrace();
					}
					String filename = UUID.randomUUID().toString().replace("-", "").substring(0, 18);
					String fullname = targetPath + File.separator + title + File.separator + filename + ".MP4";
					HttpFileUtil.getInstance().getVideoFileTo(downloadUrl, fullname);
					CSVUtils.copySingleFile(fullname, zippath + title + File.separator + filename + ".MP4");
					targetZip = zippath + title + ".zip";
					// 最终的打包
					logger.info("执行打包");
					ZipCompressor zc = new ZipCompressor(targetZip);
					zc.compress(imgZipPath, fullname);
					logger.info("打包成功");
					// 删除临时文件
					HttpFileUtil.deleteDir(new File(zippath + title + File.separator));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		driver.quit();
	}

	// 抽取包含标签的所有的文本
	private String extraAllText(Page page, String value, Map<String, Object> context) {
		if(StringUtils.isEmpty(value)) {
			return "";
		}

		// 微信的封面
		if(value.contains("mmbiz_jpg")) {
			// 添加封页下载资源, 此处的路径格式应为  cover/
				ResourceFile rf = new ResourceFile("cover/",value, (String) context.get("domain"));
				List<ResourceFile> oldResources = (List<ResourceFile>)page.getResultItems().get("resources");
				// 这边的判断有点问题, 因为 微信的所有的消息都是有封面的, 对于正文部分没有图片的新闻会导致下载不到封面的信息, 显然不对
				// if(oldResources!=null&&oldResources.size()>0){
				if(oldResources!=null){
					oldResources.add(rf);
				}
				value = StringUtils.replace(value, value, rf.getRelativeUrl());

		} else {
			value = value.replaceAll("<.*?>", "");
		}
		return value;
	}

	/**
	 * 临时方法, 解析  html 的标签内容到 string 字符串
	 * 将 html 标签转化为 string 字符串
	 * @param value
	 * @param key
	 * @return
	 */
    /*
	private String parseHtml2String(String value, String key) {
		StringBuilder sb = new StringBuilder();
		if(value == null) {
			return sb.toString();
		}
		String tdStr = value.substring(value.indexOf("<td"), value.indexOf("</td>"));
		tdStr = tdStr.replaceAll("<(.*?)>", "");
		value = value.replace("&nbsp;", "");
		
		// 对于不包含中文空格的, 直接返回为 null
		if(!tdStr.contains("：")) {
			return "";
		} else {
			sb.append(tdStr);
		}
		value = value.replace(tdStr, "");
		
		value = value.replaceAll("<(.*?)>", ",");
		value = value.replace("&nbsp;", "");
		String[] temp = value.split(",");
		for(String str : temp) {
			if(str.trim().length() > 0) {
				sb.append(str.trim() + ",");
			}
		}
		if(sb.toString().contains("")) {
			return sb.toString().trim().substring(0, sb.toString().trim().length() - 1);
		}
		else {
			return "";
		}
		
	}*/

	/**
	 * 将 html 标签转化为 json 串
	 *
	 * @param str 包含 html 的字符串
	 * @return json 格式的字符串
	 */
	private String translateHtml2Json(String str) {
		Map<String, Object> map = new LinkedHashMap<>();
		String[] strs = str.split("\\n");
		for (String string : strs) {
			if (string.contains("<img src=")) {
				String value = string.substring(string.indexOf("src=") + 5, string.indexOf(".jpg") + 4);
				map.put("img", value);
			}
			if (string.contains("<li><span>")) {
				String key = string.substring(string.indexOf("<span>") + 6, string.indexOf("</span>"));
				String value = string.substring(string.indexOf("</span>") + 7, string.indexOf("</li>"));
				if (value.contains("</b>") && !value.contains("</a>")) {
					value = value.substring(value.indexOf("(") + 1, value.indexOf("分"));
				}
				if (value.contains("</b>") && value.contains("</a>")) {
					value = value.substring(value.indexOf("<b>") + 3, value.indexOf("<a"));
				}
				map.put(key, value);
			}
		}
		strs = str.split("</ul>\n");
		for (String string : strs) {
			if (!string.contains("<li><span>") && string.contains("<h4>") && !string.contains("<h4 class")) {
				String key = string.substring(string.indexOf("<h4>") + 4, string.indexOf("</h4>"));
				String value = string.substring(string.indexOf("<li>") + 4, string.indexOf("</li>"));
				map.put(key, value);
			}
		}

		// 将 map 集合转化为 json 字符串
		String jsonString = JSONUtils.toJSONString(map);

		return jsonString;
	}

	/**
	 * 处理日期类型字段
	 *
	 * @return
	 */
	private String processDateField(String dateStr) {
		String value = dateStr;
		try {
			value = HtmlFormatter.convertPubDate(dateStr);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return value;
	}

	/**
	 * 处理新闻内容字段
	 *
	 * @param page
	 * @param text
	 * @param fieldExtractor
	 * @param context
	 * @return
	 */
	private String processNewContent(Page page, String text, FieldExtractor fieldExtractor, Map<String, Object> context) {
		String value = text;
		//去除广告等其他干扰信息
		Map<String, List<String>> excludeExps = fieldExtractor.getExcludeExp();
		if (excludeExps != null) {
			for (Map.Entry<String, List<String>> excludeExp : excludeExps.entrySet()) {
				if (FieldExtractor.EXCLUDEEXPTYPE_CSSQUERY.equals(excludeExp.getKey())) {
					value = HtmlFormatter.htmlRemoveRegionByCssQuary(value, excludeExp.getValue());
				} else if (FieldExtractor.EXCLUDEEXPTYPE_REGEX.equals(excludeExp.getKey())) {
					value = HtmlFormatter.htmlRemoveRegionByRegex(value, excludeExp.getValue());
				} else if (FieldExtractor.EXCLUDEEXPTYPE_XPATH.equals(excludeExp.getKey())) {
					value = HtmlFormatter.htmlRemoveRegionByXpath(value, excludeExp.getValue());
				}
			}
		}

		//通过dom方式去除广告等干扰
		if (fieldExtractor.getRemoveDom() != null) {
			value = HtmlFormatter.removeDom(value, fieldExtractor.getRemoveDom());
		}


		//下载附件资源
		if (fieldExtractor.getResourceExtractors() != null && fieldExtractor.getResourceExtractors().size() > 0) {
			for (ResourceExtractor resExtractor : fieldExtractor.getResourceExtractors()) {
				value = processResource(page, value, resExtractor, context);
			}
		}

		// 包含链接的内容的特殊处理
//		if(fieldExtractor.get)


		if(!this.objName.equals("news_wx_19")) {
			// 19 所的数据不进行格式的调整
			value = HtmlFormatter.html2textWithOutCssandStyle(value);
		}

		return value;
	}

	/**
	 * 资源字段处理
	 *
	 * @param page
	 * @param html
	 * @param resExtractor
	 * @return
	 */
	private String processResource(Page page, String html, ResourceExtractor resExtractor, Map<String, Object> context) {
		String resultHtml = html;

		List<String> resourceUrls = new ArrayList<String>();
		String tempPdfName = "";
		if(!StringUtils.isEmpty(resExtractor.getTargetUrlPattern())) {
			String helpResource = resExtractor.getSelector().select(html);
			String helpdownloadUrl = HttpFileUtil.getRealDownloadUrl(helpResource, (String) context.get("domain"),page.getRequest().getUrl());
			System.setProperty("webdriver.chrome.marionette", "chromedriver.exe");
			ChromeOptions options = new ChromeOptions();
//			ChromeDriver driver = new ChromeDriver();
			ChromeDriver driver = ChromeDriverUtils.getHeadlessChromeDrver();
			driver.manage().window().maximize();
			driver.get(helpdownloadUrl);
			if(ChromeDriverUtils.doesWebElementExist(driver, resExtractor.getTargetUrlPattern())) {
				WebElement element = driver.findElement(By.xpath(resExtractor.getTargetUrlPattern()));
				if(null != element && !StringUtils.isEmpty(element.getAttribute("href"))) {
					String targetUrl = element.getAttribute("href");
					resourceUrls.add(targetUrl);
				}
			}
			if(ChromeDriverUtils.doesWebElementExist(driver, resExtractor.getTargetFileName())) {
				WebElement element = driver.findElement(By.xpath(resExtractor.getTargetFileName()));
				if(null != element && !StringUtils.isEmpty(element.getText())) {
					tempPdfName = element.getText();
					System.out.println("tempPdfmndsd: " + tempPdfName);
				}
			}
			driver.quit();

		} else {
			if (resExtractor.isMulti()) {
				resourceUrls = resExtractor.getSelector().selectList(html);
			} else {
				Selector selector = resExtractor.getSelector();
				if(null != selector) {
					String resourceUrl = selector.select(html);
					if (!StringUtils.isEmpty(resourceUrl)) {
						resourceUrls.add(resourceUrl);
					}
				}

			}
		}


		if (resExtractor.getResourceUrlPatterns() != null && resExtractor.getExcludeUrlPatterns().size() > 0) {

			resourceUrls = extractUrl(resourceUrls, resExtractor.getResourceUrlPatterns(), true);
		}

		if (resExtractor.getExcludeUrlPatterns() != null && resExtractor.getExcludeUrlPatterns().size() > 0) {

			resourceUrls = extractUrl(resourceUrls, resExtractor.getExcludeUrlPatterns(), false);
		}


		// 图片处理...
		List<ResourceFile> resources = new ArrayList<ResourceFile>();
		for (String fileSrc : resourceUrls) {
			String filePath = resExtractor.getResourceName() + "/";

			if (!StringUtils.isEmpty(fileSrc)) {
				fileSrc = HtmlFormatter.dealFileSrc(fileSrc);
				//处理相对路径的图片路径
				String absoluteFileSrc = HtmlFormatter.getAbsoluteFileSrc(fileSrc, page.getRequest().getUrl());
				/*String domain = HtmlFormatter.getDomain(page.getRequest().getUrl());
				if(!HtmlFormatter.urlValid(fileSrc)){
					fileSrc = "http://"+domain+"/"+fileSrc;
				}*/

				// 处理路径中包含特殊字符的请求地址
				if (fileSrc.contains(" ") || fileSrc.contains("%20")) {
					fileSrc = fileSrc.replaceAll(" ", "").replace("%20", "");
					resultHtml = resultHtml.replaceAll("(?<=src=\").*?(?=\">)", absoluteFileSrc);
				} else {
					// 将文档中的路径统一替换为绝对路径
					if(fileSrc.contains("&")) {
						fileSrc = fileSrc.replace("&", "&amp;");
					}
					resultHtml = resultHtml.replace(fileSrc, absoluteFileSrc);
				}
				// 根据绝对路径下载对应的 附件资源
				ResourceFile rf = new ResourceFile(filePath, absoluteFileSrc, (String) context.get("domain"));
				resources.add(rf);
				// 将 content 中的图片的路径 即 src 属性替换为本地的图片的路径
				// if (absoluteFileSrc.contains("&")) {
				// 	resultHtml = StringUtils.replace(resultHtml, absoluteFileSrc.replaceAll("&", "&amp;"), rf.getRelativeUrl());
				// } else {
					resultHtml = StringUtils.replace(resultHtml, absoluteFileSrc, rf.getRelativeUrl());
					if(!StringUtils.isEmpty(rf.getRelativeUrl()) && rf.getRelativeUrl().contains(".pdf")) {
						context.put("sourceSite", rf.getRelativeUrl());
					}
				if(!StringUtils.isEmpty(rf.getRelativeUrl()) && !StringUtils.isEmpty(tempPdfName)) {
					context.put("searchText", tempPdfName);
				}
				// }
			}

		}
		if(null != context.get("sourceSite")) {
			page.putField("sourceSite", (String)context.get("sourceSite"));
		}
		if(null != context.get("searchText")) {
			page.putField("searchText", (String)context.get("searchText"));
		}
		List<ResourceFile> oldResources = (List<ResourceFile>) page.getResultItems().get("resources");
		if (oldResources != null && oldResources.size() > 0) {
			oldResources.addAll(resources);
		} else {
			page.putField("resources", resources);
		}
		return resultHtml;
	}

	private List<String> extractUrl(List<String> resUrls, List<String> patterns, boolean isMatch) {
		List<String> resultUrls;
		//按照匹配规则过滤
		if (isMatch) {
			resultUrls = new ArrayList<String>();
			for (String resUrl : resUrls) {
				for (String urlPatternStr : patterns) {
					Pattern urlPattern = Pattern.compile(urlPatternStr);
					Matcher matcher = urlPattern.matcher(resUrl);
					if (matcher.matches()) {
						resultUrls.add(resUrl);
						continue;
					}
				}

			}
			//按照排除规则过滤
		} else {
			resultUrls = new ArrayList<String>();
			resultUrls.addAll(resUrls);

			for (String resUrl : resUrls) {
				for (String urlPatternStr : patterns) {
					Pattern urlPattern = Pattern.compile(urlPatternStr);
					Matcher matcher = urlPattern.matcher(resUrl);
					if (matcher.matches()) {
						resultUrls.remove(resUrl);
						continue;
					}
				}

			}

		}
		return resultUrls;
	}


	public List<Pattern> getTargetUrlPatterns() {
		return targetUrlPatterns;
	}

	public List<Pattern> getHelpUrlPatterns() {
		return helpUrlPatterns;
	}

	public Selector getTargetUrlRegionSelector() {
		return targetUrlRegionSelector;
	}

	public Selector getHelpUrlRegionSelector() {
		return helpUrlRegionSelector;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}


}
