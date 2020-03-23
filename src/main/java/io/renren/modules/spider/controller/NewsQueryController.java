package io.renren.modules.spider.controller;


import java.util.*;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.mss.crawler.common.DateUtil;
import io.renren.common.utils.DateUtils;
import io.renren.common.utils.Query;
import io.renren.modules.spider.service.IAutoClassification;
import io.renren.modules.spider.service.IRemoveDouble;
import io.renren.modules.spider.service.impl.NewsQueryServiceImpl;
import org.apache.commons.collections.MapUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;

import io.renren.common.utils.PageUtils;
import io.renren.common.utils.R;
import io.renren.modules.spider.entity.News;
import io.renren.modules.spider.service.INewsQueryService;

/**
 * NewsQueryController
 *
 * @author mfq
 * @version 18/4/2
 */
@RequestMapping("/newsquery")
@Controller
public class NewsQueryController {
	@Autowired
	private NewsQueryServiceImpl newsQueryService;
	
	@Value("${crawler.news.image.nginx.rootpath}")
	private String rootpath;

	@Autowired
	private IRemoveDouble removeDouble;

	@Autowired
	private IAutoClassification utoClassification;
	
    private Logger LOG = LogManager.getLogger(NewsQueryController.class);
    @RequestMapping("/searchByQuery")
    @ResponseBody
    public R searchByQuery(@RequestParam Map<String, Object> params){
    	int pageNumber = Integer.parseInt(params.get("page").toString());
    	int pageSize = Integer.parseInt(params.get("limit").toString());
    	params.put("offset", (pageNumber-1)*pageSize);
		String entityName = MapUtils.getString(params, "entity");
		int offset = (pageNumber-1)*pageSize;
    	List<News> searchByQuery = newsQueryService.searchMongoByQuery(entityName, params, offset, pageSize);
    	int count = (int) newsQueryService.searchMongoCountByQuery(entityName,params);
    	PageUtils pageUtil = new PageUtils(searchByQuery, count, pageSize, pageNumber);
    	return R.ok().put("page", pageUtil);
    }


	@RequestMapping("/searchByQueryMysql")
	@ResponseBody
	public R searchByQueryMysql(@RequestParam Map<String, Object> params){
		int pageNumber = Integer.parseInt(params.get("page").toString());
		int pageSize = Integer.parseInt(params.get("limit").toString());
		params.put("offset", (pageNumber-1)*pageSize);
		String entityName = MapUtils.getString(params, "entity");
		int offset = (pageNumber-1)*pageSize;
		List<News> searchByQuery = newsQueryService.searchByQuery(params);
		int count = (int) newsQueryService.searchCountByQuery(params);
		PageUtils pageUtil = new PageUtils(searchByQuery, count, pageSize, pageNumber);
		return R.ok().put("page", pageUtil);
	}

	@RequestMapping("/searchCrawlerProblemByQuery")
	@ResponseBody
	public R searchCrawlerProblemByQuery(@RequestParam Map<String, Object> params){
		int pageNumber = Integer.parseInt(params.get("page").toString());
		int pageSize = Integer.parseInt(params.get("limit").toString());
//		params.put("offset", (pageNumber-1)*pageSize);
		params.put("limit", pageSize);
		List<String> tableList = new ArrayList<>();
		tableList.add("news_cn");
		tableList.add("news_en");
		tableList.add("news_wx");
		// 最近 5 天没有采集到数据
		params.put("targetDate", DateUtil.getTargetDayPrevGivenDay(new Date(), 5));
		List<News> resultList = new ArrayList<>();
		int resultCount = 0;
		Query query = null;
		if(null != params.get("dbname") && "0".equals(params.get("dbname"))) {
			for(String tableName : tableList) {
				params.put("dbname", tableName);
				query = new Query(params);
				List<News> searchByQuery = newsQueryService.searchProblemByQuery(query);
		        int count = newsQueryService.searchProblemCountByQuery(query);

		        resultList.addAll(searchByQuery);
		        resultCount += count;
			}
		} else {
			query = new Query(params);
			resultList = newsQueryService.searchProblemByQuery(query);
			resultCount = newsQueryService.searchProblemCountByQuery(query);
		}
//		List<News> searchByQuery = newsQueryService.searchProblemByQuery(params);
//		int count = newsQueryService.searchProblemCountByQuery(params);
		PageUtils pageUtil = new PageUtils(resultList, resultCount, query.getLimit(), pageNumber);
		return R.ok().put("page", pageUtil);
	}
    
    @RequestMapping("/searchByid")
    @ResponseBody
    public News searchByid(@RequestBody String str){
    	Map<String, Object> params = (Map<String, Object>) JSON.parse(str);
    	params.put("RootPath", rootpath);
    	return newsQueryService.searchByid(params);
    }

	@RequestMapping("/searchMongoById")
	@ResponseBody
	public Map searchMongoById(@RequestBody String str){
		Map<String, Object> params = (Map<String, Object>) JSON.parse(str);
		String id = MapUtils.getString(params, "id");
		String entity = MapUtils.getString(params, "entity");
		return newsQueryService.searchMongoById(entity,id);
	}

	/**
	 * 对象列表
	 */
	@RequestMapping("/select")
	@ResponseBody
	public R select(){
		Set<String> entitys = newsQueryService.getEntitys();
		List list = new ArrayList();
		for(String entity:entitys) {
			Map<String,Object> obj = Maps.newHashMap();
			obj.put("name", entity);
			obj.put("code", entity);
			list.add(obj);
		}
		return R.ok().put("list", list);
	}

	/**
	 * 对象列表
	 */
	@RequestMapping("/selectAllStatus")
	@ResponseBody
	public R selectAllStatus(){
		List list = new ArrayList<>();
		// 一共有4中不同的状态, 一次进行数据的配置
		Map<String,Object> obj = Maps.newHashMap();
		obj.put("name", "初始");
		obj.put("code", removeDouble.STATUS_INITIAL);
		list.add(obj);
		obj = Maps.newHashMap();
		obj.put("name", "通过");
		obj.put("code", removeDouble.STATUS_PASS);
		list.add(obj);
		obj = Maps.newHashMap();
		obj.put("name", "未通过");
		obj.put("code", removeDouble.STATUS_DEL);
		list.add(obj);
		obj = Maps.newHashMap();
		obj.put("name", "重复");
		obj.put("code", removeDouble.STATUS_DOUBLE);
		list.add(obj);
		return R.ok().put("list", list);
	}

	/**
	 * 执行去重
	 */
	@RequestMapping("/removeDouble")
	@ResponseBody
	public R removeDouble(@RequestBody Map<String, Object> jsonParams) {

		String entity = MapUtils.getString(jsonParams, "entity");
		String begindateStr = MapUtils.getString(jsonParams, "begindate");
		String enddateStr = MapUtils.getString(jsonParams, "enddate");
		Date begindate = null;
		Date enddate = null;
		Integer distance = MapUtils.getInteger(jsonParams, "distance");

		//默认的相似距离
		if(distance==null||distance==0) {
			distance = 5;
		}
		int count =0;
		try{
			if(!StringUtils.isEmpty(begindateStr)&&!StringUtils.isEmpty(enddateStr)) {
				begindate = DateUtils.strToDate(begindateStr,DateUtils.DATE_PATTERN_yyyy_MM_dd);
				enddate = DateUtils.strToDate(enddateStr,DateUtils.DATE_PATTERN_yyyy_MM_dd);
			}
			count = removeDouble.executeCompute(entity, begindate, enddate,distance);
		}catch(Exception e){
			e.printStackTrace();
			return R.error(e.getMessage());
		}

		return R.ok().put("count", count);
	}

	/**
	 * 执行自动分类
	 */
	@RequestMapping("/autoCategory")
	@ResponseBody
	public R autoCategory(@RequestBody Map<String, Object> jsonParams) {

		String entity = MapUtils.getString(jsonParams, "entity");
		String begindateStr = MapUtils.getString(jsonParams, "begindate");
		String enddateStr = MapUtils.getString(jsonParams, "enddate");
		Date begindate = null;
		Date enddate = null;

		try{
			if(!StringUtils.isEmpty(begindateStr)&&!StringUtils.isEmpty(enddateStr)) {
				begindate = DateUtils.strToDate(begindateStr,DateUtils.DATE_PATTERN_yyyy_MM_dd);
				enddate = DateUtils.strToDate(enddateStr,DateUtils.DATE_PATTERN_yyyy_MM_dd);
			}
			utoClassification.executeClassification(entity, begindate, enddate);
			//removeDouble.executeCompute(entity, fromdate, todate);
		}catch(Exception e){
			e.printStackTrace();
			return R.error(e.getMessage());
		}

		return R.ok();
	}

	/**
	 * 下载数据到本地
	 */
	@RequestMapping("/down2local")
	@ResponseBody
	public String down2Local(@RequestBody String jsonParams) {
		Map<String, Object> params = (Map<String, Object>) JSON.parse(jsonParams);
		JSONArray arraies = (JSONArray) params.get("ids");
		List<String> ids = new ArrayList<>();
		for(int i = 0; i < arraies.size(); i++) {
			ids.add(arraies.getString(i));
		}
		String entity = (String) params.get("entity");
		String[] id = ids.toArray(new String[ids.size()]);
		return newsQueryService.down2Local(id, entity);
	}

	/**
	 * 修改数据
	 */
	@RequestMapping("/update")
	@ResponseBody
	public R update(@RequestBody String jsonParams){
		try{
			newsQueryService.update(jsonParams);
		}catch(Exception e){
			e.printStackTrace();
			return R.error(e.getMessage());
		}

		return R.ok();
	}

	/**
	 * 审核单条数据
	 */
	@RequestMapping("/check")
	@ResponseBody
	public R check(@RequestBody String jsonParams){
		try{
			newsQueryService.check(jsonParams);
		}catch(Exception e){
			e.printStackTrace();
			return R.error(e.getMessage());
		}

		return R.ok();
	}

	/**
	 * 批量审核数据
	 */
	@RequestMapping("/multiCheck")
	@ResponseBody
	public R multiCheck(@RequestBody String jsonParams){
		try{
			newsQueryService.multiCheck(jsonParams);
		}catch(Exception e){
			e.printStackTrace();
			return R.error(e.getMessage());
		}

		return R.ok();
	}

	/**
	 * 批量确认非重复数据
	 */
	@RequestMapping("/multiVerifyNODouble")
	@ResponseBody
	public R multiVerifyNODouble(@RequestBody String jsonParams){

		JSONObject params = JSONObject.parseObject(jsonParams);
		JSONArray arraies = params.getJSONArray("ids");
		List<String> ids = new ArrayList<>();
		for(int i = 0; i < arraies.size(); i++) {
			ids.add(arraies.getString(i));
		}
		String entity = (String) params.getString("entity");
		try{
			removeDouble.multiVerifyNODouble(entity,ids);
		}catch(Exception e){
			e.printStackTrace();
			return R.error(e.getMessage());
		}
		return R.ok();
	}
}
