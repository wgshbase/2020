package io.renren.modules.spider.service.impl;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mss.crawler.spiderjson.util.PingIPUtils;
import io.renren.modules.spider.entity.News;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicUpdate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.mongodb.BasicDBObject;
import com.mss.word.HtmlToWordByPOI;

import io.renren.modules.spider.dao.NewsDao;
import io.renren.modules.spider.dao.UnpackRecordDao;
import io.renren.modules.spider.entity.UnpackRecord;
import io.renren.modules.spider.service.INewsQueryService;
import io.renren.modules.spider.service.IRemoveDouble;
import io.renren.modules.spider.utils.CSVUtils;
import io.renren.modules.spider.utils.UnpackUtils;

@Service("newsQueryService")
public class NewsQueryServiceImpl implements INewsQueryService{
	@Autowired
	private NewsDao newsDao;

	@Autowired
	private UnpackRecordDao recordDao;

	@Autowired
	private MongoTemplate mongoTemplate;

	private HtmlToWordByPOI poi;

	private static String fileStorePath = "D:/data/";

	private static String chinPrefix = "<p style=\"text-align:center\"><span style=\"color:red\">————————————————————本文来源于国外网站,以下是机器翻译内容,向下阅读可查看原文————————————————————</span></p>";

	private static String engPrifix = "<p style=\"text-align: center;\"><span>————————————————————原文————————————————————</span></p>";

	public List<News> searchByQuery(Map<String, Object> map) {
		return newsDao.queryListByKeyword(map);
	}
	public int searchCountByQuery(Map<String, Object> map) {

		return newsDao.queryListCountByKeyword(map);
	}
	public News searchByid(Map<String, Object> params) {
		News news = newsDao.queryById(params);
		String content = news.getContent();
		String contentTr = news.getContentTr();
		if(!org.springframework.util.StringUtils.isEmpty(contentTr)) {
			news.setContentTr(contentTr.replace("${RootPath}", (String) params.get("RootPath")));
			news.setChinContent(getChinContent(news.getContentTr()));
			news.setEngContent(getEngContent(news.getContentTr()));
		}
		news.setContent(content.replace("${RootPath}", (String) params.get("RootPath")));

		return news;
	}

	public List<News> searchProblemByQuery(Map<String, Object> params) {
		List<News> newsList = newsDao.searchProblemByQuery(params);
		if(null != newsList && newsList.size() > 0) {
			for(News news : newsList) {
				String siteDomain = news.getSiteDomain();
				try {
					if(PingIPUtils.pingIp(siteDomain)) {
						news.setCrawlerProblem("疑似采集网站需求模板变动");
					} else {
						news.setCrawlerProblem("采集网站不存在了");
					}
				} catch (Exception e) {
					news.setCrawlerProblem("采集网站不存在了");
					e.printStackTrace();
				}
			}
		}
		return newsList;
	}

	public int searchProblemCountByQuery(Map<String, Object> params) {
		return newsDao.searchProblemCountByQuery(params);
	}

	public String getChinContent(String contentTr) {
		if(!org.apache.commons.lang3.StringUtils.isEmpty(contentTr)) {
			return contentTr.substring(contentTr.indexOf(chinPrefix) + chinPrefix.length(), contentTr.indexOf(engPrifix));
		}
		return "空内容";
	}

	public String getEngContent(String contentTr) {
		if(!org.apache.commons.lang3.StringUtils.isEmpty(contentTr)) {
			return contentTr.substring(contentTr.indexOf(engPrifix) + engPrifix.length(), contentTr.length());
		}
		return "Empty Content!!";
	}


	@Override
	public List searchMongoByQuery(String entity,Map<String, Object> map,int pageIndex,int pageSize) {

		String keyWord = MapUtils.getString(map, "keyWord");
		Query query = new Query();
		if(!StringUtils.isEmpty(keyWord)) {
			Pattern pattern = Pattern.compile("^.*"+keyWord+".*$", Pattern.CASE_INSENSITIVE);
			String fieldName = "title";
			query.addCriteria(Criteria.where(fieldName).regex(pattern));
		}

		// 是否需要判定信息的审核状态
		String status = MapUtils.getString(map, "status");
		if(!org.springframework.util.StringUtils.isEmpty(status)) {
			query.addCriteria(Criteria.where("status").is(status));

			// 状态显示为重复状态的数据, 将数据按照对应的 重复键进行排序
			if(status.equals(IRemoveDouble.STATUS_DOUBLE)) {
				query.with(new Sort(new Sort.Order(Sort.Direction.DESC, "double_key")));
			}
		}

		String notStatus = MapUtils.getString(map, "notStatus");
		if(!org.springframework.util.StringUtils.isEmpty(notStatus)) {
			query.addCriteria(Criteria.where("status").ne(notStatus));
		}

		// 设置排序的规则 ---> 替换为按照发布时间倒叙排序
		// query.with(new Sort(new Sort.Order(Sort.Direction.DESC, "crawler_date")));
		query.with(new Sort(new Sort.Order(Sort.Direction.DESC, "pubDate")));

		// 获取采集的数据的采集日期区间值
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date begindate = null;
		Date enddate = null;
		try {
			String begindateStr = MapUtils.getString(map, "begindate");
			String enddateStr = MapUtils.getString(map, "enddate");
			begindate = sdf.parse((begindateStr.equals("")?"1970-01-01":begindateStr));
			enddate = sdf.parse((enddateStr.equals("")?"2099-01-01":enddateStr));
		} catch (ParseException e) {
			System.out.println("日期格式化出错, 请检查相关设置");
			e.printStackTrace();
		}
		query.addCriteria(Criteria.where("pubDate").gte(begindate).lte(enddate));

		query.skip(pageIndex);// 从那条记录开始
		query.limit(pageSize);// 取多少条记录
		return mongoTemplate.find(query, Map.class,entity);
	}
	@Override
	public long searchMongoCountByQuery(String entity,Map<String, Object> map) {

		Query query = new Query();

		// 是否需要判定信息的审核状态
		String status = MapUtils.getString(map, "status");
		if(!org.springframework.util.StringUtils.isEmpty(status)) {
			query.addCriteria(Criteria.where("status").is(status));
		}

		// 获取采集的数据的采集日期区间值
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date begindate = null;
		Date enddate = null;
		try {
			String begindateStr = MapUtils.getString(map, "begindate");
			String enddateStr = MapUtils.getString(map, "enddate");
			begindate = sdf.parse((begindateStr.equals("")?"1970-01-01":begindateStr));
			enddate = sdf.parse((enddateStr.equals("")?"2099-01-01":enddateStr));
		} catch (ParseException e) {
			System.out.println("日期格式化出错, 请检查相关设置");
			e.printStackTrace();
		}
		query.addCriteria(Criteria.where("pubDate").gte(begindate).lte(enddate));

		return mongoTemplate.count(query, entity);
	}
	@Override
	public Map<String,Object> searchMongoById(String entity,String id) {
		return mongoTemplate.findById(id, Map.class,entity);
	}
	@Override
	public Set<String> getEntitys() {
		return mongoTemplate.getDb().getCollectionNames();
	}
	@Override
	public String down2Local(String[] ids, String entity) {
		try {
			poi = new HtmlToWordByPOI();
			poi.setRootPath(fileStorePath);
			for(String id : ids) {
				Map<String, String> map = mongoTemplate.findById(id, Map.class, entity);
				// 当前的网站的域名
				String sitedomain = map.get("crawler_site");
				String title = map.get("title");
				if(StringUtils.isEmpty(title)) {
					System.out.println("The news you wanna download has no title...");
					continue;
				}
				if(title.contains(" ")) {
					try {
						title = URLEncoder.encode(title, "UTF-8");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
				}
				//poi.toWord(getTargetHtml(map), fileStorePath + entity + File.separator + title + ".doc");

			}
			return "1";
		} catch (Exception e) {
			e.printStackTrace();
			return "0";
		}
	}

	@Override
	public void update(String jsonParams) {
		Map<String, Object> params = (Map<String, Object>) JSON.parse(jsonParams);
		String id = MapUtils.getString(params, "_id");
		/*String entity = MapUtils.getString(params, "entity");
		Query query = new Query(Criteria.where("_id").is(id));
		BasicDBObject dbo = new BasicDBObject();

		dbo = BasicDBObject.parse(jsonParams.replace("&lt;", "<").replace("&gt;", ">").replace("&amp;", "&"));
		BasicUpdate update = new BasicUpdate(dbo);
		mongoTemplate.updateFirst(query, update, entity);

		// 抽取发布时间字段进行格式化
		Matcher m = Pattern.compile("\"pubDate\":\"(.*?)\"").matcher(jsonParams);
		if(m.find()) {
			System.out.println(m.group(1));
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date date = null;
			try {date = sdf.parse(m.group(1));} catch (ParseException e) {}
			if(null != date) {
				Update updateDate = Update.update("pubDate", date);
				mongoTemplate.updateFirst(query, updateDate, entity);
			}
		}*/
		newsDao.updateNewsInformation(params);
	}

	@Override
	public void check(String jsonParams) {
		Map<String, Object> params = (Map<String, Object>) JSON.parse(jsonParams);
		String id = MapUtils.getString(params, "id");
		String entity = MapUtils.getString(params, "entity");
		String status = MapUtils.getString(params, "status");
		Query query = new Query(Criteria.where("_id").is(id));
		Update update = Update.update("status", status);
		mongoTemplate.updateFirst(query, update, entity);

	}
	@Override
	public void multiCheck(String jsonParams) {
		Map<String, Object> params = (Map<String, Object>) JSON.parse(jsonParams);
		JSONArray arraies = (JSONArray) params.get("ids");
		List<String> ids = new ArrayList<>();
		for(int i = 0; i < arraies.size(); i++) {
			ids.add(arraies.getString(i));
		}
		String entity = (String) params.get("entity");
		String status = MapUtils.getString(params, "status");
		Query query = new Query(Criteria.where("_id").in(ids));
		Update update = Update.update("status", status);
		mongoTemplate.updateMulti(query, update, entity);
	}
	public String getProject(String params) {
		Map<String, Object> map = (Map<String, Object>) JSON.parse(params);
		String entity = MapUtils.getString(map, "entity");
		Query query = new Query();
		query.addCriteria(Criteria.where("status").is(IRemoveDouble.STATUS_PASS));
		Map result = mongoTemplate.findOne(query, Map.class, entity);
		Set keys = result.keySet();
		Object project = "";
		for(Object key : keys) {
			if("crawler_project".equals(key.toString())) {
				project = result.get(key);
				break;
			}
		}
		if(project == null) {
			return "";
		}
		return (String)project;
	}
	@Override
	public String down2local(String params) {
		Map<String, Object> map = (Map<String, Object>) JSON.parse(params);
		String entity = MapUtils.getString(map, "entity");
		String begindateStr = MapUtils.getString(map, "begindate");
		String enddateStr = MapUtils.getString(map, "enddate");
		//1. 查询目标的数据
		Query query = new Query();

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date begindate = null;
		Date enddate = null;
		try {
			begindate = sdf.parse((begindateStr.equals("")?"1970-01-01":begindateStr));
			enddate = sdf.parse((enddateStr.equals("")?"2099-01-01":enddateStr));
		} catch (ParseException e) {
			System.out.println("日期格式化出错, 请检查相关设置");
			e.printStackTrace();
		}
		query.addCriteria(Criteria.where("pubDate").gte(begindate).lte(enddate));
		// 添加额外的查询条件
		query.addCriteria(Criteria.where("status").is(IRemoveDouble.STATUS_PASS));
		List<Map> maps = mongoTemplate.find(query, Map.class, entity);
		if(maps==null || maps.isEmpty()) {
			return "-1";
		}

		String project = getProject(params);

		//2. 将数据按照项目进行打包
		Map<String, String> result = UnpackUtils.unpack(project, maps);
		//3. 添加打包记录...
		UnpackRecord record = new UnpackRecord();

		record.setId(UUID.randomUUID().toString().replace("-", "").substring(0, 15));
		record.setCreateTime(result.get("createTime"));
		record.setFilestorePath(result.get("filestorePath"));
		record.setZipname(result.get("zipname"));
		record.setZipPath(result.get("zipPath"));
		record.setProject(project);

		recordDao.save(record);

		return "1";
	}
	@Override
	public Map extract(String jsonParams) {
		Map<String, Object> params = (Map<String, Object>) JSON.parse(jsonParams);
		String id = MapUtils.getString(params, "id");
		String entity = MapUtils.getString(params, "entity");
		Query query = new Query();
		query.addCriteria(Criteria.where("_id").is(id));
		// 查找对象
		Map map = mongoTemplate.findById(id, Map.class, entity);

		return UnpackUtils.extract(map);
	}
	@Override
	public String report(String jsonParams) {
		Map<String, Object> params = (Map<String, Object>) JSON.parse(jsonParams);
		JSONArray arraies = (JSONArray) params.get("ids");
		String entity = MapUtils.getString(params, "entity");
		String project = MapUtils.getString(params, "project");
		Query query = new Query();
		query.addCriteria(Criteria.where("_id").in(arraies.toJavaList(String.class)));
		List<Map> list = mongoTemplate.find(query, Map.class, entity);
		StringBuilder sb = new StringBuilder();
		if(null == list || list.size() == 0) {
			return "";
		}
		for(Map map : list) {
			sb.append(UnpackUtils.getTargetHtml(map));
		}
		HtmlToWordByPOI poi = new HtmlToWordByPOI();
		if(sb.length() > 0) {
			String wordpath = fileStorePath + "reports" + File.separator + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + File.separator;
			System.out.println(wordpath + UUID.randomUUID().toString().replace("-", "").substring(0, 18)+ ".doc");
			UnpackUtils.delAllFile(wordpath);
			String totalPath = wordpath + UUID.randomUUID().toString().replace("-", "").substring(0, 18)+ ".doc";
			poi.toWord(sb.toString(), totalPath);
			/*try {
				String decode = URLDecoder.decode(wordpath,"UTF-8");
				if(decode.contains("/")) {
					decode = decode.replace("/", File.separator);
				}
				Runtime.getRuntime().exec("explorer " + decode);
			} catch (IOException e) {
				e.printStackTrace();
			}*/
			return totalPath;
		}

		return "";
	}

	@Override
	public Map<String, String> createReport(String jsonParams) {
		Map<String, String> result = new HashMap<>();
		List<Map> list = new ArrayList<>();
		// 1. 获取请求参数
		Map<String, Object> params = (Map<String, Object>) JSON.parse(jsonParams);
		String searchKeywords = (String) params.get("searchKeywords");

		JSONArray entities = (JSONArray) params.get("checkedEntitiesList");
		JSONArray columns = (JSONArray) params.get("checkedReportList");

		// 2. 分别查询所有的符合搜索条件的数据
		if(entities.size() == 0) {
			return result;
		}

		// 2.1 拼接请求体 query
		Query query = new Query();
		// 获取采集的数据的采集日期区间值
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date begindate = null;
		Date enddate = null;
		try {
			String begindateStr = MapUtils.getString(params, "begindate");
			String enddateStr = MapUtils.getString(params, "enddate");
			begindate = sdf.parse((begindateStr.equals("")?"1970-01-01":begindateStr));
			enddate = sdf.parse((enddateStr.equals("")?"2099-01-01":enddateStr));
		} catch (ParseException e) {
			System.out.println("日期格式化出错, 请检查相关设置");
			e.printStackTrace();
		}
		query.addCriteria(Criteria.where("pubDate").gte(begindate).lte(enddate));

		// 添加额外的查询条件, 状态为通过的数据
		query.addCriteria(Criteria.where("status").is(IRemoveDouble.STATUS_PASS));

		// 关键词模糊查询
		Pattern pattern=Pattern.compile("^.*"+searchKeywords+".*$", Pattern.CASE_INSENSITIVE);
		query.addCriteria(Criteria.where("nlpKeywords").regex(pattern));

		for(int i = 0; i < entities.size(); i++) {
			List<Map> t = mongoTemplate.find(query, Map.class, entities.get(i).toString());
			list.addAll(t);
		}
		// 查询结果为空, 直接返回
		if(list.size() == 0) {
			return result;
		}
		// 3. 拼接数据并返回
		result.put("totalCount", list.size() + "");
		result.put("list", CSVUtils.parseList2Html(list, columns));
		return result;
	}

}
