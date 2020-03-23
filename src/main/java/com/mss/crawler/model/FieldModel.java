package com.mss.crawler.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 抽取属性模板
 * @author 伟其
 *
 */
public class FieldModel implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * xpath
	 */
	private String xpath;
	
	/**
	 * 字段名称
	 */
	private String fieldName;
	
	/**
	 * 字段代码
	 */
	private String fieldNum;
	
	/**
	 * 图片的路径属性
	 */
	private String imgSrcName;
	
	/**
	 * 采集数据类型
	 */
	private String dataType;
	
	/**
	 * dom元素
	 */
	private DomModel removeDom;
	
	/**
	 * 是否为空
	 */
	private Boolean required;
	
	/**
	 * 扩展参数
	 */
	private Map<String, Object> extra = new HashMap<String, Object>();

	public String getXpath() {
		return xpath;
	}

	public void setXpath(String xpath) {
		this.xpath = xpath;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getFieldNum() {
		return fieldNum;
	}

	public void setFieldNum(String fieldNum) {
		this.fieldNum = fieldNum;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public String getImgSrcName() {
		return imgSrcName;
	}

	public void setImgSrcName(String imgSrcName) {
		this.imgSrcName = imgSrcName;
	}

	/**
	 * @return the required
	 */
	public Boolean getRequired() {
		return required;
	}

	/**
	 * @param required the required to set
	 */
	public void setRequired(Boolean required) {
		this.required = required;
	}

	public DomModel getRemoveDom() {
		return removeDom;
	}

	public void setRemoveDom(DomModel removeDom) {
		this.removeDom = removeDom;
	}

	public Map<String, Object> getExtra() {
		return extra;
	}

	public void addExtra(String key, String value) {
		extra.put(key, value);
	}
}
