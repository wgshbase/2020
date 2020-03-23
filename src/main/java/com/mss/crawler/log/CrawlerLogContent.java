package com.mss.crawler.log;

import java.util.Date;

/**
 * 采集日志内容对象
 * @author 伟其
 *
 */
public class CrawlerLogContent {
	
	/**
	 * 页面编号
	 */
	private String pageNum;
	
	/**
	 * 页面采集状态
	 */
	private String crawlerStatus;
	
	/**
	 * 任务ID
	 */
	private String taskID;
	
	/**
	 * 页面类型
	 */
	private String pageType;
	
	/**
	 * 页面大小
	 */
	private String pageSize;
	
	/**
	 * 采集耗时
	 */
	private String crawlerSpendTime;
	
	/**
	 * 页面采集完成时间
	 */
	private Date finishTime;
	
	/**
	 * 页面标题
	 */
	private String pageTitle;
	
	/**
	 * 页面URL
	 */
	private String url;
	
	/**
	 * 采集IP
	 */
	private String IP;
	
	/**
	 * http响应状态码
	 */
	private String httpStatusCode;
	
	/**
	 * 错误信息
	 */
	private String errorInfo;

	public String getPageNum() {
		return pageNum;
	}

	public void setPageNum(String pageNum) {
		this.pageNum = pageNum;
	}

	public String getCrawlerStatus() {
		return crawlerStatus;
	}

	public void setCrawlerStatus(String crawlerStatus) {
		this.crawlerStatus = crawlerStatus;
	}

	public String getTaskID() {
		return taskID;
	}

	public void setTaskID(String taskID) {
		this.taskID = taskID;
	}

	public String getPageType() {
		return pageType;
	}

	public void setPageType(String pageType) {
		this.pageType = pageType;
	}

	public String getPageSize() {
		return pageSize;
	}

	public void setPageSize(String pageSize) {
		this.pageSize = pageSize;
	}

	public String getCrawlerSpendTime() {
		return crawlerSpendTime;
	}

	public void setCrawlerSpendTime(String crawlerSpendTime) {
		this.crawlerSpendTime = crawlerSpendTime;
	}

	public Date getFinishTime() {
		return finishTime;
	}

	public void setFinishTime(Date finishTime) {
		this.finishTime = finishTime;
	}

	public String getPageTitle() {
		return pageTitle;
	}

	public void setPageTitle(String pageTitle) {
		this.pageTitle = pageTitle;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getIP() {
		return IP;
	}

	public void setIP(String iP) {
		IP = iP;
	}

	public String getHttpStatusCode() {
		return httpStatusCode;
	}

	public void setHttpStatusCode(String httpStatusCode) {
		this.httpStatusCode = httpStatusCode;
	}

	public String getErrorInfo() {
		return errorInfo;
	}

	public void setErrorInfo(String errorInfo) {
		this.errorInfo = errorInfo;
	}
}
