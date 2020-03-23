package com.mss.crawler.spiderjson;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import com.mss.crawler.common.XXNetProxy;
import com.mss.crawler.spiderjson.downloader.CrawlerPhantomJSDownloader;
import com.mss.crawler.spiderjson.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.base.Joiner;
import com.mss.crawler.common.MayiProxy;
import com.mss.crawler.spiderjson.config.SpiderConfig;
import com.mss.crawler.spiderjson.pipeline.Html2PdfPipeline;
import com.mss.crawler.spiderjson.pipeline.ImgPipeline;
import com.mss.crawler.spiderjson.pipeline.JSONPipeline;
import com.mss.crawler.spiderjson.pipeline.NewsDBPipeline;
import com.mss.crawler.spiderjson.pipeline.NewsTranslate2DBPipeline;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.downloader.Downloader;
import us.codecraft.webmagic.downloader.HttpClientDownloader;
import us.codecraft.webmagic.downloader.PhantomJSDownloader;
import us.codecraft.webmagic.pipeline.Pipeline;
import us.codecraft.webmagic.proxy.Proxy;
import us.codecraft.webmagic.proxy.SimpleProxyProvider;
import us.codecraft.webmagic.scheduler.Scheduler;

@RestController
@RequestMapping(value = "/CrawlerApi")
public class SpiderBuilder {
	private static Logger logger = LoggerFactory.getLogger(SpiderBuilder.class);
	private MayiProxy mayiProxy = new MayiProxy();
	private XXNetProxy xxnet = new XXNetProxy();

	public Spider builder(String cfgFilePath) throws Exception {
		SpiderConfig rootCfg = SpiderConfig.create(new File(cfgFilePath));
		return builder(rootCfg);
	}

	public Spider builderByJson(String json) throws Exception {
		SpiderConfig rootCfg = SpiderConfig.create(json);
		return builder(rootCfg);
	}


	public Spider builder(SpiderConfig rootCfg) {
		// 设置握手协议
		// 显示所有的网络相关的 debug 的信息...
		// System.setProperty("javax.net.debug", "all");
		Site site = Site.me();

		//初始化页面解析器
		IConfigAblePageProcessor pageProcessor = iniPageProcessor(site, rootCfg);

		//初始化爬虫
		JSONSpider spider = JSONSpider.create(pageProcessor);
		spider.setConfig(rootCfg);

		spider.thread(1);
		spider.addUrl(rootCfg.getStartURL());

		//初始化管道
		List<Pipeline> pipelines = iniPipline(rootCfg);
		spider.setPipelines(pipelines);

		//初始化下载器
		Downloader downloader;

		if (rootCfg.isUseProxy()) {
			downloader = iniDownLoaderWithProxy(rootCfg, site);
		} else {
			downloader = iniDownLoader(rootCfg);
		}

		spider.setDownloader(downloader);

		//初始化url队列
		Scheduler scheduler = iniScheduler(rootCfg);
		if (scheduler != null) {
			spider.setScheduler(scheduler);
		}

		return spider;
	}

	private Scheduler iniScheduler(SpiderConfig rootCfg) {
		Scheduler scheduler = null;
		return scheduler;
	}

	private List<Pipeline> iniPipline(SpiderConfig rootCfg) {
		List<Pipeline> pipelines = new ArrayList<Pipeline>();
		if (rootCfg.getPipelines() != null && rootCfg.getPipelines().length > 0) {
			for (String pipeline : rootCfg.getPipelines()) {
				if (Constants.PIPELINE_JSONPIPELINE.equals(pipeline)) {
					pipelines.add(new JSONPipeline(rootCfg.getRootPath()));
				} else if (Constants.PIPELINE_IMGPIPELINE.equals(pipeline)) {
					pipelines.add(new ImgPipeline(rootCfg.getRootPath()));
				} else if (Constants.PIPELINE_Html2PdfPipeline.equals(pipeline)) {
					pipelines.add(new Html2PdfPipeline(rootCfg.getRootPath()));
				} else if (Constants.PIPELINE_NEWSDBPIPELINE.equals(pipeline)) {
					pipelines.add(new NewsDBPipeline());
				} else if (Constants.PIPELINE_NEWSTRANSLATE2DBPIPELINE.equals(pipeline)) {
					pipelines.add(new NewsTranslate2DBPipeline(rootCfg.getRootPath()));
				}
			}
			//缺省初始化管道
		} else {
			//需要按照顺序进行
			pipelines.add(new JSONPipeline());
			pipelines.add(new ImgPipeline());
		}
		return pipelines;

	}

	private Downloader iniDownLoader(SpiderConfig rootCfg) {
		if(null != rootCfg.getPhantomJs() && rootCfg.getPhantomJs()) {
			// JS 预加载内核
			CrawlerPhantomJSDownloader downloader = new CrawlerPhantomJSDownloader();
			return downloader;
		}
		HttpClientDownloader downLoader = new HttpClientDownloader();
		return downLoader;
	}

	private Downloader iniDownLoaderWithProxy(SpiderConfig rootCfg, Site site) {
		if(null != rootCfg.getGoogleSite() && rootCfg.getGoogleSite()) {
			HttpClientDownloader downLoader = new HttpClientDownloader();
			Proxy proxy = new Proxy(xxnet.getProxy_ip(), xxnet.getProxy_port());
			SimpleProxyProvider proxyProvider = SimpleProxyProvider.from(proxy);
			downLoader.setProxyProvider(proxyProvider);
			return downLoader;
		} else {
			//使用大蚂蚁代理"Proxy-Authorization", authHeader
			String authHeader = mayiProxy.getAuthHeader();
			site.addHeader("Proxy-Authorization", authHeader);
			HttpClientDownloader downLoader = new HttpClientDownloader();
			Proxy proxy = new Proxy(mayiProxy.getProxy_ip(), mayiProxy.getProxy_port());
			SimpleProxyProvider proxyProvider = SimpleProxyProvider.from(proxy);
			downLoader.setProxyProvider(proxyProvider);
			return downLoader;
		}
	}

	private IConfigAblePageProcessor iniPageProcessor(Site site, SpiderConfig config) {
		IConfigAblePageProcessor modelPageProcessor = null;
		site.setDomain(config.getDomain());
		// System.out.println(config.getTimeout() + " S 是超时时间");
		site.setTimeOut(config.getTimeout());
		site.setRetryTimes(config.getRetry());
		site.setCharset(config.getCharset());
		//site.setRetrySleepTime(config.getr);
		//site.setCycleRetryTimes(crawlerParam.getCycleRetryTimes());
		site.getHeaders().put("User-Agent", config.getUserAgent());
		if (Constants.SITE_PAGE_PROCESSORMODEL.equals(config.getPageProcessorModel())) {
			modelPageProcessor = new SiteModelPageProcessor(site, config);
		} else if (Constants.SUBJECT_PAGE_PROCESSORMODEL.equals(config.getPageProcessorModel())) {
			modelPageProcessor = new SubjectModelPageProcessor(site, config);
		} else if (Constants.WEIXIN_PAGE_PROCESSORMODEL.equals(config.getPageProcessorModel())) {
			modelPageProcessor = new WeixinModelPageProcessor(site, config);
		} else if (Constants.BATCHURLS_PAGE_PROCESSORMODEL.equals(config.getPageProcessorModel())) {
			modelPageProcessor = new BatchUrlModelPageProcessor(site, config);
		} else if (Constants.LISTANDDETAIL_PAGE_PROCESSORMODEL.equals(config.getPageProcessorModel())) {
			modelPageProcessor = new ListDetailPageProcessor(site, config);
		} else if (Constants.IPTEST_PAGE_PROCESSORMODEL.equals(config.getPageProcessorModel())) {
			modelPageProcessor = new IPTestModelPageProcessor(site, config);
		} else if (Constants.WEIBO_PAGE_PROCESSORMODEL.equals(config.getPageProcessorModel())) {
			modelPageProcessor = new WeiBoModelPageProcessor(site, config);
		}

		return modelPageProcessor;

	}

	private String getAuthHeader(String appkey, String secret) {
		// 定义申请获得的appKey和appSecret
		//String appkey = "170799173";
		//String secret = "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX";

		// 创建参数表
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("app_key", appkey);
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		format.setTimeZone(TimeZone.getTimeZone("GMT+8"));//使用中国时间，以免时区不同导致认证错误
		paramMap.put("timestamp", format.format(new Date()));

		// 对参数名进行排序
		String[] keyArray = paramMap.keySet().toArray(new String[0]);
		Arrays.sort(keyArray);

		// 拼接有序的参数名-值串
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(secret);
		for (String key : keyArray) {
			stringBuilder.append(key).append(paramMap.get(key));
		}

		stringBuilder.append(secret);
		String codes = stringBuilder.toString();

		// MD5编码并转为大写， 这里使用的是Apache codec
		String sign = org.apache.commons.codec.digest.DigestUtils.md5Hex(codes).toUpperCase();
		paramMap.put("sign", sign);
		// 拼装请求头Proxy-Authorization的值，这里使用 guava 进行map的拼接
		String authHeader = "MYH-AUTH-MD5 " + Joiner.on('&').withKeyValueSeparator("=").join(paramMap);
		return authHeader;
	}

	@ApiOperation(value = "启动java爬虫", notes = "程序爬取启动器")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "jsonConfig", value = "json配置", required = true, paramType = "query", dataType = "String"),
			@ApiImplicitParam(name = "reqTime", value = "发送请求时间(2017-05-05 12:00:01.000)", required = true, paramType = "query", dataType = "String"),
	})
	@RequestMapping(value = "/runCrawler", method = RequestMethod.GET)
	public static Map<String, String> runCrawler(@RequestParam String jsonConfig, @RequestParam String reqTime) {

		logger.info("run crawler ........................" + reqTime);
		Map<String, String> result = new HashMap<String, String>();


		SpiderBuilder builder = new SpiderBuilder();
		Spider spider;
		try {
			spider = builder.builderByJson(jsonConfig);
			spider.run();
			result.put("status", "success");
		} catch (Exception e) {
			e.printStackTrace();
			result.put("status", e.getMessage());
		}
		return result;
	}

	public static void main(String[] args) throws Exception {

		//微信采集 欧参
//		String cfgFilePath = "D:/workspace/product/BSCrawler/doc/crawlerConfigex/batchUrls_chinaiiss.txt";
//		String cfgFilePath = "D:/workspace/product/BSCrawler/doc/temp/listDetail_com.txt";
//		String cfgFilePath = "D:/workspace/product/BSCrawler/doc/temp/cnDetail_haiyang.txt";


		// 23 所采集测试
//		String cfgFilePath = "D:/workspace/product/BSCrawler/doc/23/en/listDetail_darpa.txt";
//		String cfgFilePath = "D:/workspace/product/BSCrawler/doc/23/en/listDetail_ndia.txt";
//		String cfgFilePath = "D:/workspace/product/BSCrawler/doc/23/en/listDetail_cans.txt";
//		String cfgFilePath = "D:/workspace/product/BSCrawler/doc/23/cn/cnDetail_nmpa.txt";

		// 海洋出版社模板
//		String cfgFilePath = "D:/workspace/product/BSCrawler/doc/oceanPublic/en/listDetail_noc.txt";
//		String cfgFilePath = "D:/workspace/product/BSCrawler/doc/oceanPublic/en/listDetail_cimas.txt";
//		String cfgFilePath = "D:/workspace/product/BSCrawler/doc/oceanPublic/en/listDetail_today_uri.txt";
//		String cfgFilePath = "D:/workspace/product/BSCrawler/doc/oceanPublic/en/listDetail_uct.txt";
//		String cfgFilePath = "D:/workspace/product/BSCrawler/doc/oceanPublic/en/listDetail_jamstec.txt";
//		String cfgFilePath = "D:/workspace/product/BSCrawler/doc/oceanPublic/en/listDetail_mbari.txt";
//		String cfgFilePath = "D:/workspace/product/BSCrawler/doc/oceanPublic/en/listDetail_whoi2.txt";
//		String cfgFilePath = "D:/workspace/product/BSCrawler/doc/oceanPublic/en/listDetail_whoi.txt";
//		String cfgFilePath = "D:/workspace/product/BSCrawler/doc/oceanPublic/en/listDetail_washington.txt";
//		String cfgFilePath = "D:/workspace/product/BSCrawler/doc/oceanPublic/en/listDetail_offshorewind.txt";
//		String cfgFilePath = "D:/workspace/product/BSCrawler/doc/oceanPublic/en/listDetail_phys.txt";
//		String cfgFilePath = "D:/workspace/product/BSCrawler/doc/oceanPublic/en/listDetail_google.txt";
//		String cfgFilePath = "D:/workspace/product/BSCrawler/doc/oceanPublic/en/listDetail_noaa.txt";
//		String cfgFilePath = "D:/workspace/product/BSCrawler/doc/oceanPublic/en/listDetail_cambodiadaily.txt";
//		String cfgFilePath = "D:/workspace/product/BSCrawler/doc/oceanPublic/en/listDetail_vims.txt";
//		String cfgFilePath = "D:/workspace/product/BSCrawler/doc/oceanPublic/en/listDetail_offshorewind.txt";
//		String cfgFilePath = "D:/workspace/product/BSCrawler/doc/oceanPublic/en/listDetail_nasa.txt";
//		String cfgFilePath = "D:/workspace/product/BSCrawler/doc/oceanPublic/en/listDetail_csic.txt";
//		String cfgFilePath = "D:/workspace/product/BSCrawler/doc/oceanPublic/en/listDetail_niwa.txt";
//		String cfgFilePath = "D:/workspace/product/BSCrawler/doc/oceanPublic/en/listDetail_princeton.txt";
//		String cfgFilePath = "D:/workspace/product/BSCrawler/doc/oceanPublic/en/listDetail_oregonstate.txt";
//		String cfgFilePath = "D:/workspace/product/BSCrawler/doc/oceanPublic/en/listDetail_tamu.txt";
//		String cfgFilePath = "D:/workspace/product/BSCrawler/doc/oceanPublic/en/listDetail_hawaii.txt";
//		String cfgFilePath = "D:/workspace/product/BSCrawler/doc/oceanPublic/en/listDetail_rd.txt";
//		String cfgFilePath = "D:/workspace/product/BSCrawler/doc/oceanPublic/en/listDetail_ucar.txt";
//		String cfgFilePath = "D:/workspace/product/BSCrawler/doc/oceanPublic/en/listDetail_csrio.txt";
//		String cfgFilePath = "D:/workspace/product/BSCrawler/doc/oceanPublic/en/listDetail_theindependent.txt";
//		String cfgFilePath = "D:/workspace/product/BSCrawler/doc/oceanPublic/en/listDetail_express.txt";
//		String cfgFilePath = "D:/workspace/product/BSCrawler/doc/oceanPublic/en/listDetail_thehill.txt";



//		String cfgFilePath = "D:/workspace/product/BSCrawler/doc/oceanPublic/cn/cnDetail_energy.txt";
//		String cfgFilePath = "D:/workspace/product/BSCrawler/doc/oceanPublic/cn/cnDetail_cas.txt";
//		String cfgFilePath = "D:/workspace/product/BSCrawler/doc/oceanPublic/cn/cnDetail_mnr.txt";
//		String cfgFilePath = "D:/workspace/product/BSCrawler/doc/oceanPublic/cn/cnDetail_scs.txt";
//		String cfgFilePath = "D:/workspace/product/BSCrawler/doc/oceanPublic/cn/cnDetail_ocnsm.txt";
//		String cfgFilePath = "D:/workspace/product/BSCrawler/doc/oceanPublic/cn/cnDetail_notcsoa.txt";
//		String cfgFilePath = "D:/workspace/product/BSCrawler/doc/oceanPublic/cn/cnDetail_cimamnr.txt";
//		String cfgFilePath = "D:/workspace/product/BSCrawler/doc/oceanPublic/cn/cnDetail_chinare.txt";
//		String cfgFilePath = "D:/workspace/product/BSCrawler/doc/oceanPublic/cn/cnDetail_nsoas.txt";
//		String cfgFilePath = "D:/workspace/product/BSCrawler/doc/oceanPublic/cn/cnDetail_nsoas2.txt";
//		String cfgFilePath = "D:/workspace/product/BSCrawler/doc/oceanPublic/cn/cnDetail_nsmc.txt";
//		String cfgFilePath = "D:/workspace/product/BSCrawler/doc/oceanPublic/cn/cnDetail_hnhky.txt";
//		String cfgFilePath = "D:/workspace/product/BSCrawler/doc/oceanPublic/cn/cnDetail_sdocean.txt";
//		String cfgFilePath = "D:/workspace/product/BSCrawler/doc/oceanPublic/cn/cnDetail_iap.txt";
//		String cfgFilePath = "D:/workspace/product/BSCrawler/doc/oceanPublic/cn/cnDetail_qdio.txt";
//		String cfgFilePath = "D:/workspace/product/BSCrawler/doc/oceanPublic/cn/cnDetail_qdio2.txt";
//		String cfgFilePath = "D:/workspace/product/BSCrawler/doc/oceanPublic/cn/cnDetail_scsio.txt";
//		String cfgFilePath = "D:/workspace/product/BSCrawler/doc/oceanPublic/cn/cnDetail_scsio2.txt";
//		String cfgFilePath = "D:/workspace/product/BSCrawler/doc/oceanPublic/cn/cnDetail_idsse.txt";
//		String cfgFilePath = "D:/workspace/product/BSCrawler/doc/oceanPublic/cn/cnDetail_idsse2019.txt";
//		String cfgFilePath = "D:/workspace/product/BSCrawler/doc/oceanPublic/cn/cnDetail_idsse2018.txt";
//		String cfgFilePath = "D:/workspace/product/BSCrawler/doc/oceanPublic/cn/cnDetail_yic.txt";
//		String cfgFilePath = "D:/workspace/product/BSCrawler/doc/oceanPublic/cn/cnDetail_scsfri.txt";
//		String cfgFilePath = "D:/workspace/product/BSCrawler/doc/oceanPublic/cn/cnDetail_banyuetan.txt";
//		String cfgFilePath = "D:/workspace/product/BSCrawler/doc/oceanPublic/cn/cnDetail_stdaily.txt";
//		String cfgFilePath = "D:/workspace/product/BSCrawler/doc/oceanPublic/cn/cnDetail_cgs_ddyw.txt";



		// 中文资讯
//		String cfgFilePath = "D:/workspace/product/BSCrawler/doc/oceanPublic/cn/cnDetail_81.txt";

		// 特殊采集 批量 URL
//		String cfgFilePath = "D:/workspace/product/BSCrawler/doc/crawlerConfigex/batchUrls_cnwebsite.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/crawlerConfigex/batchUrls_chinaiiss.txt";


		// JH 微信数据采集模板
//		 String cfgFilePath = "D:/workspace/product/BSCrawler/doc/crawlerConfigex/weixin_batch_adding.txt";
//		 String cfgFilePath = "D:/workspace/product/BSCrawler/doc/temp/jh_electronic/listDetail_com.txt";

		 String cfgFilePath = "D:/workspace/product/BSCrawler/doc/crawlerConfigex/weixin_batch_19.txt";
//		 String cfgFilePath = "D:/workspace/product/BSCrawler/doc/crawlerConfigex/weixin_deskwar.txt";

		// 卫星知识图谱数据采集, 维基百科与百度百科
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/huangnana/batchUrls_baidu.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/huangnana/batchUrls_wiki.txt";


		// 视频采集
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/movie/movie_ifeng.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/movie/movie_cntv_defense.txt";

		// 涉海机构采集
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/shehai/listDetail_carnegieendowment.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/shehai/listDetail_sipri.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/shehai/listDetail_bostonherald.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/shehai/listDetail_floridachinesenews.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/shehai/listDetail_chinesetoday.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/shehai/listDetail_vientianetimes.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/shehai/listDetail_thejakartapost.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/shehai/listDetail_inhabitat.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/shehai/listDetail_phys.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/shehai/listDetail_offshorewind.txt";


		// 11 院采集
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/11/listDetail_space.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/11/listDetail_tsagi.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/11/listDetail_darpa.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/11/listDetail_esa.txt";
//		 String cfgFilePath = "D:/workspace/product/BSCrawler/doc/11/listDetail_jaxa.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/11/listDetail_nasa.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/11/listDetail_nasa_europa.txt";

		// app 相关模板配置
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/appCrawlerTemplates/cnDetail_chinanews.txt";
		//String cfgFilePath = "D:/workspace/product/BSCrawler/doc/appCrawlerTemplates/cnDetail_yna.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/appCrawlerTemplates/cnDetail_sina.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/appCrawlerTemplates/cnDetail_kyodo.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/appCrawlerTemplates/cnDetail_zaobao.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/appCrawlerTemplates/cnDetail_sputniknews.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/appCrawlerTemplates/cnDetail_chinatimes.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/appCrawlerTemplates/cnDetail_huanqiu.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/appCrawlerTemplates/cnDetail_dsti_spaceflight.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/appCrawlerTemplates/cnDetail_dsti_nuclear.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/appCrawlerTemplates/cnDetail_dsti_aviation.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/appCrawlerTemplates/cnDetail_dsti_ship.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/appCrawlerTemplates/cnDetail_dsti_arms.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/appCrawlerTemplates/cnDetail_dsti_electron.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/appCrawlerTemplates/cnDetail_guancha.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/appCrawlerTemplates/cnDetail_jianzai.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/appCrawlerTemplates/cnDetail_haiwainet.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/appCrawlerTemplates/listDetail_38north.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/appCrawlerTemplates/listDetail_navaltoday.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/appCrawlerTemplates/listDetail_navy2.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/appCrawlerTemplates/listDetail_navy4.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/appCrawlerTemplates/listDetail_janes.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/appCrawlerTemplates/listDetail_miltimes.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/appCrawlerTemplates/listDetail_defense.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/appCrawlerTemplates/listDetail_alert5.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/appCrawlerTemplates/listDetail_india.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/appCrawlerTemplates/weibo_batch_adding.txt";

		// 战环所模板
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/zhsCrawlerTemplates/listDetail_noaa_satellite.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/zhsCrawlerTemplates/listDetail_557.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/zhsCrawlerTemplates/listDetail_africanews.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/zhsCrawlerTemplates/listDetail_eumetsat.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/zhsCrawlerTemplates/listDetail_spacenews.txt";
//		 String cfgFilePath = "D:/workspace/product/BSCrawler/doc/zhsCrawlerTemplates/listDetail_noaa.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/zhsCrawlerTemplates/listDetail_weather.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/zhsCrawlerTemplates/listDetail_spacenewsfeed.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/zhsCrawlerTemplates/listDetail_space.txt";
//		 String cfgFilePath = "D:/workspace/product/BSCrawler/doc/zhsCrawlerTemplates/listDetail_militaryaerospace.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/zhsCrawlerTemplates/listDetail_defense.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/zhsCrawlerTemplates/listDetail_dni.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/zhsCrawlerTemplates/listDetail_stratcom.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/zhsCrawlerTemplates/listDetail_dcma.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/zhsCrawlerTemplates/listDetail_nws.txt";

		// 航天关键词词表
		//String cfgFilePath = "D:/workspace/product/BSCrawler/doc/crawlerConfigex/00keywords_space.txt";

		// 装备库
		//String cfgFilePath = "D:/workspace/product/BSCrawler/doc/weaponRep/artillery.txt";
		//String cfgFilePath = "D:/workspace/product/BSCrawler/doc/weaponRep/extra.txt";
		//String cfgFilePath = "D:/workspace/product/BSCrawler/doc/weaponRep/warship.txt";
		//String cfgFilePath = "D:/workspace/product/BSCrawler/doc/weaponRep/aircraft.txt";
		//String cfgFilePath = "D:/workspace/product/BSCrawler/doc/weaponRep/spaceship.txt";
		//String cfgFilePath = "D:/workspace/product/BSCrawler/doc/weaponRep/extra.txt";
		//String cfgFilePath = "D:/workspace/product/BSCrawler/doc/weaponRep/guns.txt";

		//微信采集
		//String cfgFilePath = "D:/workspace/product/BSCrawler/doc/crawlerConfigex/batchUrls_chinaiiss.txt";
		//String cfgFilePath = "D:/workspace/product/BSCrawler/doc/crawlerConfigex/batchUrls_cnwebsite.txt";
		//String cfgFilePath = "D:/workspace/product/BSCrawler/doc/crawlerConfigex/0cnDetail_gov.txt";
		//String cfgFilePath = "D:/workspace/product/BSCrawler/doc/crawlerConfigex/subject.txt";

		// 微信模板拆分
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/jhweixin/weixin_junyingzixun.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/jhweixin/weixin_zhongguoweixingdaohangdingweiyingyongguanlizhongxin.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/jhweixin/weixin_jiaotonganquanyingjiguojiagongchengshiyanshi.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/jhweixin/weixin_deeptechshenkeji.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/jhweixin/weixin_zhiyuanzhanlueyufangwuyanjiusuo.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/jhweixin/weixin_zhongguobeidouweixingdaohangxitong.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/jhweixin/weixin_gaoduanzhuangbeichanyeyanjiuzhongxin.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/jhweixin/weixin_keluoliaofudejunshiketing.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/jhweixin/weixin_zhongguoxinyidairengongzhineng.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/jhweixin/weixin_zhongguozhihuiyukongzhixuehui.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/jhweixin/weixin_leimanjunshixiandaijianchuan.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/jhweixin/weixin_zhongguohangtiankejijituan.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/jhweixin/weixin_leidatongxindianzizhan.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/jhweixin/weixin_tiandiyitihuaxinxiwangluo.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/jhweixin/weixin_diankefangwuyanjiu.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/jhweixin/weixin_guofangkejiyaowen.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/jhweixin/weixin_haiyangfangwuqianyan.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/jhweixin/weixin_junminrongheguancha.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/jhweixin/weixin_wangxinkejiqianyan.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/jhweixin/weixin_xueshuplus.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/jhweixin/weixin_zhanlueqianyanjishu.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/jhweixin/weixin_zhongguohangtiankepu.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/jhweixin/weixin_zhongguozairenhangtian.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/jhweixin/weixin_zhongguozhanluezhiyuan.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/jhweixin/weixin_guojidianzizhan.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/jhweixin/weixin_jungongheikeji.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/jhweixin/weixin_junpingchenguangwen.txt";
		//String cfgFilePath = "D:/workspace/product/BSCrawler/doc/jhweixin/weixin_kongtiandashiye.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/jhweixin/weixin_lanhaixingzhiku.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/jhweixin/weixin_weibopinshewang.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/jhweixin/weixin_weixingyuwangluo.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/jhweixin/weixin_womendetaikong.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/jhweixin/weixin_yuhangtansuoju.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/jhweixin/weixin_zhanluewangjunshi.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/jhweixin/weixin_beiguofangwu.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/jhweixin/weixin_chenduhangtian.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/jhweixin/weixin_dianbozhimao.txt";
		//String cfgFilePath = "D:/workspace/product/BSCrawler/doc/jhweixin/weixin_diankexiaoshan.txt";
		//String cfgFilePath = "D:/workspace/product/BSCrawler/doc/jhweixin/weixin_guojitaikong.txt";
		//String cfgFilePath = "D:/workspace/product/BSCrawler/doc/jhweixin/weixin_guokehuanyu.txt";
		//String cfgFilePath = "D:/workspace/product/BSCrawler/doc/jhweixin/weixin_hangtianfangwu.txt";
		//String cfgFilePath = "D:/workspace/product/BSCrawler/doc/jhweixin/weixin_hangtianchangcheng.txt";
		//String cfgFilePath = "D:/workspace/product/BSCrawler/doc/jhweixin/weixin_huanqiujunshi.txt";
		//String cfgFilePath = "D:/workspace/product/BSCrawler/doc/jhweixin/weixin_junshangkeji.txt";
		//String cfgFilePath = "D:/workspace/product/BSCrawler/doc/jhweixin/weixin_manbuyuzhou.txt";
		//String cfgFilePath = "D:/workspace/product/BSCrawler/doc/jhweixin/weixin_weixingyingyong.txt";
		//String cfgFilePath = "D:/workspace/product/BSCrawler/doc/jhweixin/weixin_wurenzhengfeng.txt";
		//String cfgFilePath = "D:/workspace/product/BSCrawler/doc/jhweixin/weixin_xingjizhihui.txt";
		//String cfgFilePath = "D:/workspace/product/BSCrawler/doc/jhweixin/weixin_zhongguohangtian.txt";
		//String cfgFilePath = "D:/workspace/product/BSCrawler/doc/jhweixin/weixin_zhongguojunshi.txt";
		//String cfgFilePath = "D:/workspace/product/BSCrawler/doc/jhweixin/weixin_zhuangbeicankao.txt";
		//String cfgFilePath = "D:/workspace/product/BSCrawler/doc/jhweixin/weixin_zhuangbeikeji.txt";
		//String cfgFilePath = "D:/workspace/product/BSCrawler/doc/jhweixin/weixin_hangxiaoyu.txt";
		//String cfgFilePath = "D:/workspace/product/BSCrawler/doc/jhweixin/weixin_taikongwang.txt";
		//String cfgFilePath = "D:/workspace/product/BSCrawler/doc/jhweixin/weixin_taibowang.txt";
		//String cfgFilePath = "D:/workspace/product/BSCrawler/doc/jhweixin/weixin_tiesuohan.txt";
		//String cfgFilePath = "D:/workspace/product/BSCrawler/doc/jhweixin/weixin_weixingjie.txt";
		//String cfgFilePath = "D:/workspace/product/BSCrawler/doc/jhweixin/weixin_wurenji.txt";
		//String cfgFilePath = "D:/workspace/product/BSCrawler/doc/jhweixin/weixin_xiaohuojian.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/jhweixin/weixin_deskwar.txt";

		// 社科院采集
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/temp/cnDetail_cssn.txt";
		//String cfgFilePath = "D:/workspace/product/BSCrawler/doc/crawlerConfigex/cnDetail_cssn1.txt";
		//String cfgFilePath = "D:/workspace/product/BSCrawler/doc/temp/cnDetail_legalinfo.txt";
		//String cfgFilePath = "D:/workspace/product/BSCrawler/doc/temp/cnDetail_chinatrial.txt";
		//String cfgFilePath = "D:/workspace/product/BSCrawler/doc/temp/cnDetail_legaldaily.txt";
		//String cfgFilePath = "D:/workspace/product/BSCrawler/doc/temp/cnDetail_xinhua.txt";
		//String cfgFilePath = "D:/workspace/product/BSCrawler/doc/temp/cnDetail_gmw.txt";
		//String cfgFilePath = "D:/workspace/product/BSCrawler/doc/temp/cnDetail_bjfy.txt";


		//外文采集
		//String cfgFilePath = "D:/workspace/product/BSCrawler/doc/crawlerConfigex/listDetail_milbases.txt";
		//String cfgFilePath = "D:/workspace/product/BSCrawler/doc/crawlerConfigex/listDetail_nsg.txt";
		//String cfgFilePath = "D:/workspace/product/BSCrawler/doc/crawlerConfigex/listDetail_wiki.txt";
		//String cfgFilePath = "D:/workspace/product/BSCrawler/doc/crawlerConfigex/listDetail_5571.txt";
		//String cfgFilePath = "D:/workspace/product/BSCrawler/doc/crawlerConfigex/listDetail_agcarmy.txt";
//		String cfgFilePath = "D:/workspace/product/BSCrawler/doc/crawlerConfigex/listDetail_army.txt";
		//String cfgFilePath = "D:/workspace/product/BSCrawler/doc/crawlerConfigex/listDetail_marines.txt";
		//String cfgFilePath = "D:/workspace/product/BSCrawler/doc/crawlerConfigex/listDetail_pacom.txt";
//		String cfgFilePath = "D:/workspace/product/BSCrawler/doc/crawlerConfigex/listDetail_jcs.txt";
//		String cfgFilePath = "D:/workspace/product/BSCrawler/doc/crawlerConfigex/listDetail_miltimes.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/crawlerConfigex/listDetail_defensenews.txt";
//		 String cfgFilePath = "D:/workspace/product/BSCrawler/doc/crawlerConfigex/listDetail_space.txt";
		//String cfgFilePath = "D:/workspace/product/BSCrawler/doc/crawlerConfigex/listDetail_afmc.txt";
//		String cfgFilePath = "D:/workspace/product/BSCrawler/doc/crawlerConfigex/listDetail_armyguide.txt";
//		 String cfgFilePath = "D:/workspace/product/BSCrawler/doc/crawlerConfigex/listDetail_ball.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/crawlerConfigex/listDetail_boeing.txt";
//		String cfgFilePath = "D:/workspace/product/BSCrawler/doc/crawlerConfigex/listDetail_breakdefense.txt";
		//String cfgFilePath = "D:/workspace/product/BSCrawler/doc/crawlerConfigex/listDetail_chinamil.txt";
//		 String cfgFilePath = "D:/workspace/product/BSCrawler/doc/crawlerConfigex/listDetail_defense.txt";
		//String cfgFilePath = "D:/workspace/product/BSCrawler/doc/crawlerConfigex/listDetail_defensemedianetwork.txt";
		//String cfgFilePath = "D:/workspace/product/BSCrawler/doc/crawlerConfigex/listDetail_einpresswire.txt";
//		 String cfgFilePath = "D:/workspace/product/BSCrawler/doc/crawlerConfigex/listDetail_esa.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/crawlerConfigex/listDetail_gpsworld.txt";
		//String cfgFilePath = "D:/workspace/product/BSCrawler/doc/crawlerConfigex/listDetail_iaea.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/crawlerConfigex/listDetail_janes.txt";
//		 String cfgFilePath = "D:/workspace/product/BSCrawler/doc/crawlerConfigex/listDetail_jaxa.txt";
//		 String cfgFilePath = "D:/workspace/product/BSCrawler/doc/crawlerConfigex/listDetail_militaryaerospace.txt";
//		String cfgFilePath = "D:/workspace/product/BSCrawler/doc/crawlerConfigex/listDetail_militarytoday.txt";
//		String cfgFilePath = "D:/workspace/product/BSCrawler/doc/crawlerConfigex/listDetail_french_aerospace.txt";
//		 String cfgFilePath = "D:/workspace/product/BSCrawler/doc/crawlerConfigex/listDetail_nasa.txt";
//		 String cfgFilePath = "D:/workspace/product/BSCrawler/doc/crawlerConfigex/listDetail_navy.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/crawlerConfigex/listDetail_nuclear.txt";
		//String cfgFilePath = "D:/workspace/product/BSCrawler/doc/crawlerConfigex/listDetail_raytheon.txt";
		//String cfgFilePath = "D:/workspace/product/BSCrawler/doc/crawlerConfigex/listDetail_spacedaily.txt";
		//String cfgFilePath = "D:/workspace/product/BSCrawler/doc/crawlerConfigex/listDetail_spaceflightnow.txt";
//		String cfgFilePath = "D:/workspace/product/BSCrawler/doc/crawlerConfigex/listDetail_spacelaunchreport.txt";
//		 String cfgFilePath = "D:/workspace/product/BSCrawler/doc/crawlerConfigex/listDetail_spacenews.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/crawlerConfigex/listDetail_spaceref.txt";
		//String cfgFilePath = "D:/workspace/product/BSCrawler/doc/crawlerConfigex/listDetail_sputniknews.txt";
		//String cfgFilePath = "D:/workspace/product/BSCrawler/doc/crawlerConfigex/listDetail_thespacereview.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/crawlerConfigex/listDetail_tia.txt";
		// 上不去
//		String cfgFilePath = "D:/workspace/product/BSCrawler/doc/crawlerConfigex/listDetail_af.txt";
//		String cfgFilePath = "D:/workspace/product/BSCrawler/doc/crawlerConfigex/listDetail_af_21.txt";
//		 String cfgFilePath = "D:/workspace/product/BSCrawler/doc/crawlerConfigex/listDetail_af_30.txt";
		// 上不去
		//String cfgFilePath = "D:/workspace/product/BSCrawler/doc/crawlerConfigex/listDetail_af_341.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/crawlerConfigex/listDetail_af_45.txt";
		// 上不去
		//String cfgFilePath = "D:/workspace/product/BSCrawler/doc/crawlerConfigex/listDetail_af_46.txt";

		//String cfgFilePath = "D:/workspace/product/BSCrawler/doc/crawlerConfigex/listDetail_af_50.txt";
		//String cfgFilePath = "D:/workspace/product/BSCrawler/doc/crawlerConfigex/listDetail_af_90.txt";
		//String cfgFilePath = "D:/workspace/product/BSCrawler/doc/crawlerConfigex/listDetail_af_91.txt";
		//String cfgFilePath = "D:/workspace/product/BSCrawler/doc/crawlerConfigex/listDetail_af.txt";
		//String cfgFilePath = "D:/workspace/product/BSCrawler/doc/crawlerConfigex/listDetail_afgsc.txt";


		// 涉密局采集
//		String cfgFilePath = "D:/workspace/product/BSCrawler/doc/shemi/listDetail_wikileaks.txt";
//		String cfgFilePath = "D:/workspace/product/BSCrawler/doc/shemi/listDetail_reuters.txt";
//		String cfgFilePath = "D:/workspace/product/BSCrawler/doc/shemi/listDetail_thehill_defense.txt";
//		String cfgFilePath = "D:/workspace/product/BSCrawler/doc/shemi/listDetail_belfercenter_europe.txt";
//		String cfgFilePath = "D:/workspace/product/BSCrawler/doc/shemi/listDetail_csrc.txt";

		//百度新闻采集
		//String cfgFilePath = "D:/workspace/product/BSCrawler/doc/crawlerConfigex/58.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/crawlerConfigex/subject_google.txt";
		//String cfgFilePath = "D:/workspace/product/BSCrawler/doc/crawlerConfigex/subject_tempCN.txt";
		//String cfgFilePath = "D:/workspace/product/BSCrawler/doc/crawlerConfigex/subject.txt";
//		String cfgFilePath = "D:/workspace/product/BSCrawler/doc/crawlerConfigex/cnDetail_163.txt";
//		 String cfgFilePath = "D:/workspace/product/BSCrawler/doc/crawlerConfigex/cnDetail_81.txt";
//		String cfgFilePath = "D:/workspace/product/BSCrawler/doc/crawlerConfigex/cnDetail_dsti.txt";
//		 String cfgFilePath = "D:/workspace/product/BSCrawler/doc/crawlerConfigex/cnDetail_ifeng.txt";
//		 String cfgFilePath = "D:/workspace/product/BSCrawler/doc/crawlerConfigex/cnDetail_knowfar.txt";
//		 String cfgFilePath = "D:/workspace/product/BSCrawler/doc/crawlerConfigex/cnDetail_mod.txt";
//		 String cfgFilePath = "D:/workspace/product/BSCrawler/doc/crawlerConfigex/cnDetail_people.txt";
//		String cfgFilePath = "D:/workspace/product/BSCrawler/doc/crawlerConfigex/cnDetail_sinamil.txt";
		//String cfgFilePath = "D:/workspace/product/BSCrawler/doc/crawlerConfigex/cnDetail_sinamil_old.txt";
		// String cfgFilePath = "D:/workspace/product/BSCrawler/doc/crawlerConfigex/cnDetail_sohu.txt";
//		 String cfgFilePath = "D:/workspace/product/BSCrawler/doc/crawlerConfigex/cnDetail_spacechina.txt";
//		String cfgFilePath = "D:/workspace/product/BSCrawler/doc/crawlerConfigex/cnDetail_xinhua.txt";
//		 String cfgFilePath = "D:/workspace/product/BSCrawler/doc/crawlerConfigex/cnDetail_huanqiu.txt";
		//String cfgFilePath = "D:/workspace/product/BSCrawler/doc/crawlerConfigex/cnDetail_afwing.txt";
		//String cfgFilePath = "D:/workspace/product/BSCrawler/doc/weaponRep/listDetail_mp4.txt";

		SpiderBuilder builder = new SpiderBuilder();
		Spider spider = builder.builder(cfgFilePath);
		//spider.run();
		spider.start();

       // List<String> cfgs = new ArrayList<>();
       // cfgs.add("D:/workspace/product/BSCrawler/doc/crawlerConfigex/cnDetail_163.txt");
       // cfgs.add("D:/workspace/product/BSCrawler/doc/crawlerConfigex/cnDetail_81.txt");
       // cfgs.add("D:/workspace/product/BSCrawler/doc/crawlerConfigex/cnDetail_dsti.txt");
       // cfgs.add("D:/workspace/product/BSCrawler/doc/crawlerConfigex/cnDetail_ifeng.txt");
       // cfgs.add("D:/workspace/product/BSCrawler/doc/crawlerConfigex/cnDetail_knowfar.txt");
       // cfgs.add("D:/workspace/product/BSCrawler/doc/crawlerConfigex/cnDetail_mod.txt");
       // cfgs.add("D:/workspace/product/BSCrawler/doc/crawlerConfigex/cnDetail_people.txt");
       // cfgs.add("D:/workspace/product/BSCrawler/doc/crawlerConfigex/cnDetail_sinamil.txt");
       // cfgs.add("D:/workspace/product/BSCrawler/doc/crawlerConfigex/cnDetail_sohu.txt");
       // cfgs.add("D:/workspace/product/BSCrawler/doc/crawlerConfigex/cnDetail_spacechina.txt");
       // cfgs.add("D:/workspace/product/BSCrawler/doc/crawlerConfigex/cnDetail_xinhua.txt");
       //
       // for(String cfgFilePath : cfgs) {
       // 	  SpiderBuilder builder = new SpiderBuilder();
       //       Spider spider = builder.builder(cfgFilePath);
       //       //spider.run();
       //       spider.start();
       // }

	}

}
