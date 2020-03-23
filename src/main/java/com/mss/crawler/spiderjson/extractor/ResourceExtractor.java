package com.mss.crawler.spiderjson.extractor;

import java.util.List;

import us.codecraft.webmagic.selector.Selector;

/**
 * 对文件资源进行抽取
 * @author wangdw
 *
 */
public class ResourceExtractor extends Extractor{
	
	/**
	 * 资源名称
	 */
	private String resourceName;

	/**
	 * 资源链接匹配规则
	 */
	private List<String> resourceUrlPatterns;
	/**
	 * 排除资源链接匹配规则
	 */
	private List<String> excludeUrlPatterns;

	/**
	 * 辅助资源理解匹配规则
	 */
	private String targetFileName;

	/**
	 * 最终资源url定位规则
	 */
	private String targetUrlPattern;
	
	
	public ResourceExtractor(Selector selector, Source source, boolean notNull, boolean multi,String resourceName,List<String> resourceUrlPatterns,List<String> excludeUrlPatterns,String targetFileName,String targetUrlPattern) {
		super(selector, source, notNull, multi);
		this.resourceName = resourceName;
		this.resourceUrlPatterns = resourceUrlPatterns;
		this.excludeUrlPatterns = excludeUrlPatterns;
		this.targetFileName = targetFileName;
		this.targetUrlPattern = targetUrlPattern;
	}

	public String getResourceName() {
		return resourceName;
	}	

	public List<String> getResourceUrlPatterns() {
		return resourceUrlPatterns;
	}

	public List<String> getExcludeUrlPatterns() {
		return excludeUrlPatterns;
	}

	public String getTargetFileName() {
		return targetFileName;
	}

	public String getTargetUrlPattern() {
		return targetUrlPattern;
	}
}
