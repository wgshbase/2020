package com.mss.crawler.spiderjson.model;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mss.crawler.common.*;
import com.mss.crawler.spiderjson.util.HtmlFormatter;
import com.mss.crawler.spiderjson.util.ImageUtils;
import com.sun.tools.internal.xjc.reader.xmlschema.BindYellow;
import io.renren.modules.spider.utils.CSVUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.SetCookie;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.mss.crawler.spiderjson.JSONSpider;
import com.mss.crawler.spiderjson.ResourceFile;
import com.mss.crawler.spiderjson.SpiderNextAction;
import com.mss.crawler.spiderjson.config.JSONExtractorConfig;
import com.mss.crawler.spiderjson.config.SpiderConfig;
import com.mss.crawler.spiderjson.extractor.ObjectModelExtractor;
import com.mss.crawler.spiderjson.scheduler.FileCacheDoubleRemvoeFilter;
import com.mss.crawler.spiderjson.util.HttpFileUtil;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.selector.Html;
import us.codecraft.webmagic.selector.Selectable;

/**
 * json模型的页面解析处理器
 * 支持json配置文件的页面解析处理器
 * @author wangdw
 *
 */
@Component
public class WeixinModelPageProcessor implements IConfigAblePageProcessor,SpiderNextAction {
	
	protected Logger logger = LoggerFactory.getLogger(WeixinModelPageProcessor.class);
	
    private Map<String,ObjectModelExtractor> jsonModelExtractors = new HashMap<String,ObjectModelExtractor>();
    
    private FileCacheDoubleRemvoeFilter fileCacheDoubleRemvoeFilter;
    
    public final static String TYPE_LABEL = "type";
    
    public final static String LIST_OBJECT_LABEL = "listObject";
    
    public final static String TITLE_LABEL = "title";
    
    public final static String DATE_LABEL = "pubdate";
    
    private BlockingQueue<Request> indexsQueue = new LinkedBlockingQueue<Request>();

    private Site site;
    
    private SpiderConfig config;
    
    private String weixinMpRootUrl = "http://mp.weixin.qq.com/mp";
    
//    private String jsonRegex = "\\{\"list\":\\[\\{.*?\\]\\}";
    private String jsonRegex = "\\{.*?\"app_msg_list\":\\[\\{.*?\\]\\}";

    private String jsonPath = "$.list[*].app_msg_ext_info,$.list[*].app_msg_ext_info.multi_app_msg_item_list[*]";
    
	private String[] indexs;
	
	private String seedRex ="http://weixin.sogou.com/weixin?type=${type}&s_from=input&ie=utf8&query=${query}&_sug_=n&_sug_type_=&w=01019900&sut=4926&sst0=1553594312076&lkt=0,0,0";
	
	private int searchType=1;

	private boolean pollflag = false;

	private Request topElement = null;

	private String rootPath;

	private JSONSpider spider;

	private IP ipProxy = null;

	private boolean needIPProxy = false;

	WebDriver wxPlatformDriver = null;

	@Value("${crawler.weixin.platform.username}")
	private String wxUsername;
	@Value("${crawler.weixin.platform.password}")
	private String wxPassword;
	
	//爬取文章类型
	public static final int SEARCH_ARTICLE = 2 ;
	//爬取公众号类型
	public static final int SEARCH_ACCOUNT = 1 ;

    public WeixinModelPageProcessor(Site site ,SpiderConfig config) {
        this.site = site;
        this.config = config;
        this.indexs = config.getIndexs();
        this.seedRex = config.getSeedRex();
        this.rootPath = config.getRootPath();
        this.wxUsername = config.getUsername();
        this.wxPassword = config.getPassword();
        
        this.fileCacheDoubleRemvoeFilter = new FileCacheDoubleRemvoeFilter(this.rootPath+"/"+this.site.getDomain());
        
        this.iniPageModel(config.getExtractorCfgs());
        
        //初始化词索引队列
        String[] indexs = config.getIndexs();
        if(indexs!=null&&indexs.length>0){
        	List<String> seedUrls = getSearchSeed(indexs);
        	for(String seedUrl:seedUrls){
				Request newSeed = this.createReq(seedUrl, "search",null, null, null);
				indexsQueue.add(newSeed);
			}
        }
		System.setProperty("webdriver.chrome.marionette", "chromedriver.exe");

    }
    
    public void iniPageModel(Map<String,JSONExtractorConfig> jsonMap) {
    	
    	 for(Map.Entry<String,JSONExtractorConfig> exCfg:config.getExtractorCfgs().entrySet()){
    		 ObjectModelExtractor extractor = ObjectModelExtractor.create(exCfg.getValue());
    		 jsonModelExtractors.put(exCfg.getKey(), extractor);
         }
    }

    @Override
	public void process(Page page) {

    	//获取页面抽取类型		
		Request req = page.getRequest();
		Object type = req.getExtra(TYPE_LABEL);		
		Html html = page.getHtml();

		//生成种子url
		if(type==null){
			topElement = this.indexsQueue.element();
			Request newReq = this.indexsQueue.poll();
			page.addTargetRequest(newReq);

			//处理公众号列表
		}else if("search".equals(type)){

			WebDriver driver;
			if(!needIPProxy) {
				driver = new ChromeDriver();
				driver.manage().window().maximize();
				driver.get(page.getRequest().getUrl());
			} else {
				// 获取 ip
				getIpProxy();
				String ip = ipProxy.getIp() + ":" + ipProxy.getPort();
				System.out.println("外层模块=============================: " + ipProxy);
				ChromeOptions options = new ChromeOptions();
				options.addArguments("--proxy-server=http://" + ip);
				driver = new ChromeDriver(options);
				try {Thread.sleep(5000l);} catch (InterruptedException e) {}
				// 清空全部的 cookie
				driver.manage().deleteAllCookies();
				driver.manage().window().maximize();
				driver.get(page.getRequest().getUrl());
			}
			try {

				String curUrl = page.getRequest().getUrl();
				Matcher m = Pattern.compile("(.*?)query=(.*?)&(.*?)").matcher(curUrl);
				// 目标公众号
				String targetPublicName = "";
				if(m.find()) {
					targetPublicName = URLDecoder.decode(m.group(2));
				}
				if(wxPlatformDriver == null) {
					/*ChromeOptions options = new ChromeOptions();
					// 获取 ip 代理
					ipProxy = IPProxyUtils.getIP();
					String ip = ipProxy.getIp() + ":" + ipProxy.getPort();
					options.addArguments("--proxy-server=http://" + ip);*/
					// 创建平台的窗口
					try {
						wxPlatformDriver = new ChromeDriver();
						wxPlatformDriver.manage().window().maximize();
						wxPlatformDriver.get("https://mp.weixin.qq.com/");
						String user = wxUsername;
						String password = wxPassword;
						try {
							Thread.sleep(5000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						System.out.println("正在输入微信公众号登录账号和密码......");
						// 清空账号框中的内容
						wxPlatformDriver.findElements(By.xpath("//form/div/div/div/span/input[@name='account']")).clear();
						// 自动填入登录用户名
						WebElement usernameEl = wxPlatformDriver.findElement(By.xpath("//form/div/div/div/span/input[@name='account']"));
						usernameEl.sendKeys(user);
						// 清空密码框中的内容
						wxPlatformDriver.findElements(By.xpath("//input[@name='password']")).clear();
						// 自动填入登录密码
						WebElement passwordEl = wxPlatformDriver.findElement(By.xpath("//input[@name='password']"));
						passwordEl.sendKeys(password);

						// 自动输入完毕之后手动点一下记住我
						System.out.println("请在登录界面点击:记住账号");
						try {
							Thread.sleep(10000l);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

						// 自动点击登录按钮进行登录
						wxPlatformDriver.findElement(By.xpath("//a[@class='btn_login']")).click();
						// 拿手机扫二维码！
						System.out.println("请拿手机扫码二维码登录公众号");
						try {
							Thread.sleep(20000l);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						System.out.println("登录成功");
						System.out.println("点击素材管理");
						wxPlatformDriver.findElement(By.xpath("//ul[@class='weui-desktop-sub-menu']/li[3]/a[@title='素材管理']")).click();
						Thread.sleep(3000l);
						System.out.println("点击新建图文素材");
						wxPlatformDriver.findElement(By.xpath("//div[@class='weui-desktop-global__extra']//button[@class='weui-desktop-btn weui-desktop-btn_primary']")).click();
						Thread.sleep(3000l);
					} catch (Exception e) {
						e.printStackTrace();
					}


				}
				if(!StringUtils.isEmpty(targetPublicName) && null != wxPlatformDriver) {
					System.out.println("视图切换，点击超链接...");
					Set<String> windowHandles = wxPlatformDriver.getWindowHandles();
					for (String windowhandle : windowHandles) {
						if (windowhandle != wxPlatformDriver.getWindowHandle()) {
							wxPlatformDriver.switchTo().window(windowhandle);
						}
					}
					Thread.sleep(8000l);
					wxPlatformDriver.findElement(By.xpath("//li[@id='js_editor_insertlink']")).click();
					Thread.sleep(500l);
//					System.out.println("点击查找文章"); //
//					wxPlatformDriver.findElement(By.xpath("//label[@class='frm_radio_label']//i[@class='icon_radio']")).click();
					System.out.println("点击选择其他公众号"); //
					wxPlatformDriver.findElement(By.xpath("//p[@class='inner_link_account_msg']/button")).click();
					Thread.sleep(6000l);


					System.out.println("输入目标公众号：" + targetPublicName); // //input[@class='frm_input js_acc_search_input']
					// 清空密码框中的内容
					wxPlatformDriver.findElements(By.xpath("//div[@class='weui-desktop-form__controls']/div[@class='inner_link_account_area']/div[@class='weui-desktop-search__wrp']/div/span[@class='weui-desktop-form__input-wrp']/input[@class='weui-desktop-form__input']")).clear();
					// 自动填入公众号
					WebElement publicnameEl = wxPlatformDriver.findElement(By.xpath("//div[@class='weui-desktop-form__controls']/div[@class='inner_link_account_area']/div[@class='weui-desktop-search__wrp']/div/span[@class='weui-desktop-form__input-wrp']/input[@class='weui-desktop-form__input']"));
					publicnameEl.sendKeys(targetPublicName);
					Thread.sleep(5000l);
					System.out.println("点击搜索"); // //a[@class='js_acc_search_btn frm_input_append']
					wxPlatformDriver.findElement(By.xpath("//button[@class='weui-desktop-icon-btn weui-desktop-search__btn']")).click();
					// 解析真实的公众号 //div[@class='js_acc_item search_biz_item']
					// 切换视图...
					windowHandles = wxPlatformDriver.getWindowHandles();
					for (String windowhandle : windowHandles) {
						if (windowhandle != wxPlatformDriver.getWindowHandle()) {
							wxPlatformDriver.switchTo().window(windowhandle);
						}
					}
					Thread.sleep(5000l);
					System.out.println("选择目标公众号");
					List<WebElement> elements = wxPlatformDriver.findElements(By.xpath("//strong[@class='inner_link_account_nickname']"));
//					System.out.println(wxPlatformDriver.getPageSource());
					if(null != elements) {
						for(WebElement element : elements) {
							String nickname = wxPlatformDriver.findElements(By.xpath("//strong[@class='inner_link_account_nickname']")).get(elements.indexOf(element)).getText();
							if(!StringUtils.isEmpty(nickname) && targetPublicName.equals(nickname)) {
								// 点击...
								element.click();
								Thread.sleep(8000l);
								break;
							}
						}
					}

					System.out.println("解析公众号:" + targetPublicName);
					for(int i = 0; i < 2; i++) {
						System.out.println(targetPublicName + " 列表页的第 " + (i + 1) + " 页...");
						// 解析目标公众号...
						List<WebElement> detailElements = wxPlatformDriver.findElements(By.xpath("//label[@class='inner_link_article_item']")); // 总 li
						if(null != detailElements) {
							for(WebElement element : detailElements) {
								// 超链接
								WebElement currA = element.findElements(By.xpath("//label[@class='inner_link_article_item']/span[@class='weui-desktop-vm_default']/a")).get(detailElements.indexOf(element));
								String currentUrl = currA.getAttribute("href");
								// 标题
								String duplicateKey = element.findElements(By.xpath("//label[@class='inner_link_article_item']/div/div[@class='inner_link_article_title']")).get(detailElements.indexOf(element)).getText();
								// 发布时间
								WebElement currD = element.findElements(By.xpath("//label[@class='inner_link_article_item']/div/div[@class='inner_link_article_date']")).get(detailElements.indexOf(element));
								String pubdate = currD.getText();
								//判断是否重复
								if(!this.fileCacheDoubleRemvoeFilter.isContains(getRemoveKey(duplicateKey))){
									Request newSeed = this.createReq(currentUrl, "article", null, duplicateKey, pubdate);
									page.addTargetRequest(newSeed);
								}
//							Request newSeed = this.createReq(currentUrl, "article_list",null, null, null);
//							page.addTargetRequest(newSeed);
							}
						}
						boolean webElementExist = doesWebElementExist(wxPlatformDriver, "//a[@class='weui-desktop-btn weui-desktop-btn_default weui-desktop-btn_mini']");
						System.out.println("pageing button element exists: " + webElementExist);
						if(webElementExist) {
							// 点击翻页
							wxPlatformDriver.findElement(By.xpath("//a[@class='weui-desktop-btn weui-desktop-btn_default weui-desktop-btn_mini']")).click();
							Thread.sleep(5000l);
							windowHandles = wxPlatformDriver.getWindowHandles();
							for (String windowhandle : windowHandles) {
								if (windowhandle != wxPlatformDriver.getWindowHandle()) {
									wxPlatformDriver.switchTo().window(windowhandle);
								}
							}
						}

					}

					// 关闭弹出窗
					boolean webElementExist = doesWebElementExist(wxPlatformDriver, "//div[@class='weui-desktop-link-dialog']/div/div/div[@class='weui-desktop-dialog__hd']/button[@class='weui-desktop-icon-btn weui-desktop-dialog__close-btn']");
					System.out.println("close buton exists: " + webElementExist);
					if(webElementExist) {
						wxPlatformDriver.findElement(By.xpath("//div[@class='weui-desktop-link-dialog']/div/div/div[@class='weui-desktop-dialog__hd']/button[@class='weui-desktop-icon-btn weui-desktop-dialog__close-btn']")).click();
						// 切换视图...
						Set<String> windowHandlesCloseSet = wxPlatformDriver.getWindowHandles();
						for (String windowhandle : windowHandlesCloseSet) {
							if (windowhandle != wxPlatformDriver.getWindowHandle()) {
								wxPlatformDriver.switchTo().window(windowhandle);
							}
						}
					}



				}

				// 搜索结果页面需要验证
				// 之前的截屏解决采集验证码的问题, 现在不能用了, 我们现在的初步的解决的方案就是: 发现验证码的时候进行替换 ip 操作, 并进行重新的cookie清零...
				/*if(null != new Html(driver.getPageSource()).xpath("//input[@id='seccodeInput']/@placeholder").get() &&
						new Html(driver.getPageSource()).xpath("//input[@id='seccodeInput']/@placeholder").get().indexOf("请输入验证码") > -1) {
					*//*System.out.println("验证码问题...提前退出");
					// 退出当前的 driver
					driver.quit();
					// 获取 ip 代理
					ipProxy = IPProxyUtils.getIP();
					setIpProxy(ipProxy);
					// 设置需要代理为 true
					needIPProxy = true;
					System.out.println("验证码模块=============================: " + ipProxy);
					String ip = ipProxy.getIp() + ":" + ipProxy.getPort();
					ChromeOptions options = new ChromeOptions();
					options.addArguments("--proxy-server=http://" + ip);
					driver = new ChromeDriver(options);
					Thread.sleep(5000l);
					// 清空全部的 cookie
					// driver.manage().deleteAllCookies();
					driver.manage().window().maximize();
					driver.get(page.getRequest().getUrl());*//*


					File screenshotAs = ((ChromeDriver) driver).getScreenshotAs(OutputType.FILE);
					String seccodeImg = rootPath + "/authcode/" + ImageUtils.getRandomFilename() + ".png";
					try {//rootPath + "/authcode/"

						CSVUtils.copySingleFile(screenshotAs.getAbsolutePath(), seccodeImg);
					} catch (IOException e) {}

					// 获取验证码的位置
					WebElement element = ((ChromeDriver) driver).findElementById("seccodeImage");
					Point location = element.getLocation();
					Dimension size = element.getSize();
					int left = location.x;
					int top = location.y;
					int width = size.width;
					int height = size.height;

					String cutImage = ImageUtils.cutImage(seccodeImg, seccodeImg.substring(0, seccodeImg.lastIndexOf("/") + 1), left, top, width, height);
					String code = "";
					if(!StringUtils.isEmpty(cutImage)) {
						JSONObject authCode = YzmUtils.getAuthCode(cutImage, "1006");
						code = authCode.getString("pic_str");
						String pidId = authCode.getString("pic_id");
						if(code.length() != 6) {
							YzmUtils.reportError(pidId);
						}
					}
					if(!"".equals(code)) {
						System.out.println("============验证码========" + code);
						((ChromeDriver) driver).findElementById("seccodeInput").sendKeys(code);
						((ChromeDriver) driver).findElementById("submit").click();

						// 点击按钮之后等一下等待界面的加载
						Thread.sleep(5000L);

						String currentWindow = driver.getWindowHandle();
						Set<String> handles = driver.getWindowHandles();
						Iterator<String> it = handles.iterator();
						while (it.hasNext()) {
							String handle = it.next();
							if (currentWindow.equals(handle))
								continue;
							driver.switchTo().window(handle);
						}
					}
				}*/

				// 点击的时候不能随意点击, 先确定是不是目标的公众号再进行点击
				// System.out.println(driver.getPageSource());
//				List<Selectable> links = new Html(driver.getPageSource()).xpath("//div[@id='main']/div[@class='news-box']/ul/li").nodes();
//				Integer targetLiIndex = null;
//				// 获取目标的搜索名称
//				String targetName = new Html(driver.getPageSource()).xpath("//input[@class='query']/@value").get();
//				logger.info("Target publicname is " + targetName);
//				String chatnumber = CSVUtils.chatnumberMap.get(targetName);
//				if(links.size()!=0){
//					for(Selectable link : links) {
//						// 判断当前的搜索结果的公众号的 chatnumber 是否与目标的 chatnumber 一致, 一致的情况下进行对应的数据的采集
//						String curPublucname = link.xpath("//label[@name='em_weixinhao']/text()").get();
//						if(!StringUtils.isEmpty(curPublucname) && curPublucname.equals(chatnumber)) {
//							logger.info("find =============================" + chatnumber + "---------------------" + targetName);
//							targetLiIndex = links.indexOf(link);
//						}
//					}
//					if(null != targetLiIndex) {
//						String currentUrl = null;
//
//							targetLiIndex = targetLiIndex + 1;
//							driver.findElement(By.xpath("//div[@id='main']/div[@class='news-box']/ul/li["+ targetLiIndex +"]/div/div/p[@class='tit']/a")).click();
//						try {
//							Thread.sleep(5000l);
//						} catch (InterruptedException e) {}
//						String currentWindow = driver.getWindowHandle();
//							Set<String> handles = driver.getWindowHandles();
//							Iterator<String> it = handles.iterator();
//							while (it.hasNext()) {
//								String handle = it.next();
//								if (currentWindow.equals(handle))
//									continue;
//								driver.switchTo().window(handle);
//							}
//							currentUrl = driver.getCurrentUrl();
//							System.out.println("============>" + currentUrl);
//
//						Request newSeed = this.createReq(currentUrl, "article_list",null, null, null);
//						page.addTargetRequest(newSeed);
//					}
//
//				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				// 最后关闭浏览器, 节省 cpu 的开销
				driver.quit();
				boolean webElementExist = doesWebElementExist(wxPlatformDriver, "//span[contains(@class,'btn btn_default btn_input js_btn_p')]//button[@class='js_btn']");
				System.out.println("element exists: " + webElementExist);
				if(webElementExist) {
					wxPlatformDriver.findElement(By.xpath("//span[contains(@class,'btn btn_default btn_input js_btn_p')]//button[@class='js_btn']")).click();
					// 切换视图...
					Set<String> windowHandles = wxPlatformDriver.getWindowHandles();
					for (String windowhandle : windowHandles) {
						if (windowhandle != wxPlatformDriver.getWindowHandle()) {
							wxPlatformDriver.switchTo().window(windowhandle);
						}
					}
				}

			}


			/*if(null != page.getHtml().xpath("//input[@id='seccodeInput']/@placeholder").get() &&
					page.getHtml().xpath("//input[@id='seccodeInput']/@placeholder").get().indexOf("请输入验证码") > -1) {
				// page = crackAuthCode(page);

				String result = ClientProxyHttpClientHttp.doPostRequest(page.getRequest().getUrl());
				html = new Html(result);

			}

			List<Selectable> links = html.xpath("//div[@id='main']/div[@class='news-box']/ul/li").nodes();
			// 获取目标的搜索名称
			String targetName = html.xpath("//input[@class='query']/@value").get();
			logger.info("Target publicname is " + targetName);
			String chatnumber = CSVUtils.chatnumberMap.get(targetName);
			if(links.size()!=0){
				// 默认获取的是第一个的链接, 此处添加判断对应的公众号的标题是否为对应的公众号
				// String seedUrl = links.get(0).xpath("//li/div/div/a/@href").get();
				// Request newSeed = this.createReq(seedUrl, "article_list",null, null, null);
				// page.addTargetRequest(newSeed);

				for(Selectable link : links) {
					// 判断当前的搜索结果的公众号的 chatnumber 是否与目标的 chatnumber 一致, 一致的情况下进行对应的数据的采集
					String curPublucname = link.xpath("//label[@name='em_weixinhao']/text()").get();
					if(!StringUtils.isEmpty(curPublucname) && curPublucname.equals(chatnumber)) {
						logger.info("find =============================" + chatnumber + "---------------------" + targetName);

						String seedUrl = link.xpath("//p[@class='tit']/a/@href").get();
						Request newSeed = this.createReq(seedUrl, "article_list",null, null, null);
						page.addTargetRequest(newSeed);
					}
				}

			}*/
						
		//处理文章列表
		}else if("article_list".equals(type)){
			if(page.getHtml().toString().contains("url +=")) {
				String seedUrl = HtmlFormatter.getRealUrlSeed(page.getHtml().toString());
				Request newSeed = this.createReq(seedUrl, "article_list",null, null, null);
				page.addTargetRequest(newSeed);

				// 将检索词重新添加到队列中
				pollflag = true;
				if(pollflag) {
					this.indexsQueue.add(topElement);
				}
				pollflag = false;

			} else {
				if(page.getHtml().xpath("//title/text()").get().indexOf("请输入验证码") > -1) {
					page = crackAuthCode(page);
				}
				if(null != page.getHtml().xpath("//input[@id='seccodeInput']/@placeholder").get() &&
						page.getHtml().xpath("//input[@id='seccodeInput']/@placeholder").get().indexOf("请输入验证码") > -1) {
					page = crackAuthCode(page);
				}
				parseSeed(page);
			}


			
		//处理文章页
		}else if("article".equals(type)){
			
			//设置运行环境变量
	    	Map<String,Object> context = new HashMap<String,Object>();
	    	context.put("domain", site.getDomain());
	    	List<String> objectNames = new ArrayList<String>();
	    	page.putField("url", page.getRequest().getUrl());
	    	page.putField("rawText", page.getRawText());
	    	page.putField(LIST_OBJECT_LABEL, req.getExtra(LIST_OBJECT_LABEL));
	    	page.putField(TITLE_LABEL, req.getExtra(TITLE_LABEL));
	    	
	    	for (Map.Entry<String,ObjectModelExtractor> extractor : jsonModelExtractors.entrySet()) {
	             //解析页面
	    		Map<String,Object> process = (Map<String,Object>) extractor.getValue().process(page,context);
	             if (process == null || (process instanceof List && ((List) process).size() == 0)) {
	                 continue;
	             }
	             objectNames.add(extractor.getValue().getObjectName());
	            
	             //整合列表页和详情页的信息
	             if(process instanceof Map && req.getExtra(LIST_OBJECT_LABEL)!=null){
	            	 process = addExtInfo(page,process,(String)req.getExtra(LIST_OBJECT_LABEL),site.getDomain());
	             }
				 page.putField("pdffiles", "/" + site.getDomain() + "/");
	             page.putField(extractor.getValue().getObjectName(), process);
	             
	             // 添加 Solr 需要的额外的信息
		         page.putField("news_category", config.getNewsCategory());
		         page.putField("dbType", config.getDbType());
	             
		         page.putField("copyright", process.get("copyright"));
		         
	             //将成功解析好的对象，doubleKey入栈
	             String title = (String) process.get("title");
	             this.fileCacheDoubleRemvoeFilter.push(getRemoveKey(title));
	         }
	         
	         if(objectNames.size()>0){
	         	 page.putField("objectNames", objectNames);
	         }
	         
	         page.putField("domain", site.getDomain());
	         
		}
        
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

	// 使用 md5 对标题进行编码, 作为去重的标准
    private String getRemoveKey(String title) {
    	String removeKey = DigestUtils.md5Hex(JSON.toJSONString(title.trim()));
    	return removeKey;
	}

	/**
     * 生成检索微信公众号或文章的采集种子url
     * 文章：    http://weixin.sogou.com/weixin?type=2&query=%E8%A3%85%E5%A4%87%E7%A7%91%E6%8A%80&ie=utf8&s_from=input&_sug_=n&_sug_type_=
  	 * 公众号：http://weixin.sogou.com/weixin?type=1&s_from=input&query=%E8%A3%85%E5%A4%87%E7%A7%91%E6%8A%80&ie=utf8&_sug_=n&_sug_type_=
     * @return
     * @throws UnsupportedEncodingException
     */
    public List<String> getSearchSeed(String[] indexs){
          List<String> seeds = new ArrayList<String>();
          for(String searchText:indexs){
        	  String seedUrl = seedRex;
        	  seedUrl = StringUtils.replace(seedUrl, "${type}", searchType+"");
        	  try {
				seedUrl = StringUtils.replace(seedUrl, "${query}", URLEncoder.encode(searchText, "utf-8"));
				seeds.add(seedUrl);
        	  } catch (UnsupportedEncodingException e) {
				e.printStackTrace();
        	  }
        	  seeds.add(seedUrl);
          }
          return seeds;
  	}
    
    private Request createReq(String seedUrl,String type,String json, String title, String date){
    	if(!HtmlFormatter.urlValid(seedUrl)) {
    		if(seedUrl.startsWith("/")) {
			    String domain = HtmlFormatter.getDomain(this.seedRex);
			    seedUrl = "https://" + domain + seedUrl;
		    }
	    }
    	Request newSeed = new Request(seedUrl);
		HashMap extras = new HashMap();
		extras.put(TYPE_LABEL, type);
		extras.put(TITLE_LABEL, title);
		extras.put(DATE_LABEL, date);
		if(!StringUtils.isEmpty(json)){
			extras.put(LIST_OBJECT_LABEL, json);
		}
		newSeed.setExtras(extras);
		return newSeed;
    }
    
    private Map addExtInfo(Page page,Map detailMa,String listJson,String domain){
    	
    	JSONObject json = JSONObject.parseObject(listJson);
    	// 处理封页信息
    	String fileSrc = json.getString("cover");
    	// 添加封页下载资源, 此处的路径格式应为  cover/
    	ResourceFile rf = new ResourceFile("cover/",fileSrc,domain);
    	List<ResourceFile> oldResources = (List<ResourceFile>)page.getResultItems().get("resources");
    	// 这边的判断有点问题, 因为 微信的所有的消息都是有封面的, 对于正文部分没有图片的新闻会导致下载不到封面的信息, 显然不对
    	// if(oldResources!=null&&oldResources.size()>0){
    	if(oldResources!=null){
			oldResources.add(rf);
		}
    	
    	detailMa.put("author", json.get("author"));
    	detailMa.put("digest", json.get("digest"));
    	detailMa.put("fileid", json.get("fileid"));
    	detailMa.put("headimg", rf.getRelativeUrl());
//    	detailMa.put("pdffiles", "/" + domain + "/");
    	detailMa.put("copyright", json.get("copyright"));
    	return detailMa;
    }
    
    
  	/**
  	 * 解析列表頁的url
  	 * @param page
  	 */
  	private void parseSeed(Page page){
		//String prefix = "msgList = ";
        //String suffix = "seajs.use";
  		/*//String json = page.getJson().get();
        String htmlText = page.getHtml().get();
        int startIndex = htmlText.indexOf(prefix) + prefix.length();
        int endIndex = htmlText.indexOf(suffix);*/
       
        //String jsonStr = htmlText.substring(startIndex, endIndex-1);
		System.out.println(page.getHtml());
        String jsonStr = page.getHtml().regex(jsonRegex).get();
        JSONObject json;
        JSONArray articleJSONArray;
        List<JSONObject> articles = new ArrayList<>();
        String temp = "";
        if(!StringUtils.isEmpty(jsonStr)){
			try {
				
				json = JSONObject.parseObject(jsonStr);
				logger.info(jsonStr);
				/*articleJSONArray = json.getJSONArray("list");
		        for (int i = 0; i < articleJSONArray.size(); i++) {
		            JSONObject articleJSON = articleJSONArray.getJSONObject(i).getJSONObject("app_msg_ext_info");
		            // 获取到对应的 JSON 串中的时间戳, 添加发布时间 即 pubDate 字段到对应的 articleJSON 中
		            Integer datetime = (Integer) articleJSONArray.getJSONObject(i).getJSONObject("comm_msg_info").get("datetime");
		            String pubdate = parseDateTime2PubDate(datetime);
		            String moreArticleJSONs = articleJSON.getString("multi_app_msg_item_list");
		            String copyright = articleJSON.getString("copyright_stat");
		            // 判断微信公众号的新闻是否为原创
		            articleJSON.put("copyright", copyright);
		            //String title = articleJSON.getString("title").trim();
		            //String key = account + "_" + title;
		            articleJSON.put("pubDate", pubdate);
		            articles.add(articleJSON);
		            if(null != moreArticleJSONs && !moreArticleJSONs.isEmpty()) {
		            	String[] splits = moreArticleJSONs.split("}");
		            	
		            	for(String split: splits) {
		            		temp = getTempStr(split);
		            		if(temp != null && temp.length() > 1) {
		            			JSONObject jsonObject = JSONObject.parseObject(temp + "}");
		            			copyright = articleJSON.getString("copyright_stat");
		            			articleJSON.put("copyright", copyright);
		            			jsonObject.put("pubDate", pubdate);
		            			articles.add(jsonObject);
		            		}
		            	}
		            }
		            //next.add(new CrawlDatum(articleUrl, "article").key(key).meta("account", account));
		            // 解析所有的文章信息
		            for(JSONObject obj: articles) {
		            	String articleUrl = "";
		            	if(obj.getString("content_url").startsWith("http")) {
		            		articleUrl =  obj.getString("content_url").replace("&amp;", "&");
		            	} else {
		            		articleUrl = "https://mp.weixin.qq.com" + obj.getString("content_url").replace("&amp;", "&");
		            		
		            	}
		            	
		            	//判断是否重复
		            	String duplicateKey = obj.getString("title");
		            	String date = (String) obj.get("pubDate");
		            	if(!this.fileCacheDoubleRemvoeFilter.isContains(getRemoveKey(duplicateKey))){
		            		Request newSeed = this.createReq(articleUrl, "article", obj.toJSONString(), duplicateKey, date);				
		            		page.addTargetRequest(newSeed);
		            	}
		            }
		        }*/
				articleJSONArray = json.getJSONArray("app_msg_list");
				for (int i = 0; i < articleJSONArray.size(); i++) {
					JSONObject articleJSON = articleJSONArray.getJSONObject(i);
					// 获取到对应的 JSON 串中的时间戳, 添加发布时间 即 pubDate 字段到对应的 articleJSON 中
					Integer datetime = (Integer) articleJSON.get("create_time");
					String pubdate = parseDateTime2PubDate(datetime);
					articleJSON.put("pubDate", pubdate);
					articles.add(articleJSON);

					//判断是否重复
					String duplicateKey = articleJSON.getString("title");
					String articleUrl = articleJSON.getString("link");
					if(!this.fileCacheDoubleRemvoeFilter.isContains(getRemoveKey(duplicateKey))){
						Request newSeed = this.createReq(articleUrl, "article", articleJSON.toJSONString(), duplicateKey, pubdate);
						page.addTargetRequest(newSeed);
					}
				}

			} catch (JSONException e) {
				e.printStackTrace();
			}
        }
        
	}

  	// 解析微信的请求参数中的  datetime 转化为 pubdate 字段
  	private String parseDateTime2PubDate(Integer datetime) {
  		String temp = datetime + "000";
  		long dateStamp = Long.parseLong(temp);
  		return new SimpleDateFormat("yyyy-MM-dd").format(new Date(dateStamp));
	}

	private String getTempStr(String split) {
  		//split = split.replaceFirst(split.charAt(0) + "", "");
  		if(split.startsWith("[")) {
  			split = split.replace("[", "");
  		} else if (split.startsWith("]")) {
  			split = split.replace("]", "");
  		} else {
  			split = split.replaceFirst(split.charAt(0) + "", "");
  		}
		return split;
	}

	/**
	 * 破解搜索结果页面验证码
	 */
	public synchronized String crackSearchResultAuthCode(String htmlStr, String url) {
		if(new Html(htmlStr).xpath("//input[@id='seccodeInput']/@placeholder").get().indexOf("请输入验证码") > -1) {
			//生成验证码图谱地址                                                       1553300709952
			String codeUrl = url;
			String filePath = rootPath + "/authcode/" + DigestUtils.md5Hex(codeUrl)+".jpg";

			try {

				//下载验证码图片
				List<Cookie> cookieL = HttpFileUtil.getInstance().getFileAndCookieTo(codeUrl, filePath,new String[]{"sig"});
				for(Cookie cookie:cookieL){
					System.out.println(cookie.getName()+"="+cookie.getValue());
				}
				JSONObject json = YzmUtils.getAuthCode(filePath, "1006");
				if(json!= null) {
					return json.getString("pic_str");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return "";
	}
	/**
	 * 破解验证码
	 * @param page
	 */
	private synchronized Page crackAuthCode(Page page) {
		
		if(page.getHtml().xpath("//title/text()").get().indexOf("请输入验证码") > -1) {
			//生成验证码图谱地址
			String codeUrl = weixinMpRootUrl+"/verifycode?cert=" + (System.currentTimeMillis()+Math.random());
			String filePath = rootPath + "/authcode/" + DigestUtils.md5Hex(codeUrl)+".jpg";
			
			try {
				
				//下载验证码图片
				List<Cookie> cookieL = HttpFileUtil.getInstance().getFileAndCookieTo(codeUrl, filePath,new String[]{"sig"});
				for(Cookie cookie:cookieL){
					System.out.println(cookie.getName()+"="+cookie.getValue());
				}
				JSONObject json = YzmUtils.getAuthCode(filePath, "1004");
				if(json != null && !json.isEmpty()) {
					/*Page authCodePage = this.spider.download(CrawlerCommonUtils.getPostRequest("http://mp.weixin.qq.com/mp/verifycode", new String[]{"cert", "input"}, 
							new String[]{(System.currentTimeMillis()+Math.random()) + "", json.getString("pic_str")}));
					*/
					Map<String,Object> params = new HashMap<>();
					params.put("input", json.getString("pic_str"));
					String rspJson = HttpClientPoolUtil.httpPostRequest(codeUrl, params, cookieL, null);
					System.out.println("rspJson="+rspJson);
					JSONObject resultJson = JSONObject.parseObject(rspJson);
					logger.info("verifycode json is " + resultJson);
					if("0".equals(resultJson.get("ret")+"")) { //验证通过
						logger.info("authCode is cracked!");
						String seedUrl = page.getRequest().getUrl()+System.currentTimeMillis()+Math.random();
						Request newSeed = this.createReq(seedUrl, "article_list",null, null, null);
						for(Cookie cookie:cookieL){
							newSeed.addCookie(cookie.getName(), cookie.getValue());
						}
						page.addTargetRequest(newSeed);
					} else {
						YzmUtils.reportError(json.getString("pic_id"));
						//将搜索词放入队列中重新爬取
						logger.error("verifycode fail " + resultJson);		
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if(page.getHtml().xpath("//input[@id='seccodeInput']/@placeholder").get().indexOf("请输入验证码") > -1) {
			//生成验证码图谱地址                                                       1553300709952
			String codeUrl = "https://weixin.sogou.com/antispider/util/seccode.php?tc=1553300336" + (System.currentTimeMillis()/1000);
			String filePath = rootPath + "/authcode/" + DigestUtils.md5Hex(codeUrl)+".jpg";

			try {

				//下载验证码图片
				List<Cookie> cookieL = HttpFileUtil.getInstance().getFileAndCookieTo(codeUrl, filePath,new String[]{"sig"});
				for(Cookie cookie:cookieL){
					System.out.println(cookie.getName()+"="+cookie.getValue());
				}
				JSONObject json = YzmUtils.getAuthCode(filePath, "1006");
				if(json != null && !json.isEmpty()) {
					/*Page authCodePage = this.spider.download(CrawlerCommonUtils.getPostRequest("http://mp.weixin.qq.com/mp/verifycode", new String[]{"cert", "input"},
							new String[]{(System.currentTimeMillis()+Math.random()) + "", json.getString("pic_str")}));
					*/
					Map<String,Object> params = new HashMap<>();
					params.put("c", json.getString("pic_str"));
					params.put("r", "%2Fweixin%3Ftype%3D1%26s_from%3Dinput%26query%3D%E5%86%9B%E9%B9%B0%E8%B5%84%E8%AE%AF%26ie%3Dutf8%26_sug_%3Dn%26_sug_type_%3D");
					params.put("v", "5");

					//https://weixin.sogou.com/antispider/thank.php
					String checkUrl = "https://weixin.sogou.com/antispider/thank.php";

					String rspJson = HttpClientPoolUtil.httpPostRequest(checkUrl, params, cookieL, null);
					System.out.println("rspJson="+rspJson);
					JSONObject resultJson = JSONObject.parseObject(rspJson);
					logger.info("verifycode json is " + resultJson);
					System.out.println("================> "  + resultJson.get("code")+"");
					String snuid = (String) resultJson.get("id");
					if("0".equals(resultJson.get("code")+"")) { //验证通过
						logger.info("authCode is cracked!");
						String seedUrl = page.getRequest().getUrl()+System.currentTimeMillis()+Math.random();
						Request newSeed = this.createReq(seedUrl, "search",null, null, null);
						for(Cookie cookie:cookieL){
							newSeed.addCookie(cookie.getName(), cookie.getValue());
						}
						if(!StringUtils.isEmpty(snuid)) {
							newSeed.addCookie("SNUID", snuid);
						}
						newSeed.addCookie("seccodeRight", "success");
						newSeed.addCookie("refresh", "1");
						page.addTargetRequest(newSeed);
					} else {
						YzmUtils.reportError(json.getString("pic_id"));
						//将搜索词放入队列中重新爬取
						logger.error("verifycode fail " + resultJson);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	
		return page;
	}
  	
    @Override
    public Site getSite() {
        return site;
    }
    
	@Override
	public void setSpider(JSONSpider spider) {
		this.spider = spider;
		this.spider.setNextAction(this);
	}

	public Request getNextReq() {
		Request req = indexsQueue.poll();
		return req;
	}

	public IP getIpProxy() {
		if(null == ipProxy || (ipProxy.getExpireTime() < System.currentTimeMillis()) || null == ipProxy.getIp() || null == ipProxy.getPort()) {
			ipProxy = IPProxyUtils.getIP();
		}
		return ipProxy;
	}

	public void setIpProxy(IP ipProxy) {
		this.ipProxy = ipProxy;
	}
}
