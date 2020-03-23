package com.mss.crawler.spiderjson;

public final class Constants {
	
	//通用网站处理模型
	public static final String SITE_PAGE_PROCESSORMODEL="site";
	
	//主题网站处理模型
	public static final String SUBJECT_PAGE_PROCESSORMODEL="subject";
	
	//微信搜索处理模型
	public static final String WEIXIN_PAGE_PROCESSORMODEL="weixin";
	
	//批量Url采集处理模型
	public static final String BATCHURLS_PAGE_PROCESSORMODEL="batchUrls";
	
	//列表详情采集模型
	public static final String LISTANDDETAIL_PAGE_PROCESSORMODEL="listAndDetail";

	//微博搜索采集
	public static final String WEIBO_PAGE_PROCESSORMODEL="weibo";

	//列表详情采集模型
	public static final String IPTEST_PAGE_PROCESSORMODEL="ipTest";
	
	//json文件管道
	public static final String PIPELINE_JSONPIPELINE="JSONPipeline";
	
	//资源文件文件管道
	public static final String PIPELINE_IMGPIPELINE="ImgPipeline";
	
	//数据库管道
	public static final String PIPELINE_NEWSDBPIPELINE="NewsDBPipeline";
	
	//数据库+外文翻译处理管道
	public static final String PIPELINE_NEWSTRANSLATE2DBPIPELINE="NewsTranslate2DBPipeline";
	
	//html原文文件管道
	public static final String PIPELINE_Html2PdfPipeline="Html2PdfPipeline";
	
	
	
	/**
	 * 请求类型
	 */
	public static final String REQUEST_TYPE = "requestType";
	
	/**
	 * 首页
	 */
	public static final String REQUEST_INDEX = "index";
	
	/**
	 * 详情
	 */
	public static final String REQUEST_DETAIL = "detail";
	
	/**
	 * 搜索请求
	 */
	public static final String REQUEST_SEARCH = "search";
	
	/**
	 * 搜索请求
	 */
	public static final String REQUEST_DETAIL_LIST = "detailList";
	
	/**
	 * 分页PAGE请求
	 */
	public static final String REQUEST_LIST_PAGING = "listPaging";

}
