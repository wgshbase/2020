package io.renren.modules.spider.entity;

import java.io.Serializable;

import org.hibernate.validator.constraints.NotBlank;

/**
 * 网页抽取模板
 *
 * @author Gao Shen
 * @version 16/4/12
 */
public class SpiderInfo implements Serializable{

	private static final long serialVersionUID = -2211329133190737811L;
	/**
     * 抓取模板id
     */
    private String id;
    /**
     * 网站名称
     */
    @NotBlank(message="参数名不能为空")
    private String siteName;
    /**
     * 域名
     */
    @NotBlank(message="参数名不能为空")
    private String domain;
    /**
     * json配置
     */
    @NotBlank(message="参数名不能为空")
    private String jsonData;
    
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getSiteName() {
		return siteName;
	}
	public void setSiteName(String siteName) {
		this.siteName = siteName;
	}
	public String getDomain() {
		return domain;
	}
	public void setDomain(String domain) {
		this.domain = domain;
	}
	public String getJsonData() {
		return jsonData;
	}
	public void setJsonData(String jsonData) {
		this.jsonData = jsonData;
	}
	
}
