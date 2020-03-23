package com.mss.crawler.common;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.alibaba.fastjson.JSONObject;

import us.codecraft.webmagic.selector.Html;
import us.codecraft.webmagic.selector.Selectable;

public class HtmlUtils {

	
	
	
	/**
	 * 替换HTML中某个标签的属性值
	 * @param html
	 * @param tagName
	 * @param attrName
	 * @param replaceValue
	 * @return
	 */
	public static String replaceHtmlAttrValue(String html,String tagName, 
			String attrName, String replaceValue) {
		Document doc = new Html(html).getDocument();
		doc.getElementsByTag("img").get(0).attr("src", replaceValue);
		return new Html(doc).xpath("//body/*").toString();
	}
	
	public static void main(String[] args) {
		/*String str = FileUtils.fileToString(new File("D:\\data\\test.txt"));
		JSONObject json = JSONObject.parseObject(str);
		String newsContent = json.getString("newsContent");
		System.out.println(denoisHtml(newsContent));*/
	}
}
