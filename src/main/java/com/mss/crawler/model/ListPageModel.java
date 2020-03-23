package com.mss.crawler.model;

import java.util.List;

/**
 * 列表页爬取模板对象
 * @author 伟其
 *
 */
public class ListPageModel {

	/**
	 * 访问URL
	 */
	private String url;
	
	/**
	 * 第一个列表Xpath
	 */
	private String listXpath1;
	
	/**
	 * 第二个列表Xpath
	 */
	private String listXpath2;
	
	/**
	 * feild集合
	 */
	private List<FieldModel> fieldList;
	
	/**
	 * 是否分页 
	 */
	private boolean isPaging;

	/**
	 * 分页模板
	 */
	private PagingModel pagingModel;

	/**
	 * 附加参数
	 */
	private String extra;
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getListXpath1() {
		return listXpath1;
	}

	public void setListXpath1(String listXpath1) {
		this.listXpath1 = listXpath1;
	}

	public String getListXpath2() {
		return listXpath2;
	}

	public void setListXpath2(String listXpath2) {
		this.listXpath2 = listXpath2;
	}

	public List<FieldModel> getFieldList() {
		return fieldList;
	}

	public void setFieldList(List<FieldModel> fieldList) {
		this.fieldList = fieldList;
	}

	public boolean isPaging() {
		return isPaging;
	}

	public void setPaging(boolean isPaging) {
		this.isPaging = isPaging;
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
