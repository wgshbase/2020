package com.mss.crawler.model;

public class FileModel {

	private Long fileSize;
	
	private String fileUrl;
	
	private String localFilePath;
	
	private String relativeFilePath;
	
	public Long getFileSize() {
		return fileSize;
	}

	public void setFileSize(Long fileSize) {
		this.fileSize = fileSize;
	}

	public String getFileUrl() {
		return fileUrl;
	}

	public void setFileUrl(String fileUrl) {
		this.fileUrl = fileUrl;
	}

	public String getLocalFilePath() {
		return localFilePath;
	}

	public void setLocalFilePath(String localFilePath) {
		this.localFilePath = localFilePath;
	}

	public String getRelativeFilePath() {
		return relativeFilePath;
	}

	public void setRelativeFilePath(String relativeFilePath) {
		this.relativeFilePath = relativeFilePath;
	}

	public FileModel(){
		
	}
	
	public FileModel(Long fileSize, String fileUrl, String localFilePath,
			String relativeFilePath) {
		super();
		this.fileSize = fileSize;
		this.fileUrl = fileUrl;
		this.localFilePath = localFilePath;
		this.relativeFilePath = relativeFilePath;
	}
}
