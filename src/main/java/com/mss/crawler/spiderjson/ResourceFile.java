package com.mss.crawler.spiderjson;

import java.io.File;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

public class ResourceFile {
	
	private String ROOTPATH_LABEL ="${RootPath}";
	//相对路径
	private String relativePath;
	//源文件名
	private String fileName;
	//新文件名
	private String newFileName;
	//文件下载url
	private String downUrl;
	//文件扩展名
	private String fileExtName;
	//域名
	private String domain;
	
	private String realPath;

	//文件扩展名
	private String[] extNames = {"jpg","jpeg","png","gif","pdf","doc","docx","xls","xlsx","ppt"};
	
	public ResourceFile(String relativePath,String downUrl,String domain){
		
		String[] urlStr = StringUtils.split(downUrl,"/");
		fileName = urlStr[urlStr.length-1];
		fileExtName = getFileExtension(fileName);
		// 文件名的声明方式
		newFileName = DigestUtils.md5Hex(downUrl)+"."+fileExtName;
		this.downUrl = downUrl;
		this.domain = domain;
		this.relativePath = relativePath;

	}
	
	private String getFileExtension(String fullName){
		 String fileName = new File(fullName).getName();
		 int dotIndex = fileName.lastIndexOf('.');
		 if(dotIndex==-1){
			 dotIndex = fileName.lastIndexOf('=');
		 }
		 if(dotIndex > -1){
			 String extName = fileName.substring(dotIndex + 1);	
			 for(String node:extNames){
				 if(node.equalsIgnoreCase(extName)){
					 return node;
				 }
			 }
			 return "jpg";
		 }else{
			 return "jpg";
		 }
		
	}
	
	public String getRelativePath() {
		return relativePath;
	}
	public void setRelativePath(String relativePath) {
		this.relativePath = relativePath;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getDownUrl() {
		return downUrl;
	}
	public void setDownUrl(String downUrl) {
		this.downUrl = downUrl;
	}
	public String getFileExtName() {
		return fileExtName;
	}
	public void setFileExtName(String fileExtName) {
		this.fileExtName = fileExtName;
	}

	/**
	 * 获取文件相对路径
	 * @return
	 */
	public String getRelativeUrl(){
		return ROOTPATH_LABEL+"/"+domain+"/"+relativePath+"/"+newFileName;
	}

	public String getNewFileName() {
		return newFileName;
	}

	public void setNewFileName(String newFileName) {
		this.newFileName = newFileName;
	}

	public String getRealPath() {
		return realPath;
	}

	public void setRealPath(String realPath) {
		this.realPath = realPath;
	}
	
	public String getROOTPATH_LABEL() {
		return ROOTPATH_LABEL;
	}

	public static void main(String[] args){
		ResourceFile rf = new ResourceFile("C://","https://mmbiz.qpic.cn/mmbiz_jpg/q6ZQ3YDgSRewZxAJuWnjG6A97830JLDx04wFImUCKtsy3VD40YmARa8ftF61Xpr77iafG9WwATEE8ibia19KNRPYQ/640?wx_fmt=jpeg","mmbiz.qpic.cn");
		//-319159660.jpeg
		System.out.println(rf.getNewFileName());
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}
}
