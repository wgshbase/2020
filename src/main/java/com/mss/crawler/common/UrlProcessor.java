package com.mss.crawler.common;

import us.codecraft.webmagic.utils.UrlUtils;

public class UrlProcessor extends UrlUtils{

	/**
	 * 获取完整的URL链接
	 * @param href
	 * @param url
	 * @return
	 */
	public static String getFullHref(String href, String url) {
		
		
		if(!href.startsWith("http")) {
			
			if (href.startsWith("//"+getDomain(url))) {
				return getHost(url).substring(0, getHost(url).indexOf(":")+1) + href;
			}
			
			if(href.indexOf("/") == -1) {
				return url.substring(0, url.lastIndexOf("/")+1)+href;
			}
			
			if(!href.startsWith("/")) {
				href = getHost(url)+"/"+href;
			} else {
				href = getHost(url)+href;
			}
		}
		return href;
	}
	
	public static void main(String[] args) {
		System.out.println(getFullHref("//airbusdefenceandspace.com/wp-content/uploads/2017/03/press-release-cis-08032017-edrs.png", "https://airbusdefenceandspace.com/newsroom/news-and-features/spacedatahighway-to-reach-asia-pacific/"));
	}
}
