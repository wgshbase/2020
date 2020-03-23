package com.mss.crawler.common;

/**
 * IP 类
 * @Author wgsh
 * @Date wgshb on 2019/4/9 16:27
 */
public class IP {

	private String ip;
	private String port;

	private long expireTime;

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public long getExpireTime() {
		return expireTime;
	}

	public void setExpireTime(long expireTime) {
		this.expireTime = expireTime;
	}

	@Override
	public String toString() {
		return "IP{" +
				"ip='" + ip + '\'' +
				", port='" + port + '\'' +
				", expireTime='" + expireTime + '\'' +
				'}';
	}
}
