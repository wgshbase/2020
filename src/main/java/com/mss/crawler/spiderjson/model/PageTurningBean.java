package com.mss.crawler.spiderjson.model;

public class PageTurningBean {
	
	//基于分页Url规则
	public static final int PAGINGTYPE_URL = 1;
	//基于分页按钮的xpath路径
	public static final int PAGINGTYPE_NEXTPAGE = 2;
	
	
	//分页类型
	private int pagingType = PAGINGTYPE_URL;
	//总页数
	private int pageNum;
	
	//分页url
	private String pagingUrl;
	
	//下一页url
	private String nextPageText;	
	//下一页xpath
	private String pageXpath;

	
	public int getPagingType() {
		return pagingType;
	}
	public void setPagingType(int pagingType) {
		this.pagingType = pagingType;
	}
	public int getPageNum() {
		return pageNum;
	}
	public void setPageNum(int pageNum) {
		this.pageNum = pageNum;
	}
	public String getPagingUrl() {
		return pagingUrl;
	}
	public void setPagingUrl(String pagingUrl) {
		this.pagingUrl = pagingUrl;
	}
	public String getNextPageText() {
		return nextPageText;
	}
	public void setNextPageText(String nextPageText) {
		this.nextPageText = nextPageText;
	}
	public String getPageXpath() {
		return pageXpath;
	}
	public void setPageXpath(String pageXpath) {
		this.pageXpath = pageXpath;
	}

}
