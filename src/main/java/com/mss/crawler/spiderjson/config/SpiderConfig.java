package com.mss.crawler.spiderjson.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.mss.crawler.spiderjson.model.ListContentModel;
import org.apache.commons.io.FileUtils;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.ParserConfig;
import com.mss.crawler.spiderjson.model.PagingModel;

import us.codecraft.webmagic.selector.Selector;

public class SpiderConfig {
	// 解决 JsonConfig 的 bug
	static{  
		ParserConfig.getGlobalInstance().setAsmEnable(false);  
	} 
	/**
	 * 爬虫标识
	 */
	private String id;

	/**
	 * 页面处理模型："site" = 网站类型," subject"=主题类型
	 */
	private String pageProcessorModel;

	/**
	 * 需要翻译字段
	 */
	private List<String> translateFields;

	public List<String> getTranslateFields() {
		return translateFields;
	}

	public void setTranslateFields(List<String> translateFields) {
		this.translateFields = translateFields;
	}

	/**
	 * 管道列表
	 */
	private String[] pipelines;

	/**
	 * 只爬取当前页
	 */
	private boolean singlePage = false;

	/**
	 * 使用多少抓取线程
	 */
	private int thread = 1;
	/**
	 * 失败的网页重试次数
	 */
	private int retry = 2;
	/**
	 * 抓取每个网页睡眠时间
	 */
	private int sleep = 5000;
	/**
	 * 最大抓取网页数量,0代表不限制
	 */
	private int maxPageGather = 10;
	/**
	 * HTTP链接超时时间
	 */
	private int timeout = 200000;
	/**
	 * 网站权重
	 */
	private int priority;
	/**
	 * 网站名称
	 */
	private String siteName;
	/**
	 * 域名
	 */
	private String domain;
	/**
	 * 起始链接
	 */
	private String[] startURL;

	/**
	 * 主题索引列表
	 */
	private String[] indexs;

	/**
	 * 主题索引列表
	 */
	private String indexFile;

	/**
	 * 种子链接表达式
	 */
	private String seedRex;

	/**
	 * 列表抽取表达式
	 */
	private String listHrefXPath;
	
	/**
	 * 根据 regex 抽取列表详情页链接 
	 */
	private String listHrefRegex;

	/**
	 * 根据图片的 onclick 事件确定详情页链接
	 */
	private String listHrefOnclick;
	
	/**
	 * 网站语种
	 */
	private String language;
	
	/**
	 * 新闻的类型
	 */
	private String newsCategory;
	
	/**
	 * 对应的索引库
	 */
	private String dbType;
	
	/**
	 * 网站的来源
	 */
	private String src;

	/**
	 * 是否需要翻墙的站点
	 */
	private Boolean isGoogleSite;

	/**
	 *  模板的分类, 不同的项目对应不同的模板
	 */
	private String sslm;

	/**
	 * 需要 js 加载
	 *
	 */
	private Boolean isPhantomJs;

	/**
	 * 按照 JsonPath 获取详情页的链接
	 */
	private String listHrefJsonPath;

	/**
	 * 抽取对象配置
	 */
	private Map<String, JSONExtractorConfig> extractorCfgs;

	/**
	 * 编码
	 */
	private String charset = "utf-8";

	/**
	 * 爬取页数
	 */
	private int pageSize;

	/**
	 * 采集资源文件根路径
	 */
	private String rootPath;

	/**
	 * User Agent
	 */
	private String userAgent = "Mozilla/5.0 (Windows NT 5.2) AppleWebKit/534.30 (KHTML, like Gecko) Chrome/12.0.742.122 Safari/534.30";

	/**
	 * 请求头部信息
	 */
	private Map<String, String> headers;
	/**
	 * 是否保存网页快照,默认保存
	 */
	private boolean saveCapture = true;

	/**
	 * 主内容抽取页url匹配规则
	 */
	private List<String> targetUrlPatterns = new ArrayList<String>();

	/**
	 * 主内容抽取区域选择器
	 */
	private Selector targetUrlRegionSelector;

	/**
	 * 辅助url抽取页 url匹配规则
	 */
	private List<String> helpUrlPatterns = new ArrayList<String>();
	/**
	 * 辅助url抽取页区域选择器
	 */
	private Selector helpUrlRegionSelector;

	/**
	 * 回调更新信息Url
	 */
	private String callbackURL;

	/**
	 * 是否使用代理
	 */
	private boolean isUseProxy;

	/**
	 * 处理列表翻页
	 */
	private PagingModel pagingModel;

	/**
	 * 处理列表的基础信息块
	 */
	private ListContentModel listContentModel;

	/**
	 * 微信采集平台用户名
	 * @return
	 */
	private String username;

	/**
	 * 微信采集平台密码
	 */
	private String password;

	public ListContentModel getListContentModel() {
		return listContentModel;
	}

	public void setListContentModel(ListContentModel listContentModel) {
		this.listContentModel = listContentModel;
	}

	public Boolean getPhantomJs() {
		return isPhantomJs;
	}

	public void setPhantomJs(Boolean phantomJs) {
		isPhantomJs = phantomJs;
	}

	public String getSslm() {
		return sslm;
	}

	public void setSslm(String sslm) {
		this.sslm = sslm;
	}

	public Boolean getGoogleSite() {
		return isGoogleSite;
	}

	public void setGoogleSite(Boolean googleSite) {
		isGoogleSite = googleSite;
	}

	public String getListHrefOnclick() {
		return listHrefOnclick;
	}

	public void setListHrefOnclick(String listHrefOnclick) {
		this.listHrefOnclick = listHrefOnclick;
	}

	public String getSrc() {
		return src;
	}

	public void setSrc(String src) {
		this.src = src;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}

	public String getDbType() {
		return dbType;
	}

	public void setDbType(String dbType) {
		this.dbType = dbType;
	}

	public String getNewsCategory() {
		return newsCategory;
	}

	public void setNewsCategory(String newsCategory) {
		this.newsCategory = newsCategory;
	}
	
	public String getListHrefJsonPath() {
		return listHrefJsonPath;
	}

	public void setListHrefJsonPath(String listHrefJsonPath) {
		this.listHrefJsonPath = listHrefJsonPath;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getListHrefXPath() {
		return listHrefXPath;
	}

	public void setListHrefXPath(String listHrefXPath) {
		this.listHrefXPath = listHrefXPath;
	}

	public String getListHrefRegex() {
		return listHrefRegex;
	}

	public void setListHrefRegex(String listHrefRegex) {
		this.listHrefRegex = listHrefRegex;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public PagingModel getPagingModel() {
		return pagingModel;
	}

	public void setPagingModel(PagingModel pagingModel) {
		this.pagingModel = pagingModel;
	}

	public boolean isUseProxy() {
		return isUseProxy;
	}

	public void setUseProxy(boolean isUseProxy) {
		this.isUseProxy = isUseProxy;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCallbackURL() {
		return callbackURL;
	}

	public void setCallbackURL(String callbackURL) {
		this.callbackURL = callbackURL;
	}

	public List<String> getTargetUrlPatterns() {
		return targetUrlPatterns;
	}

	public void setTargetUrlPatterns(List<String> targetUrlPatterns) {
		this.targetUrlPatterns = targetUrlPatterns;
	}

	public Selector getTargetUrlRegionSelector() {
		return targetUrlRegionSelector;
	}

	public void setTargetUrlRegionSelector(Selector targetUrlRegionSelector) {
		this.targetUrlRegionSelector = targetUrlRegionSelector;
	}

	public List<String> getHelpUrlPatterns() {
		return helpUrlPatterns;
	}

	public void setHelpUrlPatterns(List<String> helpUrlPatterns) {
		this.helpUrlPatterns = helpUrlPatterns;
	}

	public Selector getHelpUrlRegionSelector() {
		return helpUrlRegionSelector;
	}

	public void setHelpUrlRegionSelector(Selector helpUrlRegionSelector) {
		this.helpUrlRegionSelector = helpUrlRegionSelector;
	}

	public SpiderConfig() {

	}

	public String getPageProcessorModel() {
		return pageProcessorModel;
	}

	public void setPageProcessorModel(String pageProcessorModel) {
		this.pageProcessorModel = pageProcessorModel;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public static SpiderConfig create(File file) throws IOException {
		String json = FileUtils.readFileToString(file);
		SpiderConfig cfg = (SpiderConfig) JSONObject.parseObject(json, SpiderConfig.class);
		return cfg;
	}

	public static SpiderConfig create(String json) {
		json = formatJson(json);
		SpiderConfig cfg = (SpiderConfig) JSONObject.parseObject(json, SpiderConfig.class);
		return cfg;
	}

	// 格式化爬虫的采集模板中的特殊字符
	private static String formatJson(String json) {
		if(json.contains("&lt;")){ 
			json = json.replaceAll("&lt;", "<");
		}
		if(json.contains("&gt;")) {
			json = json.replaceAll("&gt;", ">");
			
		}
		if(json.contains("&amp;")) {
			json = json.replace("&amp;", "&");
		}
		return json;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	public boolean isSaveCapture() {
		return saveCapture;
	}

	public void setSaveCapture(boolean saveCapture) {
		this.saveCapture = saveCapture;
	}

	public int getThread() {
		return thread;
	}

	public void setThread(int thread) {
		this.thread = thread;
	}

	public int getRetry() {
		return retry;
	}

	public void setRetry(int retry) {
		this.retry = retry;
	}

	public int getSleep() {
		return sleep;
	}

	public void setSleep(int sleep) {
		this.sleep = sleep;
	}

	public int getMaxPageGather() {
		return maxPageGather;
	}

	public void setMaxPageGather(int maxPageGather) {
		this.maxPageGather = maxPageGather;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public String getSiteName() {
		return siteName;
	}

	public void setSiteName(String siteName) {
		this.siteName = siteName;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String[] getStartURL() {
		return startURL;
	}

	public void setStartURL(String[] startURL) {
		this.startURL = startURL;
	}

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public boolean isSinglePage() {
		return singlePage;
	}

	public void setSinglePage(boolean singlePage) {
		this.singlePage = singlePage;
	}

	public Map<String, JSONExtractorConfig> getExtractorCfgs() {
		return extractorCfgs;
	}

	public void setExtractorCfgs(Map<String, JSONExtractorConfig> extractorCfgs) {
		this.extractorCfgs = extractorCfgs;
	}

	public String getRootPath() {
		return rootPath;
	}

	public void setRootPath(String rootPath) {
		this.rootPath = rootPath;
	}

	public String[] getIndexs() {
		return indexs;
	}

	public void setIndexs(String[] indexs) {
		this.indexs = indexs;
	}

	public String getSeedRex() {
		return seedRex;
	}

	public void setSeedRex(String seedRex) {
		this.seedRex = seedRex;
	}

/*	public String getListHrefXPath() {
		return listHrefXPath;
	}

	public void setListHrefXPath(String listHrefXPath) {
		this.listHrefXPath = listHrefXPath;
	}*/

	public String getIndexFile() {
		return indexFile;
	}

	public void setIndexFile(String indexFile) {
		this.indexFile = indexFile;
	}

	public String[] getPipelines() {
		return pipelines;
	}

	public void setPipelines(String[] pipelines) {
		this.pipelines = pipelines;
	}

	public static void main(String[] args) throws IOException{
		
		String cfgFilePath = "D:/workspace/product/BSCrawler/doc/crawlerConfigex/cnDetail_sinamil.txt";
		String json = FileUtils.readFileToString(new File(cfgFilePath));
		SpiderConfig cfg = (SpiderConfig) JSONObject.parseObject(json, SpiderConfig.class);
	}
}
