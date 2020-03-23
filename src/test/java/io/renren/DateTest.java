package io.renren;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mss.crawler.spiderjson.util.ChromeDriverUtils;
import com.mss.crawler.spiderjson.util.HtmlFormatter;
import com.mss.translate.GoogleTranslate;
import com.mss.word.HtmlToWordByPOI;
import io.renren.modules.spider.entity.WebInfo;
import io.renren.modules.spider.utils.CSVUtils;
import io.renren.utils.ExcelUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.mss.crawler.custom.weapon.KeywordsThesaurus;
import com.mss.crawler.custom.weapon.Weapon;
import com.mss.crawler.spiderjson.util.HttpFileUtil;

import io.renren.modules.spider.dao.KeywordsThesaurusMapper;
import io.renren.modules.spider.dao.NewsDao;
import io.renren.modules.spider.dao.WeaponMapper;
import io.renren.modules.spider.entity.News;
import io.renren.modules.spider.service.ParseWeapon2JsonServiceImpl;
import io.renren.modules.spider.service.impl.IImport2CloudServiceImpl;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DateTest {

	private static final String prefix = "\\$\\{RootPath\\}";

	@Autowired
	private IImport2CloudServiceImpl import2CloudService;

	@Autowired
	private ParseWeapon2JsonServiceImpl impl;

	@Autowired
	private NewsDao dao;

	@Autowired
	private WeaponMapper mapper;

	@Autowired
	private KeywordsThesaurusMapper thesaurusMapper;

	private WebElement findElement;
	private GoogleTranslate translate = new GoogleTranslate();

	private static String prefixStr = "<p style=\"text-align:center\"><span style=\"color:red\">————————————————————本文来源于国外网站,以下是机器翻译内容,向下阅读可查看原文————————————————————</span></p>";
	private static String suffixStr = "<p style=\"text-align: center;\"><span>————————————————————原文————————————————————</span></p>";

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
		System.setProperty("webdriver.chrome.marionette", "chromedriver.exe");
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
//					WebElement helpEle = tempdriver.findElement(By.xpath("//a[@data-csrc-pub-link='true']"));
					News news = new News();
					if(doesWebElementExist(tempdriver, "//h1[@id='news-title']")) {
						String title = tempdriver.findElement(By.xpath("//h1[@id='news-title']")).getText();
						System.out.println(title);
						news.setTitle(title);
					}

					if(doesWebElementExist(tempdriver, "//div[@id='news-content']")) {
						String content = tempdriver.findElement(By.xpath("//div[@id='news-content']")).getText();
						System.out.println(content);
						news.setContent(content);
					}
					String helpText = "";
					if(doesWebElementExist(tempdriver, "//a[@data-csrc-pub-link='true']")) {
						WebElement helpEle = tempdriver.findElement(By.xpath("//a[@data-csrc-pub-link='true']"));
						System.out.println(helpEle.getText());
						if(!StringUtils.isEmpty(helpEle.getText())) {
							helpText = helpEle.getText();
							System.out.println("help: " + helpText);
						}
						if(!StringUtils.isEmpty(helpText)) {
							news.setContent(news.getContent().replace(helpText, "<b>" + helpText + "</b>"));
						}
						System.out.println("content: " + news.getContent());

					}
					if(doesWebElementExist(tempdriver, "//small[@id='news-date']")) {
						String pubdate = tempdriver.findElement(By.xpath("//small[@id='news-date']")).getText();
						pubdate = HtmlFormatter.convertPubDate(pubdate);
						System.out.println(pubdate);
						news.setPubdate(pubdate);
					}
					news.setUrl(realDownloadUrl);
					news.setId(UUID.randomUUID().toString().replace("-",""));
					news.setCrawlerdate(new Date());
					dao.addChromeDriverNews2NewsEn(news);



					/*String text = helpEle.getText();
					System.out.println(text);
					if(null != helpEle) {
						helpEle.click();
						Set<String> windowHandles = tempdriver.getWindowHandles();
						for (String windowhandle : windowHandles) {
							if (windowhandle != tempdriver.getWindowHandle()) {
								tempdriver.switchTo().window(windowhandle);
							}
						}
						if(doesWebElementExist(tempdriver, "//a[contains(text(),'Local Down')]")) {
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

					}*/
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					tempdriver.quit();
				}
			}

		}
		driver.quit();

	}

	/**
	 * 查询 author 字段进行拆分入库
	 */
	@Test
	public void testSrparateAuthor2DB() {
		List<News> list = dao.getTargetTablesAuthor();
		for(News news : list) {
			String author = news.getAuthor();
			Map<String, String> map = new HashMap<>();
			map.put("id", news.getId());
			String[] split = author.split("</tr>");
			for(String str : split) {
				System.out.println("-------------------------------------");
				Matcher m = Pattern.compile("(.*?)<th([^>]*?)>(.*?)</th>(\\s*)<(td[^>]*?)>(.*?)</td>").matcher(str);
				if(m.find()) {
//				System.out.println(m.group(3) + m.group(6));
					if(m.group(3).replace(" ","").equalsIgnoreCase("Type/Application:")) {
						System.out.println(m.group(3) + " <> " + m.group(6));
						map.put("rwlx", m.group(6));
					}
					if(m.group(3).replace(" ","").equalsIgnoreCase("Power:")) {
						System.out.println(m.group(3) + " <> " + m.group(6));
						map.put("dl", m.group(6));
					}
					if(m.group(3).replace(" ","").equalsIgnoreCase("Contractors:")) {
						System.out.println(m.group(3) + " <> " + m.group(6));
						map.put("yfdw", m.group(6));
					}
					if(m.group(3).replace(" ","").equalsIgnoreCase("Lifetime:")) {
						System.out.println(m.group(3) + " <> " + m.group(6));
						map.put("rwsz", m.group(6));
					}
					if(m.group(3).replace(" ","").equalsIgnoreCase("Mass:")) {
						System.out.println(m.group(3) + " <> " + m.group(6));
						map.put("wxzl", m.group(6));
					}
					if(m.group(3).replace(" ","").equalsIgnoreCase("Operator:")) {
						System.out.println(m.group(3) + " <> " + m.group(6));
						map.put("wxpt", m.group(6));
					}
					if(m.group(3).replace(" ","").equalsIgnoreCase("LaunchVehicle:")) {
						System.out.println(m.group(3) + " <> " + m.group(6));
						map.put("yzhj", m.group(6));
					}
					if(m.group(3).replace(" ","").equalsIgnoreCase("Orbit:")) {
						System.out.println(m.group(3) + " <> " + m.group(6));
						map.put("gdlx", m.group(6));
					}
				}
				System.out.println("===================================");
				dao.updateBSWxOtherColumns(map);
			}
		}
	}

	/**
	 * 采集数据入库其他
	 */
	@Test
	public void testINsert2Db() {
		List<News> list = dao.getTargetTablesContent();
		for(News news : list) {
			String content = news.getContent();
			if(content.contains(prefixStr) && content.contains(suffixStr))
			content = content.substring(content.indexOf(prefixStr) + prefixStr.length(), content.indexOf(suffixStr));

			Matcher m = Pattern.compile("(<p>(\\s*)进一步的(.*?)</p>)").matcher(content);
			if(m.find()) {
				System.out.println("------------------------");
				System.out.println(m.group());
				System.out.println("------------------------");
				content = content.replace(m.group(), "");
			}
				System.out.println("------------------------");
//			System.out.println(content);
			dao.updateInfoContent(news.getId(), content);
			System.out.println("------------------------");
		}
	}

	/**
	 * 导出数据到本地的 txt 文件
	 */
	@Test
	public void testExport2LocalTxt() {
		List<News> list = dao.findTatgetNewsData();
		for(News news : list) {
			File file = null;
			FileWriter fw = null;
			file = new File("F:\\Data\\"+ news.getId() +".txt");
			try {
				if (!file.exists()) {
					file.createNewFile();
				}
				fw = new FileWriter(file);
				fw.write(news.getTitle() + "\r\n");
				fw.write(news.getContent().replaceAll("<.*?>",""));

				System.out.println(list.indexOf(news) + " 写数据成功！");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally{
				if(fw != null){
					try {
						fw.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}

	/**
	 * 查询涉海机构的数据并打包为对应的文件
	 */
	@Test
	public void testShehaiPacket() throws UnsupportedEncodingException {
		List<News> list = dao.findSheMiNews();
		HtmlToWordByPOI poi = new HtmlToWordByPOI();
		poi.setRootPath("D:/data/webmagic/");
		for(News news : list) {
			System.out.println("current index: " + list.indexOf(news));
			try {
				String name = news.getTitle();
				if(name.contains(" ")) {
					name = name.replaceAll(" ", ",");
				}
				poi.toWord(getTargetCnHtml(news), "D:/data/shemi/"+ news.getSiteDomain() + "/（" + news.getPubdate() + "）" +name + ".doc");
			} catch (Exception e) {
				e.printStackTrace();
			}
			/*Map<String,Object> dataMap = new HashMap<>();
			dataMap.put("pubdate", news.getPubdate());
			dataMap.put("url", news.getUrl());
			if(!StringUtils.isEmpty(news.getSearchText()) && !StringUtils.isEmpty(news.getSourceSite())) {
				dataMap.put("attachfiles", news.getSearchText());
				// dataMap.put("filepath", "HYPERLINK \"" + news.getSourceSite().replace("${RootPath}", "D:/data/webmagic").replace("\\","\\\\")+"\"");
				dataMap.put("filepath", "HYPERLINK \"d:\\\\\\\\pdf\\\\\\\\" + news.getSourceSite().substring(news.getSourceSite().lastIndexOf("/") + 1)+"\"");
			}else{
				dataMap.put("attachfiles", "无");
				dataMap.put("filepath", "");
			}
			String cnprefix = "<p>————————————————————本文来源于国外网站,以下是机器翻译内容,向下阅读可查看原文————————————————————</p>";
			String enprefix = "<p>————————————————————原文————————————————————</p>";
			String text = news.getFormattedContent();
			dataMap.put("titleTr", news.getTitle());
			dataMap.put("contentTr", news.getContent());
			dataMap.put("title", news.getTitle());
			dataMap.put("content", getWordContent(text.substring(text.indexOf(enprefix) + enprefix.length())));

			try {
				DocUtils docUtils = new DocUtils();
				docUtils.createDoc(dataMap, "D:/data/shemi/"+ news.getSiteDomain() + "/" + news.getTitleTr().replace(" ","").replace("-","") + ".doc");
			} catch (Exception e) {
				e.printStackTrace();
			}*/
		}
		System.out.println();
	}

	public String getWordContent(String htmlContent) {
		String[] split = htmlContent.split("</p>");
		StringBuilder sb = new StringBuilder();
		if(split.length > 0) {
			for(String str : split) {
				sb.append(str.replaceAll("<.*?>", "")).append("</w:t></w:r></w:p><w:p w:rsidR=\"00286808\" w:rsidRDefault=\"009213D8\"><w:r><w:rPr><w:rFonts w:hint=\"eastAsia\"/></w:rPr><w:t>");

			}
		}
		return sb.toString();
	}

	public static String getTargetEnHtml(News news) {
		StringBuilder sb = new StringBuilder();

		String cnprefix = "<p style=\"text-align:center\"><span style=\"color:red\">————————————————————本文来源于国外网站,以下是机器翻译内容,向下阅读可查看原文————————————————————</span></p>";
		String enprefix = "<p style=\"text-align: center;\"><span>————————————————————原文————————————————————</span></p>";

		// 发布时间与原文链接, 来源网站
		String pubdate = news.getPubdate();
		String url = news.getUrl();
		sb.append("<b>发布时间: </b><p> " + pubdate.trim() + "</p>").append("<b>原文链接:</b><p> " + url.trim() + "</p>");

		String titleTr = news.getTitleTr();
		sb.append("<b>译文标题: </b><p>" + titleTr.trim() + "</p>");
		String contentTr = news.getContentTr();
		contentTr = contentTr.substring(cnprefix.length(), contentTr.indexOf(enprefix));
		contentTr = contentTr.replace("${RootPath}", "D:/data/webmagic");
		sb.append("<b>译文: </b><p>" + contentTr.trim() + "</p>");
		// 原文的标题和正文
		String title = news.getTitle();
		String content = news.getContent();
		content = content.replace("${RootPath}", "D:/data/webmagic");
		sb.append("<b>原文标题:</b><p> " + title.trim() + "</p>");
		sb.append("<b>原文:</b> <p>" + content.trim() + "</p>");

		return sb.toString();
	}
	public static String getTargetCnHtml(News news) {
		StringBuilder sb = new StringBuilder();

		// 发布时间与原文链接, 来源网站
		String pubdate = news.getPubdate();
		String url = news.getUrl();
		sb.append("<b>发布时间: </b><p> " + pubdate.trim() + "</p>").append("<b>原文链接:</b><p> " + url.trim() + "</p>");

		String titleTr = news.getTitle();
		sb.append("<b>标题: </b><p>" + titleTr.trim() + "</p>");
		String contentTr = news.getContent();
		contentTr = contentTr.replace("${RootPath}", "D:/data/webmagic");
		sb.append("<b>译文: </b><p>" + contentTr.trim() + "</p>");

		return sb.toString();
	}

	@Test
	public void testNotExistPic() {
		List<News> list = dao.selectTotalNews();
		for(News news : list) {
			if(!StringUtils.isEmpty(news.getHeadimg())) {
				String headimg = news.getHeadimg();
				headimg = headimg.replace("${RootPath}", "D:/data/webmagic");
				File f = new File(headimg);
				if(!f.exists()) {
					System.out.println(headimg + " is not exist!!!");
				}
			}
		}
	}

	/**
	 * 调整小安要的数据的格式
	 *
	 */
	@Test
	public void testChangeDb2NewTable() {
		List<News> list = dao.selectTotalNews();
		System.out.println();
		for (News news  : list) {
			news.setId(UUID.randomUUID().toString().replace("-",""));
			if(null != news.getContentTr() && news.getContentTr().contains("${RootPath}")) {
				news.setContentTr(news.getContentTr().replace("${RootPath}", "/upload/imgs"));
			}
			if(null != news.getHeadimg() && news.getHeadimg().contains("${RootPath}")) {
				news.setHeadimg(news.getHeadimg().replace("${RootPath}", "/upload/imgs"));
			}
			dao.insertEn2XWZX(news);
			System.out.println("====================" + (1 + list.indexOf(news)));

		}
	}

	/**
	 * 翻译 excel 的对应的 简介
	 */
	@Test
	public void testTranslateIntriduction() {
		List<News> list = dao.selectIntroduction();
		for(News news : list){
			if(news.getSummary() == null) {
				continue;
			}
			news.setSummary(translate.translate(news.getSummary()).trim());
			dao.updateAA(news);
		}

	}

	/**
	 * 将excel 文件导入到数据库中
	 */
	@Test
	public void testImportExel2Db() throws Exception {
		// List<Map<String, String>> maps = ExcelUtils.readExcel(new File("C:\\Users\\wgshb\\Desktop\\军队-磁场环境.xls"));
		// List<Map<String, String>> maps = ExcelUtils.readExcel(new File("C:\\Users\\wgshb\\Desktop\\军队-太空战场环境要素.xls"));
		List<Map<String, String>> maps = ExcelUtils.readExcel(new File("C:\\Users\\wgshb\\Desktop\\军队-网络环境要素.xls"));
		// List<Map<String, String>> maps = ExcelUtils.readExcel(new File("C:\\Users\\wgshb\\Desktop\\军队-核生化.xls"));
		for(Map<String, String> map : maps) {
			dao.addExcel2AA(map);
		}
	}

	/**
	 * 将 txt 文本导入到 数据库中
	 */
	@Test
	public void testEditDistance() throws IOException {
		List<String> hangtianbaikeNameList = dao.getHangtianbaikeNameList();
		File file = new File("htbk.txt");// Text文件
		BufferedReader br = new BufferedReader(new FileReader(file));// 构造一个BufferedReader类来读取文件
		String s = null;
		while ((s = br.readLine()) != null) {// 使用readLine方法，一次读一行
			News news = new News();
			String[] split = s.split("\\t");
			String id = split[0];
			String title = split[1];
			if(!hangtianbaikeNameList.contains(title)) {
				String tags = split[2];
				if(tags != null && tags.length() > 1 && tags.contains(";")) {
					if(tags.startsWith(";")) {
						tags = tags.substring(1, tags.length());
					}
					if(tags.endsWith(";")) {
						tags = tags.subSequence(0, tags.length() - 1).toString();
					}
				}
				String summary = split[3];
				String content = split[4];
				//System.out.println(title + " <----> " + tags + " <----> " + summary+ " <----> " + content);
				news.setId(id);
				news.setTitle(title);
				news.setKeywords(tags);
				news.setSummary(summary);
				news.setContent(content);
				dao.addHangtianbaike2LocalTable(news);

			}

		}
		br.close();
	}

	/**
	 * 为新闻正文添加对应的图片字段信息
	 */
	@Test
	public void testAddImg() {
		// 1. 查询对应的新闻正文内容,
		List<News> list = dao.selectNewsContent2AddImg();

		// 2. 添加图片信息
		for(News news : list) {
			String contentTr = news.getContentTr();
			String attchfiles = news.getAttchfiles();
			if(contentTr.contains("<div id=\"map_canvas_child\"></div>")) {
				contentTr = contentTr.replace("<div id=\"map_canvas_child\"></div>","<div id=\"map_canvas_child\"><img src=\""+ attchfiles +"\"/></div>");
				news.setContentTr(contentTr);
				dao.updateContentTr(news);
			}

		}
		// 3. 进行内容的替换
	}

	/**
	 * 将数据库采集的英文数据转化为 word 格式
	 */
	@Test
	public void testTranslateColumn2Words() {
		List<News> list = dao.getTargetNewsEnColumns();
		HtmlToWordByPOI poi = new HtmlToWordByPOI();
		poi.setRootPath("D:/tmp/");
		for(News news : list) {
			poi.toWord(news.getContentTr().replace("————————————————————本文来源于国外网站,以下是机器翻译内容,向下阅读可查看原文————————————————————",		"——————本文来源于国外网站,以下是机器翻译内容,向下阅读可查看原文—————").replace("————————————————————原文————————————————————","——————————————————原文——————————————————").replace("${RootPath}","D:/data/webmagic"), "D:/tmp/weather/" + news.getSiteDomain() + "/"  + news.getTitle() + "(" + news.getTitleTr()  + ")_" + news.getPubdate() +".doc");
		}
	}

	@Test
	public void testDeleteDownLoadColumn() {
		List<News> list = dao.getNews2Delete();
		for(News news : list) {
			String filename = news.getAttchfiles().substring(news.getAttchfiles().lastIndexOf("/") + 1, news.getAttchfiles().length());
			if(new File("D:/data/webmagic/nsgreg.nga.mil/files/"+ filename).isFile()) {
				news.setAttchfiles("");
				dao.updateAttchfiles2null(news);
				System.out.println("清空对应的内容-------->" + filename);
			}
		}
	}


	// 获取下载的附件
	@Test
	public void testDownloadAttachfiles() throws IOException {
		List<News> files = dao.getAttachFiles2Download();
		for (News news : files) {
			String f = news.getUrl();
			/*if (f.contains(" ")) {
				f = f.replace(" ", "%20");
			}*/
			//File file = new File("D:/data/webmagic/nsgreg.nga.mil/files/" + f);
			System.setProperty("webdriver.chrome.marionette", "chromedriver.exe");
			// 无头浏览器
			// ChromeOptions co = new ChromeOptions();
			// co.addArguments("--headless");
			// WebDriver driver = new ChromeDriver(co);
			WebDriver driver = new ChromeDriver();
			driver.manage().window().maximize();
			// 打开起始页
			driver.get(f);
			try {
				driver.findElement(By.xpath("//a[contains(@onmouseover,'window.status=')]")).click();
				try {
					Thread.sleep(30000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			driver.quit();

		}
	}

	// 抽取新闻的正文部分的附件字段及对应的发布时间字段
	@Test
	public void testExtractPdfAndPubdate() {
		//1. 查询当天采集的数据的正文部分
		List<News> list = dao.selectContentFromNewEn();
		for (News news : list) {
			System.out.print(list.indexOf(news) + 1 + " : ");
			//2. 抽取其中的 pdf 部分
			String pdf = extractpdf(news.getContent());
			//3. 抽取其中的 发布时间 部分
			String pubdate = extractpubdate(news.getContent());
			//4. 进行数据的更新
			news.setAttchfiles(pdf);
			news.setPubdate(pubdate);
			System.out.println("------------> " + news.getId() + " -- " + news.getAttchfiles() + " -- " + news.getPubdate());
			dao.updatePdfAndPubdate(news);

		}
	}

	private String extractpubdate(String content) {
		String[] trs = content.split("</tr>");
		String pubdate = "";
		for (String tr : trs) {
			if (tr.contains("Edition Date:")) {
				pubdate = tr.replace("Edition Date:", "").replaceAll("(<.*?>)", "").trim();
				//System.out.print("--------------> " + pubdate);
				try {
					pubdate = HtmlFormatter.convertPubDate(pubdate);
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
		}
		return pubdate;
	}

	private String extractpdf(String content) {
		String pdf = "";
		String[] trs = content.split("</tr>");
		for (String tr : trs) {
			if (tr.contains("Document: ")) {
				Matcher m = Pattern.compile("(<a(.*?)href=\")(.*?)\"").matcher(tr);
				if (m.find()) {
					pdf = m.group(3);
					//System.out.print("----" + pdf + "\n");
				}
			}
		}
		return pdf;
	}

	@Test
	public void testIdData() {
		List<String> ids = dao.getAllIds("news_wx_19");
		addList2LocalTxt(ids, "F:/url.txt");
	}

	/**
	 * 将集合中的内容输出到本地的文件
	 */
	public static void addList2LocalTxt(List<String> list, String targetPath) {
		try {
			File f = new File(targetPath);
			// 创建新的文件
			f.createNewFile();
			FileWriter fw = new FileWriter(f, true);

			BufferedWriter bw = new BufferedWriter(fw);
			for (String str : list) {
				bw.write(str + "\r\n ");// 往已有的文件上添加字符串
			}
			bw.close();
			fw.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testXmlInsert() {
		// 读取数据库中所有的包含 attchfiles 属性的数据, 替换其对应的 headimg 属性(若之前的属性为空的话)
		List<News> attchfilesList = dao.selectNewsAttchfiles();
		// 直接下载文件到新的地址, 同时保存对应的文件的名称
		for (News news : attchfilesList) {
			String name = news.getAttchfiles();
			try {
				String toPath = "F:/novel/newimg/" + UUID.randomUUID().toString().replace("-", "") + news.getAttchfiles().substring(news.getAttchfiles().lastIndexOf("."), news.getAttchfiles().length());
				new HttpFileUtil().getFileTo(news.getAttchfiles(), toPath, new HashMap<>());
				String headimg = "${RootPath}" + File.separator + toPath.subSequence(toPath.lastIndexOf("/") + 1, toPath.length());
				news.setHeadimg(headimg);
				// 更新封面字段
				dao.updateHeadimg(news);
			} catch (IOException e) {
				System.out.println(news.getTitle() + " <------------------------------------------------------------------------------ 封面拷贝出错");
				e.printStackTrace();
			}
			System.out.println(attchfilesList.indexOf(news) + 1 + "/" + attchfilesList.size());
		}


	}

	public static List<String> getFileContext(String path) throws Exception {
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-8"));
		List<String> list = new ArrayList<String>();
		String str = null;
		while ((str = bufferedReader.readLine()) != null) {
			if (str.trim().length() > 2) {
				list.add(str);
			}
		}
		bufferedReader.close();
		return list;
	}

	/**
	 * 获取专业期刊的封面相关属性
	 *
	 * @throws Exception
	 */
	@Test
	public void downloadHeadImg() throws Exception {
		List<String> keysList = getFileContext("F:/novel/en.txt");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
		for (String keys : keysList) {
			News news = new News();
			news.setId(UUID.randomUUID().toString().replace("-", ""));
			news.setTitle(keys);

			news.setCrawlerdate(sdf.parse(sdf.format(new Date())));
			//下载单曲音频
			System.setProperty("webdriver.chrome.marionette", "chromedriver.exe");
			ChromeOptions chromeOptions = new ChromeOptions();
			chromeOptions.addArguments("--headless");
			WebDriver driver = new ChromeDriver(chromeOptions);
			driver.manage().window().maximize();
			driver.get("http://mall.cnki.net/magazine/ArticleSearchNew.aspx");
			driver.findElement(By.id("headq")).sendKeys(keys);
			driver.findElement(By.id("button_shswx")).click();
			Set<String> windowHandles = driver.getWindowHandles();
			//System.out.println(driver.getWindowHandle());
			for (String windowhandle : windowHandles) {
				if (windowhandle != driver.getWindowHandle()) {
					//temp.close();
					// 切换到播放音乐的窗口(句柄)
					driver.switchTo().window(windowhandle);
				}
			}
			List<WebElement> h2s = driver.findElements(By.xpath("//h2"));
			if (null == h2s) {
				System.out.println("---------------------");
			} else if (h2s.size() == 1) {
				try {
					String title = h2s.get(0).getAttribute("title");
					//System.out.println("------------>" + title);
					WebElement aEle = driver.findElement(By.xpath("//ul[@class='tablist']/li/a"));
					String href = aEle.getAttribute("href");
					WebDriver temp = new ChromeDriver(chromeOptions);
					temp.manage().window().maximize();
					temp.get(href);
					WebElement img = temp.findElement(By.id("faceImg"));
					WebElement p = temp.findElement(By.xpath("//span[@id='bookmessage']/p"));
					String imgPath = img.getAttribute("src");
					String toPath = "F:/novel/img/" + UUID.randomUUID().toString().replace("-", "") + imgPath.substring(imgPath.lastIndexOf("."), imgPath.length());
					new HttpFileUtil().getFileTo(imgPath, toPath, new HashMap<>());
					news.setAttchfiles(imgPath);
					news.setHeadimg("${RootPath}" + File.separator + toPath.subSequence(toPath.lastIndexOf("/") + 1, toPath.length()));
					String text = p.getText();
					news.setContent(text);
					temp.quit();
				} catch (Exception e) {
					e.printStackTrace();
				}

			} else if (h2s.size() > 1) {
				for (WebElement ele : h2s) {
					try {
						String title = ele.getAttribute("title");
						//System.out.println("------------>" + title);
						if (title != null && title.equals(keys)) {
							WebElement aEle = driver.findElement(By.xpath("//h2[@title='" + title + "']/.."));
							String href = aEle.getAttribute("href");
							WebDriver temp = new ChromeDriver(chromeOptions);
							temp.manage().window().maximize();
							temp.get(href);
							WebElement img = temp.findElement(By.id("faceImg"));
							WebElement p = temp.findElement(By.xpath("//span[@id='bookmessage']/p"));
							String imgPath = img.getAttribute("src");
							String toPath = "F:/novel/img/" + UUID.randomUUID().toString().replace("-", "") + imgPath.substring(imgPath.lastIndexOf("."), imgPath.length());
							new HttpFileUtil().getFileTo(imgPath, toPath, new HashMap<>());
							news.setAttchfiles(imgPath);
							news.setHeadimg("${RootPath}" + File.separator + toPath.subSequence(toPath.lastIndexOf("/") + 1, toPath.length()));
							String text = p.getText();
							news.setContent(text);
//						System.out.println(imgPath);
//						System.out.println(text);
							temp.quit();

						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

			dao.insertHeadImg(news);
			driver.quit();
			// 输出文件的内容到 mysql
		}

	}

	/**
	 * 部分的新闻的正文部分图片的路径存在 ? 号, 需要进行替换...
	 */
	@Test
	public void testRemoveSo() {
		String tablename = "hymax_wei_xin_gong_zhong_hao_copy";
		List<News> list = dao.getNewsContent(tablename);
		for (News news : list) {
			String newText = news.getContent();
			if (newText.contains("<img")) {
				// 格式化所有的图片标签
				Matcher m = Pattern.compile("<img(.[^>]*?)((src|SRC)=(\"|\')(.[^>]*?)(\"|\')[^>])*>").matcher(newText);
				while (m.find()) {
					String group = m.group();

					String g = "";
					if (group.contains("src")) {
						String t = group.substring(group.indexOf(" src=") + 5, group.indexOf(" src=") + 6);
						String re = group.substring(4, group.indexOf(" src=")).trim();
						String temp = group.replace(re, "").replaceFirst(t, "");

						g = temp.substring(temp.indexOf(" src=") + 1, temp.length() - 1);
						g = g.replace("src=", "src=" + t);
						if (!StringUtils.isEmpty(g)) {
							if (g.contains(" "))
								g = g.substring(0, g.indexOf(" "));
						}

						// 去除图片的结尾处的 ?
						if (g.contains("?")) {
							//g = g.replace("?", "");
							news.setContent(news.getContent().replace(g, g.replace("?", "")));

						}
					}

				}

			}
		}
	}

	/**
	 * 按照标题去除数据库中的重复数据
	 * 对于标题重复的数据, 干掉采集时间较早的记录
	 */
	@Test
	public void testRemoveDulpRecored() {
		String tablename = "news_en";
		// 获取重复数据的标题的集合
		List<String> titles = dao.getDulpTitles(tablename);
		// 包含最小的采集时间的集合
		List<News> newsList = dao.getTitleWithMinCrawlerdate(tablename, titles);
		// 标题重复的集合
		List<News> newsTotalList = dao.getTitleWithCrawlerdate(tablename, titles);
		List<String> newsIds = getIdsFromList(newsList);
		List<String> newsTotalIds = getIdsFromList(newsTotalList);
		// 求出 newsTotalList 与 newsList 的差集
		newsTotalIds.removeAll(newsIds);

		dao.deleteNewsBatch(tablename, newsTotalIds);

		System.out.println(newsTotalIds.size());
	}

	private List<String> getIdsFromList(List<News> newsList) {
		List<String> ids = new ArrayList<>();
		if (newsList.size() > 0) {
			for (News news : newsList) {
				ids.add(news.getId());
			}
		}
		return ids;
	}

	@Test
	public void testImageFormat() {
		String path = System.getProperty("user.dir") + "\\image";
		HttpFileUtil.resizeImagesDirs(path);
	}

	@Test
	public void testCreateHtml() {
		List<News> list = dao.getNews2Makeup();
		for (News news : list) {
			createHtml(news);
		}
	}

	private void createHtml(News news) {

		StringBuilder stringHtml = new StringBuilder();
		PrintStream printStream = null;
		int count = 0;
		try {
			File file = new File("./htmls/" + news.getId() + ".html");
			if (!file.exists()) {
				// 创建新文件
				try {
					file.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			//打开文件
			printStream = new PrintStream(new FileOutputStream(file));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		//输入HTML文件内容
		stringHtml.append("<html><head>");
		stringHtml.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");
		stringHtml.append("<title>" + news.getTitle() + "</title>");
		stringHtml.append("</head>");
		stringHtml.append("<body>");
		stringHtml.append(news.getContent().replace("${RootPath}", "F:/BaiduNetdiskDownload/upyun"));
		stringHtml.append("</body></html>");
		try {
			//将HTML文件内容写入文件中
			printStream.println(stringHtml.toString());
			System.out.println("finish output html + " + (count++));
		} catch (Exception e) {
			e.printStackTrace();

		}
	}

	/**
	 * 获取待处理的新闻记录
	 */
	@Test
	public void testProcessNews() {
		String prefixSrc = "${RootPath}/weixin.sogou.com/image";
		String olaPrefixSrc = "http://weixin-images.bestdatatech.com/images";
		String pdfpath = "/weixin.sogou.com/";
		List<News> list = dao.getNews2Makeup();
		for (News news : list) {
			String id = DigestUtils.md5Hex(JSON.toJSONString(news.getTitle().trim()));
			// String date = getDate(Long.parseLong(news.getPubdate()));
			String attchfiles = "";
			if (news.getAttchfiles().contains("[")) {
				attchfiles = news.getAttchfiles().replace(olaPrefixSrc, prefixSrc).replace("[", "").replace("]", "")
						.replace("\"", "");
			} else {
				attchfiles = news.getAttchfiles().replace(olaPrefixSrc, prefixSrc);
			}
			String content = news.getContent().replace(olaPrefixSrc, prefixSrc);
			String headimg = news.getHeadimg().replace(olaPrefixSrc, prefixSrc);
			String pdffiles = pdfpath + news.getId() + ".pdf";
			String filename = news.getChatnumber() + "-" + news.getTitle();

			news.setId(id);
			news.setAttchfiles(attchfiles);
			news.setContent(content.replaceAll("<iframe(.*?)</iframe>", ""));
			news.setHeadimg(headimg);
			news.setPdffiles(pdffiles);
			news.setFilename(filename);
			news.setSiteDomain("weixin.sogou.com");
			news.setNewsCategory("微信资讯");
			news.setDbType("news_cn");

			dao.updateNewsColumns(news);

			// System.out.println(date+ "id: " + id);
			// System.out.println("content: " + news.getContent() + "\n" +
			// news.getTitle()+"\n" + news.getPubdate() +"\n"
			// +news.getAttchfiles() +"\n" + news.getHeadimg());
		}
	}

	private String getDate(Long t) {
		Date date = new Date(t);

		SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		return sd.format(date);
	}

	/**
	 * 获取所有的武器库的名称
	 */
	@Test
	public void testGatherWeaponNames() throws IOException {
		List<String> list = dao.getWeaponNames();
		System.out.println("================" + list.size());
		BufferedWriter writer = new BufferedWriter(new FileWriter("names.txt"));
		for (String str : list) {
			if (str.length() > 0) {
				writer.write(str);
				writer.newLine();
				writer.flush();
			}
		}
		writer.close();
	}

	/**
	 * 更新摘要字段
	 */
	@Test
	public void testUpdateNewSummary() {
		List<News> list = dao.selectNews2update();
		for (News news : list) {
			String summary = getSummaryByContent(news.getFormattedContent());
			news.setSummary(summary);
			// System.out.println(news.getSummary().length());
			if (news.getSummary().length() > 30) {
				dao.updateNewsSummary(news);
			}
			// dao.updateNewsSummary(news);

		}
	}

	private String getSummaryByContent(String content) {
		content = content.replaceAll("<p(.*?)>", "").replaceAll("</p>", "").replaceAll("<img(.*?)>", "");
		if (content.length() <= 200 && content.length() > 150) {
			return content.trim().substring(0, content.length() - 1) + "...";
		} else if (content.length() > 200) {
			return content.trim().substring(0, 200) + "...";
		}
		return "";

	}

	/**
	 * 抽取采集的数据进行格式化
	 */
	@Test
	public void testSeparateKeywords2Classifies() {
		List<KeywordsThesaurus> list = thesaurusMapper.getAllTargetFields();
		List<KeywordsThesaurus> keywords = new ArrayList<>(list.size());
		for (KeywordsThesaurus thesaurus : list) {
			// 生成临时的 KeywordsThesaurus, 用于存放相关的属性
			KeywordsThesaurus keywordThesaurus = new KeywordsThesaurus();
			keywordThesaurus.setId(thesaurus.getId());
			StringBuilder sb = new StringBuilder();
			if (!StringUtils.isEmpty(thesaurus.getUpperWords())) {
				sb.append(thesaurus.getUpperWords() + "&&&");
			}
			if (!StringUtils.isEmpty(thesaurus.getLowerWords())) {
				sb.append(thesaurus.getLowerWords() + "&&&");
			}
			if (!StringUtils.isEmpty(thesaurus.getReferWords())) {
				sb.append(thesaurus.getReferWords() + "&&&");
			}

			if (!StringUtils.isEmpty(sb.toString())) {
				// 仅仅包含一个属性值
				if (!sb.toString().replaceFirst("&&&", "").contains("&&&")) {
					String temp = sb.toString().replaceFirst("&&&", "").trim();
					String prefix = temp.substring(0, 1);
					String value = temp.substring(2, temp.length());
					switch (prefix) {
						case "S":
							keywordThesaurus.setUpperWords(value);
							break;
						case "F":
							keywordThesaurus.setLowerWords(value);
							break;
						case "C":
							keywordThesaurus.setReferWords(value);
							break;
						case "Y":
							keywordThesaurus.setFormalWords(value);
							break;
						case "D":
							keywordThesaurus.setInformalWords(value);
							break;
					}
				} else {
					// 包含多个属性值
					String[] words = sb.toString().trim().split("&&&");
					for (String word : words) {
						String prefix = word.substring(0, 1);
						String value = word.substring(2, word.length());
						switch (prefix) {
							case "S":
								keywordThesaurus.setUpperWords(value);
								break;
							case "F":
								keywordThesaurus.setLowerWords(value);
								break;
							case "C":
								keywordThesaurus.setReferWords(value);
								break;
							case "Y":
								keywordThesaurus.setFormalWords(value);
								break;
							case "D":
								keywordThesaurus.setInformalWords(value);
								break;
						}
					}

				}
				keywords.add(keywordThesaurus);
			}
		}
		System.out.println("总的数据量为 ------------> " + keywords.size());
		// 批量更新目标的词表相关的值
		for (int i = 0, n = keywords.size(); i < n; i++) {
			if (i > 0 && i % 1000 == 0 || i == n - 1) {
				int startIndex = (i > 1000) ? i - 1000 : 0;
				int toIndex = i == n - 1 ? n - 1 : i;
				List<KeywordsThesaurus> subList = keywords.subList(startIndex, toIndex);

				thesaurusMapper.batchUpdateKeywordsThesaurus(subList);
				System.out.println("更新结果 ------------- " + i + "条");
			}
		}
	}

	/**
	 * 去除采集的范畴号的空格
	 */
	@Test
	public void testTrimThesaurusNum() {
		List<KeywordsThesaurus> list = thesaurusMapper.getAllThesaurusNum4Trim();
		List<KeywordsThesaurus> keywords = new ArrayList<>(list.size());
		long starttime = System.currentTimeMillis();
		for (KeywordsThesaurus thesaurus : list) {
			if (!StringUtils.isEmpty(thesaurus.getCategoryNum())) {
				String categoryNum = thesaurus.getCategoryNum().trim();
				Matcher m = Pattern.compile("\\d{4}").matcher(categoryNum);
				if (m.find()) {
					// System.out.println(m.group());
					thesaurus.setCategoryNum(m.group());
					// thesaurusMapper.trimThesaurusNum(thesaurus);
					keywords.add(thesaurus);
				}
			}
		}
		for (int i = 0, n = list.size(); i < n; i++) {
			if (i > 0 && i % 1000 == 0 || i == n - 1) {
				int startIndex = (i > 1000) ? i - 1000 : 0;
				int toIndex = i == n - 1 ? n - 1 : i;
				List<KeywordsThesaurus> subList = keywords.subList(startIndex, toIndex);
				thesaurusMapper.batchUpdateKeywordsThesaurus(subList);
			}
		}

		System.out.println("===运行耗时" + (System.currentTimeMillis() - starttime) + "SSS");
	}

	/**
	 * 添加关键字范畴相关的关系到表
	 *
	 * @throws IOException
	 */
	@Test
	public void testInsert2Thesaurus() throws IOException {
		Map<String, Map<String, String>> data = new HashMap<>();
		// BufferedReader br = new BufferedReader(new FileReader(new
		// File("words.txt")));
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("words.txt"), "GB2312"));
		String content = null;
		// List<String> jsons = new ArrayList<String>(100);
		while ((content = br.readLine()) != null) {
			try {
				int fromindex = content.indexOf("id=\"") + 4;
				String dataKey = content.subSequence(fromindex, fromindex + 6).toString();
				String[] as = content.split("</a>");
				Map<String, String> temp = new HashMap<>();
				KeywordsThesaurus thesaurus = new KeywordsThesaurus();
				thesaurus.setClassifyNum(dataKey);
				for (String a : as) {
					if (a.contains("<a")) {
						String tempKey = a.subSequence(a.indexOf("'") + 1, a.indexOf("'") + 5).toString().trim();
						String tempVal = a.subSequence(a.indexOf(tempKey) + 8, a.length()).toString().trim();
						// temp.put(tempKey, tempVal);
						thesaurus.setCategoryNum(tempKey);
						thesaurus.setCategoryName(tempVal);
						thesaurusMapper.insertThesaurus(thesaurus);
					}
				}
				// data.put(dataKey, temp);
			} catch (Exception e) {
				System.out.println("=====" + content.subSequence(0, 30));
				e.printStackTrace();
			}
		}
	}

	/**
	 * 格式化新闻的正文...
	 *
	 * @param content
	 * @return
	 */
	private String formatContent(String content) {
		// 对于外文的正文部分的开头内容进行替换标签的操作
		content = content.replaceAll("<div(.*?)>", "<p>").replaceAll("</div>", "</p>");
		StringBuilder sb = new StringBuilder();
		// 判断正文部分若包含 img 标签, img 标签是否被 p 标签包围
		String regex = "<p(.*?)<img(.*?)</p";
		String imgRegex = "<img(.*?)>";
		Matcher m = Pattern.compile(regex).matcher(content);
		Matcher imgMatcher = Pattern.compile(imgRegex).matcher(content);
		boolean flag = content.contains("<img") && !m.matches();
		if (flag) {
			while (imgMatcher.find()) {
				String imgStr = imgMatcher.group();
				imgStr = imgStr.replaceAll("\\.", "&&&").replaceAll("\\$\\{", "&&").replaceAll("\\}", "@@@");
				content = content.replaceAll("\\.", "&&&").replaceAll("\\$\\{", "&&").replaceAll("\\}", "@@@");
				String replaceMent = "<p>" + imgStr + "</p>";
				content = content.replace(imgStr, replaceMent).replace("&&&", ".").replace("&&", "${").replace("@@@",
						"}");
			}
		}

		// 正文部分由 br 进行换行
		if (!content.contains("<p") && content.contains("<br>")) {
			content = content.replaceAll("<div(.*?)>", "").replaceAll("</div>", "");
			regex = "(.*?)<br>";
			Matcher brMatcher = Pattern.compile(regex).matcher(content);
			while (brMatcher.find()) {
				// System.out.println(brMatcher.group());
				content = content.replace(brMatcher.group(), "<p>" + brMatcher.group() + "</p>");
			}
		}

		Document d = Jsoup.parseBodyFragment(content);
		// 获取指定的标签的集合
		Elements elementsByTag = d.getElementsByTag("p");
		for (Element e : elementsByTag) {
			if (e.children().size() > 0 && e.children().toString().contains("<img")) {
				sb.append("<p class=\"detail-pic\">" + e.children().toString() + "</p>");
				// System.out.println("<p>" + e.children().toString() + "</p>");
			} else {
				if (!StringUtils.isEmpty(e.text())) {
					sb.append("<p>" + e.text() + "</p>");
				}
				// System.out.println("<p>" + e.text() + "</p>");
			}
		}
		return sb.toString();
	}

	/**
	 * 为武器库添加同义词
	 *
	 * @throws IOException
	 */
	@Test
	public void testAddWeaponNames4Search() throws IOException {
		List<Weapon> list = mapper.getAllContent();
		System.out.println("================" + list.size());
		for (Weapon weapon : list) {
			String synonymTitle = getSynonymTitle(weapon.getTitle());
			if (!synonymTitle.equals(weapon.getTitle())) {
				weapon.setSynonymTitle(weapon.getTitle() + "," + synonymTitle);
			} else {
				weapon.setSynonymTitle(synonymTitle);
			}
			mapper.addSynonymTitle(weapon);
		}
	}

	private String getSynonymTitle(String title) {
		String synonymTitle = "";
		if (title.trim().contains("/")) {
			String[] temp = title.trim().split("/");
			/*
			 * String tempTitle = title.replaceAll("/", ""); String regexTemp =
			 * "[0-9]+"; Matcher m2 =
			 * Pattern.compile(regexTemp).matcher(tempTitle); List<String> list
			 * = new ArrayList<>(); while(m2.find()) { list.add(m2.group());
			 * tempTitle = tempTitle.replaceAll(m2.group(), "&&"); } for(String
			 * str : temp) { m2 = Pattern.compile(regexTemp).matcher(str);
			 * if(m2.find()) { synonymTitle += tempTitle.replace("&&",
			 * m2.group()) + ","; } }
			 */
			for (String str : temp) {
				synonymTitle += str + ",";
			}
			synonymTitle = synonymTitle.substring(0, synonymTitle.length() - 1).replaceAll("\\.", "")
					.replaceAll("“", "").replaceAll("”", "").replaceAll("\\)", "").replaceAll("）", "")
					.replaceAll("\\([\\s\\S]+", "").replaceAll("\\（[\\s\\S]+", "").replaceAll("/[\\s\\S]+", "")
					.replaceAll(" ", "").replaceAll("-", "");
			System.out.println("===============5" + synonymTitle);
			return synonymTitle;
		}
		if (title.trim().contains(".")) {
			synonymTitle = title.replaceAll("\\.", "").replaceAll("“", "").replaceAll("”", "")
					.replaceAll("\\([\\s\\S]+", "").replaceAll("\\（[\\s\\S]+", "").replaceAll("/[\\s\\S]+", "")
					.replaceAll(" ", "").replaceAll("-", "");
			System.out.println(synonymTitle);
			return synonymTitle;
		}
		if (title.trim().contains("“") || title.trim().contains("”")) {
			synonymTitle = title.replaceAll("“", "").replaceAll("”", "").replaceAll("\\([\\s\\S]+", "")
					.replaceAll("\\（[\\s\\S]+", "").replaceAll("/[\\s\\S]+", "").replaceAll(" ", "")
					.replaceAll("-", "");
			System.out.println(synonymTitle);
			return synonymTitle;
		}
		if (title.trim().contains("(") || title.trim().contains("（")) {
			synonymTitle = title.replaceAll("\\([\\s\\S]+", "").replaceAll("\\（[\\s\\S]+", "").replaceAll(" ", "")
					.replaceAll("-", "");
			System.out.println(synonymTitle);
			return synonymTitle;
		}
		if (title.trim().contains("\\u201c") || title.trim().contains("\\u201d")) {
			synonymTitle = title.replaceAll("\\u201c", "").replaceAll("\\u201d", "").replaceAll(" ", "").replaceAll("-",
					"");
			System.out.println(synonymTitle);
			return synonymTitle;
		}

		if (title.trim().contains(" ")) {
			synonymTitle = title.replaceAll(" ", "").replaceAll("-", "");
			System.out.println(synonymTitle);
			return synonymTitle;
		}
		if (title.trim().contains("-")) {
			synonymTitle = title.replaceAll("-", "");
			System.out.println(synonymTitle);
			return synonymTitle;
		}
		String regex = "[a-zA-Z\u4e00-\u9fa5][a-zA-Z0-9\u4e00-\u9fa5]+";
		Matcher m = Pattern.compile(regex).matcher(title);
		if (m.find()) {
			synonymTitle = m.group();
			System.out.println("===============" + m.group());
			return synonymTitle;
		}
		return synonymTitle;
	}

	/*
	 * 为数据库的数据添加指定的前缀
	 *
	 * @Test public void testAddPrefix() { List<News> newses =
	 * dao.getNamespace4Replace(); List<News> lists = new ArrayList<>();
	 * for(News news : newses) { String attchfiles = news.getAttchfiles();
	 * String title = news.getTitle(); String content = news.getContent();
	 * String contentTr = news.getContentTr();
	 * if(StringUtils.isEmpty(attchfiles) || attchfiles.contains("${RootPath}"))
	 * { continue; }
	 *
	 * if(StringUtils.isEmpty(attchfiles) || contentTr.contains("${RootPath}"))
	 * { continue; } System.out.println("--------------begin" +
	 * contentTr.length()); contentTr = addPrefix(contentTr); content =
	 * addPrefix(content); System.out.println("--------------after" +
	 * contentTr.length());
	 *
	 * attchfiles = addPrefix(attchfiles); news.setAttchfiles(attchfiles);
	 * //news.setContentTr(contentTr); //news.setContent(content);
	 * //news.setTitle(title); //lists.add(news); dao.updateNews(news,
	 * "news_cn_copy");
	 *
	 * if((!StringUtils.isEmpty(attchfiles) &&
	 * !attchfiles.contains("${RootPath}")) || (!StringUtils.isEmpty(title) &&
	 * !title.contains("${RootPath}"))) { if(attchfiles != null) { attchfiles =
	 * addPrefix(attchfiles); } if(title != null) { title = addPrefix(title); }
	 * //content = addPrefix(content); news.setAttchfiles(attchfiles);
	 * news.setContent(content); news.setTitle(title); lists.add(news); } } //
	 * 更新数据库 //dao.updatebatch(lists); }
	 */

	// 添加指定的前缀
	private String addPrefix(String str) {
		if (str.contains("null\\")) {
			str = str.replaceAll("null", "");
			str = str.replaceAll("\\\\", "/");
			str = str.replaceAll("//", "/");
		}
		// (?<=src=\")
		String regex = "/[a-zA-Z0-9][-a-zA-Z0-9]{0,62}(.[a-zA-Z0-9][-a-zA-Z0-9]{0,62})+.?";
		Matcher m = Pattern.compile(regex).matcher(str);
		String temp = null;
		if (m.find()) {
			// System.out.println(m.group());
			String group = m.group();
			temp = group.subSequence(0, group.lastIndexOf("/") - 6).toString();
			System.out.println(temp + "===> ");
		}
		if (temp != null) {
			String replacement = "\\$\\{RootPath\\}" + temp;
			str = str.replaceAll(temp, replacement);
			return str;
		}
		return str;
	}


	// 将数据库的数据写出到 text 中
	@Test
	public void testWeapon() throws IOException {
		List<String> list = impl.parseContent2Json();
		System.out.println("================" + list.size());
		BufferedWriter writer = new BufferedWriter(new FileWriter("1.txt"));
		for (String str : list) {
			if (str.length() > 0) {
				writer.write(str);
				writer.newLine();
				writer.flush();
			}
		}
		writer.close();
		/*
		 * for(int i = 0; i < list.size(); i++) {
		 * System.out.println(list.get(i).length() + " --= "); }
		 */
	}

	/**
	 * 打包 19 所的微信数据到压缩包...
	 */
	@Test
	public void test19Package() {
		// 1. 查询对应的信息,ID, Title, WebName(publicname-number),IssueTime(pubdate),WebInfoContent(content)
		Map<String, Object> params = new HashMap<>();
		params.put("pubdate", "2019-04-10");
		params.put("begindate", "2019-04-13");
		params.put("enddate", "2019-04-19");
//		packetWeixinNews("2018-06-01","2018-12-01");
		packetWeixinNews("2019-04-13", "2019-04-19");

	}

	private static List<String> keywordsListWeixin = new ArrayList<>(Arrays.asList("航天","火箭","火箭","运载火箭","导弹 ","航天器","航天器","飞行器","飞行器","卫星 ","飞船 ","飞船","航天飞机 ","航天飞机 ","载人飞船 ","载人飞行 ","载人飞行器 ","载人飞行器 ","载人轨道站 ","载人航天器 ","载人航天中心 ","载人火箭 ","载人空间飞行 ","空天飞机 ","空天飞行器 ","空天飞行器","深空探测","国际空间站","高超声速","猎鹰计划 ","高超声速 ","超高声速","超高音速","高超音速","极超音速 ","滑翔飞行器 ","猎鹰计划 ","HTV-2","X37B ","X51A ","美国国家航空航天局","美国国防部","空间","skybox","米诺陶","空天防御","欧空局","空中客车","宇航","国防","反导","核裁军","核谈","防扩散","MTCR","外空武器化","临近空间","美国空军","美国导弹防御局","美国导弹防御局","猎鹰","spacex","烈火","轨道科学公司","军控","美国国家航空航天局","波音","蓝源","臭鼬工厂","鬼怪工厂","3D打印","增材制造","纳米武器","美国国防高级研究计划局","美国国防高级研究计划局","洛克希德","雷神公司","BAE系统公司"));

	public void packetWeixinNews(String begindate, String enddate) {
		// 2. 按照指定的格式进行打包...
		/*  htty_yyyyMMdd.zip
		 *   - htty_yyyyMMdd
		 *       - FileSave
		 *           - DB
		 *               - ***.xml
		 *           - weixinImage
		 *               - ***.jpg
		 * */
		String timePath = "htyy_" + new SimpleDateFormat("yyyyMMdd").format(new Date());
		String targetPath = "D:/temp/" + timePath + "/FileSave";
		String zipPath = "D:/" + timePath + ".zip";
		Map<String, Set<String>> extras = new HashMap<>();
		// 不同的表进行分别的处理
		Map<String, Object> params = new HashMap<>();
		// 拼接 SQL
		params.put("begindate", begindate);

		if (StringUtils.isEmpty(enddate) || !CSVUtils.compareDate(begindate, enddate)) {
			enddate = CSVUtils.getNextDay(new Date());
		}

		params.put("enddate", enddate);

		List<String> targetList = new ArrayList<>();
		List<String> totalList = new ArrayList<>();

		List<WebInfo> webInfos = dao.selectNewsAsWebInfo(params);
		if (webInfos != null && webInfos.size() > 1) {
			for (WebInfo info : webInfos) {
				String title = info.getTitle();
				totalList.add(title);
				String content = info.getWebInfoContent();
				for(String keyword : keywordsListWeixin) {
					if(title.contains(keyword) || content.contains(keyword)) {
						targetList.add(title);
						break;
					}

				}
				// 1. 將 java 對象轉換為 xml 文檔
				/*extras = parseJavaBean2XmlFile(info, targetPath);

				// 2. 打包图片
				if (null != extras && extras.size() > 0) {
					Set<String> set = extras.get("attachfiles");
					for (String attachfile : set) {
						String fromPath = attachfile.replace("${RootPath}", "D:/data/webmagic");
						String filename = attachfile.subSequence(attachfile.lastIndexOf("/") + 1, attachfile.length()).toString();
						String toPath = targetPath + "/weixinImage/" + filename;
						try {
							CSVUtils.copySingleFile(fromPath, toPath);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}*/
			}
			totalList.removeAll(targetList);

			System.out.println("=============");

			// 4. 生成文件包  yyyy-MM-dd.zip
			// ZipCompressor compressor = new ZipCompressor(zipPath);
			// // 将指定的文件夹的所有文件进行压缩
			// compressor.compress(targetPath);
		}

	}

	private Map<String, Set<String>> parseJavaBean2XmlFile(WebInfo info, String targetPath) {
		Map<String, Set<String>> extras = new HashMap<>();
		XMLWriter writer = null;
		FileOutputStream out = null;
		if (!StringUtils.isEmpty(info.getAttachfiles())) {
			String attachfiles = info.getAttachfiles();
			Set<String> set = new HashSet<>();
			if (attachfiles.contains(",")) {
				String[] attachfilesArray = attachfiles.split(",");
				for (String attachfile : attachfilesArray) {
					set.add(attachfile);
				}
			} else {
				set.add(attachfiles);
			}
			extras.put("attachfiles", set);
		}
		try {
			org.dom4j.Document doc = DocumentHelper.createDocument();
			// 1. 创建根节点
			org.dom4j.Element root = doc.addElement("WebInfo");
			// 添加根节点的额外的属性
			root.addAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
			root.addAttribute("xsi:noNamespaceSchemaLocation", "bookstore.xsd");

			// 2. 添加子节点, 并设置对应的属性
			org.dom4j.Element ID = root.addElement("ID");
			ID.setText(info.getID());
			org.dom4j.Element Title = root.addElement("Title");
			Title.setText(info.getTitle());
			org.dom4j.Element WebName = root.addElement("WebName");
			WebName.setText(info.getWebName());
			org.dom4j.Element IssueTime = root.addElement("IssueTime");
			IssueTime.setText(info.getIssueTime());
			org.dom4j.Element WebInfoContent = root.addElement("WebInfoContent");
			String content = info.getWebInfoContent();
			if (!StringUtils.isEmpty(content) && content.contains("<img")) {
				content = content.replace("${RootPath}/weixin.sogou.com/image/", "upload/weixinImage/");
			}
			WebInfoContent.addCDATA(content);

			// 3. 输出文件到指定的位置
			File file = new File(targetPath + "/DB/" + UUID.randomUUID().toString().replace("-", "").substring(0, 9) + ".xml");
			if (!file.exists()) {
				file.getParentFile().mkdirs();
				file.createNewFile();
			}
			out = new FileOutputStream(file.getAbsolutePath());
			OutputFormat format = OutputFormat.createPrettyPrint();
			format.setEncoding("utf-8");
			writer = new XMLWriter(out, format);
			writer.write(doc);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return extras;
	}

	/**
	 * 下载文件到本地的对应的文件中， 同时替换图片的属性为对应的文件的位置
	 */
	@Test
	public void testDown2localDirectory() {
		// 1. 查询包含 img 标签的数据项
		List<News> tables = dao.getTargetTables();

		// 2. 遍历数据对应的文件的下载存储到本地磁盘的对应的位置，同时修改 img 的属性值为对应的文件的位置。。。
		for(News news : tables) {
			String info = news.getInfo();
			String id = news.getId();
			if(info.contains("src=")) {
				// <img src="//upload.wikimedia.org/wikipedia/commons/thumb/5/51/OV1-8_PASCOMSAT_Gridsphere.jpg/260px-OV1-8_PASCOMSAT_Gridsphere.jpg">
				Matcher m = Pattern.compile("<img src=\"([^>]*?)\">").matcher(info);
				while(m.find()) {
					// System.out.println(m.group(1));
					 String downUrl = "http:" + m.group(1);
					 if(m.group(1).startsWith("//")) {
						 String fileExtName = downUrl.substring(downUrl.lastIndexOf(".") + 1, downUrl.length());
						 // System.out.println(downUrl + " ---" + fileExtName);
						 String newsName = DigestUtils.md5Hex(downUrl)+"."+fileExtName;
						 String fileRootPath = "D:/data/webmagic/";
						 String sitedomain = "wikipedia.moesalih.com";
						 try {
							 HttpFileUtil.getInstance().getFileTo(downUrl, fileRootPath + sitedomain + "/image/" + newsName, new HashMap<>());
							 // ${RootPath}/wikipedia.moesalih.com/image/52d1f071ea8486196919f557548a6424.jpeg,${RootPath}/wikipedia.moesalih.com/image/be3758decaf1b695cc4d37a756526de7.png,${RootPath}/wikipedia.moesalih.com/image/7e187dcbb3ecd5b9bebfacc6acc713fc.jpeg,${RootPath}/wikipedia.moesalih.com/image/a4f52335b7dc9596f3c98a6b76e2b419.png,${RootPath}/wikipedia.moesalih.com/image/207ab125250018ab4e841f0b06c98b5a.png,${RootPath}/wikipedia.moesalih.com/image/47b835434c413def4153797a910a3d15.png
							 String newInfo = "${RootPath}" + "/" + sitedomain + "/image/" + newsName;
							 info = info.replace(m.group(), "<img src=\""+newInfo+"\">");

							 // 数据的更新
							 dao.updateInfo(id, info);
						 } catch (IOException e) {
							 e.printStackTrace();
						 }
					 }

				}
				System.out.println();
			}
		}


	}

	/**
	 * 抽取info转化为map集合
	 */
	@Test
	public void testInfo2Map() {
		List<News> list = dao.getInfos();

		for(News news : list) {
			Map<String, String> map = new HashMap<>();
			String info = news.getInfo();
			String id = news.getId();
			String wwm = news.getTitle();
			map.put("id", id);
			map.put("wwm", wwm);
			if(StringUtils.isEmpty(info)) {
				dao.insert2Satellite(map);
				continue;
			}
			// System.out.println("=============================");

			String[] split = info.split("</tr>");
			for(String str : split) {
				if(str.contains("<th") && str.contains("<td>")) {
					Matcher m = Pattern.compile("<tr><th([^>]*?)>([^>]*?)</th><td>([^>]*?)</td>").matcher(str.replace(" ", ""));
					// System.out.println(str);
					// System.out.println();
					if(m.find()) {
						// System.out.println(m.group(2) + " <--------> " + m.group(3));
						// map.put(m.group(2), m.group(3));
						if(m.group(2).toLowerCase().equals("launchdate")) {
							map.put("fssj", m.group(3));
							System.out.println("发射时间：" + m.group(3));
						} else if(m.group(2).toLowerCase().equals("missiontype")) {
							map.put("rwlx", m.group(3));
							System.out.println("任务类型：" + m.group(3));
						}
						if(m.group(2).toLowerCase().equals("manufacturer")) {
							map.put("yfdw", m.group(3));
							System.out.println("研发单位：" + m.group(3));
						}
						if(m.group(2).toLowerCase().equals("missionduration")) {
							map.put("rwsz", m.group(3));
							System.out.println("任务时长：" + m.group(3));
						}
						if(m.group(2).toLowerCase().equals("disposal")) {
							map.put("wxzk", m.group(3));
							System.out.println("卫星状况：" + m.group(3));
						}
						if(m.group(2).toLowerCase().equals("cosparid")) {
							map.put("gjwxbsf", m.group(3));
							System.out.println("国际卫星标识符：" + m.group(3));
						}
						if(m.group(2).toLowerCase().equals("launchmass")) {
							map.put("wxzl", m.group(3));
							System.out.println("卫星重量：" + m.group(3));
						}
						if(m.group(2).toLowerCase().equals("power")) {
							map.put("dl", m.group(3));
							System.out.println("任务动力：" + m.group(3));
						}
						if(m.group(2).toLowerCase().equals("perigeealtitude")) {
							map.put("gd", m.group(3));
							System.out.println("卫星近地点高度：" + m.group(3));
						}
						/*if(m.group(2).toLowerCase().equals("apogeealtitude")) {
							map.put("gd", m.group(3));
							System.out.println("卫星远地点高度：" + m.group(3));
						}*/
						if(m.group(2).toLowerCase().equals("operator")) {
							map.put("wxpt", m.group(3));
							System.out.println("卫星平台：" + m.group(3));
						}
						if(m.group(2).toLowerCase().equals("rocket")) {
							map.put("yzhj", m.group(3));
							System.out.println("运载火箭：" + m.group(3));
						}
						if(m.group(2).toLowerCase().equals("regime")) {
							map.put("gdlx", m.group(3));
							System.out.println("轨道类型：" + m.group(3));
						}

					}
				}

			}

			// System.out.println("*****************************");
			dao.insert2Satellite(map);
		}




	}

	/**
	 * info 内容去除 sup 标签
	 */
	@Test
	public void testBadImageUpdate() {
		List<News> list = dao.getInfos();
		for(News news : list) {
			String info = news.getInfo();
			String id = news.getId();

			if(StringUtils.isEmpty(info)) {
				continue;
			}
			if(info.contains("<sup")) {
				Matcher m = Pattern.compile("<sup([^/]*?)>(.*?)</sup>").matcher(info);
				System.out.println("-----------"+ list.indexOf(news) +"-----------------");
				while(m.find()) {
					info = info.replace(m.group(),"");
					dao.updateInfo(id,info);
					// System.out.println(m.group());
				}
				System.out.println("=============================");
			}

			// String id = news.getId();
			// info = info.replace("jpg", "jpeg");
			// dao.updateInfo(id, info);
		}
	}

	/**
	 * 清理数据的多余的信息
	 */
	@Test
	public void testCleanSatelittke() {
		// 查询数据
		List<News> list = dao.getInfos();
		for(News news : list) {
			String content = news.getContent();
			String id = news.getId();
			/*if(content.contains("External links")) {
				Matcher m = Pattern.compile("<span([^/]*?)External links(.*?)</p>").matcher(content);
				if(m.find()) {
					content = content.replace(m.group(),"");
					dao.updateContent(id,content);
					// System.out.println("----------------------------");
					// System.out.println(m.group());
					// System.out.println("=============================");
				}
			}*/
			/*if(content.contains("<sup")) {
				Matcher m = Pattern.compile("<sup([^/]*?)>(.*?)</sup>").matcher(content);
				while(m.find()) {
					content = content.replace(m.group(),"");
					dao.updateContent(id,content);
					// System.out.println("----------------------------");
					// System.out.println(m.group());
					// System.out.println("=============================");
				}
			}*/
			if(content.contains("</table>")) {
				Matcher m = Pattern.compile("<table([^/]*?)>").matcher(content);
				while(m.find()) {
					if(!m.group().contains("border=\"1\"")) {
						// System.out.println("----------------------------");
						// System.out.println("<table" + m.group(1) + " border=\"1\">");
						// System.out.println(m.group());
						//
						// System.out.println("=============================");
						content = content.replace(m.group(),"<table" + m.group(1) + " border=\"1\">");
						dao.updateContent(id,content);
					}
					// content = content.replace(m.group(),"");
					// dao.updateContent(id,content);
					// System.out.println("----------------------------");
					// System.out.println(m.group());
					// System.out.println("=============================");
				}
			}
		}
	}

	public static void main(String[] args) {
		/*XMLWriter writer=null;
		FileOutputStream out=null;
		WebInfo info = new WebInfo();
		info.setID("dsds");
		info.setWebInfoContent("akusgdkusaakufe");
		info.setWebName("小火箭-xiaohuojian");
		info.setTitle("火箭升空了...");
		info.setIssueTime("2018-11-20");
		String targetPath = "D:/temp/" + 11111 + "/FileSave";
		try {
			org.dom4j.Document doc = DocumentHelper.createDocument();
			// 1. 创建根节点
			org.dom4j.Element root = doc.addElement("WebInfo");
			// 添加根节点的额外的属性
			root.addAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
			root.addAttribute("xsi:noNamespaceSchemaLocation", "bookstore.xsd");

			// 2. 添加子节点, 并设置对应的属性
			org.dom4j.Element ID = root.addElement("ID");
			ID.setText(info.getID());
			org.dom4j.Element Title = root.addElement("Title");
			Title.setText(info.getTitle());
			org.dom4j.Element WebName = root.addElement("WebName");
			WebName.setText(info.getWebName());
			org.dom4j.Element IssueTime = root.addElement("IssueTime");
			IssueTime.setText(info.getIssueTime());
			org.dom4j.Element WebInfoContent = root.addElement("WebInfoContent");
			String content = info.getWebInfoContent();
			if(!StringUtils.isEmpty(content) && content.contains("<img")) {
				content = content.replace("${RootPath}/weixin.sogou.com/image/", "upload/weixinImage/");
			}
			WebInfoContent.addCDATA(content);

			// 3. 输出文件到指定的位置
			//File file = new File(targetPath + "/DB/" + UUID.randomUUID().toString().replace("-", "").substring(0,9) + ".xml");
			File file = new File("F:/123.xml");
			*//*if(!file.exists()) {
				file.getParentFile().mkdirs();
				file.createNewFile();
			}*//*
			out = new FileOutputStream(file.getName());
			OutputFormat format = OutputFormat.createPrettyPrint();
			format.setEncoding("utf-8");
			writer = new XMLWriter(out,format);
			writer.write(doc);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}*/

		XMLWriter writer = null;
		FileOutputStream out = null;
		WebInfo info = new WebInfo();
		info.setID("dsds");
		info.setWebInfoContent("akusgdkusaakufe");
		info.setWebName("小火箭-xiaohuojian");
		info.setTitle("火箭升空了...");
		info.setIssueTime("2018-11-21");
		try {
			org.dom4j.Document doc = DocumentHelper.createDocument();
			org.dom4j.Element root = doc.addElement("WebInfo");
			// 添加根节点的额外的属性
			root.addAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
			root.addAttribute("xsi:noNamespaceSchemaLocation", "bookstore.xsd");

			// 2. 添加子节点, 并设置对应的属性
			org.dom4j.Element ID = root.addElement("ID");
			ID.setText(info.getID());
			org.dom4j.Element Title = root.addElement("Title");
			Title.setText(info.getTitle());
			org.dom4j.Element WebName = root.addElement("WebName");
			WebName.setText(info.getWebName());
			org.dom4j.Element IssueTime = root.addElement("IssueTime");
			IssueTime.setText(info.getIssueTime());
			org.dom4j.Element WebInfoContent = root.addElement("WebInfoContent");
			String content = info.getWebInfoContent();
			if (!StringUtils.isEmpty(content) && content.contains("<img")) {
				content = content.replace("${RootPath}/weixin.sogou.com/image/", "upload/weixinImage/");
			}
			WebInfoContent.addCDATA(content);
			File file = new File("D:/temp/DB/" + UUID.randomUUID().toString().replace("-", "").substring(0, 9) + ".xml");
			if (!file.exists()) {
				file.getParentFile().mkdirs();
				file.createNewFile();
			}
			out = new FileOutputStream(file.getAbsolutePath());
			OutputFormat format = OutputFormat.createPrettyPrint();
			format.setEncoding("utf-8");
			writer = new XMLWriter(out, format);
			writer.write(doc);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 添加旧的采集系统的数据到新的平台
	 */
	@Test
	public void testAddOldCrawler2NewPlatform() {
		Map<String, String> params = new HashMap<>();
		params.put("tablename", "news_en_zhs");
		params.put("crawlerdate", "2019-11-11");
		List<News> list = dao.getAllByParams(params);
		// 添加数据到新的平台
		// #{id}, #{title}, #{content}, #{datetime}, #{source}, #{url}, #{cover}, #{source}, #{url}, #{cover}, #{title_tr},#{datetime},#{content_tr}
		for(News news : list) {
			Map<String, String> param = new HashMap<>();
			param.put("id", news.getId());
			param.put("title", news.getTitle());
			param.put("content", HtmlFormatter.formatContent(news.getContent().replace("${RootPath}/", "/upload/")));
			param.put("datetime", news.getPubdate());
			param.put("source", news.getSiteDomain());
			param.put("url", news.getUrl());
			if(!StringUtils.isEmpty(news.getAttchfiles())) {
				param.put("cover", news.getAttchfiles().replace("${RootPath}/", "/upload/").replace(",", ";"));
			} else {
				param.put("cover","");
			}
			param.put("title_tr", news.getTitleTr());
			String con = news.getContentTr();
			String cnprefix = "<p style=\"text-align:center\"><span style=\"color:red\">————————————————————本文来源于国外网站,以下是机器翻译内容,向下阅读可查看原文————————————————————</span></p>";
			String enPrefix = "<p style=\"text-align: center;\"><span>————————————————————原文————————————————————</span></p>";
			param.put("content_tr", HtmlFormatter.formatContent(con.substring(con.indexOf(cnprefix) + cnprefix.length(), con.indexOf(enPrefix)).replace("${RootPath}/", "/upload/")));
			System.out.println();
			dao.addNews2NewsPlat(param);
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
		if(result.size() > 0) {
			dao.saveRecordBatch(result);
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
			Thread.sleep(1000l);
//			driver.findElement(By.id("find_btn")).click();
			Thread.sleep(2000l);
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
				dao.saveRecordBatch(result);
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
			map.put("registernum",element2.getText().trim());
			map.put("recodename",element3.getText().trim());
			map.put("recordnum",element4.getText().trim());
			map.put("username",element5.getText().trim());
//			System.out.println(webElement.getText());
			result.add(map);

		}
		if(result.size() > 0) {
			dao.saveAnotherRecordBatch(result);
		}

		// 翻页
		for(int i = 2; i < 11169; i++) {
			System.out.println(i);
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
				map.put("registernum",element2.getText().trim());
				map.put("recodename",element3.getText().trim());
				map.put("recordnum",element4.getText().trim());
				map.put("username",element5.getText().trim());
//			System.out.println(webElement.getText());
				result.add(map);

			}
			if(result.size() > 0) {
				dao.saveAnotherRecordBatch(result);
			}
		}


	}


}
