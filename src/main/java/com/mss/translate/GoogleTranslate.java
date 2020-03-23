package com.mss.translate;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.mss.utils.ExtractUtils;
import com.mss.utils.FileUtils;
import com.mss.utils.HttpClientUtils;
import com.mss.utils.ScriptUtils;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;

/**
  
* @ClassName: GoogleTranslate  
* @Description: TODO(谷歌翻译类)
* @author wwq
* @date 2017-6-9 下午2:48:21
*/
public class GoogleTranslate {
	
	private GoogleDownloader downloader = new GoogleDownloader();
	private Logger logger = LoggerFactory.getLogger(GoogleTranslate.class);
	private static Site site = Site.me();
	private static Map<String,String> headerMap = new HashMap<>();
	
	static {
		site.addHeader("accept", "*/*");
		site.addHeader("accept-encoding", "gzip, deflate, br");
		site.addHeader("accept-language", "zh-CN,zh;q=0.8");
		site.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36");
		site.addHeader("x-client-data", "CKi1yQEIi7bJAQiktskBCMS2yQEI/JnKAQj7nMoBCKmdygE=");
		site.addHeader("content-type","application/x-www-form-urlencoded");
	}
	
	public GoogleTranslate() {
		
	}
	
	public GoogleTranslate(int thread) {
		downloader.setThread(thread);
	}
	
	/**
	 * 读取文件夹中的文件(文本文档 txt )进行翻译
	 * @param sourcePath
	 * @param savePath
	 * @return
	 */
	public void readDir(String sourcePath, String savePath) {
		
		File file = FileUtils.getFile(sourcePath);
		
		File[] files = file.listFiles();
		
		for(File f : files) {
			
			JSONObject json = JSONObject.parseObject(FileUtils.fileToString(f));
			
			String result = translate(json.getString("content"));
			
			FileUtils.print(result, savePath + f.getName().split("\\.")[0]+".html", false);
		}
	}

	/**
	 * 双语对照翻译
	 * @param source
	 * @param sourceLang
	 * @param targetLang
	 * @return
	 */
	public String comparisonTranslate(String source, String sourceLang, String targetLang) {
		return null;
	}
	
	/**
	 * 翻译
	 * @param content
	 * @param sourceLang
	 * @param targetLang
	 * @return
	 */
	public String translate(String content, String sourceLang, String targetLang) {
		
		StringBuilder url = new StringBuilder("https://translate.googleapis.com/translate_a/t?anno=3&client=te_lib&format=html&v=1.0&key=AIzaSyBOti4mM-6x9WDnZIjIeyEU21OpBXqWBgw&logld=vTE_20170501_01&sl=").
				append(sourceLang).append("&tl=").append(targetLang).append("&sp=nmt&tc=1&sr=1&tk=");
		
		String tk = getTk(content, getTKK());
		
		url.append(tk);
		
		url.append("&mode=1");
		
		Request req = HttpClientUtils.getPostRequest(url.toString(), new String[]{"q"}, new String[]{content});
	
		Page p;
		try {
			p = downloader.download(req, site.toTask());
			
			String result = ExtractUtils.convertUnicode(p.getRawText());
			
			return result;
		} catch (Exception e) {
			logger.error("",e);
		}
		return null;
	}
	
	/**
	 * 翻译方法
	 * @param content
	 * @return
	 */
	/**
	 * 翻译方法
	 * @param content
	 * @return
	 */
	public String translate(String content) {
		StringBuilder url = new StringBuilder("https://translate.googleapis.com/translate_a/t?anno=3&client=te_lib&format=html&v=1.0&key=AIzaSyBOti4mM-6x9WDnZIjIeyEU21OpBXqWBgw&logld=vTE_20170501_01&sl=en&tl=zh-CN&sp=nmt&tc=1&sr=1&tk=");
			
		String tk = getTk(content, getTKK());

		url.append(tk);
		
		url.append("&mode=1");
		
		Request req = HttpClientUtils.getPostRequest(url.toString(), new String[]{"q"}, new String[]{content});
		for(Map.Entry<String,String> node:headerMap.entrySet()){
			req.addHeader(node.getKey(), node.getValue());
		}

		Page p;
		try {
			p = downloader.download(req, site.toTask());
			if(null == p) {
				return "";
			}
			String result = ExtractUtils.convertUnicode(p.getRawText());
			
//			JSONArray jsonArr = JSONArray.parseArray(result);
//			result = jsonArr.getString(0);
			
			result = result.substring(result.indexOf("\"")+1, result.length()-1);
			
			Document doc = Jsoup.parse(result);
			
			Elements eles = doc.getElementsByTag("b");
			
			for(Element ele : eles){
				Element preEle = ele.previousElementSibling();
				if(preEle == null) {
					ele.remove();
					continue;
				}
				
				if("i".equals(preEle.tagName())) {
					preEle.html(ele.html());
					ele.remove();
				}
				//System.out.println(ele);
			}
			
			//result = result.replaceAll("<b>", "<br><b>");
			
			//result = "<!doctype html><html lang=\"en\"> <head><meta charset=\"UTF-8\"><title>Document</title></head><body>"+result+"</body></html>";
			
			return ExtractUtils.getSmartContent(doc.html());
		} catch (Exception e) {
			logger.error("",e);
		}
		return null;
	}
	
	/**
	 * 获取tk
	 * @param content
	 * @param tkk
	 * @return
	 */
	private String getTk(String content, String tkk) {
		return ScriptUtils.execScript("tk", content, tkk)+"";
	}
	
	/**
	 * 获取tkk
	 * @return
	 */
	private String getTKK() {
		String script1 = "eval('((function(){var a\\x3d1605932948;var b\\x3d2450142413;return 415780+\\x27.\\x27+(a+b)})())');";
		return ScriptUtils.executeScript(script1)+"";
	}
	
	public static void main(String[] args) {
		GoogleTranslate translate = new GoogleTranslate();
		/*String str = FileUtils.fileToString("E:/app/trans.txt");
		str = str.replaceAll("\\\\", "").replaceAll("class=\".*?\"", "").replaceAll("<a[^>]*>", "").replaceAll("</a>", "");
		String s = "";

		if(str.length() > 35000) {
			int a = (int)Math.round(Double.valueOf(str.length()) / 35000.0);
			for (int i = 0; i < a; i++) {
				String st = "";
				if((i+1) * 35000 > str.length()) {
					st = str.substring(i * 35000, str.length());
				} else {
					st = str.substring(i * 35000, (i+1) * 35000);
				}
				s += translate.translate(st);
			}
		} else {
			s = translate.translate(str);
		}
		//String s = translate.translate(Jsoup.parse(str).body().html());
		FileUtils.print(s, "E:/app/trans1.txt", false);
		System.out.println(s);*/
		//System.out.println(translate.translate("I think the meeting sends a positive signal. There has been some friction between the two countries lately. The meeting under the current circumstances shows that Sino-US relations have matured to be able to endure frictions,\" Jin Canrong, associate dean of the Department of International Studies at the Renmin University of China, told the Global Times on Sunday"));
		//translate.readDir("E:\\mss\\crawler\\www.space.com\\data", "e:/mss/crawler/logs/");
		System.out.println(translate.translate("<div class=\"t m0 x1 h1 y1 ff1 fs0 fc0 sc0 ls0 ws0\">Distribution Statement A: Approved for public release. Distribution <span class=\"ls1 ws1\">is <span class=\"ls0\">unlimited. DOPSR Case #18-S-2365</div><div class=\"t m0 x2 h2 y2 ff2 fs1 fc1 sc0 ls2 ws1\">Ms. Philomena Zimmerman</div><div class=\"t m0 x2 h3 y3 ff2 fs2 fc1 sc0 ls0 ws1\">Office of the Under Secretary of Defense for </div><div class=\"t m0 x2 h3 y4 ff2 fs2 fc1 sc0 ls3 ws0\">Research and Engineering</div><div class=\"t m0 x2 h4 y5 ff2 fs3 fc1 sc0 ls0 ws1\">National Institute of Standards and Technology </div><div class=\"t m0 x2 h4 y6 ff2 fs3 fc1 sc0 ls0 ws2\">Model-Based Enterprise Summit 2019 | April 3, 2019</div><div class=\"t m0 x2 h5 y7 ff3 fs4 fc2 sc0 ls0 ws3\">Digital Engineering Strategy </div><div class=\"t m0 x2 h5 y8 ff3 fs4 fc2 sc0 ls2 ws1\">and Implementation</div></div><div class=\"pi\" data-data='{\"ctm\":[1.400000,0.000000,0.000000,1.400000,0.000000,0.000000]}'></div></div></div>"));
	}
}
