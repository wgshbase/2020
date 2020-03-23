package com.mss.crawler.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mss.crawler.model.CommonCrawlerModel;
import com.mss.crawler.model.CommonCrawlerParam;
import com.mss.crawler.model.FieldModel;
import com.mss.crawler.model.ListPageModel;
import com.mss.crawler.model.PagingModel;

public class ObjectToModel {

	public JSONObject convert() {
		CommonCrawlerModel model = new CommonCrawlerModel();		
		CommonCrawlerParam param = new CommonCrawlerParam();
		param.setCycleRetryTimes(3);
		param.setDomain("spacenews.com");
		param.setDownloaderClassName("com.mss.crawler.downloader.MyHttpClientDownloader");
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		headers.put("Accept-Encoding", "gzip, deflate, sdch");
		headers.put("Accept-Language", "zh-CN,zh;q=0.8");
		headers.put("Cache-Control", "max-age=0");
		headers.put("Connection", "keep-alive");
		headers.put("Host", "spacenews.com");
		headers.put("Referer", "http://spacenews.com/");
		headers.put("Upgrade-Insecure-Requests", "1");
		headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36");
		
		param.setHeaders(headers);
		
		model.setCommonCrawlerParam(param);
		
		ListPageModel listPageModel = new ListPageModel();
		listPageModel.setListXpath1("//*[@id=\"main\"]/div[2]/div[1]");
		listPageModel.setListXpath2("//*[@id=\"main\"]/div[2]/div[2]");
		listPageModel.setPaging(true);
		
		PagingModel pagingModel = new PagingModel();
		pagingModel.setPagerXpath("//*[@id=\"main\"]/p[2]/a/@href");
		
		listPageModel.setPagingModel(pagingModel);
		
		FieldModel fieldModel = new FieldModel();
		fieldModel.setDataType("image");
		fieldModel.setFieldName("detailUrl");
		fieldModel.setXpath("//span/a");
		
		FieldModel fieldModel1 = new FieldModel();
		fieldModel1.setDataType("text");
		fieldModel1.setFieldName("newsTitle");
		fieldModel1.setXpath("//div/h2/a/text()");
		
		FieldModel fieldModel2 = new FieldModel();
		fieldModel2.setDataType("outHtml");
		fieldModel2.setFieldName("newsAuthor");
		fieldModel2.setXpath("//div/div[@class='launch-author']");
		
		FieldModel fieldModel3 = new FieldModel();
		fieldModel3.setDataType("outHtml");
		fieldModel3.setFieldName("newsAbstract");
		fieldModel3.setXpath("//div/p");
		
		FieldModel fieldModel4 = new FieldModel();
		fieldModel4.setDataType("text");
		fieldModel4.setFieldName("newsClassfiy");
		fieldModel4.setXpath("//div/header/a/text()");
		
		List<FieldModel> list = new ArrayList<FieldModel>();
		list.add(fieldModel);
		list.add(fieldModel1);
		list.add(fieldModel2);
		list.add(fieldModel3);
		list.add(fieldModel4);
		listPageModel.setFieldList(list);
		model.setPageModel(listPageModel);
		model.setCrawlerType("listPageModel");
		return JSON.parseObject(JSONObject.toJSONString(model));
	}
}
