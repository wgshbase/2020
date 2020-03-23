package io.renren.modules.spider.service.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mss.crawler.spiderjson.util.EditDistance;
import com.mss.word.HtmlToWordByPOI;
import io.renren.modules.spider.entity.WebInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
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

@Service(value="import2CloudService")
public class Import2CloudServiceImpl implements IImport2CloudService {

	private static final Logger LOG = LogManager.getLogger(Import2CloudServiceImpl.class);

	private static List<String> keywordsListWeixin = new ArrayList<>(Arrays.asList("航天","火箭","火箭","运载火箭","导弹 ","航天器","航天器","飞行器","飞行器","卫星 ","飞船 ","飞船","航天飞机 ","航天飞机 ","载人飞船 ","载人飞行 ","载人飞行器 ","载人飞行器 ","载人轨道站 ","载人航天器 ","载人航天中心 ","载人火箭 ","载人空间飞行 ","空天飞机 ","空天飞行器 ","空天飞行器","深空探测","国际空间站","高超声速","猎鹰计划 ","高超声速 ","超高声速","超高音速","高超音速","极超音速 ","滑翔飞行器 ","猎鹰计划 ","HTV-2","X37B ","X51A ","美国国家航空航天局","美国国防部","空间","skybox","米诺陶","空天防御","欧空局","空中客车","宇航","国防","反导","核裁军","核谈","防扩散","MTCR","外空武器化","临近空间","美国空军","美国导弹防御局","美国导弹防御局","猎鹰","spacex","烈火","轨道科学公司","军控","美国国家航空航天局","波音","蓝源","武器","臭鼬工厂","鬼怪工厂","3D打印","增材制造","纳米武器","美国国防高级研究计划局","美国国防高级研究计划局","洛克希德","雷神公司","BAE系统公司"));

	@Autowired
	private NewsDao newsDao;

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
		// 1. 默认的打包方式, 不进行任何的处理
		if(StringUtils.isEmpty(identifyCode)) {
			importByDate(begindate, enddate, tables, removeList);
			return;
		} // 2. JH 项目打包的处理
		else if("jh".equalsIgnoreCase(identifyCode)) {
			String timePath = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
			String targetPath = "D:/temp/" + timePath + "/";
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

				String day = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
				String ym = File.separator + day.replace("-", "").substring(0, 6);
				String d = File.separator + day.replace("-", "").substring(6, 8);

				// 1. 将数据库的记录转化为 tbname_日期.cvs 文件
				extras = CSVUtils.createCSVFile(identifyCode, heads, dataList, targetPath, tables[i] + "_" + begindate  + ".csv", removeList);
				//Set<String> set = extras.get("domains");
				if(extras != null && !extras.isEmpty()) {
					// 2. 导出图片到 /Domain/image/,
					if(extras.containsKey("attchfiles")) {
						Set<String> attachfiles = extras.get("attchfiles");
						for(String attachfile : attachfiles) {
							String fromDirs = "D:/data/webmagic" + attachfile.replace("${RootPath}", "");
							String toDirs = targetPath.subSequence(0, targetPath.length() - 1) +attachfile.replace("${RootPath}", "");
//						System.out.println("toDirs : ----------------------<" + toDirs);
							try {
								if(identifyCode.equalsIgnoreCase("jh")) {
									// 若表名之中不包含 wx(即不是微信的表, 不做处理)
									if(!tables[i].contains("wx")) {
										// D:/temp/20180820171636060/news.ifeng.com/image/2210bfa9afca0a375cfdcc981cd23a76.jpeg
										// /news/news.ifeng.com/201808/20/image/944ed23f8c563cf49d85cf72dccbaaed.jpg
//									Matcher m = Pattern.compile("(?<=(\\d{17}/)).*?(?=/image/)").matcher(toDirs);
//									if(m.find()) {
//										String rep = m.group();
//										toDirs = toDirs.replace(rep, "news" + File.separator + rep+ ym  + d);
//									}
										CSVUtils.copySingleFile(fromDirs, toDirs);
									}
									else {
										// 微信的表
										// 20180820171332242/weixin/chatnumber/201808/01/cover/02ebd81a4dd0f0e90bfe0b0ad1763e24.jpeg
										//String str = "20180820171332242/weixin.sogou.com/cover/02ebd81a4dd0f0e90bfe0b0ad1763e24.jpeg
										try {
											if(!StringUtils.isEmpty(attachfile)) {
												//List<String> publicnames = newsDao.getPublicnameByAttachfilesName(tables[i], "%" + attachfile + "%");
												//for(String publicname : publicnames) {
												//String replace = "weixin" + File.separator + CSVUtils.getChatnumberBypublicname(publicname) + ym + d;
												//String replace = "weixin" + File.separator + "weixin.sogou.com" + ym + d;
												//CSVUtils.copy(fromDirs, toDirs.replace("weixin.sogou.com", replace));
												//}
												//String replace = "weixin" + File.separator + "weixin.sogou.com" + ym + d;
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
									String toDirs = targetPath.subSequence(0, targetPath.length() - 1) + newPdfFiles;
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
									String toDirs = targetPath.subSequence(0, targetPath.length() - 1) + newPdfFiles;
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
			}
			// 4. 生成文件包  yyyy-MM-dd.zip
			ZipCompressor compressor = new ZipCompressor(zipPath);
			// 将指定的文件夹的所有文件进行压缩
			compressor.compress(targetPath);

			// 5. 将最后的总内容 打包为 .zip 并上传到 云
		/*QiniuCloudStorageService storageService = new QiniuCloudStorageService(getCloudStorageConfig());
		InputStream in = new FileInputStream(new File(zipPath));
		storageService.upload(in);*/
		} // 3. 19 项目的打包处理
		else if("19".equalsIgnoreCase(identifyCode)) {
			packetWeixinNewsTo19(begindate, enddate);
		} else if("zhs".equalsIgnoreCase(identifyCode)) {
			packetDataToZHS(begindate, enddate);
		}

	}

	/**
	 * 打包数据到 zhs
	 * @param begindate 检索的开始时间（发布时间）
	 * @param enddate 检索的结束时间
	 */
	public void packetDataToZHS(String begindate, String enddate) {
		/**
		 * 打包的格式:
		 * 压缩包
		 *      - 域名
		 *          - 对应的文档
		 */
		//1. 查询出指定的所有的日期期间的采集数据
		List<News> list = newsDao.findZHSNewsByDate(begindate, enddate);
		//2. 生成临时文件
		File zhsTempFile = new File("D:/tmp/" + begindate + "_" + enddate + "/");
		if(!zhsTempFile.exists()) {
			zhsTempFile.mkdirs();
		}
		HtmlToWordByPOI poi = new HtmlToWordByPOI();
		poi.setRootPath(zhsTempFile.getAbsolutePath());
		//3. 生成 word 文档

		for(News news : list) {
			poi.toWord(getTargetHtml(news), zhsTempFile + "/" + trimFileName(news.getTitleTr()) + ".doc");
		}

		//4. word 文档打包
		// 4. 生成文件包  yyyy-MM-dd.zip
		String zipPath = "D:/2019HTDT("+ begindate.substring(4,begindate.length())+"-"+enddate.substring(4,enddate.length())+").zip";
		ZipCompressor compressor = new ZipCompressor(zipPath);
		// 将指定的文件夹的所有文件进行压缩
		compressor.compress(zhsTempFile.getAbsolutePath());
	}

	// 特殊字符的格式化
	public String trimFileName(String name) {
		if(name.contains(" ")) {
			name = name.replace(" ", "");
		}
		Matcher m = Pattern.compile("[\\\\s\\\\\\\\/:\\\\*\\\\?\\\\\\\"<>\\\\|]").matcher(name);
		if(m.find()) {
			name = name.replaceAll(m.group(),"");
		}

		return name;
	}

	/**
	 * 格式化新闻日期
	 * @param news
	 * @return
	 */
	public String getTargetHtml(News news) {
		StringBuilder sb = new StringBuilder();

		String cnprefix = "<p style=\"text-align:center\"><span style=\"color:red\">————————————————————本文来源于国外网站,以下是机器翻译内容,向下阅读可查看原文————————————————————</span></p>";
		String enprefix = "<p style=\"text-align: center;\"><span>————————————————————原文————————————————————</span></p>";

		// 发布时间与原文链接, 来源网站
		String pubdate = news.getPubdate();
		String url = news.getUrl();
		sb.append("<p>发布时间: " + pubdate.trim() + "</p>").append("<p>原文链接: " + url.trim() + "</p>");

		String titleTr = news.getTitleTr();
		sb.append("<p>译文标题: " + titleTr.trim() + "</p>");
		String contentTr = news.getContentTr();
		contentTr = contentTr.substring(cnprefix.length(), contentTr.indexOf(enprefix));
		contentTr = contentTr.replace("${RootPath}", "D:/data/webmagic2");
		sb.append("<p>译文: " + contentTr.trim() + "</p>");
		// 原文的标题和正文
		String title = news.getTitle();
		String content = news.getContent();
		content = content.replace("${RootPath}", "D:/data/webmagic2");
		sb.append("<p>原文标题: " + title.trim() + "</p>");
		sb.append("<p>原文: " + content.trim() + "</p>");

		return sb.toString();
	}

	// 打包数据到 19
	public void packetWeixinNewsTo19(String begindate, String enddate) {
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

		// 0. 压缩前先判断待压缩目录的文件是否为空, 不为空的话手动置空, 防止数据的冗余打包
		LOG.info("开始清除临时文件夹...");
		delAllFile(targetPath);
		LOG.info("删除临时文件夹成功...");

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

		List<WebInfo> webInfos = newsDao.selectNewsAsWebInfo(params);
		if (webInfos != null && webInfos.size() > 1) {
			for (WebInfo info : webInfos) {

				String title = info.getTitle();
				String content = info.getWebInfoContent();
				for(String keyword : keywordsListWeixin) {
					if(title.contains(keyword) || content.contains(keyword)) {
						// 1. 將 java 對象轉換為 xml 文檔
						extras = parseJavaBean2XmlFile(info, targetPath);

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
						}
						break;
					}

				}


			}

			// 4. 生成文件包  yyyy-MM-dd.zip
			ZipCompressor compressor = new ZipCompressor(zipPath);
			// 将指定的文件夹的所有文件进行压缩
			compressor.compress(targetPath);
		}

	}

	/**
	 * 删除指定文件夹下所有文件
	 * @param path 文件夹完整绝对路径 ,"Z:/xuyun/save"
	 */
	public static boolean delAllFile(String path) {
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
				delAllFile(path + "/" + tempList[i]);//先删除文件夹里面的文件
				delFolder(path + "/" + tempList[i]);//再删除空文件夹
				flag = true;
			}
		}
		return flag;
	}
	/**
	 * 删除文件夹
	 * @param folderPath 文件夹完整绝对路径 ,"Z:/xuyun/save"
	 */
	public static void delFolder(String folderPath) {
		try {
			delAllFile(folderPath); //删除完里面所有内容
			String filePath = folderPath;
			filePath = filePath.toString();
			java.io.File myFilePath = new java.io.File(filePath);
			myFilePath.delete(); //删除空文件夹
		} catch (Exception e) {
			e.printStackTrace();
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
