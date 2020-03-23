package com.mss.crawler.spiderjson.pipeline;

import com.alibaba.fastjson.JSON;
import com.db.utils.DbBuilder;
import com.mss.crawler.spiderjson.ResourceFile;
import com.mss.crawler.spiderjson.util.BadFormatImage;
import com.mss.crawler.spiderjson.util.HtmlFormatter;
import com.mss.crawler.spiderjson.util.KeywordsUtils;
import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import io.renren.modules.spider.dao.NewsDao;
import io.renren.modules.spider.service.impl.BaiduNlpServiceImpl;
import org.elasticsearch.index.reindex.DeleteByQueryRequestBuilder;
import org.python.antlr.base.stmt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;
import us.codecraft.webmagic.utils.FilePersistentBase;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 图片文件下载
 *
 * @author wangdw
 */
public class NewsDBPipeline extends FilePersistentBase implements Pipeline {
	private Logger logger = LoggerFactory.getLogger(getClass());

	protected static String keywordsFilePath;
	protected  static Set<String> keywordsSet;
	protected static BaiduNlpServiceImpl baiduNlpService = new BaiduNlpServiceImpl();

	private static ResourceBundle resource = ResourceBundle.getBundle("casperjs");

	static{
		keywordsFilePath = resource.getString("keywords");
		keywordsSet = initKeywordsSet(keywordsFilePath);
	}

	private static Map<String, String> cnProperties = (Map) JSON.parse("{\"id\":\"id\",\"title\":\"title\",\"content\":\"content\",\"cover\":\"headimg\",\"datetime\":\"pubDate\",\"create_time\":\"crawlerdate\",\"ly\":\"src\",\"url\":\"url\",\"keywords\":\"keywords\",\"summary\":\"summary\",\"sslm\":\"sslm\",\"author\":\"author\"}");
	private static Map<String, String> enProperties = (Map) JSON.parse("{\"id\":\"id\",\"title\":\"title_tr\",\"content\":\"content_tr\",\"cover\":\"headimg\",\"datetime\":\"pubDate\",\"create_time\":\"crawlerdate\",\"ly\":\"src\",\"url\":\"url\",\"keywords\":\"keywords\",\"summary\":\"summary\",\"sslm\":\"sslm\",\"author\":\"author\"}");
	private static Map<String, String> cnOceanProperties = (Map) JSON.parse("{\"id\":\"id\",\"title\":\"title\",\"content\":\"content\",\"cover\":\"attchfiles\",\"datetime\":\"pubDate\",\"create_time\":\"crawlerdate\",\"source\":\"src\",\"url\":\"url\",\"author\":\"author\",\"nlp_keywords\":\"nlp_keywords\",\"nlp_summary\":\"nlp_summary\",\"nlp_person\":\"nlp_person\",\"nlp_place\":\"nlp_place\",\"nlp_date\":\"nlp_date\",\"nlp_organization\":\"nlp_organization\",\"nlp_emotion\":\"nlp_emotion\",\"classify_code\":\"classify_code\"}");
	private static Map<String, String> enOceanProperties = (Map) JSON.parse("{\"id\":\"id\",\"title\":\"title\",\"content\":\"content\",\"cover\":\"attchfiles\",\"datetime\":\"pubDate\",\"create_time\":\"crawlerdate\",\"source\":\"src\",\"url\":\"url\",\"author\":\"author\",\"nlp_keywords\":\"nlp_keywords\",\"nlp_summary\":\"nlp_summary\",\"nlp_person\":\"nlp_person\",\"nlp_place\":\"nlp_place\",\"nlp_date\":\"nlp_date\",\"nlp_organization\":\"nlp_organization\",\"nlp_emotion\":\"nlp_emotion\",\"title_tr\":\"title_tr\",\"source_tr\":\"src\",\"datetime_tr\":\"pubDate\",\"author_tr\":\"author\",\"cover_tr\":\"attchfiles\",\"nlp_keywords_tr\":\"nlp_keywords_tr\",\"nlp_summary_tr\":\"nlp_summary_tr\",\"content_tr\":\"content_tr\",\"nlp_person_tr\":\"nlp_person_tr\",\"nlp_place_tr\":\"nlp_place_tr\",\"nlp_date_tr\":\"nlp_date_tr\",\"nlp_emotion_tr\":\"nlp_emotion_tr\",\"nlp_organization_tr\":\"nlp_organization_tr\",\"classify_code\":\"classify_code\",\"classify_code_tr\":\"classify_code_tr\",\"country\":\"news_category\",\"url_tr\":\"url\"}");


	private static String nginxAddress = "http://47.105.60.166:10010/imgs";

	private static String fileUploadStorePath = "D:/AppData/data/store/upload";

	@Value("")

	// 图片的限制尺寸
	private static Integer limitWidth = 550;

	public NewsDBPipeline() {
	}

	public static Boolean checkObjIsPubdateRight(Map<String, Object> obj) {
		String pubDate = (String) obj.get("pubDate");
		if(!StringUtils.isEmpty(pubDate)) {
			Calendar c = Calendar.getInstance();
			c.add(Calendar.YEAR, -2);
			long relativeDate = c.getTime().getTime();
			try {
				long date = new SimpleDateFormat("yyyy-MM-dd").parse(pubDate).getTime();
				if(date > relativeDate) {
					return true;
				}
			} catch (ParseException e) {
				return false;
			}
		}
		return false;
	}

	@Override
	public void process(ResultItems resultItems, Task task) {

		List<String> objectNames = resultItems.get("objectNames");
		List<ResourceFile> rfs = (List<ResourceFile>) resultItems.get("resources");

		if (objectNames != null) {
			for (String objectName : objectNames) {
				Object object = resultItems.get(objectName);
				// 返回的对象是一个 List 的集合
				if (object instanceof ArrayList) {
					ArrayList<Map<String, Object>> objs = (ArrayList) object;
					for (Map obj : objs) {
						if (obj != null) {
							obj.put("url", resultItems.getRequest().getUrl());
							obj.put("crawlerdate", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
							obj.put("siteDomain", resultItems.get("domain"));
							// 加入額外的信息
							obj.put("news_category", resultItems.get("news_category"));
							obj.put("dbType", resultItems.get("dbType"));
							obj.put("sslm", resultItems.get("sslm"));

							// 加入图片的格式错误的集合
							obj.put("oldTypeImages", resultItems.get("oldTypeImages"));
							obj.put("realTypeImages", resultItems.get("realTypeImages"));
							obj.put("badFormatImages", resultItems.get("badFormatImages"));

							if (!StringUtils.isEmpty(resultItems.get("src"))) {
								obj.put("src", resultItems.get("src"));
							}
							if (!StringUtils.isEmpty(resultItems.get("copyright"))) {
								obj.put("copyright", resultItems.get("copyright"));
							}
							// 此处的文件的保存路径直接修改为对应的 域名为 前缀
							String fileStorePath = PATH_SEPERATOR + obj.get("siteDomain") + PATH_SEPERATOR;
							String attchfiles = getAttchfiles(rfs, fileStorePath);

							if (!StringUtils.isEmpty(attchfiles)) {
								obj.put("attchfiles", attchfiles);
							}

							if(!StringUtils.isEmpty(resultItems.get("pubdate"))) {
								obj.put("pubDate", resultItems.get("pubdate"));
							}

							if (!StringUtils.isEmpty(obj.get("attchfiles")) && null == obj.get("headimg")) {
								obj.put("headimg", getHeadimg(attchfiles));
							}

							if (!StringUtils.isEmpty(obj.get("title")) && !StringUtils.isEmpty(obj.get("content"))) {
								String keywords = KeywordsUtils.getKeywordsByTitleAndContent((String) obj.get("title"), (String) obj.get("content"));
								obj.put("keywords", keywords);
							}
							if (!StringUtils.isEmpty(obj.get("title"))) {
								writeToDB(objectName, obj);
							}

						}
					}
				} else {
					// 返回的是一个单一的对象
					Map<String, Object> obj = (Map<String, Object>) object;
					if (obj != null) {
						obj.put("url", resultItems.getRequest().getUrl());
						if(!StringUtils.isEmpty(resultItems.get("pdffiles"))) {
							obj.put("pdffiles", resultItems.get("pdffiles"));
						}
						obj.put("crawlerdate", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
						obj.put("siteDomain", resultItems.get("domain"));
						// 加入額外的信息
						obj.put("news_category", resultItems.get("news_category"));
						obj.put("dbType", resultItems.get("dbType"));
						obj.put("sslm", resultItems.get("sslm"));

						if(!StringUtils.isEmpty(resultItems.get("pubdate"))) {
							obj.put("pubDate", resultItems.get("pubdate"));
						}

						// 加入图片的格式错误的集合
						obj.put("oldTypeImages", resultItems.get("oldTypeImages"));
						obj.put("realTypeImages", resultItems.get("realTypeImages"));
						obj.put("badFormatImages", resultItems.get("badFormatImages"));

						/**
						 * 通过的domain的判断，确定dsti网站的处理方式
						 * by mcg
						 */
						if (obj.get("siteDomain").equals("www.dsti.net")) {
							String content = dealDsti((String) obj.get("content"));
							obj.put("content", content);
						}
						if (!StringUtils.isEmpty(resultItems.get("src"))) {
							obj.put("src", resultItems.get("src"));
						}
						if (!StringUtils.isEmpty(resultItems.get("searchText"))) {
							obj.put("searchText", resultItems.get("searchText"));
						}
						// 此处的文件的保存路径直接修改为对应的 域名为 前缀
						String fileStorePath = PATH_SEPERATOR + obj.get("siteDomain") + PATH_SEPERATOR;
						String attchfiles = getAttchfiles(rfs, fileStorePath);

						if (!StringUtils.isEmpty(attchfiles)) {
							obj.put("attchfiles", attchfiles);
						}

						// 对于没有封面的新闻截取首张附件的图片
						if (!StringUtils.isEmpty(obj.get("attchfiles")) && null == obj.get("headimg")) {
							obj.put("headimg", getHeadimg(attchfiles));
						}

						// 在对应的 WeixinModelProcessor 中进行添加一个标志前缀!!用于数据库的  pdffiles 路径字段生成
						if (!StringUtils.isEmpty(obj.get("pdffiles"))) {
							obj.put("pdffiles", (String) (obj.get("pdffiles")) + obj.get("id") + ".pdf");
						}

						if (!StringUtils.isEmpty(obj.get("title")) || !StringUtils.isEmpty(obj.get("content"))) {
							if(obj.get("title") == null) {
								obj.put("title", "");
							}
							String keywords = KeywordsUtils.getKeywordsByTitleAndContent((String) obj.get("title"), (String) obj.get("content"));
							obj.put("keywords", keywords);
						}
						if (!StringUtils.isEmpty(resultItems.get("copyright"))) {
							/*String copyright = resultItems.get("copyright");
							if (copyright.equals("11")) {
								obj.put("copyright", "原创");
							} else {
								obj.put("copyright", "");
							}*/
							obj.put("copyright", "原创");
						}

						// 对于个别的 没有 title 的新闻, 不执行插入数据库的操作
						if (obj.containsKey("publicName")) {
							// 微信模块, 对于部分没有正文的对象, 不执行插入数据库操作
							if (!StringUtils.isEmpty(obj.get("title")) && !StringUtils.isEmpty(obj.get("publicName")) && !StringUtils.isEmpty(obj.get("content"))) {
								String content = (String) obj.get("content");
//								obj.put("formattedContent", HtmlFormatter.formatContent(content));
								obj.put("formattedContent", "");

								// 对于长度较短的信息不予录入数据库
								if (((String) obj.get("content")).replaceAll("<.*?>", "").length() > 100) {
									writeToDB(objectName, obj);
								} else {
									logger.error("The content is too short to add to table...");
								}
							}
						} else if(obj.containsKey("sslm") && obj.get("content").toString().length() > 100) {
							writeToDB(objectName, obj);
						} else if (!StringUtils.isEmpty(obj.get("title")) && !obj.containsKey("thesaurusEn")) {
							String content = (String) obj.get("content");
							obj.put("formattedContent", HtmlFormatter.formatContent(content));
							if (((String) obj.get("formattedContent")).length() > 0) {
								writeToDB(objectName, obj);
							}
						} else if (obj.containsKey("thesaurusEn")) {
							writeToDB(objectName, obj);
						}
					}
				}
			}
		}
	}

	// 黑名单的验证
	public static Boolean checkBlackList(Map<String, Object> obj, List<String> list) {
		if (!StringUtils.isEmpty(obj.get("title")) && !StringUtils.isEmpty(obj.get("content"))) {
			if (list != null && list.size() > 0) {
				for (String word : list) {
					if (obj.get("title").toString().contains(word) || obj.get("content").toString().contains(word)) {
						return false;
					}
				}
			}
		}
		return true;
	}

	// 截取附件的第一张图片作为新闻的封面
	private String getHeadimg(String attchfiles) {
		if (attchfiles.contains(",")) {
			return attchfiles.substring(0, attchfiles.indexOf(","));
		}
		return attchfiles;
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
			}
    		
    		/*
            for ( ResourceFile entry : rfs) {
                String extName=entry.getFileExtName();
                String newFileName = entry.getNewFileName();
                StringBuffer imgFileNameNew = new StringBuffer(fileStorePath);
                imgFileNameNew.append(entry.getRelativePath());
                imgFileNameNew.append(PATH_SEPERATOR);
                imgFileNameNew.append(newFileName);	            
                attchfiles.append(imgFileNameNew).append(",");
            }
            */
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
//		DbBuilder.close(conn);
	}

	public static void addPersonNew2Local(Set<String> personSet) {
		if(null != personSet && personSet.size() > 0) {
			List<String> corpusDataCnList = new NewsDBPipeline().getCorpusCnList("corpus_data", 1 + "");
			List<String> corpusNewCnList = new NewsDBPipeline().getCorpusCnList("corpus_new", 1 + "");
			Set<String> oldOrgan = new HashSet<>();
			oldOrgan.addAll(corpusDataCnList);
			oldOrgan.addAll(corpusNewCnList);
			for(String corpus : personSet) {
				if(!oldOrgan.contains(corpus)) {
					// 新发现的机构
					Map<String, Object> corpusNew = new HashMap<>();
					corpusNew.put("create_time", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
					corpusNew.put("corpus_cn", corpus);
					corpusNew.put("occur_times", 1);
					corpusNew.put("status", 0);
					corpusNew.put("type", 1);
					corpusNew.put("id", UUID.randomUUID().toString().replace("-", "").substring(0,20));
					new NewsDBPipeline().addCorpusNew(corpusNew);
					System.out.println("发现新人名【 " + corpus + " 】!!!" );
				} else if(oldOrgan.contains(corpus) && corpusNewCnList.contains(corpus)) {
					// 已经发现的机构，对应的出现次数 + 1
					Integer occurTimes = new NewsDBPipeline().getCorpusNewOccurTimes(corpus);
					new NewsDBPipeline().updateCorpusNewOccurTimes(corpus, occurTimes + 1);
					System.out.println("人名【 " + corpus + " 】已经存在，出现的次数为"+ (occurTimes + 1) +"!!!" );
				}
			}
		}
	}


	public static void addPlaceNew2Local(Set<String> placeSet) {
		if(null != placeSet && placeSet.size() > 0) {
			List<String> corpusDataCnList = new NewsDBPipeline().getCorpusCnList("corpus_data", 3 + "");
			List<String> corpusNewCnList = new NewsDBPipeline().getCorpusCnList("corpus_new", 3 + "");
			Set<String> oldOrgan = new HashSet<>();
			oldOrgan.addAll(corpusDataCnList);
			oldOrgan.addAll(corpusNewCnList);
			for(String corpus : placeSet) {
				if(!oldOrgan.contains(corpus)) {
					// 新发现的机构
					Map<String, Object> corpusNew = new HashMap<>();
					corpusNew.put("create_time", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
					corpusNew.put("corpus_cn", corpus);
					corpusNew.put("occur_times", 1);
					corpusNew.put("status", 0);
					corpusNew.put("type", 3);
					corpusNew.put("id", UUID.randomUUID().toString().replace("-", "").substring(0,20));
					new NewsDBPipeline().addCorpusNew(corpusNew);
					System.out.println("发现新地名【 " + corpus + " 】!!!" );
				} else if(oldOrgan.contains(corpus) && corpusNewCnList.contains(corpus)) {
					// 已经发现的机构，对应的出现次数 + 1
					Integer occurTimes = new NewsDBPipeline().getCorpusNewOccurTimes(corpus);
					new NewsDBPipeline().updateCorpusNewOccurTimes(corpus, occurTimes + 1);
					System.out.println("地名【 " + corpus + " 】已经存在，出现的次数为"+ (occurTimes + 1) +"!!!" );
				}
			}
		}
	}

	public static void addOrganNew2Local(Set<String> organNew) {
		if(null != organNew && organNew.size() > 0) {
			List<String> corpusDataCnList = new NewsDBPipeline().getCorpusCnList("corpus_data", 2 + "");
			List<String> corpusNewCnList = new NewsDBPipeline().getCorpusCnList("corpus_new", 2 + "");
			Set<String> oldOrgan = new HashSet<>();
			oldOrgan.addAll(corpusDataCnList);
			oldOrgan.addAll(corpusNewCnList);
			for(String corpus : organNew) {
				if(!oldOrgan.contains(corpus)) {
					// 新发现的机构
					Map<String, Object> corpusNew = new HashMap<>();
					corpusNew.put("create_time", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
					corpusNew.put("corpus_cn", corpus);
					corpusNew.put("occur_times", 1);
					corpusNew.put("status", 0);
					corpusNew.put("type", 2);
					corpusNew.put("id", UUID.randomUUID().toString().replace("-", "").substring(0,20));
					new NewsDBPipeline().addCorpusNew(corpusNew);
					System.out.println("发现新机构【 " + corpus + " 】!!!" );
				} else if(oldOrgan.contains(corpus) && corpusNewCnList.contains(corpus)) {
					// 已经发现的机构，对应的出现次数 + 1
					Integer occurTimes = new NewsDBPipeline().getCorpusNewOccurTimes(corpus);
					new NewsDBPipeline().updateCorpusNewOccurTimes(corpus, occurTimes + 1);
					System.out.println("【 " + corpus + " 】机构已经存在，出现的次数为"+ (occurTimes + 1) +"!!!" );
				}
			}
		}
	}

	public static Map<String, Object> getFormattedResult() {
		Map<String, Object> result = new HashMap<>();
		Map<String, String> personMap = formatResult(1);
		Map<String, String> organMap = formatResult(2);
		Map<String, String> placeMap = formatResult(3);
		result.put("person", personMap);
		result.put("organ", organMap);
		result.put("place", placeMap);
		return result;
	}

	private static Map<String, String> formatResult(int type) {
		List<Map<String, String>> formarMapByType = getFormatMapByType(type);
		Map<String, String> result = new HashMap<>();
		if(null != formarMapByType && formarMapByType.size() > 0) {
			for(Map<String, String> m : formarMapByType) {
				result.put(m.get("corpusCn"), m.get("formatCn"));
			}
		}
		return result;

	}

	private static List<Map<String, String>> getFormatMapByType(int type) {
		List<Map<String, String>> result = new ArrayList<>();
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			//构建insert语句
			StringBuilder select = new StringBuilder("select d.corpus_cn as corpusCn, f.corpus_cn as formatCn from corpus_data d LEFT OUTER JOIN corpus_format f on f.id = d.format_id  where d.type = '").append(type).append("' and d.format_id != '0';");
			conn = DbBuilder.getConnection();
			conn.setAutoCommit(false);
			stmt = conn.prepareStatement(select.toString());
			rs = stmt.executeQuery();
			ResultSetMetaData rsmd = rs.getMetaData();// 取得数据库的列名
			int numberOfColumns = rsmd.getColumnCount();
			while (rs.next()) {
				Map<String, String> map = new HashMap<>();
				for (int i = 1; i < numberOfColumns + 1; i++) {
					String colName = i == 1?"corpusCn":"formatCn";
					String colValue = (String) rs.getObject(colName);
					map.put(colName, colValue);
					result.add(map);
				}
			}
			conn.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			try {
				if (stmt != null)
					stmt.close();
				if (conn != null)
					conn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	private void updateCorpusNewOccurTimes(String corpusCn, int occurTimes) {
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			//构建insert语句
			StringBuilder select = new StringBuilder("UPDATE corpus_new SET occur_times = ");
			select.append(occurTimes).append("  where corpus_cn = '").append(corpusCn).append("'");
			conn = DbBuilder.getConnection();
			conn.setAutoCommit(false);
			stmt = conn.prepareStatement(select.toString());
			stmt.executeUpdate();
			conn.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			try {
				if (stmt != null)
					stmt.close();
				if (conn != null)
					conn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private Integer getCorpusNewOccurTimes(String corpusCn) {
		List<String> result = new ArrayList<>();
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			//构建insert语句
			StringBuilder select = new StringBuilder("SELECT occur_times FROM corpus_new where corpus_cn = '").append(corpusCn).append("'");
			conn = DbBuilder.getConnection();
			conn.setAutoCommit(false);
			stmt = conn.prepareStatement(select.toString());
			rs = stmt.executeQuery();
			ResultSetMetaData rsmd = rs.getMetaData();// 取得数据库的列名
			int numberOfColumns = rsmd.getColumnCount();
			while (rs.next()) {
				for (int i = 1; i < numberOfColumns + 1; i++) {
					result.add(rs.getObject(i) + "");
				}
			}
			conn.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			try {
				if (stmt != null)
					stmt.close();
				if (conn != null)
					conn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if(result.size() == 1) {
			return Integer.parseInt(result.get(0));
		}
		return 1;
	}

	public List<String> getCorpusCnList(String tablename, String type) {
		List<String> result = new ArrayList<>();
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			//构建insert语句
			StringBuilder select = new StringBuilder("SELECT corpus_cn FROM ");
			select.append(tablename).append(" WHERE type=").append(type);

			conn = DbBuilder.getConnection();
			conn.setAutoCommit(false);
			stmt = conn.prepareStatement(select.toString());
			rs = stmt.executeQuery();
			ResultSetMetaData rsmd = rs.getMetaData();// 取得数据库的列名

			int numberOfColumns = rsmd.getColumnCount();
			while (rs.next()) {
				for (int i = 1; i < numberOfColumns + 1; i++) {
					result.add((String) rs.getObject(i));
				}
			}
			conn.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			try {
				if (stmt != null)
					stmt.close();
				if (conn != null)
					conn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	public void addCorpusNew(Map<String, Object> params) {
		try {
			//构建insert语句
			StringBuilder insert = new StringBuilder(" insert into corpus_new(");
			StringBuilder values = new StringBuilder(" VALUES(");
			List list = new ArrayList();
			//拼写语句和循环设置参数
			for (Entry<String, Object> entry : params.entrySet()) {
					insert.append(entry.getKey().toLowerCase()).append(",");
					values.append("?").append(",");
					list.add(entry.getValue());
			}
			StringBuilder sql = new StringBuilder();
			sql.append(insert.substring(0, insert.length() - 1)).append(")");
			sql.append(values.substring(0, values.length() - 1)).append(")");

			insertDB(sql.toString(), list.toArray());
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			try {
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
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
					list.add(dealNews(entry.getValue() + "", obj));
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
				// 中文资讯
				String newTable = "bs_news_cn";
				String title = (String) obj.get("title");
				String formatCon = NewsDBPipeline.dealNews((String) obj.get("content"), obj);
				String content = HtmlFormatter.formatContent(formatCon);
				obj.put("content", content);
				if(!StringUtils.isEmpty(content)) {
					if(!StringUtils.isEmpty(title) && !StringUtils.isEmpty(content)) {
						obj = NewsTranslate2DBPipeline.addExtraInfo2DynamicData(title, content, obj);
						// 判断采集的数据的关键词是否包含目标关键词
						Boolean insertFlag = checkObjIsContainsTargetKeywords(obj, keywordsFilePath);
						Boolean insertPubdateFlag = NewsDBPipeline.checkObjIsPubdateRight(obj);
						Boolean insertContentTitlePubdateFlag = NewsDBPipeline.insertContentTitlePubdateFlag(obj);

						if(insertFlag && insertPubdateFlag && insertContentTitlePubdateFlag) {
							NewsDBPipeline.addObj2OceanPublicTables(table, newTable, obj);
							Map<String, Set<String>> resultMap = baiduNlpService.recognize(content);
							// 新发现的机构的名称处理
							NewsDBPipeline.addOrganNew2Local(resultMap.get("organ"));
							// 新发现的地名处理
							NewsDBPipeline.addPlaceNew2Local(resultMap.get("place"));
							NewsDBPipeline.addPersonNew2Local(resultMap.get("person"));
						} else {
							System.out.println("采集的数据不在对应的采集范围。。。" );
						}
					}
				}

			} else {
				String newTable = "bs_xwzx";
				addObj2NewTables(table, newTable, obj);
			}

		}

	}

	protected static Boolean insertContentTitlePubdateFlag(Map<String, Object> obj) {
		if(obj.containsKey("title_tr")) {
			if(!StringUtils.isEmpty((String)obj.get("title_tr")) && !StringUtils.isEmpty((String)obj.get("pubDate")) && !StringUtils.isEmpty((String)obj.get("content_tr"))) {
				return true;
			}
		} else {
			if(!StringUtils.isEmpty((String)obj.get("title")) && !StringUtils.isEmpty((String)obj.get("pubDate")) && !StringUtils.isEmpty((String)obj.get("content"))) {
				return true;
			}
		}

		return false;
	}

	protected static Boolean checkObjIsContainsTargetKeywords(Map<String, Object> obj, String keywordsFilePath) {
		if(!StringUtils.isEmpty(obj.get("nlp_keywords"))) {
			List<String> keywordsList = Arrays.asList(obj.get("nlp_keywords").toString().split(";"));
			for(String keyword : keywordsList) {
				if(keywordsSet.contains(keyword)) {
					// 包含一个关键词即返回
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 添加数据到新的表格当中去
	 * @param newTable 新的表格的名称
	 * @param obj 表格的数据
	 */
	protected static void addObj2NewTables(String oldTable, String newTable, Map<String,Object> obj) {
		Map<String, String> fieldsProperties = new HashMap();
		if (oldTable.contains("cn")) {
			fieldsProperties = cnProperties;
		} else if (oldTable.contains("en")) {
			fieldsProperties = enProperties;
		} else {
			System.out.println("The given name " + oldTable + " is not allowed here...");
			return;
		}
		// 根据组装的 map 集合进行映射对应的数据关系
		Map<String, String> newTableMap = new HashMap<>();

		for(Entry<String, String> entry : fieldsProperties.entrySet()) {
			String value = (String) obj.get(entry.getValue());
			if(!StringUtils.isEmpty(value) && value.contains("${RootPath}")) {
				value = value.replace("${RootPath}", nginxAddress);
			}
			newTableMap.put(entry.getKey(), value);
		}

		// 添加额外的字段项
		newTableMap.put("create_user_id", "11");
		newTableMap.put("database_id", "3");
		newTableMap.put("version", "1.0");
		newTableMap.put("data_source", "1");
		newTableMap.put("status", "0");
		newTableMap.put("recommend", "0");
//		newTableMap.put("sslm", getRandomSslm(newTableMap.get("sslm")));

		// 将新的集合添加到对应的数据表中
		//构建insert语句
		StringBuilder insert = new StringBuilder(" insert into ");
		insert.append(newTable).append("(");
		StringBuilder values = new StringBuilder(" VALUES(");
		List list = new ArrayList();
		//拼写语句和循环设置参数
		for (Entry<String, String> entry : newTableMap.entrySet()) {
			insert.append(entry.getKey().toLowerCase()).append(",");
			values.append("?").append(",");
			list.add(dealNews(entry.getValue() + "", obj));
		}

		StringBuilder sql = new StringBuilder();
		sql.append(insert.substring(0, insert.length() - 1)).append(")");
		sql.append(values.substring(0, values.length() - 1)).append(")");
		new NewsDBPipeline().insertDB(sql.toString(), list.toArray());

		// 进行数据的发布...
		try {
			String resultstr = load("http://47.105.60.166:58001/search-api/submitDatabase", "databaseEn=xwzx&createTime=" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
			System.out.println(resultstr);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 添加数据到新的表格当中去
	 * @param newTable 新的表格的名称
	 * @param obj 表格的数据
	 */
	protected static void addObj2OceanPublicTables(String oldTable, String newTable, Map<String,Object> obj) {

		Map<String, String> fieldsProperties = new HashMap();
		if (oldTable.contains("cn")) {
			fieldsProperties = cnOceanProperties;
		} else if (oldTable.contains("en")) {
			fieldsProperties = enOceanProperties;
		} else {
			System.out.println("The given name " + oldTable + " is not allowed here...");
			return;
		}
		// 根据组装的 map 集合进行映射对应的数据关系
		Map<String, String> newTableMap = new HashMap<>();

		for(Entry<String, String> entry : fieldsProperties.entrySet()) {
			String value = (String) obj.get(entry.getValue());
			value = changeImageName2RealType(value, obj);
			if(!StringUtils.isEmpty(value) && value.contains("${RootPath}/")) {
				value = value.replace("${RootPath}/", "upload/");
			}
			newTableMap.put(entry.getKey(), value);
			if(oldTable.contains("en")) {
				newTableMap.put("database_id", "19");
			} else if(oldTable.contains("cn")) {
				newTableMap.put("database_id", "18");
				newTableMap.put("country", "中国");
			}
		}

		if(null != newTableMap.get("cover")) {
			String cover = newTableMap.get("cover");
			if("" != cover && cover.contains(",")) {
				cover = cover.replace(",", ";");
				newTableMap.put("cover", cover);
				if(oldTable.contains("en")) {
					newTableMap.put("cover_tr", cover);
				}
			}
		}

		// 临时处理
		String[] classifyArr = new String[]{"A01","A02","A03","A04","A05","A06"};
		int index=(int)(Math.random()*classifyArr.length);
		String rand = classifyArr[index];

		newTableMap.put("classify_code", rand);
		if(oldTable.contains("en")) {
			newTableMap.put("classify_code_tr", rand);
		}

		// 添加额外的字段项
		newTableMap.put("create_user_id", "11");
		newTableMap.put("version", "1.0");
		newTableMap.put("data_source", "1");
		newTableMap.put("status", "0");

		// 将新的集合添加到对应的数据表中
		//构建insert语句
		StringBuilder insert = new StringBuilder(" insert into ");
		insert.append(newTable).append("(");
		StringBuilder values = new StringBuilder(" VALUES(");
		List list = new ArrayList();
		//拼写语句和循环设置参数
		for (Entry<String, String> entry : newTableMap.entrySet()) {
			insert.append(entry.getKey().toLowerCase()).append(",");
			values.append("?").append(",");
			list.add(dealNews(entry.getValue() + "", obj));
//			list.add(entry.getValue());
		}

		StringBuilder sql = new StringBuilder();
		sql.append(insert.substring(0, insert.length() - 1)).append(")");
		sql.append(values.substring(0, values.length() - 1)).append(")");
		new NewsDBPipeline().insertDB(sql.toString(), list.toArray());

	}

	public static String load(String url,String query) throws Exception
	{
		URL restURL = new URL(url);
		/*
		 * 此处的urlConnection对象实际上是根据URL的请求协议(此处是http)生成的URLConnection类 的子类HttpURLConnection
		 */
		HttpURLConnection conn = (HttpURLConnection) restURL.openConnection();
		//请求方式
		conn.setRequestMethod("POST");
		//设置是否从httpUrlConnection读入，默认情况下是true; httpUrlConnection.setDoInput(true);
		conn.setDoOutput(true);
		//allowUserInteraction 如果为 true，则在允许用户交互（例如弹出一个验证对话框）的上下文中对此 URL 进行检查。
		conn.setAllowUserInteraction(false);

		PrintStream ps = new PrintStream(conn.getOutputStream());
		ps.print(query);

		ps.close();

		BufferedReader bReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

		String line,resultStr="";

		while(null != (line=bReader.readLine()))
		{
			resultStr +=line;
		}
		System.out.println(resultStr);
		bReader.close();

		return resultStr;

	}

	/**
	 * 对于准备插入数据库的数据进行最后的清理工作...
	 *
	 * @param text
	 * @param obj
	 * @return
	 */
	public static String dealNews(String text, Map<String, Object> obj) {
		String newText = text;
		if(StringUtils.isEmpty(text) || text.equalsIgnoreCase("null")) {
			return "";
		}

		// // 极个别的新闻获取不到对应的 发布时间, 仅仅在对应的请求地址上面包含对应的发布时间的信息
		// if(!StringUtils.isEmpty(obj.get("url")) && StringUtils.isEmpty(obj.get("pubDate"))) {
		// 	try {
		// 		obj.put("pubDate", HtmlFormatter.convertPubDate((String) obj.get("url")));
		// 	} catch (ParseException e) {
		// 		e.printStackTrace();
		// 	}
		// }

		// 去除前后空格
		newText = newText.trim();
		newText = StringUtils.replace(newText, "\n", "");


		// 将 section 标签替换为 p 标签
		if(newText.contains("<section")) {
			newText = newText.replaceAll("<section[^>]*?>", "<p>").replace("</section>","</p>");
		}
		Matcher spaceM = Pattern.compile("<p>[([\\s]*?)<p>([\\s]*)]{0,}<p>").matcher(newText);
		Matcher spaceM2 = Pattern.compile("</p>[([\\s]*?)</p>([\\s]*)]{0,}</p>").matcher(newText);
		while(spaceM.find()) {
			newText = newText.replaceAll(spaceM.group(), "<p>");

		}
		while(spaceM2.find()) {
			newText = newText.replaceAll(spaceM2.group(), "</p>");
		}


		newText = StringUtils.replace(newText, "images/", "/images/");
		//newText = StringUtils.replace(newText, "${RootPath}", "");
		newText = StringUtils.replace(newText, "data-src=", "src=");
		newText = StringUtils.replace(newText, "cover//", "cover/");
		newText = StringUtils.replace(newText, "image//", "image/");
		// 有待改进????
		newText = StringUtils.replace(newText, "pdf//", "pdf/");
		// 去除中文网站 p 标签后面的空格(中文空格), 其中的 \u3000 代表的是 中文空格
		newText = newText.replaceAll("<p[\\s]*>(\\s)*(\t)*(\u3000)*", "<p>");
		newText = newText.replaceAll("(\u3000)*", "");
//		newText = newText.replace("&nbsp;", "");
		newText = newText.replaceAll("<p[\\s]*>(\\t)*( )*", "<p>");
		// 去除标题中的 【】, 匹配成对出现的标签, 需要注意, 防止嵌套
		newText = newText.replaceAll("【.[^】]*】", "");

		// 正文中的 h4 标签的替换
		Matcher highlightMatcher = Pattern.compile("(<h(\\d)+)(.*?)").matcher(newText);
		if(newText.contains("<h")) {
			while(highlightMatcher.find()) {
				newText = newText.replace(highlightMatcher.group(1), "<p");
			}
		}

		// 对于非微信的数据，将所有的空 div 标签替换为 p 标签
		if(!newText.contains("js_content")) {
			newText = newText.replace("<div>", "<p>").replace("</div>", "</p>");
		}
		/*if(newText.contains("</h4>")) {
			newText = newText.replaceAll("<h4(.*?)>","<p>").replaceAll("</h4>","</p>");
		}*/

		// 去除字体的样式
		newText = newText.replaceAll("(<font\\s[^>]+>)", "");
		newText = newText.replaceAll("</font>", "");

		newText = newText.replaceAll("<strong[^>]*>", "");
		newText = newText.replaceAll("</strong>", "");
		//newText = newText.replaceAll("srcset=\"[^>]*\"", "");
		newText = newText.replaceAll("<blockquote(.*?)>", "<p>");
		newText = newText.replaceAll("</blockquote>", "</p>");
		newText = newText.replaceAll("</noscript>", "");
		newText = newText.replaceAll("<noscript>", "");

		newText = newText.replace("<br >", "</p><p>").replace("<br>", "</p><p>").replaceAll("<p>\\s*</p>", "").replace("h1>", "p>");

		// 正文部分的超链接, 除了 pdf 的超链接之外, 去除其余的所有的超链接
		if (newText.contains("</a>")) {
			Matcher matcher = Pattern.compile("(<a\\s[^>]*>)([^</a>]*)(</a>)").matcher(newText);
			String pdfReplace = "";
			String pdfStr = "";
			while (matcher.find()) {
				String textTag = matcher.group(2);
				if (textTag.contains("pdf") || textTag.contains("PDF")) {
					pdfStr = matcher.group();
					pdfReplace = "&P&D&F&";
					newText = newText.replace(pdfStr, pdfReplace);
				}
			}
			newText = newText.replaceAll("(<a\\s[^>]+>)", "");
			newText = newText.replaceAll("<a>", "");
			newText = newText.replaceAll("</a>", "");

			if (pdfReplace != "" && pdfStr != "") {
				newText = newText.replace(pdfReplace, pdfStr);
			}
		}

		// 空段落中间的 br
		if (newText.contains("<br")) {

			Matcher matcher = Pattern.compile("<p(.*?)>(.*?)</p>").matcher(newText);
			while (matcher.find()) {
				String group = matcher.group();
				if (group.matches("<p(.*?)>(.*?)<br(.*?)>(.*?)</p>")) {
					String re = group.replaceAll("<br([^>]*)>", "");
					newText = newText.replace(group, re);
				}
				String t = matcher.group(2);
				if (t.matches("<img[^>]*>(\\S)+")) {
					String words = t.subSequence(t.indexOf(">") + 1, t.length()).toString();
					if (words != null && !words.contains("<br")) {
						newText = newText.replace(words, "<br>" + words);
					}
				}
				t = t.replaceAll("<(.*?)>", "").replace("&nbsp;", "").replaceAll(" ", "").trim();
				if (t.length() == 0) {
					String g = matcher.group();
					String temp = g.replaceAll("(?!<img.+?>)<.+?>", "").replace("&nbsp;", "").replace(" ", "").trim();
					if (temp.length() == 0) {
						newText = newText.replace(g, "").trim();
					}
				}
			}

			matcher = Pattern.compile("<h3[\\s]*>(.*?)</h3>").matcher(newText);
			while (matcher.find()) {
				String t = matcher.group();
				t = t.replaceAll("<(.*?)>", "").replace("&nbsp;", "").replaceAll(" ", "").trim();
				if (t.length() == 0) {
					String g = matcher.group();
					String temp = g.replaceAll("(?!<img.+?>)<.+?>", "").replace("&nbsp;", "").replace(" ", "").trim();
					if (temp.length() == 0) {
						newText = newText.replace(g, "").trim();
					}
				}
			}

		}

		// 图片的图例居中问题
		if (newText.contains("</div>") && newText.contains("<img") && newText.contains("</span>")) {
			Matcher matcher = Pattern.compile("<div[^>]*>[^>]*<img[^>]*>[^>]*(<span[^>])*>([^>]*)</span>").matcher(newText);

			while (matcher.find()) {
				// 部分的图例的位置不对...
				String g = matcher.group();
				String spanText = matcher.group(2);
				if (!StringUtils.isEmpty(spanText)) {
					String r = g.replaceAll("<span(.*?)>", "<br/><span>");
					newText = newText.replace(g, r);
				}
			}
		}

		// 图片的图例居中的问题
		/*if (newText.contains("</p>") && newText.contains("<img") && newText.contains("</span>")) {
			Matcher matcher = Pattern.compile("<p([^>]*)<img([^>]*)(<span([^>]*)>([^>]*)</span>)([^>]*)(?=</p>)").matcher(newText);
			while (matcher.find()) {
				String g = matcher.group(3);
				String spanText = matcher.group(4);
				if (!StringUtils.isEmpty(spanText)) {
					String r = g.replaceAll("<span(.*?)>", "<br/><span>");
					newText = newText.replace(g, r);
				}
			}
		}*/

		// 某一个段落中包含图片与文字, 需要将文字进行换段...
		if (newText.contains("</p>") && newText.contains("<img")) {
			Matcher matcher = Pattern.compile("<p.*?</p>").matcher(newText);
			while (matcher.find()) {
				String g = matcher.group();
				if (g.contains("<img") && g.replaceAll("<.*?>", "").trim().length() > 0) {
//	            	String r = "";
//	            	if(!StringUtils.isEmpty(g) && !g.contains("<br")) {
//	            		r = "<br>" + g;
//	            		newText = newText.replace(g, r);
//	            	}
					Matcher mat = Pattern.compile("<p([^>]*)>([^>]*)<img([^>]*)>(.*?)").matcher(g);//
					if (mat.find()) {
						if (mat.group(2).trim().length() == 0) {
							String pref = g.substring(0, g.replaceFirst(">", "").indexOf(">") + 2);
							String replace = pref + "</p><p>" + g.substring(pref.length(), g.length());
							newText = newText.replace(g, replace);
						} else if (mat.group(2).trim().length() > 0) {
							String pref = g.substring(0, g.indexOf("<img"));
							String replace = pref + "</p><p>" + g.substring(pref.length(), g.length());
							newText = newText.replace(g, replace);
						}

					}

				}
			}
		}

		// 可以切换的图片进保留第一张
		if (newText.contains("</div>") && newText.contains("<img")) {
			Matcher matcher = Pattern.compile("<div>([^<div>]*)<div>(([^<div>]*))<img(.*?)</div>(([^</div>]*))</div>").matcher(newText);
			String temp = "";
			if (matcher.find()) {
				temp = matcher.group();
				newText = newText.replace(matcher.group(), "${saveDiv}");
			}
			while (matcher.find()) {
				// 清空所有的图片
				newText = newText.replace(matcher.group(), "");
			}
			newText = newText.replace("${saveDiv}", temp);
		}

		newText = newText.replaceAll("<br[ ]*[/]*>([\\s]*)<br[ ]*[/]*>", "<br>");
		newText = newText.replaceAll("</p>[^>]*<br[ ]*[/]*>[^>]*<p", "</p><p");

		// 去除空白的段落...
		// newText = newText.replaceAll("<p[\\s]*>[\\s]*</p>", "");

		// 解决微信中的部分的空标签(即除了图片之外的闭合标签没有标签体...)
		Matcher matcher = Pattern.compile("<section([^>]*)>(.*?)</section>").matcher(newText);
		while (matcher.find()) {
			String t = matcher.group(2);
			t = t.replaceAll("<(.*?)>", "").replace("&nbsp;", "").replaceAll(" ", "").trim();
			if (t.length() == 0) {
				String g = matcher.group();
				String temp = g.replaceAll("(?!<img.+?>)<.+?>", "").replace("&nbsp;", "").replace(" ", "").trim();
				if (temp.length() == 0) {
					newText = newText.replace(g, "").trim();
				}
			}
		}

		// 去除 emoji 表情
		newText = newText.replaceAll("[\ud800\udc00-\udbff\udfff\ud800-\udfff]", "");

		// 部分的新闻的图片的路径不正确
		if(newText.contains("imgsrc")) {
			newText = newText.replace("imgsrc", "img src");
		}

		// 部分的图片路径存在 src="thumbnail" (即缩略图的样式, 替换该值为空, 进行其余的替换)
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

		if(newText.contains("5e01e1b8c2dfa445892f98f154a67762")) {
			Matcher thumbnailMatcher = Pattern.compile("<img(.[^>]*?)((src|SRC)=(\"|\')(.[^>]*?)(\"|\')[^>])*>").matcher(newText);
			while(thumbnailMatcher.find()) {
				String extraGroup = thumbnailMatcher.group();
				Matcher tempMatcher = Pattern.compile("<img(.*?)(src=\"(.*?)5e01e1b8c2dfa445892f98f154a67762(.*?)\\.(.*?)\")(.*?)>").matcher(extraGroup);
				if(tempMatcher.find()) {
					System.out.println(tempMatcher.group(2));
					newText = newText.replace(extraGroup, extraGroup.replace(tempMatcher.group(2), "")).replace("data-src=", "src=");
				}
			}
		}

		// 格式化所有的图片标签
		Matcher m = Pattern.compile("<img(.[^>]*?)((src|SRC)=(\"|\')(.[^>]*?)(\"|\')[^>])*>").matcher(newText);
		while (m.find()) {
			String group = m.group();
			String oldGroup = group;
			// 部分的 img 标签内部包含多个src属性, 需要进行替换
			Matcher doubleMatcher =  Pattern.compile("(.*?)(src=(.*?))src=\"((.*?)RootPath(.*?))\"(.*?)").matcher(group);
			if(doubleMatcher.find()) {
				group = group.replace(doubleMatcher.group(2), "");
			}

			// 抽取对应的 src 标签体即可
			String g = "";
			if (group.contains("src=")) {
				/*String t = group.substring(group.indexOf(" src=") + 5, group.indexOf(" src=") + 6);
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
					g = g.replace("?", "");
				}*/
				Matcher imgMatcher = Pattern.compile("(.*?)(src=\"\\$\\{RootPath\\}/(.*?)\")(.*?)").matcher(group);
				if(imgMatcher.find()) {
					g = imgMatcher.group(2);
				}
			}
			if(!StringUtils.isEmpty(g)) {
				newText = newText.replace(oldGroup, "<img " + g + ">");
			}

		}


		// 修改记录对应的图片名称到真实的名称
		newText = changeImageName2RealType(newText, obj);
		// 格式化图片, 调整图片的大小
		newText = formatImage(newText,obj);

		m = Pattern.compile("<(\\w+)[^>]*style=\"[^>]*text-align: center;[^>]*\"[^>]*>").matcher(newText);
		while (m.find()) {
			String group = m.group(1);
			//System.out.println(group + m.group());
			// 对于 section 标签以及 strong 标签, 去掉对应的居中样式
			if (group.equals("section") || group.equals("strong")) {
				newText = newText.replace(m.group(), "<" + group + ">");
			}
		}

		// 正文的段落换行问题
		/*if(newText.contains("</div>") || newText.contains("</section>")) {
			Matcher temp = Pattern.compile("</([^>]+)>").matcher(newText);
			if(temp.find()) {
				String divStr = temp.group();
				String tag = temp.group(1);
				if(!StringUtils.isEmpty(tag) && !tag.equals("p") && !tag.equals("div") && !tag.equals("a")) {
					newText = newText.replace(divStr, divStr.replace(tag, "p").replace(divStr.substring(2, tag.length() + 3), divStr.replace("<" + tag, "<p")));
				}
			}
		}*/

		// 去除空的 img 标签
		newText = newText.replaceAll("<img(\\s)*>", "");

		if(newText.contains("<p>  <p>Back to list</p> </p>")) {
			newText = newText.replace("<p>  <p>Back to list</p> </p>", "");
		}
		if(newText.contains("<p>   <p>返回目录  </p>  </p>")) {
			newText = newText.replace("<p>   <p>返回目录  </p>  </p>", "");
		}
		if(newText.contains("<dfn></dfn>")) {
			newText = newText.replace("<dfn></dfn>", "");
		}

		return newText;
	}

	public static String changeImageName2RealType(String newText, Map<String, Object> obj) {
		// 对于不包含需要转换的集合或不包含图片标签的集合直接跳过
		if(null == obj.get("realTypeImages") || StringUtils.isEmpty(newText)) {
			return newText;
		}
		if(newText.contains("${RootPath}") || newText.contains(nginxAddress)) {
			// 真实的图片的名称
			List<String> realTypeImages = (List<String>) obj.get("realTypeImages");
			List<String> oldTypeImages = (List<String>) obj.get("oldTypeImages");
			if (null != realTypeImages && realTypeImages.size() > 0) {
				for (int i = 0; i < realTypeImages.size(); i++) {
					if (!StringUtils.isEmpty(newText) && newText.contains(oldTypeImages.get(i))) {
						newText = newText.replace(oldTypeImages.get(i), realTypeImages.get(i));
					}
				}
			}
		}

		return newText;
	}

	public static String formatImage(String newText, Map<String, Object> obj) {
		List<BadFormatImage> badFormatImages = (List<BadFormatImage>) obj.get("badFormatImages");
		//List<BadFormatImage> badFormatImages = HttpFileUtil.getBadFormatImages();

		if(null == badFormatImages) {
			return newText;
		}

		if (newText.contains("<img")) {
			// 格式化所有的图片标签
			Matcher m = Pattern.compile("<img(.[^>]*?)((src|SRC)=(\"|\')(.[^>]*?)(\"|\')[^>])*>").matcher(newText);

			if (badFormatImages.size() > 0) {
				for (BadFormatImage image : badFormatImages) {
					m = Pattern.compile("<img src=\"([^>]*" + image.getId() + "[^>]*)\">").matcher(newText);
					while (m.find()) {
						String g = m.group();
						int width = limitWidth;
						int height = limitWidth * image.getHeight() / image.getWidth();
//						System.out.println("新图片的高度为 --------------------------> " + height);
						String r = g.substring(0, g.length() - 1) + " width=\"" + width + "\" height=\"" + height + "\">";
						newText = newText.replace(g, r);
					}
				}
				// 清空超尺寸图片的集合
				// HttpFileUtil.setBadFormatImages(new ArrayList<>());
			}
		}

		return newText;
	}

	/**
	 * 处理国防科技信息网正文
	 * by mcg
	 *
	 * @param
	 * @return 处理好的文本
	 */
	//处理国防科技信息网正文
	public String dealDsti(String content) {
		String newText = content;
		newText = newText.replaceAll("<div.*?>", "<p>")
				.replace("</div>", "</p>")
				.replace("<br>", "</p><p>")
				.replace("<p></p>", "")
				.replace(" ", "")
				.replace("&nbsp;", "")
				.replace("<br>", "")
				.replace("<p></p>", "")
				.replace("�鳎�", ")");
		return newText;
	}

	static Set<String> initKeywordsSet(String keywordsFilePath){
		Set<String> dict=new HashSet<String>();
		try{
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(keywordsFilePath),"UTF-8"));
			String s;
			//一行一行地读取文本内容
			while((s=br.readLine())!=null){
				//只读取词
				dict.add(s.split(",")[0]);
			}
			br.close();
		}catch (IOException ex) {
			System.out.println(ex.getMessage());
		}
		return dict;
	}

}