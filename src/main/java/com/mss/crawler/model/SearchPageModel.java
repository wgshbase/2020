package com.mss.crawler.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 列表及详情页采集配置模板对象
 * @author wwq
 *
 */
public class SearchPageModel extends JsonListAndDetailModel{
	
	private String searchTextPath;

	private boolean isReadCursor = true;
	
	private String jsonRegex;
	
	private String searchType;
		
	private String searchListXpath1;
	
	private String searchListXpath2;
	
	private Map<String, SinglePageModel> modelMap = new HashMap<String, SinglePageModel>();
	
	/**
	 * feild集合
	 */
	private List<FieldModel> listPageFieldList;

	/**
	 * 搜索参数名称
	 */
	private String searchParamName;
	
	public String getSearchTextPath() {
		return searchTextPath;
	}

	public void setSearchTextPath(String searchTextPath) {
		this.searchTextPath = searchTextPath;
	}

	public String getSearchParamName() {
		return searchParamName;
	}

	public void setSearchParamName(String searchParamName) {
		this.searchParamName = searchParamName;
	}

	public List<FieldModel> getListPageFieldList() {
		return listPageFieldList;
	}

	public void setListPageFieldList(List<FieldModel> listPageFieldList) {
		this.listPageFieldList = listPageFieldList;
	}

	public String getSearchListXpath1() {
		return searchListXpath1;
	}

	public void setSearchListXpath1(String searchListXpath1) {
		this.searchListXpath1 = searchListXpath1;
	}

	public String getSearchListXpath2() {
		return searchListXpath2;
	}

	public void setSearchListXpath2(String searchListXpath2) {
		this.searchListXpath2 = searchListXpath2;
	}

	public boolean isReadCursor() {
		return isReadCursor;
	}

	public void setReadCursor(boolean isReadCursor) {
		this.isReadCursor = isReadCursor;
	}

	public String getJsonRegex() {
		return jsonRegex;
	}

	public void setJsonRegex(String jsonRegex) {
		this.jsonRegex = jsonRegex;
	}

	public String getSearchType() {
		return searchType;
	}

	public void setSearchType(String searchType) {
		this.searchType = searchType;
	}

	public Map<String, SinglePageModel> getModelMap() {
		return modelMap;
	}

	public void setModelMap(Map<String, SinglePageModel> modelMap) {
		this.modelMap = modelMap;
	}
}
