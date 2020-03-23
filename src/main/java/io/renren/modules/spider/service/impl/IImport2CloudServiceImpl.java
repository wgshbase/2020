package io.renren.modules.spider.service.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import com.alibaba.fastjson.JSON;
import com.mss.crawler.spiderjson.util.EditDistance;
import io.renren.modules.spider.utils.XMLUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import io.renren.modules.oss.cloud.CloudStorageConfig;
import io.renren.modules.spider.dao.NewsDao;
import io.renren.modules.spider.entity.News;
import io.renren.modules.spider.service.IImport2CloudService;
import io.renren.modules.spider.utils.CSVUtils;
import io.renren.modules.spider.utils.ZipCompressor;

@Service(value="import2CloudService2")
public class IImport2CloudServiceImpl implements IImport2CloudService {

	private static Map<String, String> cnProperties = (Map) JSON.parse("{\"id\":\"id\",\"title\":\"title\",\"text\":\"content\",\"cover\":\"headimg\",\"date\":\"pubdate\",\"get_date\":\"crawlerdate\",\"sourceinfo\":\"src\",\"source_url\":\"url\",\"tags\":\"keywords\",\"keywords\":\"searchText\",\"source\":\"src\"}");
	private static Map<String, String> enProperties = (Map) JSON.parse("{\"id\":\"id\",\"title\":\"title_tr\",\"text\":\"content_tr\",\"cover\":\"headimg\",\"date\":\"pubdate\",\"get_date\":\"crawlerdate\",\"source\":\"src\",\"source_url\":\"url\",\"sourceinfo\":\"src\",\"tags\":\"keywords\",\"pre_title\":\"pre_title\"}");
	private static Map<String, String> wxProperties = (Map) JSON.parse("{\"id\":\"id\",\"title\":\"title\",\"xin_wen_zheng_wen\":\"content\",\"date\":\"pubdate\",\"sourceinfo\":\"publicname\",\"source_url\":\"url\",\"cai_ji_ri_qi\":\"crawlerdate\",\"pdf\":\"pdffiles\",\"cover\":\"headimg\",\"tags\":\"keywords\"}");

	@Autowired
	private NewsDao newsDao;

	private Logger LOG = LogManager.getLogger(IImport2CloudServiceImpl.class);

	/**
	 *  导入数据到云存储
	 *  1)导出数据库数据——例子：
	 *  2)导出图片——文件包
	 *  3)按照日期，生成文件包      20180404.zip
	 *  目录结构
	 *  20180404.zip
	 *     news_cn_20180404.csv
	 *     news_en_20180404.csv
	 *     news_wx_20180404.csv
	 *     domain例：www.baidu.com
	 *        images/**
	 *        pdf/**
	 *  4)上传到云上
	 * @param begindate 采集的开始日期, 如果日期为null，导出全部数据， 如果不为空，导出和参数日期相同的采集日期的数据
	 * @param enddate
	 * @param tables 包括：news_cn,news_en,news_wx
	 * @param removeList
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	@Transactional(rollbackFor=Exception.class, isolation=Isolation.READ_COMMITTED, propagation=Propagation.REQUIRED)
	public void importByDate(String begindate, String enddate, String[] tables, List<String> removeList) throws FileNotFoundException {
		String timePath = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
		String targetPath = "D:/temp/" + timePath + "/";
		String zipPath = "D:/" + begindate + ".zip";
		Map<String, Set<String>> extras = new HashMap<>();
		// 不同的表进行分别的处理
		for(int i = 0; i < tables.length; i++) {
			Map<String, Object> params = new HashMap<>();
			// 拼接 SQL
			params.put("begindate", begindate);

			if(StringUtils.isEmpty(enddate) || !CSVUtils.compareDate(begindate, enddate)) {
				enddate = CSVUtils.getNextDay(new Date());
			}

			params.put("enddate", enddate);
			params.put("tbname", tables[i]);
			try {
				params.put("pubdate", CSVUtils.getNumberDaysBefore(new SimpleDateFormat("yyyy-MM-dd").parse(begindate), 10));
			} catch (ParseException e1) {
				params.put("pubdate", "1970-01-01");
			}

			List<LinkedHashMap<String, Object>> dataList = newsDao.getAllBy(params);
			if(dataList.size() == 0) {
				// 对于没有数据的表, 不生成对应的 csv 文件
				continue;
			}
			// 遍历集合, 获取所有的字段
			List<String> heads = new ArrayList<>();
			for(Map<String, Object> map : dataList) {
				for(Map.Entry<String, Object> entry : map.entrySet()) {
					if(!heads.contains(entry.getKey())) {
						heads.add("\"" + entry.getKey() + "\"");
					}
				}
				break;
			}

			// 1. 将数据库的记录转化为 tbname_日期.cvs 文件
			extras = CSVUtils.createCSVFile("", heads, dataList, targetPath, tables[i] + "_" + begindate  + ".csv", removeList);
			//Set<String> set = extras.get("domains");
			if(extras != null && !extras.isEmpty()) {
				// 2. 导出图片到 /Domain/image/,
				if(extras.containsKey("attchfiles")) {
					Set<String> attachfiles = extras.get("attchfiles");
					if(attachfiles.size() > 0) {
						for(String attachfile : attachfiles) {
							String fromDirs = "D:/data/webmagic" + attachfile.replace("${RootPath}", "");
							String toDirs = targetPath.subSequence(0, targetPath.length() - 1) + attachfile.replace("${RootPath}", "");
							try {
								CSVUtils.copySingleFile(fromDirs, toDirs);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}

				}
				// 3. 导出 pdf 到   /Domain/XXX.pdf
				if(extras.containsKey("pdffiles")) {
					Set<String> pdffiles = extras.get("pdffiles");
					if(pdffiles.size() > 0) {
						for(String pdffile : pdffiles) {
							String fromDirs = "D:/data/webmagic" + pdffile;
							String toDirs = targetPath.subSequence(0, targetPath.length() - 1) + pdffile;
							try {
								CSVUtils.copySingleFile(fromDirs, toDirs);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}

				}
			}
			// 4. 生成文件包  yyyy-MM-dd.zip
			ZipCompressor compressor = new ZipCompressor(zipPath);
			compressor.compress(targetPath);
		}

		// 5. 将最后的总内容 打包为 .zip 并上传到 云
		/*QiniuCloudStorageService storageService = new QiniuCloudStorageService(getCloudStorageConfig());
		InputStream in = new FileInputStream(new File(zipPath));
		storageService.upload(in);*/

	}

	/**
	 * 对于请求参数包含唯一标识的请求, 做额外的操作
	 *
	 * 方法的过程: 生成表头先(即新闻的每个字段), 创建 csv 文件, 并将数据库的记录按照一条一条的插入进 csv 文件, 并且按照对应的 表头 的顺序进行插入
	 *
	 * @param identifyCode
	 * @param begindate
	 * @param enddate
	 * @param tables
	 * @param removeList
	 * @throws FileNotFoundException
	 */
	@Override
	public void importByDateWithExtra(String identifyCode, String begindate, String enddate, String[] tables, List<String> removeList) throws FileNotFoundException {
		if(StringUtils.isEmpty(identifyCode)) {
			importByDate(begindate, enddate, tables, removeList);
			return;
		}
		String timepath = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
		String targetPath = "D:/temp/" + timepath;
		String targetFilePath = targetPath + "/files/";
		String zipPath = "D:/" + begindate + "_" + enddate + ".zip";
		Map<String, Set<String>> extras = new HashMap<>();
		// 不同的表进行分别的处理
		for(int i = 0; i < tables.length; i++) {
			Map<String, Object> params = new HashMap<>();
			// 拼接 SQL
			params.put("begindate", begindate);

			if(StringUtils.isEmpty(enddate) || !CSVUtils.compareDate(begindate, enddate)) {
				enddate = CSVUtils.getNextDay(new Date());
			}

			params.put("enddate", enddate);
			params.put("tbname", tables[i]);
			// 数据库中最新的采集的消息全部都要
			try {
				// 对于出版的日期设置为当前日期的之前的 5 天
				params.put("pubdate", CSVUtils.getNumberDaysBefore(new SimpleDateFormat("yyyy-MM-dd").parse(begindate), 600));
			} catch (ParseException e1) {
				params.put("pubdate", "1970-01-01");
			}

			List<LinkedHashMap<String, Object>> dataList = newsDao.getAllBy(params);
			if(dataList.size() == 0) {
				// 对于没有数据的表, 不生成对应的 csv 文件
				continue;
			}

			String day = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
			String ym = File.separator + day.replace("-", "").substring(0, 6);
			String d = File.separator + day.replace("-", "").substring(6, 8);

			// 1. 将数据库的记录转化为 tbname_日期.cvs 文件
			Map<String, String> fieldsProperties = new HashMap();
			String tablename = "";
			if(tables[i].contains("cn")) {
				fieldsProperties = cnProperties;
				tablename = "news";
			} else if(tables[i].contains("en")) {
				fieldsProperties = enProperties;
				tablename = "news";
			} else if(tables[i].contains("wx")) {
				fieldsProperties = wxProperties;
				tablename = "wei_xin_gong_zhong_hao";
			}
			extras = XMLUtils.createXMLFile(identifyCode, dataList, targetPath, tablename + "-" + begindate  + System.currentTimeMillis() + ".xml", removeList, fieldsProperties);
			//2. 导出数据对应的附件内容
			if(extras != null && !extras.isEmpty()) {
				// 2. 导出图片到 /Domain/image/,
				if(extras.containsKey("attchfiles")) {
					Set<String> attachfiles = extras.get("attchfiles");
					for(String attachfile : attachfiles) {
						String fromDirs = "D:/data/webmagic" + attachfile.replace("${RootPath}", "");
						String toDirs = targetFilePath.subSequence(0, targetFilePath.length() - 1) + attachfile.replace("${RootPath}", "");
						try {
							if(identifyCode.equalsIgnoreCase("jh")) {
								// 若表名之中不包含 wx(即不是微信的表, 不做处理)
								if(!tables[i].contains("wx")) {
									CSVUtils.copySingleFile(fromDirs, toDirs);
								}
								else {
									try {
										if(!StringUtils.isEmpty(attachfile)) {
											CSVUtils.copySingleFile(fromDirs, toDirs);
										}
									} catch (Exception e) {
										System.out.println("-------------------------" + attachfile);
										e.printStackTrace();
									}
								}
							} else {
								CSVUtils.copySingleFile(fromDirs, toDirs);
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
				// 3. 导出 pdf 到   /Domain/XXX.pdf
				if(extras.containsKey("pdffiles")) {
					Set<String> pdffiles = extras.get("pdffiles");
					for(String pdffile : pdffiles) {
						// JH 的项目
						if(identifyCode.equalsIgnoreCase("jh")) {
							if(!StringUtils.isEmpty(pdffile) && pdffile.contains("/")) {
								String fromDirs = "D:/data/webmagic" + pdffile.replace("//", "/").replace("${RootPath}", "");
								String newPdfFiles = "/wxgzh" + pdffile.substring(pdffile.lastIndexOf("/"), pdffile.length());
								String toDirs = targetFilePath.subSequence(0, targetFilePath.length() - 1) + newPdfFiles;
								try {
									CSVUtils.copySingleFile(fromDirs, toDirs);
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
							/*try {
								if(!StringUtils.isEmpty(pdffile)) {
									// 对于 pdf 的路径做出额外的处理, 替换域名为 wxgzh
									String publicname = "";
									try {
										String id = pdffile.substring(pdffile.replaceFirst("/", "").indexOf("/") + 2, pdffile.lastIndexOf("."));
										publicname = newsDao.getPublicnameById(tables[i], id);
									} catch (Exception e) {
										System.out.println("-----------------------------> " + pdffile);
										e.printStackTrace();
									}
									if(publicname != "") {
										// String replace = "weixin"+ File.separator + CSVUtils.getChatnumberBypublicname(publicname) + ym + d + File.separator + "pdf";
										String replace = "weixin"+ File.separator + "weixin.sogou.com" + ym + d + File.separator + "pdf";
										//CSVUtils.copy(fromDirs, toDirs.replace("weixin.sogou.com", replace));
										CSVUtils.copySingleFile(fromDirs, toDirs.replace("weixin.sogou.com", replace));
									}
									CSVUtils.copySingleFile(fromDirs, toDirs);

								}

							} catch (IOException e) {
								e.printStackTrace();
							}*/
						}
					}
					//3.5 导出微信的封面到指定的位置
					if(extras.containsKey("headimg") && ((String)(params.get("tbname"))).contains("wx")) {
						// JH 项目
						if(identifyCode.equalsIgnoreCase("jh")) {
							Set<String> headimgs = extras.get("headimg");
							for(String pdffile : headimgs) {
								String fromDirs = "D:/data/webmagic" + pdffile.replace("//", "/").replace("${RootPath}", "");
								String newPdfFiles = "/upload/image/20180329" + pdffile.replace("${RootPath}", "");
								String toDirs = targetFilePath.subSequence(0, targetFilePath.length() - 1) + newPdfFiles;
								try {
									if(!StringUtils.isEmpty(pdffile)) {
										CSVUtils.copySingleFile(fromDirs, toDirs);
									}
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}
					}
				}
			}
			// 4. 生成文件包  yyyy-MM-dd.zip
			ZipCompressor fileCompressor = new ZipCompressor(targetPath + "/files.zip");
			fileCompressor.compress(targetFilePath);
			removeDirectory(targetFilePath);
			ZipCompressor compressor = new ZipCompressor(zipPath);
			// 将指定的文件夹的所有文件进行压缩
			compressor.compressFilesOnly(targetPath + "/");

		}

		// 5. 将最后的总内容 打包为 .zip 并上传到 云
		/*QiniuCloudStorageService storageService = new QiniuCloudStorageService(getCloudStorageConfig());
		InputStream in = new FileInputStream(new File(zipPath));
		storageService.upload(in);*/
	}

	/***
	 * 删除指定文件夹下所有文件
	 *
	 * @param path 文件夹完整绝对路径
	 * @return
	 */
	public static  boolean delAllFile(String path) {
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
				delAllFile(path + "/" + tempList[i]);// 先删除文件夹里面的文件
				removeDirectory(path + "/" + tempList[i]);// 再删除空文件夹
				flag = true;
			}
		}
		return flag;
	}

	/***
	 * 删除文件夹
	 *
	 *  folderPath文件夹完整绝对路径
	 */
	public  static void removeDirectory(String folderPath) {
		try {
			delAllFile(folderPath); // 删除完里面所有内容
			String filePath = folderPath;
			filePath = filePath.toString();
			java.io.File myFilePath = new java.io.File(filePath);
			myFilePath.delete(); // 删除空文件夹
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 返回云存储的配置文件
	private CloudStorageConfig getCloudStorageConfig() {
		CloudStorageConfig config = new CloudStorageConfig();
		config.setType(1);
		config.setQiniuDomain("https://www.qiniu.com/");
		config.setQiniuAccessKey("_LbIjh7by1ob-4GyjUO9bHfhNr0HaFyfqVOc5c3o");
		config.setQiniuSecretKey("7Be6Jz-aX-CTHwXgL8KklQrfasUNbLi9p-hi_3dc");
		config.setQiniuBucketName("best-jh");
		return config;
	}

	@Override
	public List<News> getNewsPdfs(String begindate, String enddate) {
		return newsDao.getNewsPdfs(begindate, enddate);
	}

	// 清楚对应的 pdf 文件不存在对应的字段属性值
	@Override
	public void clearNotExistPdf(News news) {
		newsDao.clearNotExistPdf(news);

	}

	// 按照信息的标题进行数据的去重
	@Override
	public List<String> removeDulpRecords(String[] tables, double similarityFactor) {
		for(int i = 0; i < tables.length; i++) {
			String tablename = tables[i];
			List<String> titles = newsDao.getDulpTitles(tablename);

			if(titles!= null && titles.size() > 0) {
				System.out.println();
				System.out.println("数据表 " + tablename + " 共发现 " + titles.size() + " 个标题重复");
				// 包含最小的采集时间的集合
				List<News> newsList = newsDao.getTitleWithMinCrawlerdate(tablename, titles);
				// 标题重复的集合
				List<News> newsTotalList = newsDao.getTitleWithCrawlerdate(tablename, titles);
				List<String> newsIds = getIdsFromList(newsList);
				List<String> newsTotalIds = getIdsFromList(newsTotalList);
				// 求出 newsTotalList 与 newsList 的差集
				newsTotalIds.removeAll(newsIds);
				System.out.println("数据表 " + tablename + " 开始去重!");
				newsDao.deleteNewsBatch(tablename, newsTotalIds);
				System.out.println("数据表 " + tablename + " 结束去重!");
			}
		}

		return removeRecordsBySimilarityFactor(tables, similarityFactor);
	}

	// 先按照标题进行去重, 若标题相关度较高再考虑内容的对比去重
	private List<String> removeRecordsBySimilarityFactor(String[] tables, double similarityFactor) {
		String pubdate = CSVUtils.getNumberDaysBefore(new Date(), 5);
		// 由于数据量较大, 直接声明对应的集合的容量
		int sumCount = 0;
		for(String tablename : tables) {
			sumCount += newsDao.selectTotalCount(tablename, pubdate);
		}
		List<String> removeList = new ArrayList<>();

		// 1. 获取所有的数据
		for(String tablename : tables) {

			if(tablename.equals("news_en")) continue;
			System.out.println("table " + tablename + " begin checking:");
			// 查询近 10 天的数据
			List<News> newsList = newsDao.selectNewsContent(tablename, pubdate);
			// 2. 遍历数据, 进行查重
			// 声明一个集合用于存放已经被识别的索引
			List<String> recognizedIndexList = new ArrayList<>();
			for(int i = 0; i < newsList.size() - 1; i++) {
				System.out.print(" " + (i+1) + " / " + newsList.size());
				// 存放重复数据的 id 的集合
				List<String> idList = new ArrayList<>();

				boolean overflag = false;

				idList.add(newsList.get(i).getId());
				for(int j = i + 1; j < newsList.size(); j++) {

					// 若当前的遍历的集合的索引值与后面的某个已经被识别的值相等, 直接跳过
					if(recognizedIndexList.size() > 0) {
						for(String recognizedIndex : recognizedIndexList) {
							String index = recognizedIndex.substring(recognizedIndex.indexOf(",") + 1, recognizedIndex.length());
							if((i + "").equals(index)) {
								overflag = true;
								break;
							}
						}
					}
					if(overflag) {
						break;
					}
					double factor = EditDistance.similar(newsList.get(i).getTitle(), newsList.get(j).getTitle());
					// 两个数据的相似度超限, 插入对应的集合
					if(factor > similarityFactor) {
						// 标题一样, 再判断正文内容
						double contentFactor = EditDistance.similar(newsList.get(i).getContent(), newsList.get(j).getContent());
						if(contentFactor > (similarityFactor / 2)) {
							// 添加识别出来的 id 到已经识别索引
							recognizedIndexList.add(i + "," + j);
							idList.add(newsList.get(j).getId());
						}
					}

				}
				// 如果 idList 的长度大于 1, 说明有重复数据, 找出时间较早的数据, 删除后面的数据
				if(idList.size() > 1) {
					// 根据给定 id 查找时间较早的数据
					String minPubdate = newsDao.selectMinPubdateByGivingIdlist(idList, tablename);
					// 添加需要删除的对象的集合
					List<String> temp = newsDao.removeDulpRecordsHavingOlderPubdate(idList, minPubdate, tablename);
					if(null != temp && temp.size() > 0) {
						removeList.addAll(temp);
					}
					// 若存在相同的时间的数据, 比较字符串的长度
					List<News> minPubdateList = newsDao.selectCountRecordsWithMinPubdate(idList, minPubdate, tablename);
					// 保留尽可能长的数据
					if(minPubdateList.size() > 1) {
						List<String> strList = new ArrayList<>(minPubdateList.size());
						for(News news : minPubdateList) {
							news.setContent(EditDistance.trimNewsContent(newsList.get(i).getContent()));
							strList.add(news.getContent());
						}
						String maxContent = Collections.max(strList);

						// 再遍历集合, 去除包含最长的内容的 news 对象即可
						for(int w = 0; w < minPubdateList.size(); w++) {
							if(w != strList.indexOf(maxContent)) {
								removeList.add(minPubdateList.get(w).getId());
							}
						}

					}
				}

			}
		}
		if(removeList.size() > 0) {
			// 不直接删除原表的数据,而是进行统计汇总, 保留对应的集合, 在进行打包的时候对应的记录不打包
			//newsDao.deleteNewsBatch(tablename, removeList);
			System.out.println("-----------------------------------==================================================================-------------------------------" + removeList);
			return removeList;
		}
		// 根据集合的 id 删除对应的元素即可
		return removeList;
	}

	private List<String> getIdsFromList(List<News> newsList) {
		List<String> ids = new ArrayList<>();
		if(newsList.size() > 0) {
			for(News news : newsList) {
				ids.add(news.getId());
			}
		}
		return ids;
	}

}
