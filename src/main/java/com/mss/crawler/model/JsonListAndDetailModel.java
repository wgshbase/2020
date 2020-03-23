package com.mss.crawler.model;

/**
 * 列表JSON請求模板
 * @author wwq
 */
public class JsonListAndDetailModel extends ListAndDetailModel{

	private String dataKey;

	private String jsonPath;
	
	private String jsonFieldType;
	
	private boolean firstIsJson;
	
	public String getDataKey() {
		return dataKey;
	}

	public void setDataKey(String dataKey) {
		this.dataKey = dataKey;
	}

	public String getJsonPath() {
		return jsonPath;
	}

	public void setJsonPath(String jsonPath) {
		this.jsonPath = jsonPath;
	}

	public boolean isFirstIsJson() {
		return firstIsJson;
	}

	public void setFirstIsJson(boolean firstIsJson) {
		this.firstIsJson = firstIsJson;
	}

	public String getJsonFieldType() {
		return jsonFieldType;
	}

	public void setJsonFieldType(String jsonFieldType) {
		this.jsonFieldType = jsonFieldType;
	}
}
