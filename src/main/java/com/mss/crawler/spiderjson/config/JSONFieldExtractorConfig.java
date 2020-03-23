package com.mss.crawler.spiderjson.config;

import java.util.List;
import java.util.Map;

import com.mss.crawler.model.DomModel;

/**
 * 对象字段抽取配置
 * @author wangdw
 *
 */
public class JSONFieldExtractorConfig {
	
	/**
	 * 字段名
	 */
	private String fieldName;
	/**
	 * 抽取表达式
	 */
	private String fieldExtractorExp;
	/**
	 * 过滤表达式
	 */
	private Map<String,List<String>> excludeRegionExp;

	/**
	 * 视频的来源类型
	 */
	private String videoType;

	/**
	 * 视频的标题
	 */
	private String videoTitle;

	/**
	 * dom元素
	 */
	private DomModel removeDom;
	/**
	 * 过滤表达式类型
	 *//*
	private String excludeExpType;*/
	
	/**
	 * 从html抽取，还是从json抽取
	 */
	private String sourceType;
	
	/**
	 * 是否不为空
	 */
	private boolean notNull;
	/**
	 * 是否包含多个值
	 */
	private boolean multi;
/*	*//**
	 * 字段抽取类型
	 *//*
	private String fieldExtractorType;*/

	/**
	 * 视频文件的保存路径
	 */
	private String videoStorePath;

	/**
	 * 字段内容内部的资源文件抽取配置
	 */
	private List<InterFileExtractorConfig> innerFileConfigs;

	/**
	 * 视频封面
	 */
	private String videoHeadimg;

	/**
	 * 视频打包路径
	 */
	private String zippath;
	
	public String getFieldName() {
		return fieldName;
	}
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	public boolean isNotNull() {
		return notNull;
	}
	public void setNotNull(boolean notNull) {
		this.notNull = notNull;
	}
	public boolean isMulti() {
		return multi;
	}
	public void setMulti(boolean multi) {
		this.multi = multi;
	}
	/*public String getFieldExtractorType() {
		return fieldExtractorType;
	}
	public void setFieldExtractorType(String fieldExtractorType) {
		this.fieldExtractorType = fieldExtractorType;
	}*/
	
	public String getFieldExtractorExp() {
		return fieldExtractorExp;
	}
	public void setFieldExtractorExp(String fieldExtractorExp) {
		this.fieldExtractorExp = fieldExtractorExp;
	}
	public Map<String, List<String>> getExcludeRegionExp() {
		return excludeRegionExp;
	}
	public void setExcludeRegionExp(Map<String, List<String>> excludeRegionExp) {
		this.excludeRegionExp = excludeRegionExp;
	}
	public List<InterFileExtractorConfig> getInnerFileConfigs() {
		return innerFileConfigs;
	}
	public void setInnerFileConfigs(List<InterFileExtractorConfig> innerFileConfigs) {
		this.innerFileConfigs = innerFileConfigs;
	}
	public String getSourceType() {
		return sourceType;
	}
	public void setSourceType(String sourceType) {
		this.sourceType = sourceType;
	}
	public DomModel getRemoveDom() {
		return removeDom;
	}
	public void setRemoveDom(DomModel removeDom) {
		this.removeDom = removeDom;
	}

	public String getVideoType() {
		return videoType;
	}

	public void setVideoType(String videoType) {
		this.videoType = videoType;
	}

	public String getVideoTitle() {
		return videoTitle;
	}

	public void setVideoTitle(String videoTitle) {
		this.videoTitle = videoTitle;
	}

	public String getVideoStorePath() {
		return videoStorePath;
	}

	public void setVideoStorePath(String videoStorePath) {
		this.videoStorePath = videoStorePath;
	}

	public String getVideoHeadimg() {
		return videoHeadimg;
	}

	public void setVideoHeadimg(String videoHeadimg) {
		this.videoHeadimg = videoHeadimg;
	}

	public String getZippath() {
		return zippath;
	}

	public void setZippath(String zippath) {
		this.zippath = zippath;
	}
}
