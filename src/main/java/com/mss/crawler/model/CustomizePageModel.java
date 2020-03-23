package com.mss.crawler.model;

/**
 * 定制程序爬取配置模板
 * @author 伟其
 *
 */
public class CustomizePageModel {

	/**
	 * 定制processor类名称
	 */
	private String processorClassName;
	
	/**
	 * 附加参数
	 */
	private String extra;

	public String getProcessorClassName() {
		return processorClassName;
	}

	public void setProcessorClassName(String processorClassName) {
		this.processorClassName = processorClassName;
	}

	public String getExtra() {
		return extra;
	}

	public void setExtra(String extra) {
		this.extra = extra;
	}
}
