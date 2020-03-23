package com.mss.crawler.model;

import java.util.Map;

/**
* @ClassName: CommonCrawlerParam  
  
* @Description: TODO(爬虫公共配置对象)  
  
* @author wwq  
  
* @date 2017-5-19 下午5:42:17
 */
public class CommonCrawlerParam {

	/**
	 * 爬虫domain
	 */
	private String domain;
	
	/**
	 * 爬虫对应的UUID
	 */
	private String uuid;
	
	/**
	 * 爬虫线程数量
	 */
	private Integer threadNum;
	
	/**
	 * 爬虫启动URL
	 */
	private String startUrls;
	
	/**
	 * url列表
	 */
	private String urls;
	
	/**
	 * 是否读取缓冲URL
	 */
	private boolean isReadUrlCache = true;
	
	/**
	 * 爬取间隔时间
	 */
	private String sleepTime;
	
	private String seleniumSleepTime = "6000";
	
	/**
	 * 请求头部信息
	 */
	private Map<String, String> headers;
	
	/**
	 * 请求超时重试次数
	 */
	private int cycleRetryTimes;
	
	/**
	 * 失败重试次数
	 */
	private int retryTimes;
	
	private int retrySleepTime;
	
	/**
	 * 页面子URL爬取间隔时间
	 */
	private String sonSleepTime;
	
	/**
	 * 爬取超时时间
	 */
	private String timeout;
	
	/**
	 * IP代理
	 */
	private String ipProxy;
	
	/**
	 * 存储文件的根目录
	 */
	private String rootPath;
		
	/**
	 * 运行信息存储路径
	 */
	private String runInfoPath = "run";
	
	/**
	 * 数据存储路径
	 */
	private String dataPath = "data";
	
	/**
	 * 图片存储路径
	 */
	private String imageFilePath;
	
	/**
	 * URL队列类名称
	 */
	private String urlSchedulerClassName;
	
	/**
	 * downloader类名称
	 */
	private String downloadType;
	
	/**
	 * downloader类名称
	 */
	private String downloaderClassName;
	
	/**
	 * 存储通道类名称
	 */
	private String pipelineClassName;
	
	/**
	 * 日志存储类名
	 */
	private String logStoredClassName;
	
	/**
	 * 单个线程下载文件的大小（单位是KB）
	 */
	private long threadDownloadFileSize;
	
	/**
	 * 匹配页面中标签属性的正则
	 */
	private String regexDomField;

	/**
	 * 匹配页面中A标签的正则
	 */
	private String regexALable;

	/**
	 * 匹配页面中script的正则
	 */
	private String regexScriptLable;

	/**
	 * 匹配页面中Iframe的正则
	 */
	private String regexIframeLable;
	
	/**
	 * 附件URL区域
	 */
	private String attachFileUrlRegion;
	
	/**
	 * 附件URL正则
	 */
	private String attachFileUrlRegex;
	
	/**
	 * 附件URL Xpath
	 */
	private String attachFileUrlXpath;
	
	/**
	 * 自定义附加参数
	 */
	private String extraParam;

	/**
	 * @return the domain
	 */
	public String getDomain() {
		return domain;
	}

	/**
	 * @param domain the domain to set
	 */
	public void setDomain(String domain) {
		this.domain = domain;
	}

	/**
	 * @return the uuid
	 */
	public String getUuid() {
		return uuid;
	}

	/**
	 * @param uuid the uuid to set
	 */
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	/**
	 * @return the threadNum
	 */
	public Integer getThreadNum() {
		return threadNum;
	}

	/**
	 * @param threadNum the threadNum to set
	 */
	public void setThreadNum(Integer threadNum) {
		this.threadNum = threadNum;
	}

	/**
	 * @return the startUrls
	 */
	public String getStartUrls() {
		return startUrls;
	}

	/**
	 * @param startUrls the startUrls to set
	 */
	public void setStartUrls(String startUrls) {
		this.startUrls = startUrls;
	}

	/**
	 * @return the urls
	 */
	public String getUrls() {
		return urls;
	}

	/**
	 * @param urls the urls to set
	 */
	public void setUrls(String urls) {
		this.urls = urls;
	}

	/**
	 * @return the isReadUrlCache
	 */
	public boolean isReadUrlCache() {
		return isReadUrlCache;
	}

	/**
	 * @param isReadUrlCache the isReadUrlCache to set
	 */
	public void setReadUrlCache(boolean isReadUrlCache) {
		this.isReadUrlCache = isReadUrlCache;
	}

	/**
	 * @return the sleepTime
	 */
	public String getSleepTime() {
		return sleepTime;
	}

	/**
	 * @param sleepTime the sleepTime to set
	 */
	public void setSleepTime(String sleepTime) {
		this.sleepTime = sleepTime;
	}

	/**
	 * @return the headers
	 */
	public Map<String, String> getHeaders() {
		return headers;
	}

	/**
	 * @param headers the headers to set
	 */
	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}

	/**
	 * @return the cycleRetryTimes
	 */
	public int getCycleRetryTimes() {
		return cycleRetryTimes;
	}

	/**
	 * @param cycleRetryTimes the cycleRetryTimes to set
	 */
	public void setCycleRetryTimes(int cycleRetryTimes) {
		this.cycleRetryTimes = cycleRetryTimes;
	}

	/**
	 * @return the retryTimes
	 */
	public int getRetryTimes() {
		return retryTimes;
	}

	/**
	 * @param retryTimes the retryTimes to set
	 */
	public void setRetryTimes(int retryTimes) {
		this.retryTimes = retryTimes;
	}

	/**
	 * @return the retrySleepTime
	 */
	public int getRetrySleepTime() {
		return retrySleepTime;
	}

	/**
	 * @param retrySleepTime the retrySleepTime to set
	 */
	public void setRetrySleepTime(int retrySleepTime) {
		this.retrySleepTime = retrySleepTime;
	}

	/**
	 * @return the sonSleepTime
	 */
	public String getSonSleepTime() {
		return sonSleepTime;
	}

	/**
	 * @param sonSleepTime the sonSleepTime to set
	 */
	public void setSonSleepTime(String sonSleepTime) {
		this.sonSleepTime = sonSleepTime;
	}

	/**
	 * @return the timeout
	 */
	public String getTimeout() {
		return timeout;
	}

	/**
	 * @param timeout the timeout to set
	 */
	public void setTimeout(String timeout) {
		this.timeout = timeout;
	}

	/**
	 * @return the seleniumSleepTime
	 */
	public String getSeleniumSleepTime() {
		return seleniumSleepTime;
	}

	/**
	 * @param seleniumSleepTime the seleniumSleepTime to set
	 */
	public void setSeleniumSleepTime(String seleniumSleepTime) {
		this.seleniumSleepTime = seleniumSleepTime;
	}

	/**
	 * @return the ipProxy
	 */
	public String getIpProxy() {
		return ipProxy;
	}

	/**
	 * @param ipProxy the ipProxy to set
	 */
	public void setIpProxy(String ipProxy) {
		this.ipProxy = ipProxy;
	}

	/**
	 * @return the rootPath
	 */
	public String getRootPath() {
		return rootPath;
	}

	/**
	 * @param rootPath the rootPath to set
	 */
	public void setRootPath(String rootPath) {
		this.rootPath = rootPath;
	}

	/**
	 * @return the runInfoPath
	 */
	public String getRunInfoPath() {
		return runInfoPath;
	}

	/**
	 * @param runInfoPath the runInfoPath to set
	 */
	public void setRunInfoPath(String runInfoPath) {
		this.runInfoPath = runInfoPath;
	}

	/**
	 * @return the dataPath
	 */
	public String getDataPath() {
		return dataPath;
	}

	/**
	 * @param dataPath the dataPath to set
	 */
	public void setDataPath(String dataPath) {
		this.dataPath = dataPath;
	}

	/**
	 * @return the imageFilePath
	 */
	public String getImageFilePath() {
		return imageFilePath;
	}

	/**
	 * @param imageFilePath the imageFilePath to set
	 */
	public void setImageFilePath(String imageFilePath) {
		this.imageFilePath = imageFilePath;
	}

	/**
	 * @return the urlSchedulerClassName
	 */
	public String getUrlSchedulerClassName() {
		return urlSchedulerClassName;
	}

	/**
	 * @param urlSchedulerClassName the urlSchedulerClassName to set
	 */
	public void setUrlSchedulerClassName(String urlSchedulerClassName) {
		this.urlSchedulerClassName = urlSchedulerClassName;
	}

	/**
	 * @return the downloadType
	 */
	public String getDownloadType() {
		return downloadType;
	}

	/**
	 * @param downloadType the downloadType to set
	 */
	public void setDownloadType(String downloadType) {
		this.downloadType = downloadType;
	}

	/**
	 * @return the downloaderClassName
	 */
	public String getDownloaderClassName() {
		return downloaderClassName;
	}

	/**
	 * @param downloaderClassName the downloaderClassName to set
	 */
	public void setDownloaderClassName(String downloaderClassName) {
		this.downloaderClassName = downloaderClassName;
	}

	/**
	 * @return the pipelineClassName
	 */
	public String getPipelineClassName() {
		return pipelineClassName;
	}

	/**
	 * @param pipelineClassName the pipelineClassName to set
	 */
	public void setPipelineClassName(String pipelineClassName) {
		this.pipelineClassName = pipelineClassName;
	}

	/**
	 * @return the logStoredClassName
	 */
	public String getLogStoredClassName() {
		return logStoredClassName;
	}

	/**
	 * @param logStoredClassName the logStoredClassName to set
	 */
	public void setLogStoredClassName(String logStoredClassName) {
		this.logStoredClassName = logStoredClassName;
	}

	/**
	 * @return the threadDownloadFileSize
	 */
	public long getThreadDownloadFileSize() {
		return threadDownloadFileSize;
	}

	/**
	 * @param threadDownloadFileSize the threadDownloadFileSize to set
	 */
	public void setThreadDownloadFileSize(long threadDownloadFileSize) {
		this.threadDownloadFileSize = threadDownloadFileSize;
	}

	/**
	 * @return the regexDomField
	 */
	public String getRegexDomField() {
		return regexDomField;
	}

	/**
	 * @param regexDomField the regexDomField to set
	 */
	public void setRegexDomField(String regexDomField) {
		this.regexDomField = regexDomField;
	}

	/**
	 * @return the regexALable
	 */
	public String getRegexALable() {
		return regexALable;
	}

	/**
	 * @param regexALable the regexALable to set
	 */
	public void setRegexALable(String regexALable) {
		this.regexALable = regexALable;
	}

	/**
	 * @return the regexScriptLable
	 */
	public String getRegexScriptLable() {
		return regexScriptLable;
	}

	/**
	 * @param regexScriptLable the regexScriptLable to set
	 */
	public void setRegexScriptLable(String regexScriptLable) {
		this.regexScriptLable = regexScriptLable;
	}

	/**
	 * @return the regexIframeLable
	 */
	public String getRegexIframeLable() {
		return regexIframeLable;
	}

	/**
	 * @param regexIframeLable the regexIframeLable to set
	 */
	public void setRegexIframeLable(String regexIframeLable) {
		this.regexIframeLable = regexIframeLable;
	}

	/**
	 * @return the attachFileUrlRegion
	 */
	public String getAttachFileUrlRegion() {
		return attachFileUrlRegion;
	}

	/**
	 * @param attachFileUrlRegion the attachFileUrlRegion to set
	 */
	public void setAttachFileUrlRegion(String attachFileUrlRegion) {
		this.attachFileUrlRegion = attachFileUrlRegion;
	}

	/**
	 * @return the attachFileUrlRegex
	 */
	public String getAttachFileUrlRegex() {
		return attachFileUrlRegex;
	}

	/**
	 * @param attachFileUrlRegex the attachFileUrlRegex to set
	 */
	public void setAttachFileUrlRegex(String attachFileUrlRegex) {
		this.attachFileUrlRegex = attachFileUrlRegex;
	}

	/**
	 * @return the extraParam
	 */
	public String getExtraParam() {
		return extraParam;
	}

	/**
	 * @param extraParam the extraParam to set
	 */
	public void setExtraParam(String extraParam) {
		this.extraParam = extraParam;
	}

	/**
	 * @return the attachFileUrlXpath
	 */
	public String getAttachFileUrlXpath() {
		return attachFileUrlXpath;
	}

	/**
	 * @param attachFileUrlXpath the attachFileUrlXpath to set
	 */
	public void setAttachFileUrlXpath(String attachFileUrlXpath) {
		this.attachFileUrlXpath = attachFileUrlXpath;
	}
}
