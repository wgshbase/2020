package com.mss.crawler.common;

/**
 * @Author wgsh
 * @Date wgshb on 2018/12/5 10:58
 */
public class XXNetProxy {
	private String proxy_ip = "127.0.0.1";
	private int proxy_port = 1080;

	public int getProxy_port() {
		return proxy_port;
	}

	public void setProxy_port(int proxy_port) {
		this.proxy_port = proxy_port;
	}

	public String getProxy_ip() {

		return proxy_ip;
	}

	public void setProxy_ip(String proxy_ip) {
		this.proxy_ip = proxy_ip;
	}
}
