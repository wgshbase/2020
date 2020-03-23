package com.mss.crawler.spiderjson.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mss.crawler.common.HttpClientPoolUtil;
import com.mss.crawler.common.YzmUtils;
import com.mss.crawler.spiderjson.JSONSpider;
import com.mss.crawler.spiderjson.ResourceFile;
import com.mss.crawler.spiderjson.SpiderNextAction;
import com.mss.crawler.spiderjson.config.JSONExtractorConfig;
import com.mss.crawler.spiderjson.config.SpiderConfig;
import com.mss.crawler.spiderjson.extractor.ObjectModelExtractor;
import com.mss.crawler.spiderjson.scheduler.FileCacheDoubleRemvoeFilter;
import com.mss.crawler.spiderjson.util.HttpFileUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.cookie.Cookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.selector.Html;
import us.codecraft.webmagic.selector.Selectable;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 新浪微博解析器
 * @Author wgsh
 * @Date wgshb on 2019/1/23 9:30
 */
public class WeiBoModelPageProcessor implements IConfigAblePageProcessor, SpiderNextAction {
	protected Logger logger = LoggerFactory.getLogger(WeiBoModelPageProcessor.class);

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

	private String[] indexs;

	private String seedRex;

	private int searchType=1;

	private String rootPath;

	private JSONSpider spider;

	private static String weiboLoginPage = "https://login.sina.com.cn/signup/signin.php";

	private static String weiboUsername = "13718757469";

	private static String weiboPassword = "bestdata@2017";

	private String jsonCookie = "";

	public WeiBoModelPageProcessor(Site site ,SpiderConfig config) {
		this.site = site;
		this.config = config;
		this.indexs = config.getIndexs();
		this.seedRex = config.getSeedRex();
		this.rootPath = config.getRootPath();

		this.fileCacheDoubleRemvoeFilter = new FileCacheDoubleRemvoeFilter(this.rootPath+"/"+this.site.getDomain());

		this.iniPageModel(config.getExtractorCfgs());

		//初始化词索引队列
		String[] indexs = config.getIndexs();
		if(indexs!=null&&indexs.length>0){
			List<String> seedUrls = getSearchSeed(indexs);
			for(String seedUrl:seedUrls){
				// 拼接索引词对象...
				Request newSeed = this.createReq(seedUrl, "search",null, null, null);
				indexsQueue.add(newSeed);
			}
		}

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
			Request newReq = this.indexsQueue.poll();
			page.addTargetRequest(newReq);

			//处理公众号列表
		}else if("search".equals(type)){
			List<Selectable> links = html.xpath("//div[@class='info']/div").nodes();
			if(links.size()!=0){
				String seedUrl = links.get(0).xpath("//a[@class='name']/@href").get();
				String targetUrl = HttpFileUtil.getDownloadUrl(seedUrl, page.getRequest().getUrl());
				Request newSeed = this.createReq(targetUrl, "article_list",null, null, null);
				page.addTargetRequest(newSeed);
			}
			//处理文章列表
		}else if("article_list".equals(type)){
			if(page.getHtml().xpath("//title/text()").get().indexOf("请输入验证码") > -1) {
				page = crackAuthCode(page);
			}
			parseSeed(page);
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
				page.putField(extractor.getValue().getObjectName(), process);

				// 添加 Solr 需要的额外的信息
				page.putField("news_category", config.getNewsCategory());
				page.putField("dbType", config.getDbType());

				page.putField("copyright", process.get("copyright"));

				// 添加不同的项目的标识符
				if (!StringUtils.isEmpty(config.getSslm())) {
					page.putField("sslm", config.getSslm());
				}

				//将成功解析好的对象，doubleKey入栈
				this.fileCacheDoubleRemvoeFilter.push(page.getRequest().getUrl());
			}

			if(objectNames.size()>0){
				page.putField("objectNames", objectNames);
			}

			page.putField("domain", site.getDomain());

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
	 * @param searchText
	 * @param type
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public List<String> getSearchSeed(String[] indexs){
		List<String> seeds = new ArrayList<String>();
		for(String searchText:indexs){
			String seedUrl = seedRex;
			seedUrl = org.springframework.util.StringUtils.replace(seedUrl, "${type}", searchType+"");
			try {
				seedUrl = org.springframework.util.StringUtils.replace(seedUrl, "${query}", URLEncoder.encode(searchText, "utf-8"));
				seeds.add(seedUrl);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			seeds.add(seedUrl);
		}
		return seeds;
	}

	private Request createReq(String seedUrl,String type,String json, String title, String date){
		Request newSeed = new Request(seedUrl);
		HashMap extras = new HashMap();
		extras.put(TYPE_LABEL, type);
		extras.put(TITLE_LABEL, title);
		extras.put(DATE_LABEL, date);
		if(!org.springframework.util.StringUtils.isEmpty(json)){
			extras.put(LIST_OBJECT_LABEL, json);
		}
		newSeed.setExtras(extras);
		// if(StringUtils.isEmpty(jsonCookie)) {
			//jsonCookie = new HttpFileUtil().getWeiboCookie(weiboLoginPage, weiboUsername, weiboPassword);
		//
		// }
		//System.out.println("==================================" + jsonCookie);
		// 获取当前的登陆用户的 cookie
		String cookieJson = "SINAGLOBAL=6801017706280.694.1523949699129; UM_distinctid=167f497f34d1e3-0c8c83f86f51-b78173e-1fa400-167f497f34f414; SCF=AhJeGsrBUX6-hea2Q4WuW_Kz9COcUAUoZchowecZ30InfljPedoTVxSfC0VhrUxuZx-QF-OarLBStiAlZ-jfTWo.; SUHB=06Z1FV1KMRlX6P; ALF=1551247291; SUB=_2A25xSurrDeRhGeVI6lAY8ifLzTyIHXVStPajrDV8PUJbkNAKLWjQkW1NTBqKUCJb3RfgBDXJMe2YwF0O7FBzK9I_; SUBP=0033WrSXqPxfM725Ws9jqgMF55529P9D9WFRcPqsM.amQYvCYToz6S2D5JpX5oz75NHD95Q0So2E1Kz4S0q7Ws4DqcjsUcU0Isi0; UOR=,,v.ifeng.com; _s_tentry=-; Apache=5404374921444.22.1550563363228; ULV=1550563363256:27:6:1:5404374921444.22.1550563363228:1550311067303; WBStorage=d4fb937c8bb4f57c";
		String[] cookies = cookieJson.split("; ");
		for(String cookie : cookies) {
			newSeed.addCookie(cookie.substring(0, cookie.indexOf("=")), cookie.substring(cookie.indexOf("=") + 1, cookie.length()));
		}
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
		detailMa.put("pdffiles", "/" + domain + "/");
		detailMa.put("copyright", json.get("copyright"));
		return detailMa;
	}


	/**
	 * 解析列表頁的url
	 * @param page
	 */
	private void parseSeed(Page page){
		String s = page.getHtml().toString();
		String[] ss = s.split("<script>FM.view");
		StringBuilder sb = new StringBuilder();
		for(String x : ss) {
			if (x.contains("\"html\":\"")) {
				String value = getHtml(x);
				sb.append(value);
			}
		}
		Html html = new Html(sb.toString());
		// 抽取当前页的所有的链接
		List<Selectable> links = html.xpath("//div[@class='WB_from S_txt2']").nodes();
		for(Selectable link : links) {
			String seedUrl = link.xpath("//a[@node-type='feed_list_item_date']/@href").get();
			String targetUrl = HttpFileUtil.getDownloadUrl(seedUrl, page.getRequest().getUrl());
			//判断是否重复
			if(!this.fileCacheDoubleRemvoeFilter.isContains(targetUrl)){
				Request newSeed = this.createReq(targetUrl, "article",null, null, null);
				page.addTargetRequest(newSeed);
			}
		}

	}

	public static String getHtml(String s) {
		String content = s.split("\"html\":\"")[1]
				.replaceAll("(\\\\t|\\\\n|\\\\r)", "").replaceAll("\\\\\"", "\"")
				.replaceAll("\\\\/", "/");
		content = content.substring(0,
				content.length() <= 13 ? content.length()
						: content.length() - 13);
		return content;
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
}
