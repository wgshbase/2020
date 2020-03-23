package com.mss.crawler.model;

import java.util.List;

/**
 * URL列表单网页爬取模板
 * @author 伟其
 *
 */
public class UrlListPageModel {

	/**
	 * URL列表
	 */
	private List<String> urlList;
	
	/**
	 * url列表存储路径
	 */
	private  String urlListPath;
	
	/**
	 * 属性配置模板List
	 */
	private List<FieldModel> fieldList;
	
	/**
	 * 分页模板
	 */
	private PagingModel pagingModel;
	
	/**
	 * 附加参数
	 */
	private String extra;

	public List<String> getUrlList() {
		return urlList;
	}

	public void setUrlList(List<String> urlList) {
		this.urlList = urlList;
	}

	public String getUrlListPath() {
		return urlListPath;
	}

	public void setUrlListPath(String urlListPath) {
		this.urlListPath = urlListPath;
	}

	public List<FieldModel> getFieldList() {
		return fieldList;
	}

	public void setFieldList(List<FieldModel> fieldList) {
		this.fieldList = fieldList;
	}

	public PagingModel getPagingModel() {
		return pagingModel;
	}

	public void setPagingModel(PagingModel pagingModel) {
		this.pagingModel = pagingModel;
	}

	public String getExtra() {
		return extra;
	}

	public void setExtra(String extra) {
		this.extra = extra;
	}
}
