package io.renren.modules.spider.entity;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Date;

public class News implements Serializable{

	private static final long serialVersionUID = 4212164452199968156L;
	//标识
    private String id;
    //标题
    private String title;

    //发布日期
    private String pubdate;
    
    //url
    private String url;
    
    //内容
    private String content;
    
    //附件
    private String attchfiles;    
    
    //来源
    private String src;  
    
    //发布者
    private String author;
    
    //域名
    private String siteDomain;
    
    //备注
    private String note;
    
    //采集时间
    private Date crawlerdate;
    
    // 针对外文网站的 内容文本翻译
    private String contentTr;
    
    // 格式化后的日期
    private String formattedContent;
    
    // 新闻摘要
    private String summary;
    
    // 查询文本
    private String searchText;

    // 新闻的封面
    private String headimg;
    
    // 对应的 pdf 的路径
    private String pdffiles;
    
    // chatnumber
    private String chatnumber;
    
    // filename
    private String filename;
    
    // 新闻分类
    private String newsCategory;
    
    // 数据类型
    private String dbType;

    // 资源站点
    private String sourceSite;

    // 标题翻译
	private String titleTr;

	// 关键词
	private String keywords;

	private String chinContent;

	private String engContent;

	// 采集失败的问题
	private String crawlerProblem;

	public String getCrawlerProblem() {
		return crawlerProblem;
	}

	public void setCrawlerProblem(String crawlerProblem) {
		this.crawlerProblem = crawlerProblem;
	}

	// 属性信息
	private String info;

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public String getChinContent() {
		return chinContent;
	}

	public void setChinContent(String chinContent) {
		this.chinContent = chinContent;
	}

	public String getEngContent() {
		return engContent;
	}

	public void setEngContent(String engContent) {
		this.engContent = engContent;
	}

	public String getKeywords() {
		return keywords;
	}

	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	public String getSourceSite() {
		return sourceSite;
	}

	public void setSourceSite(String sourceSite) {
		this.sourceSite = sourceSite;
	}

	public String getTitleTr() {
		return titleTr;
	}

	public void setTitleTr(String titleTr) {
		this.titleTr = titleTr;
	}

	public String getDbType() {
		return dbType;
	}

	public void setDbType(String dbType) {
		this.dbType = dbType;
	}

	public String getNewsCategory() {
		return newsCategory;
	}

	public void setNewsCategory(String newsCategory) {
		this.newsCategory = newsCategory;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getChatnumber() {
		return chatnumber;
	}

	public void setChatnumber(String chatnumber) {
		this.chatnumber = chatnumber;
	}

	public String getPdffiles() {
		return pdffiles;
	}

	public void setPdffiles(String pdffiles) {
		this.pdffiles = pdffiles;
	}

	public String getHeadimg() {
		return headimg;
	}

	public void setHeadimg(String headimg) {
		this.headimg = headimg;
	}

	public String getSearchText() {
		return searchText;
	}

	public void setSearchText(String searchText) {
		this.searchText = searchText;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getFormattedContent() {
		return formattedContent;
	}

	public void setFormattedContent(String formattedContent) {
		this.formattedContent = formattedContent;
	}

	public String getContentTr() {
		return contentTr;
	}

	public void setContentTr(String contentTr) {
		this.contentTr = contentTr;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}



	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getAttchfiles() {
		return attchfiles;
	}

	public void setAttchfiles(String attchfiles) {
		this.attchfiles = attchfiles;
	}

	public String getSrc() {
		return src;
	}

	public void setSrc(String src) {
		this.src = src;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public String getPubdate() {
		return pubdate;
	}

	public void setPubdate(String pubdate) {
		this.pubdate = pubdate;
	}

	public String getSiteDomain() {
		return siteDomain;
	}

	public void setSiteDomain(String siteDomain) {
		this.siteDomain = siteDomain;
	}

	public Date getCrawlerdate() {
		return crawlerdate;
	}

	public void setCrawlerdate(Date crawlerdate) {
		this.crawlerdate = crawlerdate;
	}

	
}
