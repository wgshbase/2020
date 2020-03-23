package com.mss.crawler.spiderjson.util;

public class BadFormatImage {
	private String id;
	private int width;
	private int height;
	public BadFormatImage() {
		super();
		// TODO Auto-generated constructor stub
	}
	public BadFormatImage(String id, int width, int height) {
		super();
		this.id = id;
		this.width = width;
		this.height = height;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public int getWidth() {
		return width;
	}
	public void setWidth(int width) {
		this.width = width;
	}
	public int getHeight() {
		return height;
	}
	public void setHeight(int height) {
		this.height = height;
	}
}