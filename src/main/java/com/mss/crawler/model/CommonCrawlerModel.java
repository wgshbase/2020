package com.mss.crawler.model;

/**
 * 采集总配置模板
 * @author 伟其
 *
 */
public class CommonCrawlerModel {
	
	/**
	 * 采集公共配置参数
	 */
	private CommonCrawlerParam commonCrawlerParam;
	
	/**
	 * 采集类型
	 */
	private String crawlerType;
	
	/**
	 * 更新爬虫状态URL
	 */
	private String updateStatusUrl;
	
	/**
	 * 对应的采集模板
	 */
	private Object pageModel;

	public CommonCrawlerParam getCommonCrawlerParam() {
		return commonCrawlerParam;
	}

	public void setCommonCrawlerParam(CommonCrawlerParam commonCrawlerParam) {
		this.commonCrawlerParam = commonCrawlerParam;
	}

	public String getCrawlerType() {
		return crawlerType;
	}

	public void setCrawlerType(String crawlerType) {
		this.crawlerType = crawlerType;
	}

	public Object getPageModel() {
		return pageModel;
	}

	public void setPageModel(Object pageModel) {
		this.pageModel = pageModel;
	}

	public String getUpdateStatusUrl() {
		return updateStatusUrl;
	}

	public void setUpdateStatusUrl1(String updateStatusUrl) {
		this.updateStatusUrl = updateStatusUrl;
	}
}
