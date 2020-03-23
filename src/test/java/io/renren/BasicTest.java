package io.renren;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.oss.common.utils.HttpUtil;
import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.mss.crawler.common.IP;
import com.mss.crawler.common.IPProxyUtils;
import com.mss.crawler.common.XXNetProxy;
import com.mss.crawler.common.YzmUtils;
import com.mss.crawler.spiderjson.pipeline.NewsDBPipeline;
import com.mss.crawler.spiderjson.util.ChromeDriverUtils;
import com.mss.crawler.spiderjson.util.GifDecoder2;
import com.mss.crawler.spiderjson.util.GifDecoder2.GifImage;
import com.mss.crawler.spiderjson.util.HtmlFormatter;
import com.mss.crawler.spiderjson.util.HttpFileUtil;
import com.mss.translate.GoogleTranslate;
import io.renren.modules.spider.entity.News;
import io.renren.modules.spider.utils.CSVUtils;
import io.renren.utils.HttpUtils;
import io.renren.utils.XMLUtil;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.xmlbeans.impl.regex.Match;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Point;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.util.StringUtils;
import us.codecraft.webmagic.downloader.HttpClientDownloader;
import us.codecraft.webmagic.selector.Html;
import us.codecraft.webmagic.selector.Selectable;
import us.codecraft.webmagic.selector.Selector;
import us.codecraft.webmagic.selector.XpathSelector;
import us.codecraft.xsoup.Xsoup;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipFile;

public class BasicTest {

	private static final Logger log = LogManager.getLogger(BasicTest.class);

	private IP ipProxy = null;

	private GoogleTranslate translate = new GoogleTranslate();

	/**
	 * 汇率网站
	 */
	@Test
	public void testCurrencyExchange() throws IOException {
		String s = "ff808081707d4aba0170808ff4e208bb";
		String imgPath = "http://www.haiguanbeian.com/upload/uploadAction!getViewImg.do?id=ff808081707d4aba0170808ff4e208bb";
		HttpFileUtil.getInstance().getVideoFileTo(imgPath, "F:/" + s + ".jpg");
	}


	/**
	 * 文件导入
	 */
	@Test
	public void testsdsdsdsd() {
		String path = "F:\\ZNSD\\brand\\urls.txt";
		List<String> strings = readTextAsList(path);
		List<String> tempStrings = new ArrayList<>();
		for(int i = 0; i < strings.size(); i++) {
			System.out.println(i);
			String t = strings.get(i);
			if(!tempStrings.contains(t) && !StringUtils.isEmpty(t)) {
				tempStrings.add(t);
			}
		}
		if(tempStrings.size() > 0) {
			fileWrite(tempStrings, "F:\\ZNSD\\brand\\temp7.txt");
		}

	}

	/**
	 * 抽取品牌库详情页的字段信息
	 * @param args
	 */
	public static void main(String[] args) {
		ArrayList<String> urls = new ArrayList();
		urls.add("http://www.haiguanbeian.com/brandForOuter/intoAction!intoupdatebrand.do?rgt=4&id=ff80808170e449ee0170e5f5725d0057:::中国");
		urls.add("http://www.haiguanbeian.com/brandForOuter/intoAction!intoupdatebrand.do?rgt=4&id=4028e5145a8ff700015a924226890130:::中国");
		if(urls.size() > 0) {
			ChromeDriver driver = ChromeDriverUtils.getHeadlessChromeDrver();
			try {
				for(String text : urls) {
					String url = text.substring(0, text.indexOf(":::"));
					String cou = text.substring(text.indexOf(":::") + 3);
					driver.get(url);
					WebDriverWait wait = new WebDriverWait(driver,60);
					wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[id='applyusername'] | //input[id='userName']")));
//					System.out.println(driver.getPageSource());
					if(ChromeDriverUtils.doesWebElementExist(driver, "//div[@id='showopusphoto']/img")) {
						WebElement element = driver.findElement(By.xpath("//div[@id='showopusphoto']/img"));
						String src = element.getAttribute("src");
						String headImg = "http://www.haiguanbeian.com" + src;
						String absoluteFileSrc = HtmlFormatter.getAbsoluteFileSrc(headImg, headImg);
						System.out.println(src + headImg + absoluteFileSrc);
					}


					String headImg = driver.findElementById("brandPhoto").getAttribute("value");
					String applyUserName = driver.findElement(By.xpath("//input[id='applyusername'] | //input[id='userName']")).getAttribute("value");
					String applyUserEnglishName = driver.findElementById("applyuserenglishname").getAttribute("value");
					String brandName = driver.findElementById("brandName").getAttribute("value");
					String country = cou;
					String brandNumber = driver.findElementById("registerNum").getAttribute("value");
					String checkMerch = driver.findElementById("checkMerch").getAttribute("value");
					// 产权期限
					String brandDateStart = driver.findElementById("rightStartTime").getAttribute("value");
					String brandDateEnd = driver.findElementById("rightEndTime").getAttribute("value");
					// 备案号
					String recordNumber = driver.findElementById("recordNum").getAttribute("value");
					// 备案期限
					String recordBeginDates = driver.findElementById("recordBeginDates").getAttribute("value");
					String recordEndDates = driver.findElementById("recordEndDates").getAttribute("value");

						System.out.println(headImg + "," + applyUserName + "," + applyUserEnglishName + "," + brandName + "," + country + "," + brandNumber + "," + checkMerch + "," + brandDateStart + "," + brandDateEnd + "," + brandNumber + "," + recordBeginDates + "," + recordEndDates + ", " + url);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				driver.quit();
			}
		}
	}

	/**
	 * 抽取专利权
	 */
	@Test
	public void testExtractZHUANLIQUANInfo() {
		ArrayList<String> urls = new ArrayList();
		urls.add("http://www.haiguanbeian.com/patentForOuter/Patent!intoUpdatePatent.do?rgt=2&id=ff8080816e608f07016e622bfd080015:::中国");
		urls.add("http://www.haiguanbeian.com/patentForOuter/Patent!intoUpdatePatent.do?rgt=2&id=ff808081707d4aba0170810151550b5f:::中国");
		if(urls.size() > 0) {
			ChromeDriver driver = ChromeDriverUtils.getHeadlessChromeDrver();
			try {
				for(String text : urls) {
					String url = text.substring(0, text.indexOf(":::"));
					String cou = text.substring(text.indexOf(":::") + 3);
					driver.get(url);
					WebDriverWait wait = new WebDriverWait(driver,60);
					wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@id='userName']")));

					String headImg = "";
					if(ChromeDriverUtils.doesWebElementExist(driver, "//input[@id='appearancePatentPic']")) {
						headImg = driver.findElementById("appearancePatentPic").getAttribute("value");
					}

					// 权利人名称
					String userName = driver.findElement(By.xpath("//input[@id='userName']")).getAttribute("value");
					// 权利人英文名称
					String applyUserEnglishName = driver.findElementById("applyuserenglishname").getAttribute("value");
					// 专利名称
					String patentName = driver.findElementById("patentName").getAttribute("value");
					String country = cou;
					// 专利类别
					String patentType = driver.findElementById("patentType").getAttribute("value");
					// 专利年费截止日期
					String annualFeeEndDate = driver.findElementById("annualFeeEndDate").getAttribute("value");
					// 专利申请日期
					String applyDate = driver.findElementById("applyDate").getAttribute("value");
					// 专利授权公告日
					String authorizationDate = driver.findElementById("authorizationDate").getAttribute("value");
					// 备案号
					String recordNumber = driver.findElementById("recordNum").getAttribute("value");
					// 备案期限
					String recordBeginDates = driver.findElementById("recordBeginDate").getAttribute("value");
					String recordEndDates = driver.findElementById("recordBeginDate").getAttribute("value");

					System.out.println(headImg + "," + userName + "," + applyUserEnglishName + "," + patentName + "," + country + "," + patentType + "," + annualFeeEndDate + "," + applyDate + "," + authorizationDate + "," + recordNumber + "," + recordBeginDates + "," + recordEndDates + ", " + url);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				driver.quit();
			}
		}
	}

	/**
	 * 抽取著作权
	 */
	@Test
	public void testExtractzhuzuoQUANInfo() {
		ArrayList<String> urls = new ArrayList();
		urls.add("http://www.haiguanbeian.com/opusForOuter/intoAction!intoupdateopus.do?rgt=6&id=ff80808170827116017084212e7d0017:::中国");
		urls.add("http://www.haiguanbeian.com/opusForOuter/intoAction!intoupdateopus.do?rgt=6&id=ff808081708271160170841f6b84000d:::中国");
		if(urls.size() > 0) {
			ChromeDriver driver = ChromeDriverUtils.getHeadlessChromeDrver();
			try {
				for(String text : urls) {
					String url = text.substring(0, text.indexOf(":::"));
					String cou = text.substring(text.indexOf(":::") + 3);
					driver.get(url);
					WebDriverWait wait = new WebDriverWait(driver,60);
					wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@id='applyusername']")));
//					System.out.println(driver.getPageSource());
					if(ChromeDriverUtils.doesWebElementExist(driver, "//div[@id='showopusphoto']/img")) {
						WebElement element = driver.findElement(By.xpath("//div[@id='showopusphoto']/img"));
						String src = element.getAttribute("src");
						String headImg = "http://www.haiguanbeian.com" + src;
						String absoluteFileSrc = HtmlFormatter.getAbsoluteFileSrc(headImg, headImg);
						System.out.println(src + headImg + absoluteFileSrc);
					}


					String headImg = driver.findElementById("opusPhoto").getAttribute("value");
					String applyUserName = driver.findElement(By.xpath("//input[@id='applyusername']")).getAttribute("value");
					String applyUserEnglishName = driver.findElementById("applyuserenglishname").getAttribute("value");
					// 作品名称
					String opusName = driver.findElementById("opusName").getAttribute("value");
					String country = cou;
					// 作者性质
					String authorType = driver.findElementById("authorType").getAttribute("value");
					// 作品类别
					String opusType = driver.findElementById("opusType").getAttribute("value");
					// 作品完成日
					String opusCompleteDate = driver.findElementById("opusCompleteDate").getAttribute("value");
					// 著作权终止日
					String opusRightEndDate = driver.findElementById("opusRightEndDate").getAttribute("value");
					// 备案号
					String recordNumber = driver.findElementById("recordNum").getAttribute("value");
					// 备案期限
					String recordBeginDates = driver.findElementById("recordBeginDates").getAttribute("value");
					String recordEndDates = driver.findElementById("recordEndDates").getAttribute("value");

					System.out.println(headImg + "," + applyUserName + "," + applyUserEnglishName + "," + opusName + "," + country + "," + authorType + "," + opusType + "," + opusCompleteDate + "," + opusRightEndDate + "," + recordNumber + "," + recordBeginDates + "," + recordEndDates + ", " + url);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				driver.quit();
			}
		}
	}

	/**
	 *
	 */

	/**
	 * 采集著作权库的信息数据
	 */
	@Test
	public void testAddZHUZUOquanURL2LocalTextTest() {
		ChromeDriver driver = ChromeDriverUtils.getDefaultChromeDriver();
		driver.get("http://www.haiguanbeian.com/zscq/search/jsp/vBrandSearchIndex.jsp#");
		String filePath = "F:/temp6.txt";
		String prefix = "http://www.haiguanbeian.com";
		// 读取本地文本内容
		List<String> existList = readTextAsList(filePath);

		try {

			driver.findElement(By.xpath("//select[@id='F_TYPE']/option[3]")).click();
			Thread.sleep(3000l);
			driver.findElement(By.id("find_btn")).click();
			Thread.sleep(3000l);
			Set<String> windowHandles = driver.getWindowHandles();
			for (String windowhandle : windowHandles) {
				if (windowhandle != driver.getWindowHandle()) {
					driver = (ChromeDriver) driver.switchTo().window(windowhandle);
				}
			}
			//设置10秒
			WebDriverWait wait = new WebDriverWait(driver,60);
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//td[@aria-describedby='searchlist_RECORD_NAME']/a")));
			List<WebElement> urlElements = driver.findElementsByXPath("//td[@aria-describedby='searchlist_RECORD_NAME']/a");
			List<WebElement> countryElements = driver.findElementsByXPath("//td[@aria-describedby='searchlist_CONUTRY_NAME']");
			List<String> temp = new ArrayList<>();
			if(null != urlElements && null != countryElements && urlElements.size() == countryElements.size()) {
				for(int i = 0; i < urlElements.size(); i++) {
					try {
						String suffix = urlElements.get(i).getAttribute("onclick");
						if(suffix.contains("/") && suffix.contains("'")) {
							suffix = suffix.substring(suffix.indexOf("('")+2, suffix.indexOf("')"));
						}
						String country = countryElements.get(i).getText();
						if(suffix.contains("/") && suffix.contains("'")) {
							suffix = suffix.substring(suffix.indexOf("('")+2, suffix.indexOf("')"));
						}
						if(!StringUtils.isEmpty(country)) {
							suffix = suffix + ":::" + country;
						}

						if(!StringUtils.isEmpty(suffix)) {
							if(!existList.contains(prefix + suffix)) {
								System.out.println("http://www.haiguanbeian.com" + suffix);
								temp.add(prefix + suffix);
							}

						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				fileWrite(temp, filePath);
			}


			for(int j = 0; j < 135; j++) {
				System.out.println(j);
				temp = new ArrayList<>();
				WebElement nextGridPager = driver.findElement(By.id("next_gridPager"));
				wait = new WebDriverWait(driver,60);
				wait.until(ExpectedConditions.elementToBeClickable(nextGridPager));
				driver.findElement(By.id("next_gridPager")).click();
				Thread.sleep(2000l);
				windowHandles = driver.getWindowHandles();
				for (String windowhandle : windowHandles) {
					if (windowhandle != driver.getWindowHandle()) {
						driver = (ChromeDriver) driver.switchTo().window(windowhandle);
					}
				}
				Thread.sleep(3000l);
				wait = new WebDriverWait(driver,60);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//td[@aria-describedby='searchlist_RECORD_NAME']/a")));
				urlElements = driver.findElementsByXPath("//td[@aria-describedby='searchlist_RECORD_NAME']/a");
				countryElements = driver.findElementsByXPath("//td[@aria-describedby='searchlist_CONUTRY_NAME']");
				if(null != urlElements && null != countryElements && urlElements.size() == countryElements.size()) {
					for(int i = 0; i < urlElements.size(); i++) {
						try {
							String suffix = urlElements.get(i).getAttribute("onclick");
							if(suffix.contains("/") && suffix.contains("'")) {
								suffix = suffix.substring(suffix.indexOf("('")+2, suffix.indexOf("')"));
							}
							String country = countryElements.get(i).getText();
							if(suffix.contains("/") && suffix.contains("'")) {
								suffix = suffix.substring(suffix.indexOf("('")+2, suffix.indexOf("')"));
							}
							if(!StringUtils.isEmpty(country)) {
								suffix = suffix + ":::" + country;
							}
							if(!StringUtils.isEmpty(suffix)) {
								if(!existList.contains(prefix + suffix)) {
									System.out.println("http://www.haiguanbeian.com" + suffix);
									temp.add(prefix + suffix);
								}

							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					fileWrite(temp, filePath);
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			driver.quit();
		}
	}

	/**
	 * 采集专利权库的信息数据
	 */
	@Test
	public void testAddZHUANLIquanURL2LocalTextTest() {
		ChromeDriver driver = ChromeDriverUtils.getDefaultChromeDriver();
		driver.get("http://www.haiguanbeian.com/zscq/search/jsp/vBrandSearchIndex.jsp#");
		String filePath = "F:/temp5.txt";
		String prefix = "http://www.haiguanbeian.com";
		// 读取本地文本内容
		List<String> existList = readTextAsList(filePath);

		try {

			driver.findElement(By.xpath("//select[@id='F_TYPE']/option[4]")).click();
			Thread.sleep(3000l);
			driver.findElement(By.id("find_btn")).click();
			Thread.sleep(3000l);
			Set<String> windowHandles = driver.getWindowHandles();
			for (String windowhandle : windowHandles) {
				if (windowhandle != driver.getWindowHandle()) {
					driver = (ChromeDriver) driver.switchTo().window(windowhandle);
				}
			}
			//设置10秒
			WebDriverWait wait = new WebDriverWait(driver,60);
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//td[@aria-describedby='searchlist_RECORD_NAME']/a")));
			List<WebElement> urlElements = driver.findElementsByXPath("//td[@aria-describedby='searchlist_RECORD_NAME']/a");
			List<WebElement> countryElements = driver.findElementsByXPath("//td[@aria-describedby='searchlist_CONUTRY_NAME']");
			List<String> temp = new ArrayList<>();
			if(null != urlElements && null != countryElements && urlElements.size() == countryElements.size()) {
				for(int i = 0; i < urlElements.size(); i++) {
					try {
						String suffix = urlElements.get(i).getAttribute("onclick");
						if(suffix.contains("/") && suffix.contains("'")) {
							suffix = suffix.substring(suffix.indexOf("('")+2, suffix.indexOf("')"));
						}
						String country = countryElements.get(i).getText();
						if(suffix.contains("/") && suffix.contains("'")) {
							suffix = suffix.substring(suffix.indexOf("('")+2, suffix.indexOf("')"));
						}
						if(!StringUtils.isEmpty(country)) {
							suffix = suffix + ":::" + country;
						}

						// 添加备案库列别
						suffix = suffix + "&&&3";

						if(!StringUtils.isEmpty(suffix)) {
							if(!existList.contains(prefix + suffix)) {
								System.out.println("http://www.haiguanbeian.com" + suffix);
								temp.add(prefix + suffix);
							}

						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				fileWrite(temp, filePath);
			}


			for(int j = 0; j < 3; j++) {
				System.out.println(j);
				temp = new ArrayList<>();
				WebElement nextGridPager = driver.findElement(By.id("next_gridPager"));
				wait = new WebDriverWait(driver,60);
				wait.until(ExpectedConditions.elementToBeClickable(nextGridPager));
				driver.findElement(By.id("next_gridPager")).click();
				Thread.sleep(2000l);
				windowHandles = driver.getWindowHandles();
				for (String windowhandle : windowHandles) {
					if (windowhandle != driver.getWindowHandle()) {
						driver = (ChromeDriver) driver.switchTo().window(windowhandle);
					}
				}
				Thread.sleep(3000l);
				wait = new WebDriverWait(driver,60);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//td[@aria-describedby='searchlist_RECORD_NAME']/a")));
				urlElements = driver.findElementsByXPath("//td[@aria-describedby='searchlist_RECORD_NAME']/a");
				countryElements = driver.findElementsByXPath("//td[@aria-describedby='searchlist_CONUTRY_NAME']");
				if(null != urlElements && null != countryElements && urlElements.size() == countryElements.size()) {
					for(int i = 0; i < urlElements.size(); i++) {
						try {
							String suffix = urlElements.get(i).getAttribute("onclick");
							if(suffix.contains("/") && suffix.contains("'")) {
								suffix = suffix.substring(suffix.indexOf("('")+2, suffix.indexOf("')"));
							}
							String country = countryElements.get(i).getText();
							if(suffix.contains("/") && suffix.contains("'")) {
								suffix = suffix.substring(suffix.indexOf("('")+2, suffix.indexOf("')"));
							}
							if(!StringUtils.isEmpty(country)) {
								suffix = suffix + ":::" + country;
							}
							if(!StringUtils.isEmpty(suffix)) {
								if(!existList.contains(prefix + suffix)) {
									System.out.println("http://www.haiguanbeian.com" + suffix);
									temp.add(prefix + suffix);
								}

							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					fileWrite(temp, filePath);
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			driver.quit();
		}
	}

	/***
	 * 文件追加写入字符串
	 *
	 * @param List<String> 字符串数组
	 *
	 * @param path txt文本地址
	 *
	 */
	public void fileWrite(List<String> name, String path) {
		FileWriter fw = null;
		try {
			// 如果文件存在，则追加内容；如果文件不存在，则创建文件
			File f = new File(path);
			fw = new FileWriter(f, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		PrintWriter pw = new PrintWriter(fw);
		if(null != name && name.size() > 0) {
			for(String n : name ) {
				pw.write(n + "\r\n");
			}
		}
		// pw.println("追加内容");
		pw.flush();
		try {
			fw.flush();
			pw.close();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 读取文件到 List集合
	 */
	public List<String> readTextAsList(String path) {
		FileReader fileReader = null;
		BufferedReader bufferedReader = null;
		List<String> list = new ArrayList<String>();
		String str = "";
		try {
			fileReader = new FileReader( path );
			bufferedReader = new BufferedReader( fileReader );
			while( (str = bufferedReader.readLine()) != null ) {
				if( str.trim().length() > 2 ) {
					list.add( str );
				}
			}
		} catch ( Exception e ) {
			e.printStackTrace();
		} finally {
			try {
				if (bufferedReader != null) {
					bufferedReader.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}

			try {
				if (fileReader != null) {
					fileReader.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return list;
	}

	@Test
	public void test123231() {
		String str = "<tr> <td width=\"20%\" class=\"tb\" rowspan=\"2\"> \n" +
				"                                                  法定代表人\n" +
				"                                        </td> <td width=\"30%\" rowspan=\"2\"> <div class=\"boss-td\"> <div class=\"clearfix\" style=\"min-height: 76px;padding-top: 8px;overflow: hidden;\"> <div class=\"pull-left bheadimgkuang\"> <span class=\"usericon boss color-8\" first-letter=\"黄\"></span> </div> <div class=\"bpen\"> <a href=\"/pl_p4ddbfb314d40e7aa35e87ad8031a972.html\" class=\"bname\"><h2 class=\"seo font-20\">黄树平</h2></a> <a class=\"btn-touzi pull-left\" onclick=\"relatedList(1,'p4ddbfb314d40e7aa35e87ad8031a972','黄树平');zhugeTrack('关联企业按钮点击',{'按钮位置':'工商信息','关联目标':'黄树平'});\">他关联4家企业 &gt; </a> </div> </div> </div> </td> <td width=\"20%\" class=\"tb\"> 注册资本 </td> <td width=\"30%\">  5386万元人民币  </td> </tr> <tr> <td width=\"20%\" class=\"tb\"> 实缴资本 </td> <td width=\"30%\"> 5386万元人民币 </td> </tr> <tr> <td class=\"tb\">经营状态</td> <td class=\"\">\n" +
				"                 存续（在营、开业、在册）             </td> <td class=\"tb\">成立日期</td> <td class=\"\">\n" +
				"                1996-05-14\n" +
				"            </td> </tr> <tr> <td class=\"tb\">统一社会信用代码</td> <td class=\"\">\n" +
				"                91210681603910660C\n" +
				"            </td> <td class=\"tb\">纳税人识别号</td> <td class=\"\">\n" +
				"                91210681603910660C\n" +
				"            </td> </tr> <tr> <td class=\"tb\">注册号</td> <td class=\"\">\n" +
				"                210681004004806\n" +
				"            </td> <td class=\"tb\" width=\"15%\">组织机构代码</td> <td class=\"\">\n" +
				"                60391066-0\n" +
				"            </td> </tr> <tr> <td class=\"tb\">企业类型</td> <td class=\"\">\n" +
				"                有限责任公司(自然人投资或控股)\n" +
				"            </td> <td class=\"tb\">所属行业</td> <td class=\"\">\n" +
				"                制造业\n" +
				"            </td> </tr> <tr> <td class=\"tb\">核准日期</td> <td class=\"\" style=\"max-width:301px;\">\n" +
				"              2016-08-05\n" +
				"          </td> <td class=\"tb\">登记机关</td> <td class=\"\">\n" +
				"              东港市市场监督管理局\n" +
				"          </td> </tr> <tr> <td class=\"tb\">所属地区</td> <td class=\"\" style=\"max-width:301px;\">\n" +
				"                辽宁省\n" +
				"            </td> <td class=\"tb\">英文名</td> <td class=\"\">\n" +
				"                Donggang Daping Fishery Group Co., Ltd.\n" +
				"            </td> </tr> <tr> <td class=\"tb\">\n" +
				"                曾用名\n" +
				"            </td> <td class=\"\"> <span>东港市大平渔业集团有限公司&nbsp;&nbsp;</span> <span>东港市大平渔业有限公司&nbsp;&nbsp;</span> </td> <td class=\"tb\">\n" +
				"                参保人数\n" +
				"            </td> <td class=\"\">\n" +
				"                24\n" +
				"            </td> </tr> <tr> <td class=\"tb\">\n" +
				"                人员规模\n" +
				"            </td> <td class=\"\">\n" +
				"                -\n" +
				"            </td> <td class=\"tb\">\n" +
				"                营业期限\n" +
				"            </td> <td class=\"\">\n" +
				"                1996-05-14 至 2046-05-04\n" +
				"            </td> </tr> <tr> <td class=\"tb\">企业地址</td> <td class=\"\" colspan=\"3\">\n" +
				"                 东港路128号\n" +
				"                <a onclick=\"showMapModal('东港路128号','丹东市');zhugeTrack('企业主页按钮点击',{'按钮名称':'查看地图'});\" class=\"m-l c_a\"> 查看地图</a> <a onclick=\"zhugeTrack('企业主页按钮点击',{'按钮名称':'附近企业'});\" href=\"/map?keyNo=498e026790d409ad89c0e5588c9df689\" class=\"m-l c_a\"> 附近企业</a> </td> </tr> <tr> <td class=\"tb\">经营范围</td> <td class=\"\" colspan=\"3\">\n" +
				"                 远洋捕捞;海上捕捞;水产品养殖;销售:水产品、预包装食品兼散装食品、乳制品(不含婴幼儿配方乳粉)、渔需物资、日用百货、农副土特产品、粮油、柴油机、造纸机械;普通货物道路运输、危险货物运输(剧毒化学品除外);货物及技术进出口;矿产资源开采加工、木材加工项目筹建。以下仅限分公司经营(成品油销售;船长45米及以下钢质渔业船舶制造;渔船坞道服务)。(依法须经批准的项目,经相关部门批准后方可开展经营活动。)             </td> </tr> ";
		Map<String, String> map = new HashMap<>();
		String[] tds = str.split("class=\"tb\"");
		for(int j = 0; j < tds.length; j++) {
			Html temp = new Html(tds[j]);
			if(tds[j].contains("法定代表人")) {
				map.put("fddbr", temp.xpath("//h2[@class='seo font-20']/text()").toString());
			} else if(tds[j].contains("注册资本")) {
				map.put("zczb", tagTrim(tds[j]).replace("注册资本", "").trim());
			} else if(tds[j].contains("企业名称")) {
				map.put("qymc", temp.xpath("//td[@width='30%']/text()").toString());
			} else if(tds[j].contains("统一社会信用代码")) {
				map.put("tyshxydm", tagTrim(tds[j]).replace("统一社会信用代码", "").trim());
			} else if(tds[j].contains("企业类型")) {
				map.put("qylx",tagTrim(tds[j]).replace("企业类型", "").trim());
			} else if(tds[j].contains("人员规模")) {
				map.put("rygm", tagTrim(tds[j]).replace("人员规模", "").trim());
			} else if(tds[j].contains("成立日期")) {
				map.put("clrq", tagTrim(tds[j]).replace("成立日期", "").trim());
			} else if(tds[j].contains("核准日期")) {
				map.put("hzrq", tagTrim(tds[j]).replace("核准日期", "").trim());
			} else if(tds[j].contains("经营状态")) {
				map.put("jyzt", tagTrim(tds[j]).replace("经营状态", "").trim());
			} else if(tds[j].contains("所属行业")) {
				map.put("sshy", tagTrim(tds[j]).replace("所属行业", "").trim());
			} else if(tds[j].contains("营业期限")) {
				map.put("yyqx", tagTrim(tds[j]).replace("营业期限", "").trim());
			} else if(tds[j].contains("企业地址")) {
				map.put("qydz", tagTrim(tds[j]).replace("企业地址", "").trim());
			} else if(tds[j].contains("经营范围")) {
				map.put("jyfw", tagTrim(tds[j]).replace("经营范围", "").trim());
			} else if(tds[j].contains("所属地区")) {
				map.put("ssdq", tagTrim(tds[j]).replace("所属地区", "").trim());
			}
		}
	}

	public static String tagTrim(String str) {
		return str.replaceAll("<.*?>", "").replaceAll(">", "").replaceAll("<.*?\"[\\s\\S]*\"","").replace("<td" ,"");
	}


	/**
	 * 通过 webdriver 的方式采集海洋相关的统一社会信用代码
	 * @throws InterruptedException
	 */
	@Test
	public void testDownLoadhaiyang() throws InterruptedException {
		ChromeDriver driver = ChromeDriverUtils.getDefaultChromeDriver();
		driver.get("https://www.qichacha.com/");
		driver.manage().window().maximize();
		driver.findElement(By.xpath("//input[@id='searchkey']")).sendKeys("船舶");
		Thread.sleep(2000l);
		driver.findElement(By.xpath("//input[@id='V3_Search_bt']")).click();
		ChromeDriverUtils.switchHandle(driver);
		driver.findElement(By.xpath("//a[@class='company-vip-btn btn btn-danger m-t']")).click();
		ChromeDriverUtils.switchHandle(driver);
		driver.findElement(By.xpath("//div[@class='text-center']/a[@class='btn-wx-d']")).click();
		ChromeDriverUtils.switchHandle(driver);
		Thread.sleep(15000l);
		ChromeDriverUtils.switchHandle(driver);
		System.out.println("success");
		driver.findElement(By.xpath("//input[@id='searchkey']")).sendKeys("海运");
		Thread.sleep(2000l);
		driver.findElement(By.xpath("//input[@id='V3_Search_bt']")).click();
		ChromeDriverUtils.switchHandle(driver);

		for(int i = 0; i < 6; i++) {
			System.out.println("第" + (i + 1) + "页数据!!!");

//			ChromeDriverUtils.switchHandle(driver);
			List<WebElement> elements = driver.findElements(By.xpath("//tbody[@id='search-result']/tr/td[3]/a"));

//			System.out.println(elements.size());
			// 解析每一页的数据
			if(null != elements && elements.size() > 0) {
				for(WebElement element : elements) {
					try {
						element.click();
						ChromeDriverUtils.switchHandle(driver);
						WebElement content = driver.findElement(By.xpath("//table[@class='ntable']"));
						WebElement title = driver.findElement(By.xpath("//div[@class='row title jk-tip']//h1"));
						System.out.println("<h1 id=\"title\">" + title.getText() + "</h1>" + content.getAttribute("innerHTML"));
						driver.close();
						Set<String> windows = driver.getWindowHandles();
						driver.switchTo().window((String) windows.toArray()[windows.size() - 1]);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			driver.findElement(By.xpath("//ul[@class='pagination pagination-md']/li[7]//a[1]")).click();
			Thread.sleep(5000l);
//			System.out.println(driver.getPageSource());

			/*nextPage.click();
			Set<String> windows = driver.getWindowHandles();
			driver.switchTo().window((String) windows.toArray()[windows.size() - 1]);
			Thread.sleep(2000l);*/

		}


	}

	// 微信采集最后添加一个闭合的 div 标签
	@Test
	public void testAddCloseTag() {
		Boolean replaceFlag = true;
		String key = "";
		while(replaceFlag) {
// 中间部分替换
			Matcher m = Pattern.compile("</w:t></w:r>(([^w:r][^\\s])*?)<w:r").matcher(key);
//					Set<String> contentReplace = new HashSet<>();
			Boolean innerFlag = true;
			while(m.find() && innerFlag) {
				System.out.println(m.group(2));
				if(!StringUtils.isEmpty(m.group(2))) {
					String c = m.group(1);
					key = key.replace(c, "<w:r><w:rPr><w:rFonts w:ascii=\"仿宋\" w:fareast=\"仿宋\" w:h-ansi=\"仿宋\" w:cs=\"仿宋\" w:hint=\"fareast\"/><wx:font wx:val=\"仿宋\"/><w:sz w:val=\"28\"/><w:sz-cs w:val=\"28\"/></w:rPr><w:t>"+ c +"</w:t></w:r>");
//							contentReplace.add(m.group(1));
					innerFlag = false;
					replaceFlag = m.find();
				}
			}
		}
		System.out.println(key);
	}


	// 采集信息格式化，将section标签替换为 p 标签
	@Test
	public void testSection2P() {
		String text = "<div class=\"rich_media_content \" id=\"js_content\">  </p>";
		if(text.contains("<section")) {
			text = text.replaceAll("<section([^>]*?)>", "<p>").replace("</section>","</p>");
		}
		Matcher spaceM = Pattern.compile("<p>[([\\s]*?)<p>([\\s]*)]{0,}<p>").matcher(text);
		Matcher spaceM2 = Pattern.compile("</p>[([\\s]*?)</p>([\\s]*)]{0,}</p>").matcher(text);
		while(spaceM.find()) {
			System.out.println(spaceM.group() + "-----------------");
			text = text.replaceAll(spaceM.group(), "<p>");

		}
		while(spaceM2.find()) {
			System.out.println(spaceM2.group() + "================");
			text = text.replaceAll(spaceM2.group(), "</p>");
		}
		System.out.println("[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[");
		System.out.println(text);

 	}

	// 判断某个元素是否存在
	public boolean doesWebElementExist(WebDriver driver, String xpath)
	{

		try
		{
			driver.findElement(By.xpath(xpath));
			return true;
		}
		catch (NoSuchElementException e)
		{
			return false;
		}
	}



	@Test
	public void testDownladCSRCANDInsert2DB() throws Exception {
		System.setProperty("webdriver.chrome.driver", "chromedriver.exe");
		ChromeOptions options = new ChromeOptions();
		ChromeDriver driver = new ChromeDriver(options);
		driver.manage().window().maximize();
		String startUrl = "https://csrc.nist.gov/News";
		String domain = HtmlFormatter.getDomain(startUrl);
		driver.get(startUrl);
//		WebElement wait = (new WebDriverWait(driver,20))
//				.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='col-sm-12 news-list-title']/strong/a")));
		List<WebElement> elements = driver.findElements(By.xpath("//div[@class='col-sm-12 news-list-title']/strong/a"));
		if(elements.size() > 0) {
			for(WebElement element : elements) {
				ChromeDriver tempdriver = null;
				try {
					String href = element.getAttribute("href");
					// 真实链接
					String realDownloadUrl = HttpFileUtil.getRealDownloadUrl(href, domain, startUrl);
					// 点击链接
					tempdriver = new ChromeDriver();
					System.out.println("Current url: " + realDownloadUrl);
					tempdriver.get(realDownloadUrl);
//					WebElement tempwait = (new WebDriverWait(tempdriver,20))
//							.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//a[@data-csrc-pub-link='true']")));
					WebElement helpEle = tempdriver.findElement(By.xpath("//a[@data-csrc-pub-link='true']"));
					if(doesWebElementExist(tempdriver, "//h1[@id='news-title']/font/font")) {
						String title = tempdriver.findElement(By.xpath("//h1[@id='news-title']")).getText();
						System.out.println(title);
					}
					if(doesWebElementExist(tempdriver, "//div[@id='news-content']")) {
						String content = tempdriver.findElement(By.xpath("//div[@id='news-content']")).getText();
						System.out.println(content);
					}
					if(doesWebElementExist(tempdriver, "//small[@id='news-date']/font/font")) {
						String pubdate = tempdriver.findElement(By.xpath("//small[@id='news-date']")).getText();
						pubdate = HtmlFormatter.convertPubDate(pubdate);
						System.out.println(pubdate);
					}

					String text = helpEle.getText();
					System.out.println(text);
					if(null != helpEle) {
						helpEle.click();
						Set<String> windowHandles = tempdriver.getWindowHandles();
						for (String windowhandle : windowHandles) {
							if (windowhandle != tempdriver.getWindowHandle()) {
								tempdriver.switchTo().window(windowhandle);
							}
						}
						WebElement targetEle = tempdriver.findElement(By.xpath("//a[contains(text(),'Local Down')]"));
//						WebElement titleEle = tempdriver.findElement(By.xpath("//h3[@id='pub-header-display-container']/span/font/font"));
						String filename = "";
//						if(titleEle != null) {
//							filename = titleEle.getText();
//						}
						if(null != targetEle) {
							String pdfHref = targetEle.getAttribute("href");

							String index = elements.indexOf(element) + 1 + "";
							if(!"".equals(filename)) {
								index = index + "" + filename;
							}
							new HttpFileUtil().getFileTo(pdfHref, "D:/"+index+".pdf", null);
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					tempdriver.quit();
				}
			}

		}
		driver.quit();

	}

	@Test
	public void testwewewewe() throws IOException {
		new HttpFileUtil().getFileTo("https://nvlpubs.nist.gov/nistpubs/SpecialPublications/NIST.SP.1800-7.pdf", "D:/"+123123132+".pdf", null);
	}

	@Test
	public void testNanoTime() {
		String text = "<div class=\"rich_media_content \" id=\"js_content\">  <p  data-mpa-powered-by=\"yiban.io\"></p>    <section >   <span ><span >作者：</span></span>   <span ><span >军鹰智库望天狼，来源：</span></span>   <span ><span >军鹰资讯</span></span>   </p><p>  </section>    <section >   <span >军事基地是指驻扎一定数量的武装力量，进行特定的军事活动，建有相应的组织机构和设施的地区。</span>   <span >二战后，美国在海外建立的军事基地最多时达2000多个。</span>   <span >本世纪初，美海外军事基地仍有374个，分布在140多个国家和地区。</span>   <span >这些军事基地在美国全球战略中发挥了重要作用。</span>   <span ><span ></span></span>  </section>    <p><img src=\"${RootPath}/weixin.sogou.com/image/6f8a348f194132d61f05b95f82dc5a43.jpeg\"></p>      <section >   <span >美军驻日本的基地是1945年美军占领日本后建立的，是二战的产物之一，至今仍是美军全球战略中重要一环，基地存续是美日军事同盟的重要基础。</span>   <span >1951年签订了《日美安全保障条约》。</span>   <span >该条约规定日本承认美国在日本驻扎陆海空军；</span>   <span >驻日美军为维护远东和平与保障日本的安全，应日本请求，可以用武力镇压内乱和暴动，以及对付外来的武力攻击；</span>   <span >美军驻扎日本的条件依照日美两国间的行政协定执行。</span>   <span >1960年又签订了《新日美安全保障条约》。</span>   <span >该协定额外增加了驻日美军及军属在引发刑事案件，只有“在检查当局起诉之后”，基地方面方可交出嫌疑犯等特权。</span>   <span ><span ></span></span>  </section>    <section >   <span >驻日美军基地是二战后“美日安保条约”的产物，也是美军在东亚地区进行兵力部署的主要阵地。</span>   <span >如今美国在日本约有140个军事基地,分散在</span>   <span >日本列岛</span>   <span >的各个地方。</span>   <span >这些基地与美军在韩国所部属的基地互相呼应，控制了</span>   <span >宗谷海峡</span>   <span >、</span>   <span >津轻海峡</span>   <span >、</span>   <span >对马海峡</span>   <span >，可应对</span>   <span >朝鲜半岛</span>   <span >的陆战与</span>   <span >西北太平洋</span>   <span >的海战，构成美国东亚“</span>   <span >岛链</span>   <span >”中最重要的一环。</span>  </section>    <section >   <span >第七舰队的“左膀右臂”———横须贺、佐世保</span>  </section>    <section >   <span >横须贺舰队基地司令部位于东京湾畔，是美在西太平洋最主要的海军基地设施，与日</span>本海上自卫队合用，是美海军第七舰队司令部所在地。第七舰队以此为母港，游弋于太平洋及印度洋。美国“小鹰”号航空母舰驻守在这里，另有8艘舰只组成的护航舰队。横须贺除了具备入坞设施外，还具备一流的船只修理设施、具有战略意义的燃料和弹药库。  </section>      <section >   <span >佐世保舰队基地司令部位于九州岛西北角，作为美在日本的第二大海军基地，由美军与日本海上自卫队合用，也是美军前沿部署部队的主要后勤保障基地。</span>   <span >美第七舰队的旗舰“蓝岭”号、“贝劳伍德”号两栖攻击舰，就以此为驻地。</span>  </section>    <section >   <span >驻日美军的“心脏”———横田、座间</span>  </section>    <section >   <span >横田与座间是驻日美军司令部所在地，其中横田还是驻日美军第五航空队的司令部驻地。</span>   <span >横田基地横跨首都东京的五市一町，是驻日美军的第二大军事基地。</span>  </section>    <section >   <span >座间兵营位于东京西南16公里处，是驻日美陆军司令部所在地和陆军第九战区支援司令部所在地，任务是为驻防西太平洋的陆军提供战斗勤务支援。</span>  </section>    <section >   <span >驻日美军的“兵营”———冲绳岛</span>  </section>    <section >   <span >冲绳岛位于琉球群岛，这个东亚海上交通要冲，是驻日美军人数最多的地方(约有2.8万名现役官兵驻防)，1972年前一直由美国控制，美军现占用该岛18%的陆地面积。</span>  </section>    <section >   <span >基地面积占全部驻日美军基地的75%，在此建有各种军事设施共39处。</span>   <span >从军种看，海军和海军陆战队占70%，指挥他们的是美国第三海军陆战队远征军司令部，这支部队是美本土以外的一支可以“从夏威夷到非洲好望角作出快速反应的部队”。</span>  </section>    <section >   <span >空中“警察”的“休息”地———三泽</span>  </section>    <section >   <span >三泽位于东京东北644公里处，是美在日本北部的一个空军基地。</span>   <span >如今该基地部队正由防御型转变成为“打击对方防空体系”的进攻型部队，其职能范围已由亚太地区拓展到世界上所有发生纠纷的地区。</span>   <span ><span ></span></span>  </section>    <p><img src=\"${RootPath}/weixin.sogou.com/image/86150382ce8016ba5c18049d80f490fb.jpeg\"></p>        <section >   <span >驻日美军在布局军事基地的重要位置美建立军事基地需考虑地理位置、自然条件、设施条件和政治条件等几个方面，选址颇为精心。</span>   <span >目前美军事基地布局的主要特点是;以本土基地为核心，以海外基地为前沿，点线结合。</span>   <span >既重视前沿基地，又重视战略运输线上的中间基地以及后方基地。</span>   <span >“前沿少量存在，本土重兵机动”。</span>   <span >控制战略要点，扼守海上咽喉。</span>   <span >目前美国在日本比较重要的军事基地有：</span>  </section>    <section >   <span >陆军：</span>  </section>    <section >   <span >座间兵营，位于东京西南40公里处，是驻日陆军司令部所在地和陆军第9战区陆军区</span>   <span ><span ></span></span>  </section>    <p><img src=\"${RootPath}/weixin.sogou.com/image/030824b942d64451b58c8bbe676060ca.jpeg\"></p>    <section >   <span >域司令部所在地，2007年12月美陆军第1军在这里设立新司令部。</span>  </section>      <section >   <span >海军：</span>  </section>    <section >   <span >横须贺基地，位于东京西南50公里神奈川县东京湾畔，是美海军第7舰队司令部驻地和“乔治·华盛顿”号航母战斗群的母港。</span>   <span ><span ></span></span>  </section>    <p><img src=\"${RootPath}/weixin.sogou.com/image/7f34d89a35023f66ee8e1c1e9102c1bd.jpeg\"></p>      <section >   <span >佐世保基地，位于九州岛西北角，是美两栖舰艇部队的常驻地和攻击型两栖舰队的出击基地，也是美军前沿部署部队的主要后勤保障基地。</span>  </section>    <section >   <span >海军陆战队：</span>  </section>    <section >   <span >岩国基地，位于本州岛最南端，是陆战队第3远征部队主力航空部队的驻地。</span>   <span >冲绳的巴特勒陆战队营地驻有陆战队第3远征部队（含第1航空联队、第3陆战师和第3勤务支援大队）和驻冲绳舰队司令部。</span>  </section>    <section >   <span >冲绳的普天间机场是美海军陆战队在日本最大规模的武装直升机机场，位于普天间市中心，因周边居民一直对基地带来的噪声、事故、美军犯罪等问题多有不满，2006年日美达成协议，同意将普天间基地转移至位于冲绳县名护市的施瓦布军营沿岸地区。</span>  </section>    <section >   <span >空军：</span>  </section>    <section >   <span >横田基地，这里没有作战机群，是美国空军和其他部队进行联络的中心和军事空运司令部的所在地，也是驻日美军司令部的驻地。</span>   <span ></span>  </section>    <p><img src=\"${RootPath}/weixin.sogou.com/image/78071490e14c642fd536e60284c20544.jpeg\"></p>      <section >   <span >冲绳嘉手纳空军基地，位于冲绳岛西南部，面积近20平方公里，是美国本土以外最大的具有快速反应能力的空军基地，也是美国宇航局指定的航天飞机紧急着陆场，驻有美空军第5航空队第18航空联队、空军第390情报中队、第82侦察机中队和353特种作战大队等部队。</span>   <span ><span ></span></span>  </section>    <p><img src=\"${RootPath}/weixin.sogou.com/image/ad0db36d1406e52caacebe473794ed33.jpeg\"></p>  </p><p>    <section >   <span >三泽基地，位于东京东北644公里，驻有美空军第35战斗机联队和海军海上巡逻机中队，美国空军第6920电子保安团、海军的通信保安团、海军陆战队的支援团E连和陆军第500军事谍报团也部署在这里。</span>   <span >由于三泽基地内美国四军谍报部队齐聚，通信、电子谍报等机密活动集中，因而也被称作美国设在太平洋东岸的谍报站。</span>   <span ><span ></span></span>  </section>    <p><img src=\"${RootPath}/weixin.sogou.com/image/0b97183769bece5e56c28e8eeb4f2ab4.jpeg\"></p>      <section >   <span >曾有在冲绳旅游的中国网友造访了嘉手纳空军基地和那霸基地，拍摄到了其中的大量军事武器装备。</span>   <span >这两个基地因其地理位置特殊，被认为是美日对华最前沿的军事基地，在这里部署的就有美国最先进的F-22第五代战斗机。</span>   <span ><span ></span></span>  </section>      <p><img src=\"${RootPath}/weixin.sogou.com/image/e04508df0fe84da899608d3c64f61e53.jpeg\"></p>    <section >   <span >对于美军基地和基地所拥有的军事特权，长期以来日本国内的反对声一直不绝于耳。</span>   <span >但是，包括执政党和最大在野党在内的大多数政治势力和日本市民还是容忍、甚至支持驻日美军基地的存在。</span>   <span >对于这种情况，日本前首相小泉的一句“名言”道出了“天机”：</span>   <span >“中美日之间不是等边三角形的关系，日美关系比什么都重要，只有日美建立了牢不可破的同盟关系，才有可能面对面地与中韩打交道。</span>   <span >”</span>   </p><p>  </section>    <section >   <span >几十年的经营，日本本土的海陆空、后勤、训练、中转、指挥、通讯等各种功能的基地，已经构筑了美国在亚太地区完整的基地体系，并拥有了强大的军事能力。</span>  </section>    <section >   <span >日美同盟在东亚安全格局中的地位不言自明。</span>   <span >特别是在美国战略重心重回亚太的今日，日美同盟体制下的驻日美军基地，将会更多地出现在中国安全的视野中。</span>  </section>    </p>";


		Matcher m = Pattern.compile("(.*?)<[^><*?]>([^\\s]*)</[^><*?]>(.*?)").matcher(text);
		while(m.find()) {
			System.out.println(m.group());
		}

	}

	/**
	 * 下载 cryptome 的附件
	 * @throws Exception
	 */
	@Test
	public void testDownloadPdfCyptome() throws Exception {
		//
		System.setProperty("webdriver.chrome.marionette", "chromedriver.exe");
		ChromeOptions options = new ChromeOptions();
		// 获取 ip 代理
		String ip = new XXNetProxy().getProxy_ip() + ":" + new XXNetProxy().getProxy_port();
		options.addArguments("--proxy-server=http://" + ip);
		// 创建平台的窗口
		WebDriver driver = new ChromeDriver();
		driver.manage().window().maximize();
		driver.get("http://www.cryptome.org/");
		Thread.sleep(10000l);
		List<WebElement> elements = driver.findElements(By.xpath("//b/a"));
		List<String> result = new ArrayList<>();
		for(WebElement element : elements) {
			String href = element.getAttribute("href");
			String text = element.getText();
//			System.out.println(href);
			if(!StringUtils.isEmpty(href)) {
				if(href.endsWith(".pdf") || href.endsWith(".zip")) {
//					result.add(text + "&&&" + href);
					try {
						HttpFileUtil.getInstance().getRemote2LocalWithProxy(href, "F:/111/" + text, new XXNetProxy());
					} catch (Exception e) {
						e.printStackTrace();
					}
//					driver.findElements(By.xpath("//b/a")).get(elements.indexOf(element)).click();
//					Thread.sleep(50000l);
				}
			}

		}
		System.out.println("=============================");
		System.out.println(result);
	}

	// 字符转url编码为BASE64编码的代码以及反编译
	@Test
	public void testUrlHanzi() throws UnsupportedEncodingException {
		String name = "%E9%9B%B7%E8%BE%BE%E9%80%9A%E4%BF%A1%E7%94%B5%E5%AD%90%E6%88%98";
		name = new String(name.getBytes("ISO-8859-1"), "GBK");
		System.out.println(URLDecoder.decode("%E9%9B%B7%E8%BE%BE%E9%80%9A%E4%BF%A1%E7%94%B5%E5%AD%90%E6%88%98", "UTF-8"));

		String curUrl = "http://weixin.sogou.com/weixin?type=1&s_from=input&ie=utf8&query=%E9%9B%B7%E8%BE%BE%E9%80%9A%E4%BF%A1%E7%94%B5%E5%AD%90%E6%88%98&_sug_=n&_sug_type_=&w=01019900&sut=4926&sst0=1553594312076&lkt=0,0,0";
		Matcher m = Pattern.compile("(.*?)query=(.*?)&(.*?)").matcher(curUrl);
		if(m.find()) {
			System.out.println(URLDecoder.decode(m.group(2)));
		}
	}

	@Test
	public void testDownWx() throws IOException {

		String imgStr = "<img data-backh=\"284\" data-backw=\"530\" data-before-oversubscription-url=\"https://mmbiz.qpic.cn/mmbiz_jpg/ibnjndiconBnmfcT5poVKdE170zHMjMXrv4cMU43x71gicZVfltou1DzTCia20WogN76tSBjnlw0CVicJoO8MNm4WEg/?wx_fmt=jpeg\" data-croporisrc=\"${RootPath}/weixin.sogou.com/image/1962e0b7885096478157b71ae8e57afb.jpeg\" data-type=\"jpeg\" data-w=\"1021\" >";
		Matcher m = Pattern.compile("(.*?)(src=\"\\$\\{RootPath\\}/(.*?)\")(.*?)").matcher(imgStr);
		if(m.find()) {
			System.out.println(m.group(2));
		}

	}

	@Test
	public  void testPython() {
		// C:\Users\wgshb\PycharmProjects\HelloPython\venv\Include
		Process proc = null;
		try {
			proc = Runtime.getRuntime().exec("python C:\\Users\\wgshb\\PycharmProjects\\HelloPython\\venv\\Include\\wx.py");
			proc.waitFor();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testChromeDriver() {
		WebDriver driver = new ChromeDriver();
		driver.manage().window().maximize();
		driver.get("https://mp.weixin.qq.com/");
		String user = "wanggaosheng@bestdatatech.com";
		String password = "wang13693146553";
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("正在输入微信公众号登录账号和密码......");
		// 清空账号框中的内容
		driver.findElements(By.xpath("//*[@id=\"header\"]/div[2]/div/div/form/div[1]/div[1]/div/span/input")).clear();
    	// 自动填入登录用户名
		WebElement usernameEl = driver.findElement(By.xpath("//*[@id=\"header\"]/div[2]/div/div/form/div[1]/div[1]/div/span/input"));
		usernameEl.sendKeys(user);
		// 清空密码框中的内容
		driver.findElements(By.xpath("//*[@id=\"header\"]/div[2]/div/div/form/div[1]/div[2]/div/span/input")).clear();
    	// 自动填入登录密码
		WebElement passwordEl = driver.findElement(By.xpath("//*[@id=\"header\"]/div[2]/div/div/form/div[1]/div[2]/div/span/input"));
		passwordEl.sendKeys(password);

		// 自动输入完毕之后手动点一下记住我
		System.out.println("请在登录界面点击:记住账号");
		try {
			Thread.sleep(10000l);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	
		// 自动点击登录按钮进行登录
		driver.findElement(By.xpath("//*[@id=\"header\"]/div[2]/div/div/form/div[4]/a")).click();
    	// 拿手机扫二维码！
		System.out.println("请拿手机扫码二维码登录公众号");
		try {
			Thread.sleep(20000l);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("登录成功");
		// 重新载入公众号登录页，登录之后会显示公众号后台首页，从这个返回内容中获取cookies信息
		driver.get("https://mp.weixin.qq.com/");
    	// 获取cookies
		Map<String, String> cookieMap = new HashMap<>();
		Set<Cookie> cookies = driver.manage().getCookies();
		if(null != cookies) {
			for(Cookie cookie : cookies) {
				cookieMap.put(cookie.getName(), cookie.getValue());
			}
		}

		// ======================获取正文=====================================
		String url = "https://mp.weixin.qq.com";
		HttpGet httpGet = new HttpGet();
//		Map<String,String> headers = new HashMap<>();
//		headers.put("HOST", "mp.weixin.qq.com");
//		headers.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:53.0) Gecko/20100101 Firefox/53.0");

		httpGet.setHeader("HOST", "mp.weixin.qq.com");
		httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:53.0) Gecko/20100101 Firefox/53.0");



		CloseableHttpClient client = HttpClients.createDefault();
		CloseableHttpResponse response = null;
		try {
			response = client.execute(httpGet);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(response);
		System.out.println();


	}

	@Test
	public void testdsdsddsdsd() {
		String table = "<table id=\"satdata\" class=\"data\">  <tbody>   <tr>    <th class=\"lhead\">Nation:</th>    <td class=\"rcont\" id=\"sdnat\">USA, China</td>   </tr>   <tr>    <th class=\"lhead\">Type / Application:</th>    <td class=\"rcont\" id=\"sdtyp\">Communication</td>   </tr>   <tr>    <th class=\"lhead\">Operator:</th>    <td class=\"rcont\" id=\"sdope\">Loral Skynet → Telesat, APT Satellite Company Ltd.</td>   </tr>   <tr>    <th class=\"lhead\">Contractors:</th>    <td class=\"rcont\" id=\"sdcon\">Space Systems/Loral (SS/L)</td>   </tr>   <tr>    <th class=\"lhead\">Equipment:</th>    <td class=\"rcont\" id=\"sdequ\">38 C-band, 16 Ku-band transponders</td>   </tr>   <tr>    <th class=\"lhead\">Configuration:</th>    <td class=\"rcont\" id=\"sdcnf\">SSL-1300</td>   </tr>   <tr>    <th class=\"lhead\">Propulsion:</th>    <td class=\"rcont\" id=\"sdpro\">?</td>   </tr>   <tr>    <th class=\"lhead\">Power:</th>    <td class=\"rcont\" id=\"sdpow\">2 deployable solar arrays, batteries</td>   </tr>   <tr>    <th class=\"lhead\">Lifetime:</th>    <td class=\"rcont\" id=\"sdlif\">13 years</td>   </tr>   <tr>    <th class=\"lhead\">Mass:</th>    <td class=\"rcont\" id=\"sdmas\">4640 kg</td>   </tr>   <tr>    <th class=\"lhead\">Orbit:</th>    <td class=\"rcont\" id=\"sdorb\">GEO</td>   </tr>  </tbody> </table>";


		String[] split = table.split("</tr>");
		for(String str : split) {
			System.out.println("-------------------------------------");
			Matcher m = Pattern.compile("(.*?)<th([^>]*?)>(.*?)</th>(\\s*)<(td[^>]*?)>(.*?)</td>").matcher(str);
			if(m.find()) {
//				System.out.println(m.group(3) + m.group(6));
				if(m.group(3).replace(" ","").equalsIgnoreCase("Type/Application:")) {
					System.out.println(m.group(6));
				}
				if(m.group(3).replace(" ","").equalsIgnoreCase("Power:")) {
					System.out.println(m.group(6));
				}
			}
			System.out.println("===================================");
		}

	}

	public IP getIpProxy() {
		if(null == ipProxy || (ipProxy.getExpireTime() < System.currentTimeMillis()) || null == ipProxy.getIp() || null == ipProxy.getPort()) {
			ipProxy = IPProxyUtils.getIP();
		}
		return ipProxy;
	}

	@Test
	public void testdadsdsdsd() throws IOException {
		ZipFile zf = new ZipFile("D:\\train.zip");
		long size = 0;
		System.out.println(getTotalFileOnly("D:\\Users\\anhuifeng\\volumes\\root\\upload\\20190719084030774"));
	}

	public static Integer getTotalFileOnly(String targetPath) {
		int total = 0;
		File uploadDirectory = new File(targetPath);
		if(!uploadDirectory.exists() || uploadDirectory.listFiles().length == 0) {
			return total;
		}
		File[] files = uploadDirectory.listFiles();
		boolean dirFlag = false;
		for(File f : files) {
			if(f.isDirectory()) {
				dirFlag = true;
				break;
			}
		}
		if(!dirFlag) {
			// 目录结构不符合要求，返回!
			return total;
		}

		for(File dir : files) {
			if(dir.isFile()) {
				continue;
			}
			File[] files1 = dir.listFiles();
			for(File corpus : files1) {
				if(corpus.isDirectory()) {
					continue;
				}
				total++;
			}
		}
		return total;
	}

	/**
	 * 测试 jsonpath
	 * @throws ParseException 
	 */
	@Test
	public void testJsonpath() throws ParseException, IOException {
		String downurl = "http://124.193.230.159/vhot2.qqvideo.tc.qq.com/AY8Twj5Ai0DfGZ8HA1WzBqYRHgUreGRIeS0dL6zAfheg/uwMROfz2r5zAoaQXGdGnC2dfDma7NyshNhpHvcEisM-VRrjn/p0355qglths.mp4?sdtfrom=v1104&guid=0abfd512ea26979cbb990cc8b695028f&vkey=C0AC7CAE88620B75D27B975C00D86687AC7DA3799C0D32A85274DE201C854730FDF919AA4A6928C122CA8F658F2F3D03F8E4BD1DA09A4921C14078F9CE21773FECB5F0AA15B108CF277D3D05C3FB96DFEA663EF51A5F757ABD055F19BC6242AA195D2D0975E2384AA3DBA07291E97240DA6B337D457F3FA59AF0FAF6A1816334";
		new HttpFileUtil().getVideoFileTo(downurl, "D:/123123.mp4");
	}

	/**
	 * 微信采集的表格信息的保留
	 */
	@Test
	public void testLeaveTableStyle() {
		String[] indexes = {"我们的太空","装备科技","卫星与网络","国防科技要闻","航小宇","雷达通信电子战","学术plus","电科小氙","战略前沿技术","中国指挥与控制学会","国科环宇","科罗廖夫的军事客厅","泰伯网","中国卫星导航定位应用管理中心","航天长城","太空网","知远战略与防务研究所","卫星界","一体化信息网络","中国航天科技集团","电科防务研究","卫星应用","中国载人航天","军工黑科技","交通安全应急国家工程实验室","国际太空","航天防务","雷曼军事现代舰船","漫步宇宙","晨读航天","中国航天科普","宇航探索局","中国航天","小火箭","DeepTech深科技","装备参考","无人争锋","空天大视野","网信科技前沿","海洋防务前沿","国际电子战","北国防务","军评陈光文","战略网军事","中国北斗卫星导航系统","军尚科技","铁索寒","星际智汇","蓝海星智库","微波射频网","中国新一代人工智能","电波之矛"};
		for(int i = 0; i < indexes.length; i++) {
			System.setProperty("webdriver.chrome.marionette", "chromedriver.exe");
			WebDriver driver = new ChromeDriver();
			driver.manage().window().maximize();
			driver.get("https://weixin.sogou.com/weixin?type=1&s_from=input&query="+indexes[i]+"&ie=utf8&_sug_=n&_sug_type_=&w=01019900&sut=4926&sst0=1553594312076&lkt=0%2C0%2C0");

			driver.findElement(By.xpath("//p[@class='tit']/a")).click();

			String currentWindow = driver.getWindowHandle();
			Set<String> handles = driver.getWindowHandles();
			Iterator<String> it = handles.iterator();
			while (it.hasNext()) {
				String handle = it.next();
				if (currentWindow.equals(handle))
					continue;
				driver.switchTo().window(handle);
			}

			List<WebElement> elements = driver.findElements(By.xpath("//h4[@class='weui_media_title']"));
			System.out.println("====================="+ indexes[i] +"=================================");
			for(WebElement e : elements) {

				System.out.println(e.getAttribute("hrefs"));

			}

			System.out.println();

			try {
				Thread.sleep(5000L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			driver.quit();
		}

	}

	@Test
	public void crackSearchResultAuthCode() throws IOException {
		WebDriver driver = new ChromeDriver();
		driver.manage().window().maximize();
		driver.get("https://weixin.sogou.com/antispider/?from=%2fweixin%3Ftype%3d1%26query%3d%E6%BC%AB%E6%AD%A5%E5%AE%87%E5%AE%99%26ie%3dutf8%26s_from%3dinput%26_sug_%3dn%26_sug_type_%3d");
		File screenshotAs = ((ChromeDriver) driver).getScreenshotAs(OutputType.FILE);
		CSVUtils.copySingleFile(screenshotAs.getAbsolutePath(), "D:/data/webmagic/weixin.sogou.com/authcode/123.png");

		// 获取验证码的位置
		WebElement element = ((ChromeDriver) driver).findElementById("seccodeImage");
		Point location = element.getLocation();
		Dimension size = element.getSize();
		int left = location.x;
		int top = location.y;
		int width = size.width;
		int height = size.height;

		String cutImage = cutImage("D:/data/webmagic/weixin.sogou.com/authcode/123.png", "D:/data/webmagic/weixin.sogou.com/authcode/", left, top, width, height);
		if(!StringUtils.isEmpty(cutImage)) {
			JSONObject authCode = YzmUtils.getAuthCode(cutImage, "1006");
			System.out.println(authCode);

		}
	}

	public void cutImage(File srcImg, String destImg, int x, int y, int width, int height){
		cutImage(srcImg, destImg, new java.awt.Rectangle(x, y, width, height));
	}

	public String cutImage(String srcImg, String destImg, int x, int y, int width, int height){
		return cutImage(new File(srcImg), destImg, new java.awt.Rectangle(x, y, width, height));
	}

	public String cutImage(File srcImg, String destImgPath, java.awt.Rectangle rect){
		String cutImgPath ="";
		File destImg = new File(destImgPath);
		if(destImg.exists()){
			String p = destImg.getPath();
			try {
				if(!destImg.isDirectory()) p = destImg.getParent();
				if(!p.endsWith(File.separator)) p = p + File.separator;
				long timestamp = System.currentTimeMillis();
				cutImage(srcImg, new java.io.FileOutputStream(p + "cut" + "_" + timestamp + "_" + srcImg.getName()), rect);
				cutImgPath = p + "cut" + "_" + timestamp + "_" + srcImg.getName();
			} catch (FileNotFoundException e) {
				log.warn("the dest image is not exist.");
			}
		}else log.warn("the dest image folder is not exist.");
		return cutImgPath;
	}

	/**
	 * <p>Title: cutImage</p>
	 * <p>Description:  根据原图与裁切size截取局部图片</p>
	 * @param srcImg    源图片
	 * @param output    图片输出流
	 * @param rect        需要截取部分的坐标和大小
	 */
	public void cutImage(File srcImg, OutputStream output, java.awt.Rectangle rect){
		if(srcImg.exists()){
			java.io.FileInputStream fis = null;
			ImageInputStream iis = null;
			try {
				fis = new FileInputStream(srcImg);
				// ImageIO 支持的图片类型 : [BMP, bmp, jpg, JPG, wbmp, jpeg, png, PNG, JPEG, WBMP, GIF, gif]
				String types = Arrays.toString(ImageIO.getReaderFormatNames()).replace("]", ",");
				String suffix = null;
				// 获取图片后缀
				if(srcImg.getName().indexOf(".") > -1) {
					suffix = srcImg.getName().substring(srcImg.getName().lastIndexOf(".") + 1);
				}// 类型和图片后缀全部小写，然后判断后缀是否合法
				if(suffix == null || types.toLowerCase().indexOf(suffix.toLowerCase()+",") < 0){
					log.error("Sorry, the image suffix is illegal. the standard image suffix is {}." + types);
					return ;
				}
				// 将FileInputStream 转换为ImageInputStream
				iis = ImageIO.createImageInputStream(fis);
				// 根据图片类型获取该种类型的ImageReader
				ImageReader reader = ImageIO.getImageReadersBySuffix(suffix).next();
				reader.setInput(iis,true);
				ImageReadParam param = reader.getDefaultReadParam();
				param.setSourceRegion(rect);
				BufferedImage bi = reader.read(0, param);
				ImageIO.write(bi, suffix, output);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if(fis != null) fis.close();
					if(iis != null) iis.close();
					if(output!= null) {
						output.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}else {
			log.warn("the src image is not exist.");
		}
	}

	/**
	 * 微信采集地址跳转, 需要重新获取
	 * @throws ParseException
	 * @throws IOException
	 */
	@Test
	public void testGetRealUrl() {
		HttpClientDownloader downloader = new HttpClientDownloader();
		Html download = downloader.download("https://weixin.sogou.com/antispider/?from=%2fweixin%3Ftype%3d1%26query%3d%E6%BC%AB%E6%AD%A5%E5%AE%87%E5%AE%99%26ie%3dutf8%26s_from%3dinput%26_sug_%3dn%26_sug_type_%3d");
		System.out.println(download);
	}

	@Test
	public void testTargetDateFormat() throws ParseException, IOException {
		String str = "D%3A%2Fdata%2FHJS%2F";
		
		String decode = URLDecoder.decode(str,"UTF-8");
		if(decode.contains("/")) {
			decode = decode.replace("/", File.separator);
		}
//		Runtime.getRuntime().exec("explorer D:\\data\\HJS\\backup\\2019-03-23\\");
		System.out.println(decode);
	}

	/**
	 * 去除文本内部的所有的 h 开头的标签
	 */
	@Test
	public void testRemoveHTag() {
		String str = "<h1 dir=\"ltr\" class=\"small-12 medium-10 medium-offset-1 large-8 large-offset-2 xlarge-7 cell article__title article__title--main u-mb-se\" itemprop=\"headline\" ><span >作者：以色列前外</span><span >长Schlomo Ben-Ami</span></p>  <h2><span >发布时间：</span>";

		Matcher m = Pattern.compile("(<h(\\d)+)(.*?)").matcher(str);
		while(m.find()) {
			str = str.replace(m.group(1), "<p");
		}
		System.out.println(str);
	}

	/**
	 * 读取本地的文件转化为 list 集合并取对应的文件对应的集合的差集...
	 * @throws Exception
	 */
	@Test
	public void testDeleteUrls() throws Exception {
		ArrayList<String> urls = readUrlsText2List("C:\\Users\\wgshb\\Desktop\\urls.txt");
		ArrayList<String> urls2 = readUrlsText2List("C:\\Users\\wgshb\\Desktop\\urls2.txt");
		boolean b = urls.removeAll(urls2);
		System.out.println(b);
		for(String u : urls) {
			System.out.println(u);
		}
	}

	private static ArrayList<String> readUrlsText2List(String FilePath) throws Exception {
		//创建集合对象
		ArrayList<String> array = new ArrayList<String>();
		//创建输入缓冲流对象
		BufferedReader br = new BufferedReader(new FileReader(FilePath));
		String line;
		while((line = br.readLine()) != null) {
			array.add(line);
		}
		//释放资源
		br.close();
		return array;
	}

	@Test
	public void testConvertPubdate() throws ParseException {
		String newText = " <div class=\"mainpic\"><img  src=\"/zh/img/thumbnail.png\" src=\"https://img.kyodonews.net/chinese/public/images/posts/72fa944b20db9cbbe0d56eb257374012/photo_l.jpg\" alt=\"\"></div>";
		if(newText.contains("thumbnail")) {
			Matcher thumbnailMatcher = Pattern.compile("<img(.[^>]*?)((src|SRC)=(\"|\')(.[^>]*?)(\"|\')[^>])*>").matcher(newText);
			while(thumbnailMatcher.find()) {
				String extraGroup = thumbnailMatcher.group();
				Matcher tempMatcher = Pattern.compile("<img(.*?)(src=\"(.*?)thumbnail(.*?)\\.(.*?)\")(.*?)>").matcher(extraGroup);
				if(tempMatcher.find()) {
					System.out.println(tempMatcher.group(2));
					newText = newText.replace(extraGroup, extraGroup.replace(tempMatcher.group(2), ""));
				}
			}
		}
		System.out.println(newText);
	}

	@Test
	public void testDeleteBeforePacket() {
		String html = "<html>\n" +
				" <head></head>\n" +
				" <body>\n" +
				"  <div itemprop=\"articleBody\"> \n" +
				"   <h1>Orbit unveils newest dual-band maritime satcom solution</h1> \n" +
				"   <p><strong>(26 February 2019 - Orbit Communication Systems) Orbit Communication Systems unveiled today its latest dual-band Ku/Ka terminal extending Orbit’s multi-band maritime satellite communications solutions.</strong></p> \n" +
				"   <p>Developed in close cooperation with SES Networks, Orbit’s dual-band Ku/Ka terminal augments the existing dual-band C/Ka terminal, supporting a multi-band terminal product range. The expansion of the frequency ranges and flexibility of the OceanTRx terminal enables ease of switching between SES’s multi-orbit Geostationary (GEO) and Medium Earth Orbit (MEO) satellites.</p> \n" +
				"   <p style=\"text-align: center;\"><img style=\"margin: 10px;\" src=\"/images/2019-february/orbit-1.jpg\" alt=\"orbit 1\"></p> \n" +
				"   <p style=\"text-align: center;\"><em>Cruise ship antenna (courtesy: Royal Caribbean)</em></p> \n" +
				"   <p>“Orbit is a long-term technology partner for SES. Over the last 4 years we have become the leading provider of high-speed satellite terminals to large class cruise vessels, and have enabled SES Networks to deliver high-speed broadband services to its customers’ passengers and crew,” said Ben Weinberger, CEO of Orbit. “This newest compact terminal can be easily installed and will be able to offer outstanding performance that is equivalent to a 2.4-meter dish.”</p> \n" +
				"   <p>“Orbit has been a creative and responsive partner as we expand our maritime managed services,” noted Eric Watko, Executive Vice President, Product, Marketing &amp; Strategy of SES Networks. “We value Orbit’s ease of integration and reliability as enablers for our customers to leverage the benefits of the market’s only integrated multi-orbit data services. The new multi-band terminal will enable an even broader range of services via our GEO and MEO satellite system.”</p> \n" +
				"   <p><strong>About Orbit’s OceanTRx 7 Multiband Terminal</strong></p> \n" +
				"   <p>Orbit’s 2.2m (87”) OceanTRx 7 Multiband C/Ka- and Ku/Ka-band stabilized maritime satcom terminals provide high-speed, cost-effective connectivity to Cruise vessels, offshore platforms, and Navy vessels, in even the most severe offshore conditions. This rugged and compact maritime terminal offers outstanding performance for its size, with the equivalent performance of a 2.4m dish contained within a small 2.7m radome footprint – significantly smaller and lighter than alternative systems. The OceanTRx terminal is fully tested and continues to set the standard for ease of integration in half a day. It is also small enough to come fully assembled in a standard 20-foot shipping container.</p> \n" +
				"   <p><strong>About Orbit</strong></p> \n" +
				"   <p>Orbit Communication Systems Ltd. (TASE: ORBI), a leading global provider of airborne communications and satellite-tracking maritime and ground-station solutions, is helping to expand and redefine how we connect. You’ll find Orbit systems on cruise ships and navy vessels, airliners and jet fighters, ground stations and offshore platforms. We deliver innovative, cost-effective, and highly reliable solutions to commercial operators, major navies and air forces, space agencies and emerging New Space companies.</p> \n" +
				"  </div>\n" +
				" </body>\n" +
				"</html>";
		Document document = Jsoup.parse(html);
		Elements elements = Xsoup.select(document, "//div[@itemprop='articleBody']/p[1]]").getElements();
		System.out.println(elements.toString());
	}
	/**
	 * 删除指定文件夹下所有文件
	 * @param path 文件夹完整绝对路径 ,"Z:/xuyun/save"
	 */
	public static boolean delAllFile(String path) {
		boolean flag = false;
		File file = new File(path);
		if (!file.exists()) {
			return flag;
		}
		if (!file.isDirectory()) {
			return flag;
		}
		String[] tempList = file.list();
		File temp = null;
		for (int i = 0; i < tempList.length; i++) {
			if (path.endsWith(File.separator)) {
				temp = new File(path + tempList[i]);
			} else {
				temp = new File(path + File.separator + tempList[i]);
			}
			if (temp.isFile()) {
				temp.delete();
			}
			if (temp.isDirectory()) {
				delAllFile(path + "/" + tempList[i]);//先删除文件夹里面的文件
				delFolder(path + "/" + tempList[i]);//再删除空文件夹
				flag = true;
			}
		}
		return flag;
	}
	/**
	 * 删除文件夹
	 * @param folderPath 文件夹完整绝对路径 ,"Z:/xuyun/save"
	 */
	public static void delFolder(String folderPath) {
		try {
			delAllFile(folderPath); //删除完里面所有内容
			String filePath = folderPath;
			filePath = filePath.toString();
			java.io.File myFilePath = new java.io.File(filePath);
			myFilePath.delete(); //删除空文件夹
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testDownloadPdf() throws IOException {
		String str = "\"rule\": [\n" +
				"\t       \t\t\"xpath(//p[@class='single-header__meta']).get()\"]";
		String[] strArr = str.split("\",");
		for(String s : strArr) {
			System.out.println(s);
		}
	}

	@Test
	public void testTemp() {
		System.setProperty("webdriver.chrome.marionette", "chromedriver.exe");
		// ChromeOptions chromeOptions = new ChromeOptions();
		// chromeOptions.addArguments("--headless");
		WebDriver driver = new ChromeDriver();
		driver.manage().window().maximize();
		driver.get("https://passport.weibo.cn/signin/login?entry=mweibo&r=https%3A%2F%2Fweibo.cn%2F&backTitle=%CE%A2%B2%A9&vt=");

		try {
			Thread.sleep(2000l);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		driver.findElement(By.id("loginName")).sendKeys("13718757469");
		driver.findElement(By.id("loginPassword")).sendKeys("bestdata@2017");
		driver.findElement(By.id("loginAction")).click();
		Set<org.openqa.selenium.Cookie> cookies = driver.manage().getCookies();
		for(Cookie cookie : cookies) {
			System.out.println(cookie.toString());
		}
		driver.quit();
	}

	/**
	 * HTTP请求
	 * @param surl 接口请求url
	 * @param json 接口请求body-json字符串
	 *  
	 * @return 接口返回结果
	 */
    public static String sendJsonWithHttp(String surl, String json) throws Exception
    {
        URL url = new URL(surl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("Content-Type", "application/json;charset=utf-8");
        conn.setRequestMethod("POST");// 提交模式
        conn.setRequestProperty("Content-Length", json.getBytes().length + "");
        conn.setConnectTimeout(100000);// 连接超时单位毫秒 //
        conn.setReadTimeout(200000);// 读取超时 单位毫秒
        conn.setDoOutput(true);// 是否输入参数
        conn.setDoInput(true);
        conn.setUseCaches(false);
        conn.connect();
        DataOutputStream out = new DataOutputStream(conn.getOutputStream());
        out.write(json.getBytes());
        out.flush();
        out.close();
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuffer sb = new StringBuffer();
        String line;
        while ((line = reader.readLine()) != null)
        {
            sb.append(line);
        }
        reader.close();
        conn.disconnect();

        return sb.toString();
    }
		
	/**
	 * 接口测试
	 * @throws Exception
	 */
	@Test
	public void interFacetest() throws Exception {
		// 请求参数集合
		Map<String, String> map = new HashMap<>();
		map.put("content", "我们之间有太多的误会");
		map.put("num", "2");
		// 请求地址
		String url = "/extractKeyword";
		// 总的请求地址
		String surl = "http://localhost:8081" + url;
		// 拼接请求地址
		if(map.size() == 1) {
			for(Map.Entry<String, String> entry : map.entrySet()) {
				surl = surl + "?" + entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), "utf-8");
			}
		} else if(map.size() > 1) {
			List<String> tempList = new ArrayList<>();
			for(Map.Entry<String, String> entry : map.entrySet()) {
				tempList.add(entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), "utf-8"));
			}
			for(int i = 0; i < tempList.size(); i++) {
				if(i == 0) {
					surl = surl + "?" + tempList.get(i); 
				} else {
					surl = surl + "&" + tempList.get(i);
				}
			}
		}
		// 返回结果
		String result = sendJsonWithHttp(surl, JSONObject.toJSONString(map));
		System.out.println(result);
	}
	
	/**
	 *  多图片不换行的问题尚待解决
	 * @throws IOException
	 */
	@Test
	public void testEditDistance() throws IOException {
		String str = "<p class=\"ql-align-justify\">1987年底，它部署在12艘可装16枚导弹的改装拉法叶级/麦德逊级潜艇(合计192枚)8艘每艘可装24枚导弹<img src=\"http://5b0988e595225.cdn.sohucs.com/images/20190108/0af31ff201564b4db50d88c533b1e580.jpeg\" max-width=\"600\">的俄亥俄级潜艇(合计192枚)。但是后者的数量将会慢慢减少，这是因为这些潜艇将在91年至99年间换装三叉戟二型D-5导弹。装载三叉戟一型的潜艇群分别隶属于大西洋舰队及太平洋舰队。两边的部署都从它的长射程受益不少，也都是以美国本土为基地，从大西洋它可以击中几乎所有俄方目标，少数处于较远区域的目标则由太平洋这边来加以攻击。<img src=\"http://5b0988e595225.cdn.sohucs.com/images/20190108/f060ef9ea732474290167d893c561ba4.jpeg\" max-width=\"600\" /></img></p> \n";
		if(str.contains("<p") && str.contains("<img") && str.replaceAll("<.*?>", "").trim().length() > 0) {
			Matcher m = Pattern.compile("<img(.*?)>").matcher(str);
			while(m.find()) {
				str = str.replace(m.group(), "<br>" + m.group() + "<br>");
			}
		}
		System.out.println(str);
	}

	@Test
	public void testMax() {
		String cnprefix = "<p>————————————————————本文来源于国外网站,以下是机器翻译内容,向下阅读可查看原文————————————————————</p>";
		String enprefix = "<p>————————————————————原文————————————————————</p>";
		String text = "<p>————————————————————本文来源于国外网站,以下是机器翻译内容,向下阅读可查看原文————————————————————</p><p>白皮书草案现在可供评论， 人工智能和机器学习中可解释性的组合方法的应用 。</p><p>这篇简短的论文介绍了一种方法，用于在一些人工智能和机器学习（AI / ML）系统中产生决策的解释或证明，使用从组合测试中的故障定位衍生的方法。 我们证明验证和可解释性问题与组合测试中的故障定位问题密切相关，并且为故障定位开发的某些方法和工具也可以应用于该问题。 这种方法在分类问题中特别有用，其目标是根据集合的特征确定集合中的对象成员资格。 我们使用概念上简单的方案来简化分类决策的合理性：识别出已识别类成员中存在但在非成员中不存在或罕见的特征组合。 该方法已在名为ComXAI的原型工具中实现，并给出了其应用实例。 包括一系列应用领域的示例以显示这些方法的实用性。</p><p>本文件的公众意见征询期于2019年7月3日结束 。 有关论文副本和提交评论的说明，请参阅文档详细信息。</p><p>————————————————————原文————————————————————</p><p>Draft White Paper on Combinatorial Methods for Explainability in AI and Machine Learning</p><p>A draft white paper is now available for comment, An Application of Combinatorial Methods for Explainability in Artificial Intelligence and Machine Learning.</p><p>This short paper introduces an approach to producing explanations or justifications of decisions made in some artificial intelligence and machine learning (AI/ML) systems, using methods derived from those for fault location in combinatorial testing. We show that validation and explainability issues are closely related to the problem of fault location in combinatorial testing, and that certain methods and tools developed for fault location can also be applied to this problem. This approach is particularly useful in classification problems, where the goal is to determine an object’s membership in a set based on its characteristics. We use a conceptually simple scheme to make it easy to justify classification decisions: identifying combinations of features that are present in members of the identified class but absent or rare in non-members. The method has been implemented in a prototype tool called ComXAI, and examples of its application are given. Examples from a range of application domains are included to show the utility of these methods.</p><p>The public comment period for this document ends on July 3, 2019. See the document details for a copy of the paper and instructions for submitting comments.</p>";
		System.out.println(text.substring(cnprefix.length(), text.indexOf(enprefix)));
		System.out.println(text.substring(text.indexOf(enprefix) + enprefix.length(), text.length()));
	}


	// 尝试基于 webdriver 采集美军基地的信息, 不能使, 因为采集的效率过低...
	@Test
	public void testDownloadWiki() {
		System.setProperty("webdriver.chrome.marionette", "chromedriver.exe");
		WebDriver driver = new ChromeDriver();
		driver.manage().window().maximize();
		// 打开起始页
		driver.get("https://zh.wikipedia.org/wiki/Category:%E9%A7%90%E5%A4%96%E7%BE%8E%E8%BB%8D%E5%9F%BA%E5%9C%B0");


		/*WebElement findElement = driver.findElement(By.xpath("//span[@id='pageindex']"));
		String index = findElement.getText();*/

		System.out.println(driver.getPageSource());
		Html h = new Html(driver.getPageSource());

		/*List<String> pagingUrls = new ArrayList<>();
		List<Selectable> selectables = h.xpath("//div[@class='songlist__item']/div[@class='songlist__songname']/span[@class='songlist__songname_txt']/a[@class='js_song']/@href | //div[@class='songlist__item songlist__item--even ']/div[@class='songlist__songname']/span[@class='songlist__songname_txt']/a[@class='js_song']/@href").nodes();
		for(Selectable selectable : selectables) {
			String node = selectable.get();
			if(node != null) {
				if(!pagingUrls.contains(node)) {
					pagingUrls.add(node);
				}
			}
 		}*/
		driver.close();

	}

	// 下载网页到本地
	public static void downloadPage(String str) {
		BufferedReader br = null;
		FileOutputStream fos = null;
		OutputStreamWriter osw = null;
		String inputLine;
		try {
			URL url = new URL(str);
			HttpURLConnection htpcon = (HttpURLConnection) url.openConnection();
			htpcon.setRequestMethod("GET");
			htpcon.setDoOutput(true);
			htpcon.setDoInput(true);
			htpcon.setUseCaches(false);
			htpcon.setConnectTimeout(1000);
			htpcon.setReadTimeout(1000);
			InputStream in = htpcon.getInputStream();

			// 通过url.openStream(),来获得输入流
			br = new BufferedReader(new InputStreamReader(url.openStream(),
					"UTF-8"));

			File file = new File("F:/download.html");
			fos = new FileOutputStream(file);
			osw = new OutputStreamWriter(fos, "utf-8");

			// 将输入流读入到临时变量中，再写入到文件
			while ((inputLine = br.readLine()) != null) {
				osw.write(inputLine);
				// System.out.println(inputLine);
			}

			br.close();
			osw.close();
			System.err.println("下载完毕!");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null && osw != null) {
					br.close();
					osw.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Test
	public void testDownUrl2LocalFile() {
//		String str = "https://zh.wikipedia.org/wiki/%E6%8B%89%E5%A7%86%E6%96%BD%E6%B3%B0%E5%9B%A0%E7%A9%BA%E5%86%9B%E5%9F%BA%E5%9C%B0";
//		downloadPage(str);
		String str = "<img >               <img src=\"D:/data/webmagic/www.militarytimes.com/image/5cd5f337800ae9d2cf4a461c3edcb397.jpeg\">  ";
		System.out.println(str.replaceAll("<img(\\s)*>", ""));

	}

	//链接url下载图片
	private static void downloadPicture(String urlList, String path) {
		URL url = null;
		try {
			url = new URL(urlList);
			DataInputStream dataInputStream = new DataInputStream(url.openStream());

			FileOutputStream fileOutputStream = new FileOutputStream(new File(path));
			ByteArrayOutputStream output = new ByteArrayOutputStream();

			byte[] buffer = new byte[1024];
			int length;

			while ((length = dataInputStream.read(buffer)) > 0) {
				output.write(buffer, 0, length);
			}
			fileOutputStream.write(output.toByteArray());
			dataInputStream.close();
			fileOutputStream.close();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public void WriteListToFile(String filePath, List<String> al) throws IOException {
		File f = new File(filePath);//"F:/novel/badcn.txt"
		BufferedWriter bw = new BufferedWriter(new FileWriter(f));
		for (int i = 0; i < al.size(); i++) {
			bw.write(al.get(i));
			bw.newLine();
		}
		bw.close();
	}

	@Test
	public void testSDsd() {
		String newText = "<p> ddd</p>";
		System.out.println(newText.replace("<br >", "</p><p>").replace("<br>", "</p><p>").replaceAll("<p>\\s*(<(?!img).*?>)\\s*</p>", ""));
	}

	@Test
	public void testSelenium2() throws InterruptedException {
		//System.setProperty("webdriver.firefox.bin","E:\\WorkUtils\\Mozilla Firefox\\firefox.exe"); 
		System.setProperty("webdriver.chrome.marionette", "chromedriver.exe");
		WebDriver driver = new ChromeDriver();
		driver.manage().window().maximize();
		driver.get("https://view46.book118.com/pdf/dXAxNC0yLmJvb2sxMTguY29tLjgwXDczODI3NC01Yjk5ZjkwNTkzMmVkLnBkZg%3d%3d?readpage=%40iasxQYTdPdE25IZPO7BaA%3D%3D&furl=o4j9ZG7fK95dS7wYrAN0MnYqzG1T0OIsJHGIUe54h13R6%40kUibFpZDu3XUEc1hq23Yi2sCfje0EKgTIt2OdA8d6403NK4%40IhUImnlNtiiXk%3D&token=XR21lX5xvPLWdXICpw2ycYIhUk5QF%406K");

		WebElement countEle = driver.findElement(By.xpath("//span[@id='pagecount']"));
		String countStr = countEle.getText();
		int count = Integer.parseInt(countStr);
		System.out.println("count" + count);
		WebElement findElement = driver.findElement(By.xpath("//span[@id='pageindex']"));
		String index = findElement.getText();

		System.out.println(driver.getPageSource());
		Html h = new Html(driver.getPageSource());
		
		/*List<String> pagingUrls = new ArrayList<>();
		List<Selectable> selectables = h.xpath("//div[@class='songlist__item']/div[@class='songlist__songname']/span[@class='songlist__songname_txt']/a[@class='js_song']/@href | //div[@class='songlist__item songlist__item--even ']/div[@class='songlist__songname']/span[@class='songlist__songname_txt']/a[@class='js_song']/@href").nodes();
		for(Selectable selectable : selectables) {
			String node = selectable.get();
			if(node != null) {
				if(!pagingUrls.contains(node)) {
					pagingUrls.add(node);
				}
			}
 		}
		driver.close();
		System.out.println(pagingUrls);
		
		List<String> albumnIds = new ArrayList<>();
		if(pagingUrls.size() > 0) {
			for(String url : pagingUrls) {
				WebDriver temp = new ChromeDriver();
				temp.manage().window().maximize(); 
				temp.get(url);
				//WebElement a = temp.findElement(By.xpath("//div[@class='data__actions']/a[1]"));
				WebElement findElement = temp.findElement(By.className("data__name_txt"));
				//System.out.println(findElement.getAttribute("title"));
				WebElement albumn = temp.findElement(By.xpath("//a[@class='js_album']"));
				String albumnId = albumn.getAttribute("data-albummid");
				String albumnTitle = albumn.getAttribute("title");
				if(!albumnIds.contains(albumnId)) {
					albumnIds.add(albumnId + ":" + albumnTitle);
				}
				System.out.println("---------------> " + albumnIds);
				Thread.sleep(5000);
				
				temp.close();
				//temp.findElement(By.xpath("//div[@class='data__actions']/a[1]")).click();
				//获取对应的专辑的 id 的xpath: li[@class='data_info__item data_info__item--even']/a/@data-albummid
				//WebDriver aDriver = new ChromeDriver();
				//aDriver.manage().window().maximize(); 
				//aDriver.get(a.getAttribute("href"));
				// 获取当前的界面的句柄
				//String windowHandle = temp.getWindowHandle();
				//temp.switchTo().window(windowHandle);
				//System.out.println(temp.getPageSource());
				//List<WebElement> music = temp.findElements(By.xpath("//audio"));
				//for(WebElement ele : music) {
					//System.out.println("-----------------> " + ele.getAttribute("src"));
				//}
				// 获取目标文件的链接, 获取不到啊...
				
				*//**
		 *  转换思路: 歌曲下载的最终的请求的连接其实就是如下:
		 *
		 *  http://dl.stream.qqmusic.qq.com/C400002bChfZ1sw9ed.m4a?guid=7653016346&vkey=E58C1DDC2EDC9E708BE9024333EF4AA2273770874B144566C253115BB5AAEF373CB5A9F6B46894DC6C14BE47122AE2D3281BAF6A15E6F512&uin=8124&fromtag=66
		 *
		 *  对比发现区别仅仅存在于两处:
		 *  	http://dl.stream.qqmusic.qq.com/(songId).m4a?guid=7653016346&vkey=(vkey)&uin=8124&fromtag=66
		 *
		 *  现在找对应的连接的问题转化为找参数的问题?
		 *  	1). 经过查找发现 songId 与请求
		 *  		 https://c.y.qq.com/v8/fcg-bin/fcg_v8_album_info_cp.fcg?albummid=004JF1BR18UKuH&g_tk=558582302&jsonpCallback=albuminfoCallback&loginUin=598130620&hostUin=0&format=jsonp&inCharset=utf8&outCharset=utf-8&notice=0&platform=yqq&needNewCode=0
		 *  			的返回结果 中的参数 songmid 相关, songId = C4000 + songmid...
		 *  		上述的请求需要找到对应的 albummid, 即为对应的专辑的 id , 在播放的界面可以直接获取到
		 *
		 *  	2). 查找 vkey 的值???
		 *  		https://u.y.qq.com/cgi-bin/musicu.fcg?g_tk=558582302&loginUin=598130620&hostUin=0&format=jsonp&inCharset=utf8&outCharset=utf-8&notice=0&platform=yqq&needNewCode=0&data=%7B%22req%22%3A%7B%22module%22%3A%22CDN.SrfCdnDispatchServer%22%2C%22method%22%3A%22GetCdnDispatch%22%2C%22param%22%3A%7B%22guid%22%3A%227653016346%22%2C%22calltype%22%3A0%2C%22userip%22%3A%22%22%7D%7D%2C%22req_0%22%3A%7B%22module%22%3A%22vkey.GetVkeyServer%22%2C%22method%22%3A%22CgiGetVkey%22%2C%22param%22%3A%7B%22songmid%22%3A%5B%22${songmId}%22%5D%2C%22songtype%22%3A%5B0%5D%2C%22uin%22%3A%22598130620%22%2C%22loginflag%22%3A1%2C%22platform%22%3A%2220%22%7D%7D%2C%22comm%22%3A%7B%22uin%22%3A598130620%2C%22format%22%3A%22json%22%2C%22ct%22%3A20%2C%22cv%22%3A0%7D%7D
		 *
		 *  总结:
		 *      现在的问题就转化到了寻找 songmid 了!
		 *      1. 寻找 songmid, 直接走上面的 url 即可!!!
		 *          https://c.y.qq.com/v8/fcg-bin/fcg_v8_album_info_cp.fcg?albummid=004JF1BR18UKuH&g_tk=558582302&jsonpCallback=albuminfoCallback&loginUin=598130620&hostUin=0&format=jsonp&inCharset=utf8&outCharset=utf-8&notice=0&platform=yqq&needNewCode=0
		 *      2. 找到了 songmid , 进行后续的操作...
		 *          这里需要替换的仅仅是请求的 albummid , 这个可以在列表页直接通过 xpath 进行获取到
		 *          1)找到 songmid 先替换上面的查找对应的 vkey 的 url , 返回包含 vkey 的json 字符串, 解析该字符串获取到 vkey;
		 *          2)使用 songmid 替换 songId 的请求, 即为最终的下载的音乐的 url
		 *
		 *//*
				
				
				
				
			}
		}
		System.out.println("---------------> " + albumnIds);*/
	}

	@Test
	public void testReplacedsd() {
		String g = "<p class=\"ql-align-center\"><img src=\"${RootPath}/www.sohu.com/image/af864d7c4be98693abcf2912782435fe.jpeg\" max-width=\"600\">中航工业哈飞直-19E武装直升机</p>";
		Matcher mat = Pattern.compile("<p([^>]*)>([^>]*)<img([^>]*)>(.*?)").matcher(g);
		if (mat.find() && mat.group(2).trim().length() == 0) {
			System.out.println(mat.group(2));
			String pref = g.substring(0, g.replaceFirst(">", "").indexOf(">") + 2);
			String replace = pref + "</p><p>" + g.substring(pref.length(), g.length());
			System.out.println(replace);

		}
	}

	@Test
	public void testSelenium() throws InterruptedException {
		//System.setProperty("webdriver.firefox.bin","E:\\WorkUtils\\Mozilla Firefox\\firefox.exe"); 
		System.setProperty("webdriver.chrome.marionette", "chromedriver.exe");
		WebDriver driver = new ChromeDriver();
		driver.manage().window().maximize();
		driver.get("https://y.qq.com/");
		driver.findElement(By.className("search_input__input")).sendKeys("Black Eyed Peas");
		;
		driver.findElement(By.className("search_input__btn")).click();
		;
		Html h = new Html(driver.getPageSource());

		List<String> pagingUrls = new ArrayList<>();
		List<Selectable> selectables = h.xpath("//div[@class='songlist__item']/div[@class='songlist__songname']/span[@class='songlist__songname_txt']/a[@class='js_song']/@href | //div[@class='songlist__item songlist__item--even ']/div[@class='songlist__songname']/span[@class='songlist__songname_txt']/a[@class='js_song']/@href").nodes();
		for (Selectable selectable : selectables) {
			String node = selectable.get();
			if (node != null) {
				if (!pagingUrls.contains(node)) {
					pagingUrls.add(node);
				}
			}
		}
		driver.close();
		System.out.println(pagingUrls);

		List<String> albumnIds = new ArrayList<>();
		if (pagingUrls.size() > 0) {
			for (String url : pagingUrls) {
				WebDriver temp = new ChromeDriver();
				temp.manage().window().maximize();
				temp.get(url);
				//WebElement a = temp.findElement(By.xpath("//div[@class='data__actions']/a[1]"));
				WebElement findElement = temp.findElement(By.className("data__name_txt"));
				//System.out.println(findElement.getAttribute("title"));
				WebElement albumn = temp.findElement(By.xpath("//a[@class='js_album']"));
				String albumnId = albumn.getAttribute("data-albummid");
				String albumnTitle = albumn.getAttribute("title");
				if (!albumnIds.contains(albumnId)) {
					albumnIds.add(albumnId + ":" + albumnTitle);
				}
				System.out.println("---------------> " + albumnIds);
				Thread.sleep(5000);

				temp.close();
				//temp.findElement(By.xpath("//div[@class='data__actions']/a[1]")).click();
				//获取对应的专辑的 id 的xpath: li[@class='data_info__item data_info__item--even']/a/@data-albummid
				//WebDriver aDriver = new ChromeDriver();
				//aDriver.manage().window().maximize(); 
				//aDriver.get(a.getAttribute("href"));
				// 获取当前的界面的句柄
				//String windowHandle = temp.getWindowHandle();
				//temp.switchTo().window(windowHandle);
				//System.out.println(temp.getPageSource());
				//List<WebElement> music = temp.findElements(By.xpath("//audio"));
				//for(WebElement ele : music) {
				//System.out.println("-----------------> " + ele.getAttribute("src"));
				//}
				// 获取目标文件的链接, 获取不到啊...

				/**
				 *  转换思路: 歌曲下载的最终的请求的连接其实就是如下:
				 *
				 *  http://dl.stream.qqmusic.qq.com/C400002bChfZ1sw9ed.m4a?guid=7653016346&vkey=E58C1DDC2EDC9E708BE9024333EF4AA2273770874B144566C253115BB5AAEF373CB5A9F6B46894DC6C14BE47122AE2D3281BAF6A15E6F512&uin=8124&fromtag=66
				 *
				 *  对比发现区别仅仅存在于两处:
				 *  	http://dl.stream.qqmusic.qq.com/(songId).m4a?guid=7653016346&vkey=(vkey)&uin=8124&fromtag=66
				 *
				 *  现在找对应的连接的问题转化为找参数的问题?
				 *  	1). 经过查找发现 songId 与请求 
				 *  		 https://c.y.qq.com/v8/fcg-bin/fcg_v8_album_info_cp.fcg?albummid=004JF1BR18UKuH&g_tk=558582302&jsonpCallback=albuminfoCallback&loginUin=598130620&hostUin=0&format=jsonp&inCharset=utf8&outCharset=utf-8&notice=0&platform=yqq&needNewCode=0
				 *  			的返回结果 中的参数 songmid 相关, songId = C4000 + songmid...
				 *  		上述的请求需要找到对应的 albummid, 即为对应的专辑的 id , 在播放的界面可以直接获取到
				 *
				 *  	2). 查找 vkey 的值???
				 *  		https://u.y.qq.com/cgi-bin/musicu.fcg?g_tk=558582302&loginUin=598130620&hostUin=0&format=jsonp&inCharset=utf8&outCharset=utf-8&notice=0&platform=yqq&needNewCode=0&data=%7B%22req%22%3A%7B%22module%22%3A%22CDN.SrfCdnDispatchServer%22%2C%22method%22%3A%22GetCdnDispatch%22%2C%22param%22%3A%7B%22guid%22%3A%227653016346%22%2C%22calltype%22%3A0%2C%22userip%22%3A%22%22%7D%7D%2C%22req_0%22%3A%7B%22module%22%3A%22vkey.GetVkeyServer%22%2C%22method%22%3A%22CgiGetVkey%22%2C%22param%22%3A%7B%22songmid%22%3A%5B%22${songmId}%22%5D%2C%22songtype%22%3A%5B0%5D%2C%22uin%22%3A%22598130620%22%2C%22loginflag%22%3A1%2C%22platform%22%3A%2220%22%7D%7D%2C%22comm%22%3A%7B%22uin%22%3A598130620%2C%22format%22%3A%22json%22%2C%22ct%22%3A20%2C%22cv%22%3A0%7D%7D
				 *
				 *  总结:
				 *      现在的问题就转化到了寻找 songmid 了!
				 *      1. 寻找 songmid, 直接走上面的 url 即可!!!
				 *          https://c.y.qq.com/v8/fcg-bin/fcg_v8_album_info_cp.fcg?albummid=004JF1BR18UKuH&g_tk=558582302&jsonpCallback=albuminfoCallback&loginUin=598130620&hostUin=0&format=jsonp&inCharset=utf8&outCharset=utf-8&notice=0&platform=yqq&needNewCode=0
				 *      2. 找到了 songmid , 进行后续的操作...
				 *          这里需要替换的仅仅是请求的 albummid , 这个可以在列表页直接通过 xpath 进行获取到
				 *          1)找到 songmid 先替换上面的查找对应的 vkey 的 url , 返回包含 vkey 的json 字符串, 解析该字符串获取到 vkey;
				 *          2)使用 songmid 替换 songId 的请求, 即为最终的下载的音乐的 url
				 *
				 */


			}
		}
		System.out.println("---------------> " + albumnIds);
	}

	@Test
	public void testDownloadSongsByAlbummid() {
		String songmid = "003QVFLh125yW3";
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
				String vkey = json.getJSONObject("req").getJSONObject("data").getString("vkey");
				System.out.println(vkey + "<----------------");
				String downloadurl = "http://dl.stream.qqmusic.qq.com/${songId}.m4a?guid=7653016346&vkey=${vkey}&uin=8124&fromtag=66"
						.replace("${songId}", "C4000" + songmid)
						.replace("${vkey}", vkey);
				if (vkey != null) {
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		temp.close();
		//1. 获取下载的 vkey
		//2. 拼接下载的 url
	}

	@Test
	public void testDeleteExtraParagraph() {
		try {
			//BufferedImage read2 = ImageIO.read(new File("D://222.gif"));
			// 后记：ImageIO.read 读取某些gif图片会报错，是个bug，所以换个方式，对流读取到byte数组中，后续的操作及文件大小都以此byte数组为准。
			byte[] fileData = IOUtils.toByteArray(new FileInputStream(new File("D://222.gif")));
			GifImage image = GifDecoder2.read(fileData);
			System.out.println(image.getWidth() + "<-------------------");

			HttpFileUtil.resizeGif(new File("D://222.gif"), image.getWidth(), "1222.gif");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testReplaceDiv() throws ParseException {
		String newText = "${RootPath}/www.81.cn/image/1b53dd11a9e28867bd7d1420f267ccaf.png";


		System.out.println(newText.contains("${RootPath}"));
	}

	@Test
	public void testDeleteImg() {
		String formatter = "<p class=\"\" style=\"margin-top: 1px;margin-bottom: 9px;color: rgb(138, 147, 158);font-size: 14px;line-height: 22px;min-height: 16px;padding-bottom: 6px;height: 18px;text-align: center;padding-top: 1px;\" type=\"imagenote\">002航母二次海试后已加快服役速度</p>";
		System.out.println("matches: " + formatter.matches("(.*?)style=\"(.*?)text-align: center;(.*?)\"(.*?)"));
		if (formatter.matches("(.*?)style=\"(.*?)text-align: center;(.*?)\"(.*?)")) {
			formatter = formatter.replaceAll("style=\"(.*?)text-align: center;(.*?)\"", "#LeaveStyle1#");
			//System.out.println(formatter + "---------------------------------------------");
		}
		formatter = formatter.replace("#LeaveStyle1#", "style=\"vertical-align: center;\"");
		System.out.println(formatter);
		String a = "<p>  金融是现代经济的血液，金融和实体经济互为依托，相互促进、相辅相成。在军民融合深度发展实践中，怎样推动财政主导融资向多元化金融创新服务体系转型，进而为中小企业“蝶变”开辟通途？四川省绵阳市的探索实践，给出了有益的参考答案。</p>";
		System.out.println(a.replaceAll("<p(.*?)>(\t)*(\u3000)+", "<p>"));
	}

	@Test
	public void testImageExtrator() throws Exception { //
		Metadata metadata = JpegMetadataReader.readMetadata(new File("D:\\data\\webmagic\\www.sohu.com\\image\\1aba05c2eee5b251fe380db27f59bfaa.jpeg"));
		System.out.println("Directory Count: " + metadata.getDirectoryCount());
		System.out.println();
		//输出所有附加属性数据
		for (Directory directory : metadata.getDirectories()) {
			System.out.println("******\t" + directory.getName() + "\t******");
			for (Tag tag : directory.getTags()) {
				if (tag.getTagName().toLowerCase().equals("width")) {
					System.out.println("----------> " + tag.getDescription());
				}
				System.out.println(tag.getTagName() + ":" + tag.getDescription());
			}
			System.out.println();
			System.out.println();
		}

		/**
		 * 读取大的文件的时候, 会出现异常, 导致错误的产生...
		 * java.lang.ArrayIndexOutOfBoundsException: 4096???
		 */
		BufferedImage image = ImageIO.read(new File("D:\\data\\webmagic\\weixin.sogou.com\\image\\d2e8809beae15f8bbcb7c9741946f373.jpg"));
	}

	@Test
	public void testWxRemove() {
		String str = " <p style=\"font-stretch";
		Matcher m = Pattern.compile("style=\".*?\"").matcher(str);
		while (m.find()) {
			System.out.println("===============" + m.group());
		}
		System.out.println(str);
		System.out.println(str.replaceAll("style\\s*=\\s*[',\"]{1}[^'\"]+[',\"]{1}\\s*", ""));
		System.out.println(str.replaceAll("style=\".*?\"", ""));
	}

	@Test
	public void testRemove1() {
		String newText = "<p style=\"text-align:center\"><down.</span></p>    </div>   </div>  </div>    </div>";
		if (newText.contains("</div>") && newText.contains("<img")) {
			Matcher matcher = Pattern.compile("<div>(\\s)*<div>(\\s)*<img(.*?)</div>(\\s)*</div>").matcher(newText);
			String temp = "";
			if (matcher.find()) {
				temp = matcher.group();
				if (!temp.contains("</p>")) {
					newText = newText.replace(matcher.group(), "${saveDiv}");
				}
			}
			while (matcher.find()) {
				String g = matcher.group();
				if (!g.contains("</p>")) {
					System.out.println("---------------------" + g);
					newText = newText.replace(g, "");
				}
				//System.out.println(g + "-------" + r);
			}
			newText = newText.replace("${saveDiv}", temp);
			System.out.println("-------" + newText);
		}
	}

	@Test
	public void testRemoveDulp() throws Exception {
		// 需要处理数据的文件位置
		FileReader fileReader = new FileReader(new File("D:\\workspace\\product\\BSCrawler\\doc\\crawlerConfigex\\q5.txt"));
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		Map<String, String> map = new HashMap<String, String>();
		String readLine = null;
		int i = 0;

		while ((readLine = bufferedReader.readLine()) != null) {
			// 每次读取一行数据，与 map 进行比较，如果该行数据 map 中没有，就保存到 map 集合中
			if (!map.containsValue(readLine)) {
				map.put("key" + i, readLine);
				i++;
			}
		}

		for (int j = 0; j < map.size(); j++) {
			System.out.println(map.get("key" + j));
		}
	}

	// text-align: left;
	@Test
	public void testUd() {
		String newText = "<p ><span ><br ></span></p>  <p ><span ></span></p>  <p ><img data-ratio=\"0.54296875\" data-s=\"300,640\" src=\"${RootPath}/weixin.sogou.com/image/f4343f939b6f7b7d5161ffcfc556153c.jpeg\" data-type=\"jpeg\" data-w=\"1280\" ></p>  <p ><span ></span><br ></p>";

		if (newText.contains("<br")) {
			Matcher matcher = Pattern.compile("<p(.*?)>(.*?)</p>").matcher(newText);
			while (matcher.find()) {
				String t = matcher.group(2);
				t = t.replaceAll("<span(.*?)>", "").replace("</span>", "").replaceAll("<br(.*?)>", "").trim();
				if (t.length() == 0) {
					String g = matcher.group();
					String temp = g.replaceAll("<p(.*?)>", "").replaceAll("<span(.*?)>", "").replace("</span>", "").replace("</p>", "").replaceAll("<br(.*?)>", "".replace(" ", "")).trim();
					if (temp.length() == 0) {
						System.out.println("-------------------------" + g);
						newText = newText.replace(g, "").trim();
					}
				}
			}
		}
		System.out.println(newText);
	}

	@Test
	public void testLong() {
		String str = "<p style=\"whit";

		System.out.println(str + "\n\n" + str.replaceAll("<iframe(.*?)</iframe>", ""));
	}

	@Test
	public void testDevideMethod() {
		Map<String, String> map = new HashMap<String, String>();
		List<Map<String, String>> maps = new ArrayList<>();
		for (int i = 0; i < 101; i++) {
			map.put(i + 1 + "", i + "");
			if (map.size() == 4) {
				maps.add(map);
			} else if (map.size() > 4) {
				map = new HashMap<String, String>();
			}
		}

		System.out.println(maps.size());
		
		/*map.put("aa1", "bbb");
		map.put("aa2", "bbb");
		map.put("aa3", "bbb");
		map.put("aa4", "bbb");
		map.put("aa5", "bbb");
		//map.put("aa6", "bbb");
		//map.put("aa7", "bbb");
		
		Map<String, String> subMap = new HashMap<String, String>();
		int i = 0;
		for (String key : map.keySet()) {
			subMap.put(key, map.get(key));
			if ((i + 1) % 2 == 0) {//如果用2来求模,能够被整出,则重新实例一个Map,这里的数字根据自己实际情况修改。
				System.out.println(subMap);
				subMap = new HashMap<String, String>();
			}
			i++;
		}
		
		if (map.size() % 2 != 0) {
			System.out.println(subMap);
		}*/

	}

	/**
	 * 测试添加新闻的摘要字段
	 */
	@Test
	public void tetsReplace() {
		String content = "<p>德国军工一直享有很高的p>新浪军事：最多军迷首选的军事门户！</p>";
		content = content.replaceAll("<p(.*?)>", "").replaceAll("</p>", "").replaceAll("<img(.*?)>", "");
		System.out.println(content);
		if (content.length() <= 100) {
			System.out.println(content);
		} else {
			System.out.println(content.substring(0, 100) + "...");
		}
		System.out.println(content.length());
		System.out.println("德国".length());
	}

	/**
	 * 测试根据标题生成数据的 id 属性值
	 */
	@Test
	public void testTitle() {
		String title = "《兵工科技》2018珠海航展专辑火热预售（纸质版） ";
		System.out.println(DigestUtils.md5Hex(JSON.toJSONString(title.trim())));
	}

	/**
	 * 获取关键词及对应的分类
	 *
	 * @throws IOException
	 */
	@Test
	public void testJsoup222() throws IOException {
		Map<String, Map<String, String>> data = new HashMap<>();
		//BufferedReader br = new BufferedReader(new FileReader(new File("words.txt")));
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("words.txt"), "GB2312"));
		String content = null;
		//List<String> jsons = new ArrayList<String>(100);
		while ((content = br.readLine()) != null) {
			try {
				int fromindex = content.indexOf("id=\"") + 4;
				String dataKey = content.subSequence(fromindex, fromindex + 6).toString();
				String[] as = content.split("</a>");
				Map<String, String> temp = new HashMap<>();
				for (String a : as) {
					if (a.contains("<a")) {
						String tempKey = a.subSequence(a.indexOf("'") + 1, a.indexOf("'") + 5).toString().trim();
						String tempVal = a.subSequence(a.indexOf(tempKey) + 8, a.length()).toString().trim();
						temp.put(tempKey, tempVal);
					}
				}
				data.put(dataKey, temp);
			} catch (Exception e) {
				System.out.println("=====" + content.subSequence(0, 30));
				e.printStackTrace();
			}
		}
		System.out.println("========" + data.size());

	}

	/**
	 * 根据 jsoup 输出对应的 html 标签内部的文本信息
	 */
	@Test
	public void testJsoupWx() {
		String content = "<div name=\"dpscontent\">  未来的通信网络  <br> &nbsp;&nbsp;负责商用卫星通信的新项目办公室将不得不考虑“托管的服务”。该能力将需要从单一网络访问来自不同供应商的卫星通信容量，以及根据需求或在发生中断时切换供应商。  <br> &nbsp;&nbsp;空军太空司令部副司令David Thompson监督国防信息系统局转让商用卫星通信权力的过程。空军太空司令部和航天与导弹系统中心的官员正在就新的权力如何实施展开详细讨论。  <br> &nbsp;&nbsp;另一个迫在眉睫的问题是未来的“事业”网络需要哪些用户终端。众议院拨款防务小组表示，各军种部长应当设立联合计划办公室以解决这些问题。  <br> &nbsp; 用户终端对于五角大楼并非小事。军方拥有约1.7万台卫星通信终端，而许多与现代商业网络不兼容。Kratos公司联邦卫星解决方案高级副总裁John Monahan表示，提议的解决方案之一是进行软件升级。Kratos公司收到了试点项目合同，正在建立独立的接口，让用户可以使用现有终端与所有卫星进行通信。公司也可以通过添加软件来提供“态势感知”，了解卫星通信是否受到干扰，或者可以切换至哪些可用的卫星。  <br> &nbsp;&nbsp;Monahan表示，所有军事终端理论上都可以使用这个接口，但对全部1.7万台设备进行更改并不现实。对于某些用户和应用程序而言，使用支持多种频率的现代终端替换旧系统或许更有意义。  <br> &nbsp;&nbsp;军方的未来网络可能需要数年时间才能实现，这也是众议院拨款防务小组指示空军购买两颗WGS卫星的关键原因。Bryce Space &amp; Technology创始人兼CEO Carissa Bryce Christensen表示，军方强烈希望利用商业太空能力，但五角大楼仍然希望拥有自己的卫星。  <br> &nbsp;&nbsp;美国政府也正在密切关注蓬勃发展的小卫星产业及其在低地轨道上运行的星座，这些星座将为全球带来革命性的电信和宽带服务。Christensen表示，未来的军用网络将不得不将以上因素纳入考虑。小卫星的使用以及小卫星公司提供的产品和服务是一个非常有趣的领域，国防机构正在考虑和评估这些能力，并试图了解如何使用这些能力。（中国航天系统科学与工程研究院&nbsp; 李薇�鳎�  <br> &nbsp;  <br> </div>";
		//String content = "<img data-ratio=\"0.6671875\" src=\"${RootPath}/weixin.sogou.com/image/d1e230266fc8c6328b6f7e6b4e09d327.jpeg\" data-type=\"jpeg\" data-w=\"640\" width=\"100%\"> ";
		//content = content.replaceAll("<p(.*?)>", "<p>");
		//System.out.println(content);
		// 获取 Document 对象
		//content = content.replaceAll("<section(.*?)>", "").replaceAll("</section(.*?)>", "").replaceAll("<div(.*?)>", "").replaceAll("</div(.*?)>", "").replaceAll("<span(.*?)>", "").replaceAll("</span(.*?)>", "").trim();
		/*if(content.contains("<img") && content.contains("<p") && (content.indexOf("<p") < content.indexOf("<img"))) {
			String p2imgStr = content.subSequence(content.indexOf("<p"), content.indexOf("<img")).toString();
			int p2img = content.indexOf(p2imgStr);
			String img2pStr = content.subSequence(content.indexOf("<p"), content.indexOf("</p>")).toString();
			int img2p = content.indexOf(img2pStr);
			if(p2img > img2p) {
				content = content.replaceAll("<img(.*?)/>", "<p><img(.*?)/></p>");
			}
		} 
		//else if(content.contains("<img") && content.contains("<p") && (content.indexOf("<p>") > content.indexOf("<img"))) {
		else if(content.contains("<img")) {
			// img 标签在首个 p 标签的前面, 说明 img 标签肯定没有被 p 标签包含, 所以需要将 img 标签包起来
			content = content.replaceAll("<img(.*?)/>", "<p><img(.*?)/></p>");
		}*/
		// 判断正文部分若包含 img 标签, img 标签是否被 p 标签包围
		String regex = "<p(.*?)<img(.*?)</p";
		String imgRegex = "<img(.*?)>";
		Matcher m = Pattern.compile(regex).matcher(content);
		Matcher imgMatcher = Pattern.compile(imgRegex).matcher(content);
		boolean flag = content.contains("<img") && !m.matches();
		if (flag) {
			while (imgMatcher.find()) {
				String imgStr = imgMatcher.group();
				// 对于标签内部的特殊字符做出特殊的处理
				imgStr = imgStr.replaceAll("\\.", "&&&").replaceAll("\\$\\{", "&&").replaceAll("\\}", "&");
				content = content.replaceAll("\\.", "&&&").replaceAll("\\$\\{", "&&").replaceAll("\\}", "&");
				String replaceMent = "<p>" + imgStr + "</p>";
				content = content.replace(imgStr, replaceMent).replace("&&&", ".").replace("&&", "${").replace("&", "}");
			}
		}

		// 正文部分由 br 进行换行, 使用 p 标签进行替换
		if (!content.contains("<p") && content.contains("<br>")) {
			content = content.replaceAll("<div(.*?)>", "").replaceAll("</div>", "");
			regex = "(.*?)<br>";
			Matcher brMatcher = Pattern.compile(regex).matcher(content);
			while (brMatcher.find()) {
				//System.out.println(brMatcher.group());
				content = content.replace(brMatcher.group(), "<p>" + brMatcher.group() + "</p>");
			}
		}

		Document d = Jsoup.parseBodyFragment(content);
		// 获取指定的 p 标签的集合
		Elements elementsByTag = d.getElementsByTag("p");

		for (Element e : elementsByTag) {
			if (e.children().size() > 0 && e.children().toString().contains("<img")) {
				//sb.append("<p class=\"detailPic\">" + e.children().toString() + "</p>");
				System.out.println("<p class=\"detail-pic\">" + e.children().toString() + "</p>");
			} else {
				if (!StringUtils.isEmpty(e.text().trim())) {
					//sb.append("<p>" + e.text() + "</p>");
					System.out.println("<p>" + e.text() + "</p>");
				}
			}
		}
	}

	/**
	 * 同义词的获取方法
	 */
	@Test
	public void testReplace123123() {
		String title = "CZ100/101手枪";
		String synonymTitle = "";
		if (title.trim().contains(".")) {
			synonymTitle = title.replaceAll("\\.", "").replaceAll("“", "").replaceAll("”", "")
					.replaceAll("\\([\\s\\S]+", "").replaceAll("\\（[\\s\\S]+", "").replaceAll("/[\\s\\S]+", "")
					.replaceAll(" ", "").replaceAll("-", "");
			System.out.println("===============1" + synonymTitle);
		}
		if (title.trim().contains("“") || title.trim().contains("”")) {
			synonymTitle = title.replaceAll("“", "").replaceAll("”", "").replaceAll("\\([\\s\\S]+", "")
					.replaceAll("\\（[\\s\\S]+", "").replaceAll("/[\\s\\S]+", "").replaceAll(" ", "")
					.replaceAll("-", "");
			System.out.println("===============2" + synonymTitle);
		}
		if (title.trim().contains("(") || title.trim().contains("（")) {
			synonymTitle = title.replaceAll("\\([\\s\\S]+", "").replaceAll("\\（[\\s\\S]+", "")
					.replaceAll("/[\\s\\S]+", "").replaceAll(" ", "").replaceAll("-", "");
			System.out.println("===============3" + synonymTitle);
		}
		if (title.trim().contains("\\u201c") || title.trim().contains("\\u201d")) {
			synonymTitle = title.replaceAll("\\u201c", "").replaceAll("\\u201d", "").replaceAll("/[\\s\\S]+", "")
					.replaceAll(" ", "").replaceAll("-", "");
			System.out.println("===============4" + synonymTitle);
		}
		if (title.trim().contains("/")) {
			String[] temp = title.trim().split("/");
			title = title.replaceAll("/", "");
			String regexTemp = "[0-9]+";
			Matcher m2 = Pattern.compile(regexTemp).matcher(title);
			while (m2.find()) {
				title = title.replaceAll(m2.group(), "&&");
			}
			for (String str : temp) {
				m2 = Pattern.compile(regexTemp).matcher(str);
				while (m2.find()) {
					synonymTitle += title.replace("&&", m2.group()) + ",";
				}
			}
			synonymTitle = synonymTitle.substring(0, synonymTitle.length() - 1);
			System.out.println("===============5" + synonymTitle);
		}
		if (title.trim().contains(" ")) {
			synonymTitle = title.replaceAll(" ", "").replaceAll("-", "");
			System.out.println("===============6" + synonymTitle);
		}
		if (title.trim().contains("-")) {
			synonymTitle = title.replaceAll("-", "");
			System.out.println("===============7" + synonymTitle);
		}
		String regex = "[a-zA-Z\u4e00-\u9fa5][a-zA-Z0-9\u4e00-\u9fa5]+";
		Matcher m = Pattern.compile(regex).matcher(title);
		if (m.find()) {
			synonymTitle = m.group();
			System.out.println("===============8" + m.group());
		}
		System.out.println("===============9" + title);
	}

	@Test
	public void testText2Map() throws IOException {
		List<Map<String, String>> list = new ArrayList<>();
		FileReader reader = new FileReader(new File("1.txt"));
		BufferedReader br = new BufferedReader(reader);
		String s = null;
		while ((s = br.readLine()) != null) {
			try {
				Map<String, String> map = new HashMap<>();
				map = str2Map(s);
				list.add(map);
			} catch (Exception e) {
				System.out.println("=====" + s.subSequence(0, 30));
				// e.printStackTrace();
			}
		}
	}

	private Map<String, String> str2Map(String str) {

		Map<String, String> result = new HashMap<>();
		if (str.indexOf("；") != -1) {
			String[] list = StringUtils.split(str, "；");
			for (String line : list) {
				if (line.indexOf("：") != -1) {
					String[] splits = StringUtils.split(line, "：");
					result.put(HtmlFormatter.removeNum(splits[0]), splits[1]);
				}
			}
		} else {
			if (str.indexOf("：") != -1) {
				String[] splits = StringUtils.split(str, "：");
				result.put(splits[0], splits[1]);
			}

		}

		return result;
	}

	@Test
	public void testRemoveStr() {
		String str = " 2802;2806";
		Matcher m = Pattern.compile("\\d{4}").matcher(str);
		if (m.find()) {
			System.out.println(m.group());
		}
		System.out.println(str.replaceAll("\\s*", ""));

		String string = "S：标引,科技文献处理,文献工作";
		System.out.println(string.trim().length());
		String value = string.substring(2, string.length());
		System.out.println(value.trim());
	}

	@Test
	public void testRegex() {
		String str = "<img class=\"pure-img lazy loaded z-loaded\" alt=\"\"The Art of Solo: A Star Wars Story\" takes you through the captivating design process of the movie. The book includes sketches, storyboards, production paintings and more. \" data-src=\"https://img.purch.com/w/640/aHR0cDovL3d3dy5zcGFjZS5jb20vaW1hZ2VzL2kvMDAwLzA3Ny8wOTcvaTAyL2FydG9mc29sbzEuanBnPzE1Mjg5MjcyMzM=\" big-src=\"https://img.purch.com/h/1400/aHR0cDovL3d3dy5zcGFjZS5jb20vaW1hZ2VzL2kvMDAwLzA3Ny8wOTcvb3JpZ2luYWwvYXJ0b2Zzb2xvMS5qcGc/MTUyODkyNzIzMw==\" src=\"https://img.purch.com/w/640/aHR0cDovL3d3dy5zcGFjZS5jb20vaW1hZ2VzL2kvMDAwLzA3Ny8wOTcvaTAyL2FydG9mc29sbzEuanBnPzE1Mjg5MjcyMzM=";
		// String regex = "<img(.*?)data-src=(.*?)src=(.*?)/>";
		if (str.contains("data-src=") && str.contains("src="))
			str = str.replaceAll("src=\".*?\"", "");
		System.out.println(" =============> " + str);
	}

	/**
	 * 1526940000 1526954566127
	 */
	@Test
	public void testDate() {
		long time = 1526853600000l;
		System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS").format(new Date(time)));
		System.out.println(System.currentTimeMillis());
	}

	@Test
	public void testMap() {
		Map<String, Object> map = new HashMap<>();
		map.put("a", 123123);
		map.put("a", null);
		System.out.println(" ===> " + map.get("a"));
	}

	@Test
	public void testReplace() {
		String str = "https://www.einpresswire.com/tracking/article.gif?t=2&a=pBjHSz7virJQ4Iqj&i=RRBOxP23HJJ96E7xdsdsdferqrqsadsad"
				+ "https://www.einpresswire.com/tracking/article.gif?t=2&amp;a=pBjHSz7virJQ4Iqj&amp;i=RRBOxP23HJJ96E7x";
		String str2 = "https://www.einpresswire.com/tracking/article.gif?t=2&a=pBjHSz7virJQ4Iqj&i=RRBOxP23HJJ96E7x";
		String replace = StringUtils.replace(str, str2.replace("&", "&amp;"), "000000");
		System.out.println(" -------> " + replace);
	}

	@Test
	public void test() {
		String s = "【的速度】大苏打似的";
		System.out.println(s.replaceAll("\\【(.*)\\】", ""));
	}

	@Test
	public void testMagic() {
		String id = DigestUtils.md5Hex(JSON.toJSONString("小火箭 | 剖析SpaceX公司的最新版猎鹰运载火箭"));
		System.out.println(id);
	}

	@Test
	public void testSelector() {
		String html = "<div class=\"f-main-leftMain-content clear\" id=\"Zoom\">  \n" +
				" <a>   </a> \n" +
				" <div class=\"TRS_Editor\"> \n" +
				"  <a> </a> \n" +
				"  <div class=\"TRS_Editor\"> \n" +
				"   <a> <p>　　<strong>　摘　　要：</strong>张总先生的新著《中国三阶教史》出版（社会科学文献出版社2013年版）了，书的副标题是“一个佛教史上湮灭的教派”。中国的所谓教派，主要特征之一在于它有着自己所谓从“初祖”开始的传授系统。没有了这个系统，教派也就无法“传灯”。由此没有了承袭的香火，被后人所遗忘或漠视是可以想象得到的结果。然而三阶教在隋及唐初之风光，不亚于其他的教派，即使是遵奉达摩为创始人的禅宗恐怕在当时的影响上还比不了它。这种教派的大起大落是使人饶有兴趣的，也是发人深省的。张总先生的书就是为此而写，不过这不仅是一本考证记叙了中国三阶教的来龙去脉和发展演变的通史，而且是一本述论近代以来对这个教派如何进行研究的学术史，作者为此收集了有关三阶教的论著178种。因为所谓学术史包含着相关学术研究观念与方法的探讨和进步，对后来者有着很大的启迪性，所以这是一本学术含量很高的专著。</p> <p>　　【作　　者】严耀中</p> <p>　　【作者单位】复旦大学文史研究院</p> <p>　　【期　　刊】《世界宗教研究》 北大2011版核心期刊 中国人文科学核心期刊要览 中文社会科学引文索引 2013年第5期180-181,共2页</p> <p>　　【关 键 词】三阶教 中国 社会科学文献出版社 空间 知识 汇集 传授系统 大起大落</p> </a>\n" +
				"   <p><a><strong>　　附件：</strong></a><a oldsrc=\"W020150115352124523930.pdf\" href=\"./W020150115352124523930.pdf\"><strong>汇集着多科知识和探索空间的《中国三阶教史》.pdf</strong></a><strong> </strong></p> \n" +
				"  </div> \n" +
				" </div> \n" +
				" <p> \n" +
				"  <!-- 文档附件start -->  \n" +
				"  <!-- 文档附件end --></p>  \n" +
				" <p style=\"border-bottom:1px #999 dashed; margin-bottom:5px; height:15px;\"></p>   \n" +
				"</div>";

		Selector selector = new XpathSelector("//a[contains(@href,'pdf')]/@href");
		System.out.println("-------------> " + selector.select(html));
	}

	@Test
	public void testDownPdf() {
		News news = (News) XMLUtil.convertXmlFileToObject(News.class, "C:\\Users\\wgshb\\Desktop\\JH项目\\xmls\\军事气象水文\\2.xml");
		System.out.println(news);
	}

	@Test
	public void testCopyFileByNio() throws IOException {
		String inFile = "F:\\111.txt";
		String outFile = "F:\\11000.txt";
		FileInputStream in = new FileInputStream(inFile);
		FileOutputStream out = new FileOutputStream(outFile);
		// 1. 获取管道
		FileChannel inChannel = in.getChannel();
		FileChannel outChannel = out.getChannel();

		// 2. 分配读取的单元数量
		ByteBuffer buffer = ByteBuffer.allocate(1024);

		while (true) {
			buffer.clear();
			// 3. 实际上读取的是管道中的缓存 buffer
			int r = inChannel.read(buffer);
			if (r == -1) {
				System.out.println("---------------> 文件复制完毕");
				break;
			}
			// 4. 关闭缓存
			buffer.flip();
			// 5. 写入输出管道...
			outChannel.write(buffer);
		}
	}

	@Test
	public void testProxy() throws IOException {
		HttpClient httpClient = new HttpClient();
		httpClient.getHostConfiguration().setProxy("127.0.0.1", 8087);
		HttpMethod method = new GetMethod("http://www.google.com/search?q=%E7%BE%8E%E5%86%9B&source=lnms&tbm=nws&pn=0&cl=2");
		httpClient.executeMethod(method);
		InputStream in = method.getResponseBodyAsStream();
		File file = new File("D:/b.html");
		OutputStream os = new FileOutputStream(file);
		byte[] buff = new byte[4096];
		int len = -1;
		while ((len = in.read(buff)) != -1) {
			os.write(buff, 0, len);
		}
		in.close();
		os.close();
		method.releaseConnection();
	}

	@Test
	public void testMovie() throws IOException {
		String videoResult = "C:\\Users\\wgshbase\\volumes\\root\\upload\\upload\\file\\20190508";
		File file = new File(videoResult);
		System.out.println(!file.mkdirs() +""+ !file.isDirectory());
	}

	public static String fileEncode(String str) {
		if (str != null) {
			//这里是专为文件写的转义方法，涉及文件操作
			return str
					.replaceAll("\\\\", "＼")
					.replaceAll("/", "／")
					.replaceAll(":", "：")
					.replaceAll("[*]", "＊")
					.replaceAll("[?]", "？")
					.replaceAll("\"", "”")
					.replaceAll(":", "：")
					.replaceAll("<", "＜")
					.replaceAll(">", "＞")
					.replaceAll("[|]", "｜");
		} else {
			//防止空，搞成空格
			return " ";
		}
	}

	/**
	 * 剔除不符合条件的 img 属性标签
	 */
	@Test
	public void testDeletebadImg() {
		String img = "<img class=\"lazy-img\" src=\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAMAAAAoyzS7AAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAAZQTFRF9fX1AAAA0VQI3QAAAAxJREFUeNpiYAAIMAAAAgABT21Z4QAAAABJRU5ErkJggg==\" src=\"${RootPath}/weixin.sogou.com/image/814e56c6ba2e8dc5fc7878de550fcba6.jpg\" alt=\"Meteor - M卫星\" >";
		Matcher m =  Pattern.compile("(.*?)(src=(.*?))src=\"((.*?)RootPath(.*?))\"(.*?)").matcher(img);
		if(m.find()) {
			System.out.println(m.group(1) + m.group(2) + m.group(3) + m.group(4));
			System.out.println(m.group(2));
			System.out.println(m.group(4));
			img = img.replace(m.group(2), "");
		}
		System.out.println("resut : " + img);


	}

	/**
	 * 抽取流水号
	 */
	@Test
	public void testGetLiushuiId() throws ParseException {
		/*File f = new File("D:\\root\\2019-11-18");
		File[] files = f.listFiles();
		List<String> names = new ArrayList<>();
		for(File fi : files) {
			if(fi.isFile()) {
				if(!names.contains(fi.getName().substring(0, fi.getName().indexOf("_")))) {
					names.add(fi.getName().substring(0, fi.getName().indexOf("_")));
				}
			}
		}
		for(String s : names) {
			System.out.println(s);
		}*/
//		System.out.println(HtmlFormatter.convertPubDate("december. 26, 2019 12:15 p.m.")); sinotrans_id
		/*sendunit_name	sendunitname_cn	receiveunit_name	pack_list_content	good_name	good_code	good_brand	deal_quantity	deal_price	total_amountl	legal_quantity*/
		for(int i = 0 ; i < 2107; i++) {
			System.out.println(UUID.randomUUID().toString().replaceAll("-", "").substring(0, 15));
		}

	}

	@Test
	public void test3232323232() throws Exception {
		ChromeDriver driver = ChromeDriverUtils.getDefaultChromeDriver();
		driver.get("http://www.haiguanbeian.com/zscq/search/jsp/vBrandSearchIndex.jsp#");

		driver.findElement(By.id("find_btn")).click();
		WebElement element = driver.findElement(By.xpath("//select[@role='listbox']"));
		Select select = new Select(element);
		select.selectByValue("100");
		Thread.sleep(3000l);
		List<WebElement> webElements = driver.findElementsByXPath("//tr[@tabindex='-1']");
		List<Map<String, String>> result = new ArrayList<>();
		for(WebElement webElement : webElements) {
			List<String> list = new ArrayList<>();
			System.out.println(webElements.indexOf(webElement)+1);
			if(ChromeDriverUtils.doesWebElementExist(driver, "//tr[@tabindex='-1']["+(webElements.indexOf(webElement)+1)+"]/td[@aria-describedby='searchlist_RECORD_NAME']")) {
				WebElement recodename = webElement.findElement(By.xpath("//tr[@tabindex='-1']["+(webElements.indexOf(webElement)+1)+"]/td[@aria-describedby='searchlist_RECORD_NAME']"));
				list.add(recodename.getAttribute("title"));
			} else {
				list.add("");
			}
			if(ChromeDriverUtils.doesWebElementExist(driver, "//tr[@tabindex='-1']["+(webElements.indexOf(webElement)+1)+"]/td[@aria-describedby='searchlist_APPLY_USER_NAME']")) {
				WebElement username = webElement.findElement(By.xpath("//tr[@tabindex='-1']["+(webElements.indexOf(webElement)+1)+"]/td[@aria-describedby='searchlist_APPLY_USER_NAME']"));
				list.add(username.getAttribute("title"));
			}else {
				list.add("");
			}
			if(ChromeDriverUtils.doesWebElementExist(driver, "//tr[@tabindex='-1']["+(webElements.indexOf(webElement)+1)+"]/td[@aria-describedby='searchlist_CONUTRY_NAME']")) {
				WebElement countryname = webElement.findElement(By.xpath("//tr[@tabindex='-1']["+(webElements.indexOf(webElement)+1)+"]/td[@aria-describedby='searchlist_CONUTRY_NAME']"));
				list.add(countryname.getAttribute("title"));
			}else {
				list.add("");
			}
			if(ChromeDriverUtils.doesWebElementExist(driver, "//tr[@tabindex='-1']["+(webElements.indexOf(webElement)+1)+"]/td[@aria-describedby='searchlist_REGISTER_NUM']")) {
				WebElement registernum = webElement.findElement(By.xpath("//tr[@tabindex='-1']["+(webElements.indexOf(webElement)+1)+"]/td[@aria-describedby='searchlist_REGISTER_NUM']"));
				list.add(registernum.getAttribute("title"));
			}else {
				list.add("");
			}
			if(ChromeDriverUtils.doesWebElementExist(driver, "//tr[@tabindex='-1']["+(webElements.indexOf(webElement)+1)+"]/td[@aria-describedby='searchlist_RECORD_NUM']")) {
				WebElement recordnum = webElement.findElement(By.xpath("//tr[@tabindex='-1']["+(webElements.indexOf(webElement)+1)+"]/td[@aria-describedby='searchlist_RECORD_NUM']"));
				list.add(recordnum.getAttribute("title"));
			}else {
				list.add("");
			}
			if(ChromeDriverUtils.doesWebElementExist(driver, "//tr[@tabindex='-1']["+(webElements.indexOf(webElement)+1)+"]/td[@aria-describedby='searchlist_REGISTER_TYPE']")) {
				WebElement registertype = webElement.findElement(By.xpath("//tr[@tabindex='-1']["+(webElements.indexOf(webElement)+1)+"]/td[@aria-describedby='searchlist_REGISTER_TYPE']"));
				list.add(registertype.getAttribute("title"));
			}else {
				list.add("");
			}
			if(ChromeDriverUtils.doesWebElementExist(driver, "//tr[@tabindex='-1']["+(webElements.indexOf(webElement)+1)+"]/td[@aria-describedby='searchlist_PRODUCT_TYPE']")) {
				WebElement producttype = webElement.findElement(By.xpath("//tr[@tabindex='-1']["+(webElements.indexOf(webElement)+1)+"]/td[@aria-describedby='searchlist_PRODUCT_TYPE']"));
				list.add(producttype.getAttribute("title"));
			}else {
				list.add("");
			}
			WebElement recordstate = webElement.findElement(By.xpath("//tr[@tabindex='-1']["+(webElements.indexOf(webElement)+1)+"]/td[@aria-describedby='searchlist_RECORD_STATE']"));
			WebElement recordbegindate = webElement.findElement(By.xpath("//tr[@tabindex='-1']["+(webElements.indexOf(webElement)+1)+"]/td[@aria-describedby='searchlist_RECORD_BEGIN_DATE']"));
			WebElement recordenddate = webElement.findElement(By.xpath("//tr[@tabindex='-1']["+(webElements.indexOf(webElement)+1)+"]/td[@aria-describedby='searchlist_RECORD_END_DATE']"));

			System.out.println(list);
			Map<String, String> map = new HashMap<>();
			map.put("recodename", list.get(0));
			map.put("username", list.get(1));
			map.put("countryname", list.get(2));
			map.put("registernum", list.get(3));
			map.put("recordnum", list.get(4));
			map.put("registertype", list.get(5));
			map.put("producttype", list.get(6));

			result.add(map);

		}

		for(int i = 0; i < 550; i++) {
			result = new ArrayList<>();
			webElements = null;
			driver.findElement(By.id("next_gridPager")).click();
			Thread.sleep(2000l);
			Set<String> windowHandles = driver.getWindowHandles();
			for (String windowhandle : windowHandles) {
				if (windowhandle != driver.getWindowHandle()) {
					driver = (ChromeDriver) driver.switchTo().window(windowhandle);
				}
			}
//			driver.findElement(By.id("find_btn")).click();
			webElements = driver.findElementsByXPath("//tr[@tabindex='-1']");
			for(WebElement webElement : webElements) {
				List<String> list = new ArrayList<>();
				System.out.println(webElements.indexOf(webElement)+1);
				try {
					if(ChromeDriverUtils.doesWebElementExist(driver, "//tr[@tabindex='-1']["+(webElements.indexOf(webElement)+1)+"]/td[@aria-describedby='searchlist_RECORD_NAME']")) {
						WebElement recodename = webElement.findElement(By.xpath("//tr[@tabindex='-1']["+(webElements.indexOf(webElement)+1)+"]/td[@aria-describedby='searchlist_RECORD_NAME']"));
						list.add(recodename.getAttribute("title"));
					} else {
						list.add("");
					}
					if(ChromeDriverUtils.doesWebElementExist(driver, "//tr[@tabindex='-1']["+(webElements.indexOf(webElement)+1)+"]/td[@aria-describedby='searchlist_APPLY_USER_NAME']")) {
						WebElement username = webElement.findElement(By.xpath("//tr[@tabindex='-1']["+(webElements.indexOf(webElement)+1)+"]/td[@aria-describedby='searchlist_APPLY_USER_NAME']"));
						list.add(username.getAttribute("title"));
					}else {
						list.add("");
					}
					if(ChromeDriverUtils.doesWebElementExist(driver, "//tr[@tabindex='-1']["+(webElements.indexOf(webElement)+1)+"]/td[@aria-describedby='searchlist_CONUTRY_NAME']")) {
						WebElement countryname = webElement.findElement(By.xpath("//tr[@tabindex='-1']["+(webElements.indexOf(webElement)+1)+"]/td[@aria-describedby='searchlist_CONUTRY_NAME']"));
						list.add(countryname.getAttribute("title"));
					}else {
						list.add("");
					}
					if(ChromeDriverUtils.doesWebElementExist(driver, "//tr[@tabindex='-1']["+(webElements.indexOf(webElement)+1)+"]/td[@aria-describedby='searchlist_REGISTER_NUM']")) {
						WebElement registernum = webElement.findElement(By.xpath("//tr[@tabindex='-1']["+(webElements.indexOf(webElement)+1)+"]/td[@aria-describedby='searchlist_REGISTER_NUM']"));
						list.add(registernum.getAttribute("title"));
					}else {
						list.add("");
					}
					if(ChromeDriverUtils.doesWebElementExist(driver, "//tr[@tabindex='-1']["+(webElements.indexOf(webElement)+1)+"]/td[@aria-describedby='searchlist_RECORD_NUM']")) {
						WebElement recordnum = webElement.findElement(By.xpath("//tr[@tabindex='-1']["+(webElements.indexOf(webElement)+1)+"]/td[@aria-describedby='searchlist_RECORD_NUM']"));
						list.add(recordnum.getAttribute("title"));
					}else {
						list.add("");
					}
					if(ChromeDriverUtils.doesWebElementExist(driver, "//tr[@tabindex='-1']["+(webElements.indexOf(webElement)+1)+"]/td[@aria-describedby='searchlist_REGISTER_TYPE']")) {
						WebElement registertype = webElement.findElement(By.xpath("//tr[@tabindex='-1']["+(webElements.indexOf(webElement)+1)+"]/td[@aria-describedby='searchlist_REGISTER_TYPE']"));
						list.add(registertype.getAttribute("title"));
					}else {
						list.add("");
					}
					if(ChromeDriverUtils.doesWebElementExist(driver, "//tr[@tabindex='-1']["+(webElements.indexOf(webElement)+1)+"]/td[@aria-describedby='searchlist_PRODUCT_TYPE']")) {
						WebElement producttype = webElement.findElement(By.xpath("//tr[@tabindex='-1']["+(webElements.indexOf(webElement)+1)+"]/td[@aria-describedby='searchlist_PRODUCT_TYPE']"));
						list.add(producttype.getAttribute("title"));
					}else {
						list.add("");
					}
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}

				Map<String, String> map = new HashMap<>();
				map.put("recodename", list.get(0));
				map.put("username", list.get(1));
				map.put("countryname", list.get(2));
				map.put("registernum", list.get(3));
				map.put("recordnum", list.get(4));
				map.put("registertype", list.get(5));
				map.put("producttype", list.get(6));

				result.add(map);

			}
			if(result.size() > 0) {
			}
		}

	}

	@Test
	public void test10086() throws Exception {
		ChromeDriver driver = ChromeDriverUtils.getHeadlessChromeDrver();
		driver.get("http://www.nbedi.com/NBCPCAM/ClassifyQuery.aspx");
		Thread.sleep(2000l);
		driver.findElement(By.id("ibQuery")).click();
		Thread.sleep(3000l);
		List<WebElement> webElements = driver.findElementsByXPath("//tr[@style='height:24px;']");
		List<Map<String, String>> result = new ArrayList<>();
		for(WebElement webElement : webElements) {
			WebElement element = webElement.findElement(By.xpath("//tr[@style='height:24px;']/td[1]"));
			WebElement element2 = webElement.findElement(By.xpath("//tr[@style='height:24px;']/td[2]"));
			WebElement element3 = webElement.findElement(By.xpath("//tr[@style='height:24px;']/td[3]"));
			WebElement element4 = webElement.findElement(By.xpath("//tr[@style='height:24px;']/td[4]"));
			WebElement element5 = webElement.findElement(By.xpath("//tr[@style='height:24px;']/td[5]"));
			Map<String, String> map = new HashMap<>();
			map.put("id",element.getText().trim());
			map.put("registernum",element2.getText().trim());
			map.put("recodename",element3.getText().trim());
			map.put("recordnum",element4.getText().trim());
			map.put("username",element5.getText().trim());
//			System.out.println(webElement.getText());
			result.add(map);

		}
		if(result.size() > 0) {
			System.out.println(result);
		}

		// 翻页
		for(int i = 2; i < 5; i++) {
			result = new ArrayList<>();
			webElements = null;
			driver.findElement(By.xpath("//input[@name='pagePagination_input']")).clear();
			driver.findElement(By.xpath("//input[@name='pagePagination_input']")).sendKeys(i + "");
			driver.findElement(By.xpath("//input[@name='pagePagination']")).click();
//			Thread.sleep(2000l);
			Set<String> windowHandles = driver.getWindowHandles();
			for (String windowhandle : windowHandles) {
				if (windowhandle != driver.getWindowHandle()) {
					driver = (ChromeDriver) driver.switchTo().window(windowhandle);
				}
			}
			webElements = driver.findElementsByXPath("//tr[@style='height:24px;']");
			for(WebElement webElement : webElements) {
				WebElement element = webElement.findElement(By.xpath("//tr[@style='height:24px;']/td[1]"));
				WebElement element2 = webElement.findElement(By.xpath("//tr[@style='height:24px;']/td[2]"));
				WebElement element3 = webElement.findElement(By.xpath("//tr[@style='height:24px;']/td[3]"));
				WebElement element4 = webElement.findElement(By.xpath("//tr[@style='height:24px;']/td[4]"));
				WebElement element5 = webElement.findElement(By.xpath("//tr[@style='height:24px;']/td[5]"));
				Map<String, String> map = new HashMap<>();
				map.put("id",element.getText().trim());
				map.put("registernum",element2.getText().trim());
				map.put("recodename",element3.getText().trim());
				map.put("recordnum",element4.getText().trim());
				map.put("username",element5.getText().trim());
//			System.out.println(webElement.getText());
				result.add(map);

			}
			if(result.size() > 0) {
				System.out.println(result);
			}
		}


	}

	@Test
	public void testcheckObjIsPubdateRight() throws ParseException {
		String text = "时间：2020/1/3 8:53:21&nbsp;&nbsp;&nbsp;&nbsp;本站原创";
		System.out.println(HtmlFormatter.convertPubDate(text));
	}


}
