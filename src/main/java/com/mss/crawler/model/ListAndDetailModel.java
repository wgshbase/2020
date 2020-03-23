package com.mss.crawler.model;

/**
 * 列表及详情页采集配置模板对象
 * @author 伟其
 *
 */
public class ListAndDetailModel extends ListPageModel{
	
	/**
	 * 是否下载正文中的图片
	 */
	private boolean isDownloadimage;
	
	/**
	 * 分页模板
	 */
	private PagingModel detailPagingModel;

	public PagingModel getDetailPagingModel() {
		return detailPagingModel;
	}

	public void setDetailPagingModel(PagingModel detailPagingModel) {
		this.detailPagingModel = detailPagingModel;
	}

	public boolean isDownloadimage() {
		return isDownloadimage;
	}

	public void setDownloadimage(boolean isDownloadimage) {
		this.isDownloadimage = isDownloadimage;
	}
}
