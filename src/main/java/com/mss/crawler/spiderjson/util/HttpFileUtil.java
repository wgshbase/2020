package com.mss.crawler.spiderjson.util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImagingOpException;
import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

import com.mss.crawler.common.XXNetProxy;
import com.mss.crawler.spiderjson.ResourceFile;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.google.common.io.Files;
import com.mss.crawler.spiderjson.util.gif.AnimatedGifEncoder;
import com.mss.crawler.spiderjson.util.gif.GifDecoder;
import com.mss.crawler.spiderjson.util.gif.Scalr;
import com.mss.crawler.spiderjson.util.gif.Scalr.Mode;

import us.codecraft.webmagic.downloader.HttpClientGenerator;
import us.codecraft.webmagic.selector.Html;

import javax.imageio.ImageIO;

public class HttpFileUtil {
	private final Map<String, CloseableHttpClient> httpClients = new HashMap<String, CloseableHttpClient>();
	private HttpClientGenerator httpClientGenerator = new HttpClientGenerator(HttpClientGenerator.SSLTypes.SSLv3);
	private static HttpFileUtil hfu = new HttpFileUtil();
	// 图片的限制尺寸(即允许的图片的最大的宽度)
	private static Integer limitWidth = 800;

	public static HttpFileUtil getInstance() {
		return hfu;
	}

	public HttpFileUtil() {

	}

	// 获取真实 url 地址
	public static String formatDownloadUrl(ResourceFile entry) {
		String fileUrl = entry.getDownUrl();
		if(fileUrl.contains("background:url(")) {
			String target = fileUrl.substring(fileUrl.indexOf("background:url(") + 15, fileUrl.indexOf(")")).replace("'","").replace("\"","");
			if(target.startsWith("/"))
			if(fileUrl.startsWith("http") && !fileUrl.startsWith("https")) {
				fileUrl = "http://" + entry.getDomain() + target;
 			} else if(fileUrl.startsWith("https") && !fileUrl.startsWith("http")){
				fileUrl = "https://" + entry.getDomain() + target;
			}
		}
		if (fileUrl.startsWith("//")) {
			fileUrl = "http:" + fileUrl;
		}

		if(fileUrl.contains("&amp;")) {
			fileUrl = fileUrl.replace("&amp;", "&");
		}

		return fileUrl;
	}

	// 获取实际 url 地址
	public static String getRealDownloadUrl(String url, String domain, String startURL) {
		if (url.toLowerCase().startsWith("http://") || url.toLowerCase().startsWith("https://")) {
			return url;
		}
		if (url.toLowerCase().startsWith("//")) {
			return "http:" + url;
		}
		if (url.startsWith("./")) {
			startURL = startURL.substring(0, startURL.lastIndexOf("/"));
			String result = startURL + url.replace("./", "/");
			return result;

		}
		if (!url.startsWith("/") && !url.startsWith("./")) {
			String prefix = "http://" + domain + "/";
			if (startURL.endsWith("/")) {
				startURL = startURL + "?1";
			}
			startURL = startURL.subSequence(startURL.indexOf(prefix) + prefix.length(), startURL.length()).toString();
			if (startURL == null || startURL.equals("")) {
				return prefix + url;
			}
			File file = new File(startURL);
			File parentUrl = file.getParentFile();
			if (parentUrl != null) {
				if (!url.contains("..")) {
					return prefix + parentUrl.toString().replace("\\", "/") + "/" + url;
				}
				String[] temp = url.split("/");
				int count = 0;
				for (int i = 0; i < temp.length; i++) {
					if (temp[i].equals("..")) {
						count++;
					}
				}
				while (count > 0) {
					parentUrl = parentUrl.getParentFile();
					count--;
				}
				url = url.replace("../", "");
				if (parentUrl == null) {
					return prefix + url;
				}
				return prefix + parentUrl + "/" + url;
			} else {
				return "http://" + domain + "/" + url;
			}

		}


		return "http://" + domain + url;
	}

	/**
	 * 获取实际的链接地址
	 * @param fileUrl  当前的链接地址
	 * @param originUrl  初始的链接地址
	 * @return
	 */
	public static String getDownloadUrl(String fileUrl, String originUrl) {
		if(fileUrl.contains("background:url(")) {
			String target = fileUrl.substring(fileUrl.indexOf("background:url(") + 15, fileUrl.indexOf(")")).replace("'","").replace("\"","");
			if(target.startsWith("/"))
				if(fileUrl.startsWith("http") && !fileUrl.startsWith("https")) {
					fileUrl = "http://" + fileUrl;
				} else if(fileUrl.startsWith("https") && !fileUrl.startsWith("http")){
					fileUrl = "https://" + fileUrl;
				}
		}
		if (fileUrl.startsWith("//")) {
			fileUrl = "http:" + fileUrl;
		} else if(fileUrl.startsWith("/")) {
			String domain = HtmlFormatter.getDomain(originUrl);
			fileUrl = "http://" + domain + fileUrl;
		} else if(!fileUrl.startsWith("/")) {
			fileUrl = originUrl.substring(0, originUrl.lastIndexOf("/") + 1) + fileUrl;
		}

		return fileUrl;
	}

	/**
	 *
	 * @param weiboLoginPage  访问的入口
	 * @param weiboUsername   用户名
	 * @param weiboPassword   密码
	 */
	public String getWeiboCookie(String weiboLoginPage, String weiboUsername, String weiboPassword) {
		System.setProperty("webdriver.chrome.marionette", "chromedriver.exe");
		// ChromeOptions chromeOptions = new ChromeOptions();
  		// chromeOptions.addArguments("--headless");
		WebDriver driver = null;
		StringBuilder sb = new StringBuilder();
		try {
			driver = new ChromeDriver();
			driver.manage().window().maximize();
			driver.get(weiboLoginPage);
			// load("xpath", "//input[@id='loginname']", driver);
			// load("xpath", "//input[@name='password']", driver);
			driver.findElement(By.name("username")).sendKeys(weiboUsername);
			// driver.findElement(By.xpath("//input[@name='password']")).sendKeys(weiboPassword);
			//driver.findElement(By.className("W_input")).sendKeys(weiboPassword);
			//driver.findElement(By.xpath("//a[@node-type='submitBtn']")).click();
			Set<org.openqa.selenium.Cookie> cookies = driver.manage().getCookies();

			for(org.openqa.selenium.Cookie cookie : cookies) {
				sb.append(cookie.toString() + ";");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		driver.quit();
		if(sb.toString().endsWith(";")) {
			sb = new StringBuilder(sb.toString().substring(0, sb.toString().length() - 1));
		}
		return sb.toString();
	}

	public static void load(String type, String by, WebDriver driver){
		WebDriverWait wait = new WebDriverWait(driver, 30);
		if ("xpath".equals(type)) {
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(by)));
		} else if("id".equals(type)) {
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(by)));
		} else if("name".equals(type)) {
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.name(by)));
		} else if("cssSelector".equals(type)) {
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(by)));
		} else if("linkText".equals(type)) {
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText(by)));
		} else {
			System.err.println("请输入正确的元素定位方式：xpath、id、name、cssSelector、linkText之一");
		}
	}

	/**
	 * 带cookie的请求
	 *
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	public String getRequestWithCookie(String reqUrl, List<Cookie> cookieL) throws IOException {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		CookieStore cookieStore = new BasicCookieStore();
		for (Cookie entry : cookieL) {
			cookieStore.addCookie(entry);
		}
		httpclient = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
		InputStream in = null;
		String responseStr = "";
		try {
			HttpGet httpget = new HttpGet(reqUrl);
			httpget.setHeader("User-Agent",
					"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:52.0) Gecko/20100101 Firefox/52.0");
			httpget.setHeader("Accept", "*/*");
			HttpResponse response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();
			BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()));

			String inputLine;
			StringBuffer result = new StringBuffer();
			while ((inputLine = reader.readLine()) != null) {
				result.append(inputLine);
			}
			responseStr = result.toString();
			reader.close();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} finally {

			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				httpclient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return responseStr;
	}

	/**
	 * 获取远程文件下载到本地
	 *
	 * @param fileUrl
	 * @param filePath
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	public List<Cookie> getFileAndCookieTo(String fileUrl, String filePath, String[] cookie_Names) throws IOException {

		System.out.println("fileUrl=" + fileUrl);
		CloseableHttpClient httpclient = HttpClients.createDefault();
		CookieStore cookieStore = new BasicCookieStore();
		httpclient = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
		InputStream in = null;
		Map<String, String> cookies = new HashMap<>();
		List<Cookie> cookieList = null;
		try {
			HttpGet httpget = new HttpGet(fileUrl);
			httpget.setHeader("User-Agent",
					"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:52.0) Gecko/20100101 Firefox/52.0");
			httpget.setHeader("Accept", "*/*");
			HttpResponse response = httpclient.execute(httpget);

			cookieList = cookieStore.getCookies();

			HttpEntity entity = response.getEntity();
			in = entity.getContent();
			File file = new File(filePath);
			System.out.println("AbsolutePath=" + file.getAbsolutePath());
			Files.createParentDirs(file);
			FileOutputStream fout = new FileOutputStream(file);
			int l = 0;
			byte[] tmp = new byte[1024];
			while ((l = in.read(tmp)) != -1) {
				fout.write(tmp, 0, l);
			}

			fout.flush();
			fout.close();
			System.out.println("downing :" + fileUrl);

		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				httpclient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return cookieList;
	}

	private String formatPath(String filePath) {
		if (filePath.contains("\\")) {
			filePath = filePath.replace("\\", "/");
		}
		while (filePath.contains("//"))
			filePath = filePath.replace("//", "/");
		return filePath;
	}

	private String getImageWidth(File file) throws Exception {
		Metadata metadata = ImageMetadataReader.readMetadata(file);
//		System.out.println("Directory Count: "+metadata.getDirectoryCount());
		//输出所有附加属性数据
		for (Directory directory : metadata.getDirectories()) {
			for (Tag tag : directory.getTags()) {
				if (tag.getTagName().toLowerCase().contains("width")) {
					String width = tag.getDescription();
					if (width.contains(" ")) {
						width = width.substring(0, width.indexOf(" ")).trim();
					}
					return width;
				}
			}
		}
		return "1";
	}

	private String getImageHeight(File file) throws Exception {
		Metadata metadata = ImageMetadataReader.readMetadata(file);
//		System.out.println("Directory Count: "+metadata.getDirectoryCount());
		//输出所有附加属性数据
		for (Directory directory : metadata.getDirectories()) {
			for (Tag tag : directory.getTags()) {
				if (tag.getTagName().toLowerCase().contains("height")) {
					String width = tag.getDescription();
					if (width.contains(" ")) {
						width = width.substring(0, width.indexOf(" ")).trim();
					}
					return width;
				}
			}
		}
		return "1";
	}

	/**
	 * 格式化指定的文件夹下的所有的图片
	 *
	 * @param path 存放图片的路径
	 */
	public static void resizeImagesDirs(String path) {
		File file = new File(path);
		if (!file.exists() || file.listFiles().length == 0) {
			return;
		}

		for (File f : file.listFiles()) {
			String fromPath = f.getAbsolutePath();
			String targetPath = "";
			String format = "";
			if (fromPath.contains(".")) {
				format = fromPath.substring(fromPath.lastIndexOf("."), fromPath.length());
			} else {
				format = ".PNG";
			}
    		/*try {
				 format = fromPath.substring(fromPath.lastIndexOf("."), fromPath.length());
			} catch (Exception e1) {
				format = ".PNG";
				e1.printStackTrace();
			}*/
			BufferedImage image = null;
			try {
				image = ImageIO.read(f);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (image != null && image.getWidth() > 900) {
				targetPath = resizeImage(f, image.getWidth(), format);
			}
			// 删除之前的文件, 重命名新的图片为之前的名称
			if (fromPath != "" && targetPath != "") {
				System.out.println("尺寸超标的图片的路径为: " + fromPath);
				new File(fromPath).delete();
				new File(targetPath).renameTo(new File(fromPath));
				System.out.println("重命名成功 ----------------> " + fromPath);
			}
		}
	}

	/**
	 * 图片尺寸的重新设计
	 *
	 * @param file   图片文件
	 * @param width  图片宽度
	 * @param format 图片的样式, 指的是图片的类型, 即后缀名称
	 */
	public static String resizeImage(File file, int width, String format) {
		//读取图片
		BufferedInputStream in = null;
		try {
			in = new BufferedInputStream(new FileInputStream(file));
			//字节流转图片对象
			Image bi = ImageIO.read(in);
			//获取图像的高度，宽度
			int height = bi.getHeight(null);
			if (width > 900) {
				int newWidth = 900;
				int newHeight = newWidth * height / width;
				//构建图片流
				BufferedImage tag = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
				//绘制改变尺寸后的图
				tag.getGraphics().drawImage(bi, 0, 0, newWidth, newHeight, null);
				//输出流

				String targetPath = file.getParentFile().getPath().replaceAll("\\\\", "/") + "/" + getTargetName(file) + "_" + System.currentTimeMillis() + format;
				BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(targetPath));
				ImageIO.write(tag, format.replace(".", ""), out);
				in.close();
				out.close();
				return targetPath;
			} else {
				in.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}

	public static String resizeGif(File file, int width, String format) {
		//
		String targetPath = "";
		targetPath = file.getParentFile().getPath().replaceAll("\\\\", "/") + "/" + getTargetName(file) + "_" + System.currentTimeMillis() + format;
		GifDecoder gd = new GifDecoder();
		AnimatedGifEncoder ge = null;
		try {
			FileInputStream in = new FileInputStream(file);
			int status = gd.read(in);
			if (status != GifDecoder.STATUS_OK) {
				return "";
			}
			ge = new AnimatedGifEncoder();
			ge.start(new FileOutputStream(new File(targetPath)));
			ge.setRepeat(0);

			for (int i = 0; i < gd.getFrameCount(); i++) {
				BufferedImage frame = gd.getFrame(i);
				/*int height = frame.getHeight();
				// 80%
				width = (int) (width * 1);
				height = (int) (height * 1);*/
				int width2 = frame.getWidth();
				int height2 = frame.getHeight();
				float rate = (float) 900 / (float) width;
				DecimalFormat df = new DecimalFormat("0");//格式化小数   
				width2 = Integer.parseInt(df.format(width2 * rate));
				height2 = Integer.parseInt(df.format(height2 * rate));
				System.out.println(width2 + "<---------------------------------------------->" + height2);
				//
				BufferedImage rescaled = Scalr.resize(frame, Mode.FIT_EXACT, width2, height2);
				//
				int delay = gd.getDelay(i);
				//

				ge.setDelay(delay);
				ge.addFrame(rescaled);
			}
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			ge.finish();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (ImagingOpException e) {
			e.printStackTrace();
		}
		return targetPath;
	}

	/**
	 * 返回附件的名称
	 *
	 * @param file
	 * @return
	 */
	private static String getTargetName(File file) {
		String imageName = "";
		if (file.getName().contains(".")) {
			imageName = file.getName().substring(0, file.getName().indexOf("."));
		} else {
			imageName = file.getName();
		}
		return imageName;
	}

	/**
	 * 获取远程文件下载到本地, 同时还需要将部分的图片的过大的状况进行图片尺寸的调整, 目前修改为 900(宽度)
	 *
	 * @param fileUrl
	 * @param filePath
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	public void getVideoFileTo(String fileUrl, String filePath) throws IOException {
		if (fileUrl.startsWith("//")) {
			fileUrl = "http:" + fileUrl;
		}
		System.out.println("fileUrl=" + fileUrl);
		CloseableHttpClient httpclient = HttpClients.createDefault();
		InputStream in = null;
		String fromPath = "";
		String targetPath = "";
		try {
			HttpGet httpget = new HttpGet(fileUrl);
			httpget.setHeader("User-Agent",
					"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:52.0) Gecko/20100101 Firefox/52.0");
			httpget.setHeader("Accept", "*/*");
			HttpResponse response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();
			in = entity.getContent();
			File file = new File(filePath);
			fromPath = filePath;
			System.out.println("AbsolutePath=" + file.getAbsolutePath());
			Files.createParentDirs(file);
			FileOutputStream fout = new FileOutputStream(file);

			int l = 0;
			byte[] tmp = new byte[1024];
			while ((l = in.read(tmp)) != -1) {
				fout.write(tmp, 0, l);
			}

			fout.flush();
			fout.close();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				httpclient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

// 	public static void main(String[] args) throws IOException, InterruptedException {
// 		//下载单曲音频
// 		System.setProperty("webdriver.chrome.marionette", "chromedriver.exe");
// //      	ChromeOptions chromeOptions = new ChromeOptions();
// //  		chromeOptions.addArguments("--headless");
// 		WebDriver driver = new ChromeDriver();
// 		driver.manage().window().maximize();
// 		driver.get("https://y.qq.com/");
// 		driver.findElement(By.className("search_input__input")).sendKeys("De Jeugd Van Tegenwoordig");
// 		driver.findElement(By.className("search_input__btn")).click();
// 		Html h = new Html(driver.getPageSource());
//
// 		List<String> pagingUrls = new ArrayList<>();
//
// 		List<WebElement> findElements = driver.findElements(By.xpath("//div[@class='songlist__item']/div[@class='songlist__songname']/span[@class='songlist__songname_txt']/a[@class='js_song'] | //div[@class='songlist__item songlist__item--even ']/div[@class='songlist__songname']/span[@class='songlist__songname_txt']/a[@class='js_song']"));
// 		for (WebElement ele : findElements) {
// 			String href = ele.getAttribute("href");
// 			if (!pagingUrls.contains(href)) {
// 				pagingUrls.add(href);
// 			}
// 		}
//
// 		driver.close();
// 		System.out.println(pagingUrls);
//
// 		List<String> songnames = new ArrayList<>();
//
// 		if (pagingUrls.size() > 0) {
// 			for (String url : pagingUrls) {
// //				ChromeOptions chromeOptions2 = new ChromeOptions();
// //		  		chromeOptions2.addArguments("--headless");
// 				WebDriver temp = new ChromeDriver();
// 				temp.manage().window().maximize();
// 				temp.get(url);
// 				Thread.sleep(1000);
//
// 				try {
// 					// 获取文件名称
// 					WebElement h1 = temp.findElement(By.xpath("//h1[@class='data__name_txt']"));
// 					String title = h1.getAttribute("title");
//
//
// 					temp.findElement(By.xpath("//div[@class='data__actions']/a[1]")).click();
//
// 					Set<String> windowHandles = temp.getWindowHandles();
// 					System.out.println(temp.getWindowHandle());
// 					for (String windowhandle : windowHandles) {
// 						if (windowhandle != temp.getWindowHandle()) {
// 							//temp.close();
// 							// 切换到播放音乐的窗口(句柄)
// 							temp.switchTo().window(windowhandle);
// 						}
// 					}
//
// 					Thread.sleep(15000);
// 					WebElement findElement = temp.findElement(By.xpath("//audio[@id='h5audio_media']/source"));
// 					System.out.println("----------------> " + findElement.getAttribute("src"));
// 					// //div[@id='song_name']/a
// 					// 获取单曲的名称
// 					WebElement a = temp.findElement(By.xpath("//div[@id='song_name']/a"));
// 					String songname = a.getText();
// 					if (!songnames.contains(songname)) {
// 						songnames.add(songname);
// 						String fileUrl = findElement.getAttribute("src");
// 						String filePath = "F:/BlackEyedPeas/" + songname + ".m4a";
// 						new HttpFileUtil().getVideoFileTo(fileUrl, filePath);
// 					}
// 				} catch (Exception e) {
// 					e.printStackTrace();
// 				}
//
// 				// 关闭窗口
// 				temp.quit();
//
// 			}
// 		}
//
// 	}

	private static void downloadmusic(String songmid) throws IOException {
		//0. 通过 albummid 获取所有的 songmid
		String songmidurl = "https://u.y.qq.com/cgi-bin/musicu.fcg?g_tk=558582302&loginUin=598130620&hostUin=0&format=jsonp&inCharset=utf8&outCharset=utf-8&notice=0&platform=yqq&needNewCode=0&data=%7B%22req%22%3A%7B%22module%22%3A%22CDN.SrfCdnDispatchServer%22%2C%22method%22%3A%22GetCdnDispatch%22%2C%22param%22%3A%7B%22guid%22%3A%227653016346%22%2C%22calltype%22%3A0%2C%22userip%22%3A%22%22%7D%7D%2C%22req_0%22%3A%7B%22module%22%3A%22vkey.GetVkeyServer%22%2C%22method%22%3A%22CgiGetVkey%22%2C%22param%22%3A%7B%22songmid%22%3A%5B%22${songmid}%22%5D%2C%22songtype%22%3A%5B0%5D%2C%22uin%22%3A%22598130620%22%2C%22loginflag%22%3A1%2C%22platform%22%3A%2220%22%7D%7D%2C%22comm%22%3A%7B%22uin%22%3A598130620%2C%22format%22%3A%22json%22%2C%22ct%22%3A20%2C%22cv%22%3A0%7D%7D".replace("${songmid}", songmid);
		WebDriver temp = new ChromeDriver();
		temp.manage().window().maximize();
		temp.get(songmidurl);
		String content = temp.getPageSource();
		content = content.replaceAll("<.*?>", "");


		JSONObject json;
		if (!StringUtils.isEmpty(content)) {
			try {
				json = JSONObject.parseObject(content);
				JSONObject data = json.getJSONObject("req").getJSONObject("data");
				String vkey = data.getString("vkey");
				String guid = getGuid(data);
				System.out.println(vkey + "<----------------");
				if (guid != "") {
					String downloadurl = "http://111.202.98.146/amobile.music.tc.qq.com/${songId}.m4a?guid=${guid}&vkey=${vkey}&uin=8124&fromtag=66"
							.replace("${songId}", "C400" + songmid)
							.replace("${vkey}", vkey)
							.replace("${guid}", guid);

					if (vkey != null) {
						new HttpFileUtil().getVideoFileTo(downloadurl, "F:/11111.m4a");
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		temp.close();
	}

	private static String getGuid(JSONObject data) {
		String testfilewifi = data.getString("testfilewifi");
		if (null != testfilewifi && testfilewifi.contains("guid")) {
			String guid = testfilewifi.subSequence(testfilewifi.indexOf("guid=") + 5, testfilewifi.indexOf("guid=") + 15).toString();
			return guid;
		} else {
			String keepalivefile = data.getString("keepalivefile");
			if (null != keepalivefile && keepalivefile.contains("guid")) {
				String guid = keepalivefile.subSequence(keepalivefile.indexOf("guid=") + 5, keepalivefile.indexOf("guid=") + 15).toString();
				return guid;
			} else {
				String testfile2g = data.getString("testfile2g");
				if (null != testfile2g && testfile2g.contains("guid")) {
					String guid = testfile2g.subSequence(testfile2g.indexOf("guid=") + 5, testfile2g.indexOf("guid=") + 15).toString();
					return guid;
				}
			}
		}
		return "";
	}

	/**
	 * 下载远程文件到本地带代理
	 * @param url
	 * @param localFilePath
	 * @param xxNetProxy
	 */
	public void getRemote2LocalWithProxy(String url, String localFilePath, XXNetProxy xxNetProxy) {
		System.out.println("Downloading " + url + "...");
		CloseableHttpClient client = new HttpClientGenerator(HttpClientGenerator.SSLTypes.SSLv3).getClient(null);
		RequestConfig config = null;
		//使用代理

		if(null != xxNetProxy && !StringUtils.isEmpty(xxNetProxy.getProxy_ip())){
			HttpHost proxy = new HttpHost(xxNetProxy.getProxy_ip(), xxNetProxy.getProxy_port());
			config = RequestConfig.custom().setProxy(proxy).build();
		}else{
		//没有代理，使用默认值
			config = RequestConfig.custom().build();
		}
		//目标文件url
		HttpGet httpGet = new HttpGet(url);
		httpGet.setHeader("User-Agent",
				"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:52.0) Gecko/20100101 Firefox/52.0");
		httpGet.setHeader("Accept", "*/*");
		httpGet.setConfig(config);

		try {
			HttpResponse respone = client.execute(httpGet);
			if(respone.getStatusLine().getStatusCode() != HttpStatus.SC_OK){
				System.out.println("Downloading " + url + " to " + localFilePath + " failed!!!");
				return ;
			}
			HttpEntity entity = respone.getEntity();
			if(entity != null) {
				InputStream is = entity.getContent();
				File file = new File(localFilePath);
				if(!file.getParentFile().exists()) {
					file.getParentFile().mkdirs();
				}
				if(!file.exists()) {
					file.createNewFile();
				}
				FileOutputStream fos = new FileOutputStream(file);
				byte[] buffer = new byte[4096];
				int len = -1;
				while((len = is.read(buffer) )!= -1){
					fos.write(buffer, 0, len);
				}
				System.out.println("Downloading " + url + " to " + localFilePath + " successfully!!!");
				fos.close();
				is.close();
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}finally{
			try {
				client.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 获取远程文件下载到本地, 同时还需要将部分的图片的过大的状况进行图片尺寸的调整, 目前修改为 900(宽度)
	 *
	 * @param fileUrl
	 * @param filePath
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	public Map<String,Object> getFileTo(String fileUrl, String filePath, Map<String,Object> resultMap) throws IOException {
		System.out.println("fileUrl=" + fileUrl);
		long beginTime = System.currentTimeMillis();

		CloseableHttpClient httpclient = HttpClients.createDefault();
		RequestConfig config = RequestConfig.custom().setSocketTimeout(20000).setConnectTimeout(20000).build();
		InputStream in = null;
		String fromPath = "";
		String targetPath = "";
		BufferedInputStream bis = null;
		try {
			HttpGet httpget = new HttpGet(fileUrl);
			httpget.setHeader("User-Agent",
					"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:52.0) Gecko/20100101 Firefox/52.0");
			httpget.setHeader("Accept", "*/*");
			httpget.setConfig(config);
			HttpResponse response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();
			in = entity.getContent();

			// 获取附件文件的真实文件类型
			String format = filePath.substring(filePath.lastIndexOf(".") + 1, filePath.length());
			String type = entity.getContentType().toString();
			String realtype = "";
			if(type.contains("image/")){
				type = type.substring(type.indexOf("image/"), type.length());
			}
			if (type.contains(";")) {
				realtype = type.subSequence(type.lastIndexOf("/") + 1, type.indexOf(";")).toString();
			} else {
				realtype = type.subSequence(type.lastIndexOf("/") + 1, type.length()).toString();
			}
			File file;
			if (type.contains("image") && realtype != "*" && !format.equals(realtype)) {
				String newPath = filePath.substring(0, filePath.lastIndexOf(".") + 1) + realtype;
				file = new File(newPath);
				String tempNew = formatPath(newPath).substring(formatPath(newPath).lastIndexOf("/") + 1, formatPath(newPath).length());
				String tempOld = formatPath(filePath).substring(formatPath(filePath).lastIndexOf("/") + 1, formatPath(filePath).length());
				resultMap.put("realTypeImage", tempNew);
				resultMap.put("oldTypeImage", tempOld);
			} else {
				file = new File(filePath);
			}
			fromPath = file.getAbsolutePath();
			System.out.println("AbsolutePath=" + file.getAbsolutePath());
			Files.createParentDirs(file);
			FileOutputStream fout = new FileOutputStream(file);
			int l = 0;
			byte[] tmp = new byte[10240];
			while ((l = in.read(tmp)) != -1) {
				fout.write(tmp, 0, l);
			}
			if (fout != null) {
				// 清空并关闭输出流
				fout.flush();
				fout.close();
			}

			// 如果是 pdf 文件的话, 不需要进行后续的判断操作...
			if (!realtype.equalsIgnoreCase("pdf") && !realtype.contains("svg")) {
				Integer width = 0;
				String idPath = file.getAbsolutePath().toString();
				String id = idPath.substring(idPath.lastIndexOf(File.separator) + 1, idPath.length());
				if (!realtype.toLowerCase().endsWith("gif")) {
					try {
						width = Integer.parseInt(getImageWidth(file));
						if (width > limitWidth) {
							System.out.println("尺寸超标的图片的路径为: " + fromPath);
							// 对于部分的尺寸过大的图片进行尺寸的调整
							int height = Integer.parseInt(getImageHeight(file));
							resultMap.put("badFormatImage", new BadFormatImage(id, width, height));
						}
					} catch (NumberFormatException e) {
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					// gif 文件
					GifDecoder gd = new GifDecoder();
					bis = new BufferedInputStream(new FileInputStream(file));
					gd.read(bis);
					BufferedImage image2 = gd.getImage();
					//GifImage  gf = GifDecoder2.read(new FileInputStream(file));
					int width2 = image2.getWidth();
					int height = image2.getHeight();
					if (width2 > limitWidth) {
						// 对于部分的尺寸过大的图片进行尺寸的调整
						System.out.println("尺寸超标的图片的路径为: " + fromPath);
						resultMap.put("badFormatImage", new BadFormatImage(id, width2, height));
					}
				}
			}

		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				httpclient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				if (bis != null) {
					bis.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		// 删除之前的文件, 新文件重命名新的图片为之前的名称
		if (fromPath != "" && targetPath != "") {
			boolean delete = new File(fromPath).delete();
			boolean renameTo = new File(targetPath).renameTo(new File(fromPath));
			System.out.println("尺寸超标的图片的路径为: " + fromPath + ", 删除原图: " + delete + ", 重命名到原图: " + renameTo);
		}

		System.out.println("运行耗时： " + (System.currentTimeMillis() - beginTime) + " sss");

		return resultMap;
	}

	public static void doDeleteEmptyDir(String dir) {
		boolean success = (new File(dir)).delete();
		if (success) {
			System.out.println("Successfully deleted empty directory: " + dir);
		} else {
			System.out.println("Failed to delete empty directory: " + dir);
		}
	}

	/**
	 * 递归删除目录下的所有文件及子目录下所有文件
	 * @param dir 将要删除的文件目录
	 * @return boolean Returns "true" if all deletions were successful.
	 *                 If a deletion fails, the method stops attempting to
	 *                 delete and returns "false".
	 */
	public static boolean deleteDir(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			//递归删除目录中的子目录下
			for (int i=0; i<children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}
		// 目录此时为空，可以删除
		return dir.delete();
	}

	public static void main(String[] args) throws IOException {
		// deleteDir(new File("F:\\ManualPath\\Video\\凤凰视频_军情\\视频：美航母进入波斯湾，中东之虎不怕擦枪走火吗\\"));
		new HttpFileUtil().getVideoFileTo("https://www.army.mil/e2/c/images/2019/08/16/561942/size0.jpg","D://1.jpg");
	}
}

