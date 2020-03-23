package com.mss.crawler.spiderjson.pipeline;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

import io.renren.modules.spider.service.impl.BaiduNlpServiceImpl;
import io.renren.modules.spider.service.impl.NlpServiceImpl;
import io.renren.modules.spider.utils.NLPUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.db.utils.DbBuilder;
import com.mss.crawler.spiderjson.ResourceFile;
import com.mss.crawler.spiderjson.util.HtmlFormatter;
import com.mss.crawler.spiderjson.util.KeywordsUtils;
import com.mss.translate.GoogleTranslate;

import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;
import us.codecraft.webmagic.utils.FilePersistentBase;


/**
 * 图片文件下载
 *
 * @author wangdw
 */

public class NewsTranslate2DBPipeline extends FilePersistentBase implements Pipeline {

	private Logger logger = LoggerFactory.getLogger(getClass());

	protected static GoogleTranslate translate = new GoogleTranslate();

	protected static BaiduNlpServiceImpl baiduNlpService = new BaiduNlpServiceImpl();
	protected static NlpServiceImpl nlpService = new NlpServiceImpl();


	public NewsTranslate2DBPipeline(String path) {
		this.path = path;
	}

	@Override
	public void process(ResultItems resultItems, Task task) {

		List<String> objectNames = resultItems.get("objectNames");
		List<ResourceFile> rfs = (List<ResourceFile>) resultItems.get("resources");
//        String fileStorePath = PATH_SEPERATOR + task.getUUID() + PATH_SEPERATOR;
//        String attchfiles =getAttchfiles(rfs,fileStorePath);
		if (objectNames != null) {
			for (String objectName : objectNames) {
				Map<String, Object> obj = (Map<String, Object>) resultItems.get(objectName);
				if (obj != null) {
					obj.put("url", resultItems.getRequest().getUrl());
					obj.put("crawlerdate", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
					obj.put("siteDomain", resultItems.get("domain"));
					obj.put("pre_title", "英译");
					// 加入額外的信息
					obj.put("news_category", resultItems.get("news_category"));
					obj.put("dbType", resultItems.get("dbType"));
					obj.put("sslm", resultItems.get("sslm"));

					// 附件信息
					obj.put("sourceSite", resultItems.get("sourceSite"));
					obj.put("searchText", resultItems.get("searchText"));

					// 加入图片的格式错误的集合
					obj.put("oldTypeImages", resultItems.get("oldTypeImages"));
					obj.put("realTypeImages", resultItems.get("realTypeImages"));
					obj.put("badFormatImages", resultItems.get("badFormatImages"));

					//obj.put("pre_title", getLanguage((String)obj.get("language")));
					if (!StringUtils.isEmpty(resultItems.get("src"))) {
						obj.put("src", resultItems.get("src"));
					}

					// 此处的文件的保存路径直接修改为对应的 域名为 前缀
					String fileStorePath = PATH_SEPERATOR + obj.get("siteDomain") + PATH_SEPERATOR;
					String attchfiles = getAttchfiles(rfs, fileStorePath);

					if (!StringUtils.isEmpty(attchfiles)) {
						obj.put("attchfiles", attchfiles);
					}

					if (!StringUtils.isEmpty(obj.get("attchfiles")) && null == obj.get("headimg")) {
						obj.put("headimg", getHeadimg(attchfiles));
					}

					if(!StringUtils.isEmpty(resultItems.get("pubdate"))) {
						obj.put("pubDate", resultItems.get("pubdate"));
					}

					String title = (String) obj.get("title");
					String content = (String) obj.get("content");
					if (!StringUtils.isEmpty(title) && !StringUtils.isEmpty(content)) {
						obj.put("title_tr", translate.translate(title).trim());
						String content_tr = translate.translate(content);
						if (!StringUtils.isEmpty(content_tr)) {
							obj.put("content_tr", getContent(title, content, content_tr));
							obj.put("formattedContent", HtmlFormatter.formatContent((String) (obj.get("content_tr"))));

							// 加入关键词
							String keywords = KeywordsUtils.getKeywordsByTitleAndContent((String) obj.get("title_tr"), (String) obj.get("content_tr"));
							obj.put("keywords", keywords);
							if (((String) obj.get("formattedContent")).replace("<p>————————————————————本文来源于国外网站,以下是机器翻译内容,向下阅读可查看原文————————————————————</p>","").replace("<p>————————————————————原文————————————————————</p>","").length() > 150) {
								writeToDB(objectName, obj);
							} else {
								logger.error("-------------> The news contains the words in the specified BlackList!!!");
							}
						}

					} else {
						logger.error("-------------> The news has no title!!!");
					}
				}
			}
		}
	}

	// 截取附件的第一张图片作为新闻的封面
	private String getHeadimg(String attchfiles) {
		if (attchfiles.contains(",")) {
			return attchfiles.substring(0, attchfiles.indexOf(","));
		}
		return attchfiles;
	}

	private String getContent(String title, String contenten, String contentcn) {
		String topLine = "<p style=\"text-align:center\"><span style=\"color:red\"><strong>————————————————————本文来源于国外网站,以下是机器翻译内容,向下阅读可查看原文————————————————————</strong></span></p>";
		String result = topLine + dealContent(contentcn) +
				"<p style=\"text-align: center;\"><span>————————————————————原文————————————————————</span></p>" +
				"<p style=\"text-align: center;\">" + title + "</p>" +
				dealContent(contenten);
		return result;
	}

	private String getAttchfiles(List<ResourceFile> rfs, String fileStorePath) {
		StringBuilder attchfiles = new StringBuilder();
		if (rfs != null) {
			for (ResourceFile entry : rfs) {
				String newFileName = entry.getNewFileName();
				StringBuffer imgFileNameNew = new StringBuffer();
				imgFileNameNew.append(entry.getROOTPATH_LABEL());
				imgFileNameNew.append(fileStorePath.replace("\\", "/"));
				imgFileNameNew.append(entry.getRelativePath());
				imgFileNameNew.append(newFileName);
				attchfiles.append(imgFileNameNew).append(",");
                
               /* String extName=entry.getFileExtName();
                String newFileName = entry.getNewFileName();
                StringBuffer imgFileNameNew = new StringBuffer(fileStorePath);
                imgFileNameNew.append(entry.getRelativePath());
                imgFileNameNew.append(PATH_SEPERATOR);
                imgFileNameNew.append(newFileName);	            
                attchfiles.append(imgFileNameNew).append(",");*/

			}
			if (attchfiles.length() > 1) {
				return attchfiles.substring(0, attchfiles.length() - 1);
			}

		}
		return null;
	}

	private void insertDB(String sql, Object[] objs) {
		Connection conn = DbBuilder.getConnection();
		DbBuilder.beginTransaction(conn);
		DbBuilder.save(conn, sql, objs);
		DbBuilder.commit(conn);
		//DbBuilder.close(conn);
	}

	public void writeToDB(String table, Map<String, Object> obj) {
		try {
			//构建insert语句
			StringBuilder insert = new StringBuilder(" insert into ");
			insert.append(table).append("(");
			StringBuilder values = new StringBuilder(" VALUES(");
			List list = new ArrayList();
			//拼写语句和循环设置参数
			for (Entry<String, Object> entry : obj.entrySet()) {
				if (entry.getValue() != null && entry.getKey() != "oldTypeImages" && entry.getKey() != "realTypeImages" && entry.getKey() != "badFormatImages") {
					insert.append(entry.getKey().toLowerCase()).append(",");
					values.append("?").append(",");
					list.add(NewsDBPipeline.dealNews(entry.getValue() + "", obj));
				}
			}

			StringBuilder sql = new StringBuilder();
			sql.append(insert.substring(0, insert.length() - 1)).append(")");
			sql.append(values.substring(0, values.length() - 1)).append(")");
			insertDB(sql.toString(), list.toArray());
		} catch (Exception e) {
			e.printStackTrace();
		}

		// 若 obj 集合中包含 key 为 sslm 并且 其对应的值为 A01 的话, 插入对应的记录到新表 bs_xwzx 中.
		if(!StringUtils.isEmpty(obj.get("sslm"))) {
			if("oceanEn".equals(obj.get("sslm"))) {
				// 外文资讯
				String newTable = "bs_news_en";
				String titleTr = (String) obj.get("title_tr");
				String title = (String) obj.get("title");
				String con = (String) obj.get("formattedContent");

				if(!StringUtils.isEmpty(con)) {
					String cnprefix = "<p>&nbsp;&nbsp;————————————————————本文来源于国外网站,以下是机器翻译内容,向下阅读可查看原文————————————————————</p>";
					String enPrefix = "<p>&nbsp;&nbsp;————————————————————原文————————————————————</p>";
					String contentTr = HtmlFormatter.formatContent(con.substring(con.indexOf(cnprefix) + cnprefix.length(), con.indexOf(enPrefix)));

					obj.put("content_tr", contentTr);
					String formatCon = NewsDBPipeline.dealNews((String) obj.get("content"), obj);
					String content = HtmlFormatter.formatContent(formatCon);
//						content = content.replace(titleFix, "<p>");
					obj.put("content", content);

					if(!StringUtils.isEmpty(titleTr) && !StringUtils.isEmpty(contentTr)) {
						obj = addExtraInfo2DynamicData(titleTr, contentTr, obj);
						// 判断采集的数据的关键词是否包含目标关键词
						Boolean insertFlag = NewsDBPipeline.checkObjIsContainsTargetKeywords(obj, NewsDBPipeline.keywordsFilePath);
						Boolean insertPubdateFlag = NewsDBPipeline.checkObjIsPubdateRight(obj);
						Boolean insertContentTitlePubdateFlag = NewsDBPipeline.insertContentTitlePubdateFlag(obj);

						if(insertFlag && insertPubdateFlag && insertContentTitlePubdateFlag) {
							NewsDBPipeline.addObj2OceanPublicTables(table, newTable, obj);
							// 新发现的机构的名称处理
							Map<String, Set<String>> resultMap = baiduNlpService.recognize((String) obj.get("content_tr"));
							NewsDBPipeline.addOrganNew2Local(resultMap.get("organ"));
							NewsDBPipeline.addPlaceNew2Local(resultMap.get("place"));
							NewsDBPipeline.addPersonNew2Local(resultMap.get("person"));
						} else {
							System.out.println("采集的数据不在对应的采集范围。。。");
						}

					}
				}

			} else {
				String newTable = "bs_xwzx";
				NewsDBPipeline.addObj2NewTables(table, newTable, obj);
			}

		}

	}

	protected static Map<String, Object> addExtraInfo2DynamicData(String title, String content, Map<String, Object> dynamicData) {
		Map<String, Object> formattedResult = NewsDBPipeline.getFormattedResult();
		content = content.replaceAll("<.*?>", "");
		// 外文资讯
		// 1. 获取抽取的实体信息
		Map<String, Set<String>> recognize = baiduNlpService.recognizeAndMaxword(content);
//		Map<String, String> entityResult = NLPUtils.getEntityResult(recognize);
		Map<String, String> entityResult = NLPUtils.getFormattedEntityResult(recognize, formattedResult);
		int limitLength = 3000;
		Map<String, Object> summaryMap;
		if(content.length() > limitLength) {
			summaryMap = baiduNlpService.autoSummary(content.substring(0,3000));
		} else {
			summaryMap = baiduNlpService.autoSummary(content);
		}
		String keywords = NLPUtils.getEntityKeywords(nlpService.keyword(title+"。"+content));


        dynamicData.put("nlp_person_tr", entityResult.get("person"));
        dynamicData.put("nlp_place_tr", entityResult.get("place"));
        dynamicData.put("nlp_organization_tr", entityResult.get("organ"));
        dynamicData.put("nlp_date_tr", entityResult.get("time"));
        dynamicData.put("nlp_date", entityResult.get("time"));

        HashMap<String, Object> sentiment = baiduNlpService.sentimentClassify((String)summaryMap.get("summary"));
        if(null != sentiment && sentiment.size() > 0) {
            // 情感分析结果
            if(sentiment.containsKey("items")) {
                com.alibaba.fastjson.JSONArray jsonArray = (com.alibaba.fastjson.JSONArray) sentiment.get("items");
                com.alibaba.fastjson.JSONObject jsonObject = jsonArray.getJSONObject(0);
                Integer sentimentAttitude = (Integer) jsonObject.get("sentiment");
                if(0 == sentimentAttitude) {
                    dynamicData.put("nlp_emotion_tr", "情感偏负向");
                    dynamicData.put("nlp_emotion", "情感偏负向");
                } else if(1 == sentimentAttitude) {
                    dynamicData.put("nlp_emotion_tr", "情感偏中性");
					dynamicData.put("nlp_emotion", "情感偏中性");
                } else if(2 == sentimentAttitude) {
                    dynamicData.put("nlp_emotion_tr", "情感偏正向");
					dynamicData.put("nlp_emotion", "情感偏正向");
                }
            }
        }
        dynamicData.put("nlp_summary_tr", (String)summaryMap.get("summary"));
        dynamicData.put("nlp_keywords_tr", keywords);
        dynamicData.put("nlp_person", entityResult.get("person"));
        dynamicData.put("nlp_place", entityResult.get("place"));
        dynamicData.put("nlp_organization", entityResult.get("organ"));
        dynamicData.put("nlp_summary", (String)summaryMap.get("summary"));
        dynamicData.put("nlp_keywords", keywords);

		return dynamicData;
	}

	/**
	 * 去除翻译正文部分的超链接.
	 *
	 * @param content 翻译的正文部分
	 * @return
	 */
	public static String dealContent(String content) {
		content = content.replaceAll("<a(.*?)>", "").replaceAll("<a>", "").replaceAll("</a>", "").replace("\\n", "");
		return content;
	}

}