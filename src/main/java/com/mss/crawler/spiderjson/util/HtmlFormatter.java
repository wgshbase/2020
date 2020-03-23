package com.mss.crawler.spiderjson.util;

import java.io.File;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.util.StringUtils;

import com.mss.crawler.model.DomModel;

import us.codecraft.xsoup.Xsoup;

public class HtmlFormatter {
	
	//定义script的正则表达式
	public static String regex_script="<script[^>]*?>[\\s\\S]*?<\\/script>";

	//定义style的正则表达式
	public static String regEx_style="<style[^>]*?>[\\s\\S]*?<\\/style>";

	//定义HTML标签的正则表达式
	public static String regEx_html="<[^>]+>";
	
	//定义（1）（2）（3）的正则表达式
	public static String regEx_num ="（[0-9]+）";
	
	//定义元素内嵌class的正则表达式
	public static String regEx_el_class="class\\s*=\\s*[',\"]{1}\\w*[',\"]{1}\\s*";
	
	//定义元素内嵌style的正则表达式
	//public static String regEx_el_style="style\\s*=\\s*[',\"]{1}[^'\"]+[',\"]{1}\\s*";
	public static String regEx_el_style="style=\".*?\"";

	//定义空格回车换行符
	public static String regEx_space = "\\s*|\t|\r|\n";//定义空格回车换行符
	
	public static String regEx_date_style0 = "(\\d{4})-(\\d{1,2})-(\\d{1,2}) (\\d{2}):(\\d{2})";
	
	public static String regEx_date_style1 = "(\\d{4})-(\\d{1,2})-(\\d{1,2})";
	
	public static String regEx_date_style2 = "(\\d{4})年(\\d{1,2})月(\\d{1,2}日)";
	
	public static String regEx_date_style3 = "[A-Za-z]+ (\\d{1,2}), (\\d{4})";
	
	public static String regEx_date_style4 = "(\\d{1,2}) [A-Za-z]+ (\\d{4})";
	
	public static String regEx_date_style5 = "(\\d{1,2})\\.(\\d{1,2})\\.(\\d{4})";
	
	public static String regEx_date_style6 = "(\\d{4})/(\\d{1,2})/(\\d{1,2})";
	
	public static String regEx_date_style7 = "(\\d{1,2})/(\\d{1,2})/(\\d{4})";
	
	public static String regEx_date_style8 = "(\\d{4})年(\\d{1,2})月(\\d{1,2})日 (\\d{2}):(\\d{2})";

	public static String regEx_date_style9 = "[A-Za-z]+\\. (\\d{1,2}), (\\d{4})";

	public static String regEx_date_style10 = "([A-Za-z]+) (\\d{1,2})[A-Za-z]+ (\\d{4})";

	public static String regEx_date_style11 = "([A-Za-z]+) (\\d{1,2})[A-Za-z]+, (\\d{4})";

	public static String regEx_date_style12 = "([A-Za-z]+) (\\d{1,2}), (\\d{4})";

	public static String regEx_date_style13 = "(\\d{2})-(\\d{1,2})-(\\d{1,2})";

	/**
	 * 此工具方法是为了格式化 pubDate 字段
	 * 依据不同的情况将爬取到的  pubDate 字段进行格式化, 便于数据库的操作
	 * 
	 * @param
	 * @return
	 * @throws ParseException 
	 * 
	 * @author wgshb
	 */
	public static String convertPubDate(String date) throws ParseException  {
		if(date == null || date.trim() == "") {
			return "";
		}
		date = date.trim();
		/*if(date.contains("ago")) {
			return getDateByAgo(date);
		}*/

		// 去除日期时间中间的所有的空格????
		/*if(date.contains(" ")) {
			date = date.replace(" ", "");
		}*/

		Pattern p0 = Pattern.compile(regEx_date_style0);
		Matcher m0 = p0.matcher(date);
		Pattern p1 = Pattern.compile(regEx_date_style1);
		Matcher m1 = p1.matcher(date);
		Pattern p2 = Pattern.compile(regEx_date_style2);
		Matcher m2 = p2.matcher(date);
		Pattern p3 = Pattern.compile(regEx_date_style3);
		Matcher m3 = p3.matcher(date);
		Pattern p4 = Pattern.compile(regEx_date_style4);
		Matcher m4 = p4.matcher(date);
		Pattern p5 = Pattern.compile(regEx_date_style5);
		Matcher m5 = p5.matcher(date);
		Pattern p6 = Pattern.compile(regEx_date_style6);
		Matcher m6 = p6.matcher(date);
		Pattern p7 = Pattern.compile(regEx_date_style7);
		Matcher m7 = p7.matcher(date);
		Pattern p8 = Pattern.compile(regEx_date_style8);
		Matcher m8 = p8.matcher(date);
		Pattern p9 = Pattern.compile(regEx_date_style9);
		Matcher m9 = p9.matcher(date);
		Pattern p10 = Pattern.compile(regEx_date_style10);
		Matcher m10 = p10.matcher(date);
		Pattern p11 = Pattern.compile(regEx_date_style11);
		Matcher m11 = p11.matcher(date);
		Pattern p12 = Pattern.compile(regEx_date_style12);
		Matcher m12 = p12.matcher(date);
		Pattern p13 = Pattern.compile(regEx_date_style13);
		Matcher m13 = p13.matcher(date);
		String str = "";
		
		// 外文网站的日期
	    // 按照年份进行截取字符串
		if(m3.find()) {
			SimpleDateFormat sdf = new SimpleDateFormat("MMMMM dd, yyyy", Locale.US);
			String t = m3.group().trim();
			String m = t.substring(0, t.indexOf(" "));
			if(m.length() > 3) {
				t = t.replace(m, m.substring(0, 3));
			}
			Date date2Format = sdf.parse(t);
			// 重新格式化日期并进行设置
			str = new SimpleDateFormat("yyyy-MM-dd").format(date2Format);
		}else if(m13.find()) {
			String year = m13.group(1);
			String month = m13.group(2);
			String day = m13.group(3);
			month = month.length()==2?month:("0" + month);
			day = day.length()==2?day:("0" + day);
			str = "20" + year + "-" + month + "-" + day;
		}else if(m9.find()) {
			SimpleDateFormat sdf = new SimpleDateFormat("MMMMM. dd, yyyy", Locale.US);
			String t = m9.group().trim();
			String m = t.substring(0, t.indexOf(" "));
			if(m.length() > 4) {
				String others = t.substring(m.length(), t.length());
				t = m.substring(0, 3) + "." + others;
			}
			Date date2Format = sdf.parse(t);
			// 重新格式化日期并进行设置
			str = new SimpleDateFormat("yyyy-MM-dd").format(date2Format);
		} else if(m10.find()) {
			String month = m10.group(1);
			String day = m10.group(2);
			String year = m10.group(3);
			SimpleDateFormat sdf = new SimpleDateFormat("MMMMM dd yyyy", Locale.US);
			String t = month + " " + day + " " + year;
			String m = t.substring(0, t.indexOf(" "));
			if(m.length() > 3) {
				t = t.replace(m, m.substring(0, 3));
			}
			Date date2Format = sdf.parse(t);
			// 重新格式化日期并进行设置
			str = new SimpleDateFormat("yyyy-MM-dd").format(date2Format);
		} else if(m11.find()) {
			String month = m11.group(1);
			String day = m11.group(2);
			String year = m11.group(3);
			SimpleDateFormat sdf = new SimpleDateFormat("MMMMM dd yyyy", Locale.US);
			String t = month + " " + day + " " + year;
			String m = t.substring(0, t.indexOf(" "));
			if(m.length() > 3) {
				t = t.replace(m, m.substring(0, 3));
			}
			Date date2Format = sdf.parse(t);
			// 重新格式化日期并进行设置
			str = new SimpleDateFormat("yyyy-MM-dd").format(date2Format);
		} else if(m12.find()) {
			String month = m12.group(1);
			String day = m12.group(2);
			String year = m12.group(3);
			SimpleDateFormat sdf = new SimpleDateFormat("MMMMM dd yyyy", Locale.US);
			String t = month + " " + day + " " + year;
			String m = t.substring(0, t.indexOf(" "));
			if (m.length() > 3) {
				t = t.replace(m, m.substring(0, 3));
			}
			Date date2Format = sdf.parse(t);
			// 重新格式化日期并进行设置
			str = new SimpleDateFormat("yyyy-MM-dd").format(date2Format);
		}	else if(m4.find()) {
			SimpleDateFormat sdf = new SimpleDateFormat("dd MMMMM yyyy", Locale.US);
			Date date2Format = sdf.parse(m4.group().trim());
			// 重新格式化日期并进行设置
			str = new SimpleDateFormat("yyyy-MM-dd").format(date2Format);
		} else if(m0.find()) {
			String year = m0.group(1);
			String month = m0.group(2);
			String day = m0.group(3);
			month = month.length()==2?month:("0" + month);
			day = day.length()==2?day:("0" + day);
			str = year + "-" + month + "-" + day;
		} else if(m1.find()) {
			String year = m1.group(1);
			String month = m1.group(2);
			String day = m1.group(3);
			month = month.length()==2?month:("0" + month);
			day = day.length()==2?day:("0" + day);
			str = year + "-" + month + "-" + day;
		} else if(m8.find()) {
			String year = m8.group(1);
			String month = m8.group(2);
			String day = m8.group(3);
			str = year + "-" + month + "-" + day;
		} else if(m2.find()) {
			// 包含 yyyy年MM月dd日 的日期格式
			str = new SimpleDateFormat("yyyy-MM-dd").format(new SimpleDateFormat("yyyy年MM月dd日").parse(m2.group()));
		} else if(m5.find()) {
			String dd = m5.group(1);
			String mm = m5.group(2);
			String yyyy = m5.group(3);
			str = yyyy + "-" + mm + "-" + dd;
		} else if(m6.find()) {
			String yyyy = m6.group(1);
			String month = m6.group(2);
			String day = m6.group(3);
			month = month.length()==2?month:("0" + month);
			day = day.length()==2?day:("0" + day);
			str = yyyy + "-" + month + "-" + day;
		} else if(m7.find()) {
			String month = m7.group(1);
			String day = m7.group(2);
			String year = m7.group(3);
			month = month.length()==2?month:("0" + month);
			day = day.length()==2?day:("0" + day);
			str = year + "-" + month + "-" + day;
		}
		if(str.length() > 1) {
			// 若 str 不为空, 直接返回 str
			return str;
		}
	    // throw new ParseException("Bad format date, Please check!!!", 6);
		
		// 默认返回前一天的格式化之后的日期
		return new SimpleDateFormat("yyyy-MM-dd").format(new Date(System.currentTimeMillis() - 60 * 60 * 24 * 1000));
		
	}
	
	private static String getDateByAgo(String date) {
		// String resultDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		int dateCount = 0;
		date = date.trim();
		String[] dates = date.split(" ");
		if(dates[1].equals("days") || dates[1].equals("day")) {
			dateCount = Integer.parseInt(dates[0]) + 1;
		} else if(dates[1].equals("hours")) {
			dateCount = 1;
		}
		String result = new SimpleDateFormat("yyyy-MM-dd").format(new Date(System.currentTimeMillis() - 60 * 60 * 24 * 1000 * dateCount));
		return result;
	}

	/**
	 * 判断url表达式是否合法
	 * @param url
	 * @return
	 */
	public static boolean urlValid(String url){
		 
		if(url.toLowerCase().indexOf("http://")!=-1||url.toLowerCase().indexOf("https://")!=-1||url.toLowerCase().indexOf("ftp://")!=-1||url.toLowerCase().indexOf("//")!=-1){
			return true;
		}
		return false;
		
	}

	/**
	 * 按照表达式规则去重内容
	 * @param text
	 * @param exp
	 * @return
	 */
	public static String html2text(String text,String exp) {
		String formatter = text;
		formatter = formatter.replaceAll(exp,"");
		return formatter;
	}

	/**
	 * 按照表达式规则去重内容
	 * @param text
	 * @param
	 * @return
	 */
	public static String html2text(String text) {
		String formatter = text;
		formatter = formatter.replaceAll(regEx_html,"");
		return formatter;
	}

	/**
	 * 去除采集的内容的 css 样式...
	 * @param text
	 * @return
	 */
	public static String html2textWithOutCssandStyle(String text) {
		if(StringUtils.isEmpty(text)){
			return "";
		}
		String formatter = text; // style="text-align: center; "
		//去掉script的正则表达式
		formatter = formatter.replaceAll(regex_script,"");
		// 对于特别的个性化的需求进行特殊的处理
		/*if(formatter.contains("text-align: center;")) {
			formatter = formatter.replaceAll("style=\"[^>]*text-align: center;[^>]*\"", "#LeaveStyle1#");
			//System.out.println(formatter + "---------------------------------------------");
		}
		if(formatter.matches("(.*?)style=\"(.*?)vertical-align: top;(.*?)\"(.*?)")) {
			formatter = formatter.replaceAll("style=\"[^>]*vertical-align: top;[^>]*\"", "#LeaveStyle2#");
		}*/

		System.out.println("-----------> html contains table tab " + formatter.contains("<table") + " : " + formatter.contains("</table>"));

		// 部分特殊的原样式需要保留的话在这边先进行内容的替换
		Matcher tableMatcher = Pattern.compile("<table([\\s\\S]*)</table>").matcher(formatter);
		String tableHtml = "";
		if(tableMatcher.find()) {
			tableHtml = tableMatcher.group();
		}
		if(tableHtml.length() > 15) {
			formatter = formatter.replace(tableHtml, "#tableHtml#");
		}

		//去掉style标签
		formatter = formatter.replaceAll(regEx_el_style,"");
		//去掉class标签 
		formatter = formatter.replaceAll(regEx_el_class,"");
		
		formatter = formatter.replace("#LeaveStyle1#", "style=\"text-align: center;\"");
		formatter = formatter.replace("#LeaveStyle2#", "style=\"vertical-align: top;\"");
		if(tableHtml.length() > 15) {
			formatter = formatter.replace("#tableHtml#",tableHtml);
		}

		return formatter;
	}
	
	public static String htmlRemoveRegionByCssQuary(String text,List<String> cssSelectors) {
		if(cssSelectors==null){
			return text;
		}
		Document doc = Jsoup.parse(text);
		for(String cssSelector:cssSelectors){
			doc.select(cssSelector).remove();
		}
		return doc.body().html();
	}
	
	public static String htmlRemoveRegionByRegex(String text,List<String> regexs) {
		if(regexs==null){
			return text;
		}
		String formatter = text;
		if(formatter == null) {
			return "";
		}
		//int originalLength = formatter.length();
		for(String regex:regexs){
			formatter = formatter.replaceAll(regex, "");
		}
		//int trimedLength = formatter.length();
		//System.out.println("Nothing has been trimed!!! -------------> " + originalLength + " ::: " + trimedLength);
		return formatter;
	}
	
	public static String htmlRemoveRegionByXpath(String text,List<String> xpaths) {
		if(xpaths==null){
			return text;
		}
		if(text != null) {
			Document doc = Jsoup.parse(text);
			for(String xpath:xpaths){
				Xsoup.select(doc, xpath).getElements().remove();
			}
			return doc.body().html();
		}
		throw new RuntimeException("The news you wanna download has no content...");
	}
	
	/**
	 * 移除dom元素
	 * @param
	 * @param removeDom
	 */
	public static String removeDom(String text, DomModel removeDom) {
		if(StringUtils.isEmpty(text)){
			return "";
		}
		if(removeDom==null){
			return text;
		}
		
		Document doc = Jsoup.parse(text);
			//根据ID移除dom元素
			if(removeDom.getId() != null) {
				String[] idArr = removeDom.getId().split(",");
				for(int i=0; i<idArr.length; i++) {
					if(doc.getElementById(idArr[i]) != null) {
						doc.getElementById(idArr[i]).remove();
					}	
				}
			}
			//根据tagname移除dom元素
			if(removeDom.getTagName() != null) {
				String[] nameArr = removeDom.getTagName().split(",");
				for(int i=0; i<nameArr.length; i++) {
					if(doc.getElementsByTag(nameArr[i]) != null && 
							doc.getElementsByTag(nameArr[i]).size() > 0) {
						doc.getElementsByTag(nameArr[i]).remove();
					}
				}
			}
			//根据class移除dom元素
			if(removeDom.getAttrClass() != null) {
				String[] classArr = removeDom.getAttrClass().split(",");
				for(int i=0; i<classArr.length; i++) {
					if(doc.getElementsByClass(classArr[i]) != null && 
							doc.getElementsByClass(classArr[i]).size() > 0) {
						doc.getElementsByClass(classArr[i]).remove();
					}
				}
			}
			return doc.body().html();
	}
	
	 public static String getDomain(String urlStr){
		java.net.URL url;
		try {
			url = new  java.net.URL(urlStr);
			return url.getHost();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return "";
		}

    }
	/*private static List<String> extractUrl(List<String> resUrls,List<String> patterns,boolean isMatch){
    	List<String> resultUrls ;
    	//按照匹配规则过滤
    	if(isMatch){
    		resultUrls = new ArrayList<String>();
    		for(String resUrl:resUrls){
    			 for (String urlPatternStr : patterns) {
    				    Pattern urlPattern = Pattern.compile(urlPatternStr);
    	                Matcher matcher = urlPattern.matcher(resUrl);
    	                if (matcher.matches()) {
    	                	resultUrls.add(resUrl);
    	                	continue;
    	                }
    	            }

    		}
    	//按照排除规则过滤
    	}else{
    		resultUrls = new ArrayList<String>();
    		resultUrls.addAll(resUrls);

    		for(String resUrl:resUrls){
	   			 for (String urlPatternStr : patterns) {
	   				    Pattern urlPattern = Pattern.compile(urlPatternStr);
	   	                Matcher matcher = urlPattern.matcher(resUrl);
	   	                if (matcher.matches()) {
	   	                	resultUrls.remove(resUrl);
	   	                	continue;
			   	        }
			   	 }

		   	}

    	}


    	return resultUrls;

    }
	*/

	/**
	 * 获取文件的真实路径, 主要处理图片路径中可能包含 .. 的问题
	 * 
	 * @param fileSrc 原始图片的路径, 当前界面的 url
	 * @param url
	 * @return
	 * @author wgshb
	 */
	public static String getAbsoluteFileSrc(String fileSrc, String url) {
		fileSrc = dealFileSrc(fileSrc);
		if(fileSrc.startsWith(".") && !fileSrc.startsWith("..")) {
			return url.substring(0, url.lastIndexOf("/")) + fileSrc.replaceFirst("\\.", "");
		}
		if(fileSrc.startsWith("http://") || fileSrc.startsWith("https://")) {
			return fileSrc;
		}
		String domain = HtmlFormatter.getDomain(url);
		String prefix = "https://"+domain;
		String midfix = "";
		url = url.subSequence(url.indexOf(prefix) + prefix.length(), url.length()).toString();
		if(!HtmlFormatter.urlValid(fileSrc)){
			File file = new File(url);
			File parentFile = file.getParentFile();
			// 路径是否包含 ..
			if(!fileSrc.contains("..")) {
				if(fileSrc.startsWith("/")) {
					return prefix + fileSrc;
				}
				if(url.endsWith("/")) {
					return prefix + url + fileSrc;
				} else {
					return prefix + parentFile.toString().replace("\\", "/") + "/" + fileSrc;
				}

			}
			String[] temp = fileSrc.split("/");
			int count = 0;
			for (int i = 0; i < temp.length; i++) {
				if (temp[i].equals("..")) {
					count++;
				}
			}
			while(count > 0) {
				parentFile = parentFile.getParentFile();
				count--;
			}
			fileSrc = fileSrc.replace("../", "");
			if(parentFile.toString().contains("\\")) {
				String t = parentFile.toString();
				if(t.startsWith("\\")) {
					t = t.substring(1, t.length());
				}
				midfix = t.replace("\\", "/");
			}
			//StringUtils.replace(text, searchString, replacement)
			
			return prefix + "/" + midfix + "/" + fileSrc;
		}
		return fileSrc;
	}

	public static String dealFileSrc(String fileSrc) {
		if(fileSrc.contains(" ")) {
			fileSrc = fileSrc.replaceAll(" ", "%20");
		}
//		if(fileSrc.contains("\\\\\\\"")) {
//			fileSrc = fileSrc.replace("\\\\\\\"", "");
//		}
//
//		if(fileSrc.contains("\\\\")) {
//			fileSrc = fileSrc.replace("\\\\", "");
//		}

		return fileSrc;
	}

	public static String removeNum(String formatter) {
		if(!StringUtils.isEmpty(formatter)) {
			//formatter = formatter.replaceAll(" ", "%20");
			formatter = formatter.replaceAll(regEx_num,"");
		}
		return formatter;
	}

	/**
	 * 格式化新闻的正文部分
	 */
	/**
     * 将日期进行格式化
     * @param content
     * @return
     */
    public static String formatContent(String content) {
    	StringBuilder sb = new StringBuilder();
    	if(content.contains("</h4>")) {
    		content = content.replaceAll("<h4(\\s)*>","<p>").replaceAll("</h4>","</p>");
	    }
		// 判断正文部分若包含 img 标签, img 标签是否被 p 标签包围
		String regex = "<p(.*?)<img(.*?)</p";
		String imgRegex = "<img(.*?)>";
		Matcher m = Pattern.compile(regex).matcher(content);
		Matcher imgMatcher = Pattern.compile(imgRegex).matcher(content);
		boolean flag = content.contains("<img") && !m.matches();
		if(flag) {
			while(imgMatcher.find()) {
				String imgStr = imgMatcher.group();
				imgStr = imgStr.replaceAll("\\.", "&&&").replaceAll("\\$\\{", "&&").replaceAll("\\}", "@@@");
				content = content.replaceAll("\\.", "&&&").replaceAll("\\$\\{", "&&").replaceAll("\\}", "@@@");
				String replaceMent = "<p>" + imgStr + "</p>";
				content = content.replace(imgStr, replaceMent).replace("&&&", ".").replace("&&", "${").replace("@@@", "}");
			}
		}
		
		// 正文部分由 br 进行换行
		if(!content.contains("<p") && content.contains("<br>")) {
			content = content.replaceAll("<div(.*?)>", "").replaceAll("</div>", "");
			String[] contents = content.split("<br>");
			for(String str : contents) {
				if(!StringUtils.isEmpty(str) && str.length() > 5) {
					content = content.replace(str, "<p>" + str + "</p>");
				}
			}
			/*
			regex = "([\\s\\S])*?(<br>)";
			Matcher brMatcher = Pattern.compile(regex).matcher(content);
			while(brMatcher.find()) {
				String brStr = brMatcher.group();
				content = content.replace(brMatcher.group(), "<p>" + brMatcher.group() + "</p>");
			}
			*/
		}
		
		Document d = Jsoup.parseBodyFragment(content);
		// 获取指定的标签的集合
		Elements elementsByTag = d.getElementsByTag("p");
		for(Element e : elementsByTag) {
			if(e.children().size() > 0 && e.children().toString().contains("<img")) {
//				sb.append("<p class=\"detail-pic\">" + e.children().toString() + "</p>");
				sb.append("<p>" + e.children().toString() + "</p>");
				//System.out.println("<p>" + e.children().toString() + "</p>");
			} else {
				if(!StringUtils.isEmpty(e.text())) {
					sb.append("<p>&nbsp;&nbsp;" + e.text() + "</p>");
				}
				//System.out.println("<p>" + e.text() + "</p>");
			}
		}
		return sb.toString();
	}

	public static String getPlainText(String contentTemp) {
    	List<String> regexList = new ArrayList<>();
    	regexList.add("#(.*?)#");
    	regexList.add("[.*?]");
    	for(String regex : regexList) {
    		Matcher m = Pattern.compile(regex).matcher(contentTemp);
    		while(m.find()) {
    			contentTemp = contentTemp.replace(m.group(),"").trim();
		    }
	    }
    	return contentTemp;
	}

	/**
	 * 获取微信的真实地址
	 */
	public static String getRealUrlSeed(String oldurl) {
		Matcher m = Pattern.compile("http://mp.weixin.qq.com/profile(.*?)==").matcher(oldurl);
		if(m.find()) {
			return m.group();
		}
		return oldurl;
	}

}