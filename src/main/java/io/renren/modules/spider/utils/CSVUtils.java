package io.renren.modules.spider.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import com.alibaba.fastjson.JSONArray;
import com.mss.crawler.common.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import io.renren.modules.spider.dao.NewsDao;

@Component
public class CSVUtils {
	public static List<String> blackList = FileUtils.fileToList(new File("blacklist.txt"));
	private static String pdfTempTxtPath = System.getProperty("user.dir") + File.separator + "pdftemp" + File.separator + "pdf.txt"; 
	private static Logger logger = Logger.getLogger(CSVUtils.class);
	@Autowired
	private NewsDao dao;
	private static CSVUtils csvUtils;
	
	@PostConstruct
	public void init() {
		csvUtils = this;
		csvUtils.dao = this.dao;
	}
	
	public static Map<String, String> chatnumberMap = new HashMap<>();
	static {
		chatnumberMap.put("军鹰资讯", "JoinInformation");
		chatnumberMap.put("高端装备产业研究中心", "www-equipinfo-com-cn");
		chatnumberMap.put("我们的太空", "ourspace0424");
		chatnumberMap.put("装备科技", "jfjbzzfs");
		chatnumberMap.put("卫星与网络", "satnetdy");
		chatnumberMap.put("国防科技要闻", "CDSTIC");
		chatnumberMap.put("航小宇", "hangxiaoyucasc");
		chatnumberMap.put("雷达通信电子战", "RadarCommEW");
		chatnumberMap.put("学术plus", "caeit-e");
		chatnumberMap.put("电科小氙", "gh_b9d6024b902d");
		chatnumberMap.put("战略前沿技术", "Tech999");
		chatnumberMap.put("中国指挥与控制学会", "c2_china");
		chatnumberMap.put("中国军事", "zhongguojunshiwenhua");
		chatnumberMap.put("无人机", "wurenji1");
		chatnumberMap.put("国科环宇", "UCAS2004");
		chatnumberMap.put("科罗廖夫的军事客厅", "keluoliaofucn");
		chatnumberMap.put("泰伯网", "news_3snews");
		chatnumberMap.put("中国战略支援", "zgzlzy");
		chatnumberMap.put("中国卫星导航定位应用管理中心", "chinabeidougov");
		chatnumberMap.put("航天长城", "ccgsdw");
		chatnumberMap.put("太空网", "taikongmedia");
		chatnumberMap.put("知远战略与防务研究所", "knowfar2014");
		chatnumberMap.put("卫星界", "sat-china1998");
		chatnumberMap.put("天地一体化信息网络", "gh_07e2c969979a");
		chatnumberMap.put("中国航天科技集团", "cascwx");
		chatnumberMap.put("电科防务研究", "CETC-ETDR");
		chatnumberMap.put("卫星应用", "SatelliteApplication");
		chatnumberMap.put("中国载人航天", "zairenhangtian");
		chatnumberMap.put("军工黑科技", "jungonghkj");
		chatnumberMap.put("交通安全应急国家工程实验室", "NELTSEI");
		chatnumberMap.put("国际太空", "Space_international");
		chatnumberMap.put("航天防务", "AerospaceDefense");
		chatnumberMap.put("桌面战争", "Kriegspielwar");
		chatnumberMap.put("雷曼军事现代舰船", "xdjc-rimnds");
		chatnumberMap.put("漫步宇宙", "qqtaikong");
		chatnumberMap.put("晨读航天", "SpaceNewsDigest");
		chatnumberMap.put("中国航天科普", "space-more");
		chatnumberMap.put("宇航探索局", "gh_70808b3b0a7e");
		chatnumberMap.put("中国航天", "zght-caecc");
		chatnumberMap.put("小火箭", "ixiaohuojian");
		chatnumberMap.put("DeepTech深科技", "deeptechchina");
		chatnumberMap.put("装备参考", "Armament999");
		chatnumberMap.put("无人争锋", "UI-STRIVE");
		chatnumberMap.put("空天大视野", "AerospaceVision");
		chatnumberMap.put("网信科技前沿", "E-frontiers");
		chatnumberMap.put("军民融合观察", "JMRHGC");
		chatnumberMap.put("海洋防务前沿", "maritime-defense");
		chatnumberMap.put("环球军事", "crihuanqiujunshi");
		chatnumberMap.put("国际电子战", "EW21cn");
		chatnumberMap.put("北国防务", "sinorusdef");
		chatnumberMap.put("军评陈光文", "guangwen-chen");
		chatnumberMap.put("战争跟踪狂人", "goldnews");
		chatnumberMap.put("战略网军事", "chinaiiss-com");
		chatnumberMap.put("中国北斗卫星导航系统", "beidousystem");
		chatnumberMap.put("军尚科技", "jsimstechnology");
		chatnumberMap.put("铁索寒", "cold-iron");
		chatnumberMap.put("星际智汇", "space_707");
		chatnumberMap.put("蓝海星智库", "SICC_LHX");
		chatnumberMap.put("微波射频网", "mwrfnet");
		chatnumberMap.put("中国新一代人工智能", "NGAI-CHINA");
		chatnumberMap.put("电波之矛", "RadarEW");
		chatnumberMap.put("航天那些事", "spacechinaforever");
		chatnumberMap.put("海鹰资讯", "hiwing_news");
		chatnumberMap.put("讲武堂", "qqmiljwt");
		chatnumberMap.put("军工圈", "jungongquan");
		chatnumberMap.put("国防网", "zlw6645");
		chatnumberMap.put("军武酷", "JUNWUKU");
		chatnumberMap.put("烽火军事", "Fenghuojunshi");
		chatnumberMap.put("军民融合产业圈", "JMRHCYQ");
		chatnumberMap.put("雷科防务", "raco_defense");
		chatnumberMap.put("空天防务观察", "AerospaceWatch");
		chatnumberMap.put("新浪军事", "sinamilnews");
		chatnumberMap.put("国际时事军事", "shishijunshi");
		chatnumberMap.put("凤凰军事新闻", "ifengjsnews");
		chatnumberMap.put("兵工科技", "binggongkeji");
		chatnumberMap.put("鹰眼图说军事", "tsjunshi81");
		chatnumberMap.put("全球军情解码", "jqjm88");
		chatnumberMap.put("凤凰军事解说", "syzhbn88");
		chatnumberMap.put("铁血军事", "tiexuejunshi");
		chatnumberMap.put("局座召忠", "zhangzhaozhong45");
	}
	
	/**
	 * 输出pdf 的名称到指定的文件夹
	 */
	public static void writePdfName2TempText(String pdfname) {
        try {
            File f = new File(pdfTempTxtPath);
            File parentFile = f.getParentFile();
            if(!parentFile.exists()) {
            	parentFile.mkdirs();
            }
            // 创建新的文件
            f.createNewFile();
            FileWriter fw = new FileWriter(f, true);

            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(pdfname + "\r\n");// 往已有的文件上添加字符串
            bw.close();
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
	}

	/**
	 * List 集合转化为 字符串
	 */
	public static String parseList2String(List<String> list) {
		if(null == list || list.size() == 0) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for(String str : list) {
			sb.append(str).append(", ");
		}
		return sb.toString().subSequence(0, sb.toString().length() - 2).toString();
	}

	/**
	 * map 集合转化为 字符串
	 * @param extractNamedEntity
	 * @return
	 */
	public static String parseMap2String(Map<String, Set<String>> map) {
		if(null == map || map.isEmpty()) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		if(map.containsKey("nr")) {
			sb.append("人物:").append(parseList2String(new ArrayList<>(map.get("nr")))).append("\r\n");
		}
		if(map.containsKey("ns")) {
			sb.append("地点:").append(parseList2String(new ArrayList<>(map.get("ns")))).append("\r\n");
		}
		if(map.containsKey("nt")) {
			sb.append("机构名:").append(parseList2String(new ArrayList<>(map.get("nt")))).append("\r\n");
		}
		return sb.toString().subSequence(0, sb.lastIndexOf("\r\n")).toString();
	}

	/**
	 * 解析 list 集合到 html 界面
	 * @param list
	 * @param columns
	 * @return
	 */
	public static String parseList2Html(List<Map> list, JSONArray columns) {
		StringBuilder sb = new StringBuilder();
		for(Map map : list) {
			sb.append("<hr>" + "第 " + (list.indexOf(map) + 1) + " 条结果<br>");
			for(int i = 0; i < columns.size(); i++) {
				String column = columns.get(i).toString();
				switch(column) {
					case "title":
						sb.append("标题: " + map.get("title")+"<br>");
						if(map.containsKey("title_tr")) {
							sb.append("翻译标题: " + map.get("title_tr")+"<br>");
						}
						break;
					case "url":
						sb.append("原文链接: " + map.get("crawler_url")+"<br>");
						break;
					case "pubDate":
						if(map.get("pubDate") instanceof Date) {
							String pubdate = new SimpleDateFormat("yyyy-MM-dd").format(map.get("pubDate"));
							sb.append("发布时间: " + pubdate+"<br>");
						} else {
							sb.append("发布时间: " + map.get("pubDate")+"<br>");
						}
						break;
					case "summary":
						sb.append("摘要: " + map.get("nlpSummary")+"<br>");
						break;
				}

			}
		}
		return sb.toString();
	}

	/**
	 * 获取新闻的正文文字部分
	 */
	public static String getContentWithoutHtmlTag(String html) {
		if(StringUtils.isEmpty(html)) {
			return "";
		}
		return html.replaceAll("<.*?>", "");
	}
	
	/**
	 * 导出数据 dataList 到 指定的文件 file 中
	 * @param
	 *           outPutPath (路径+文件名)，csv文件不存在会自动创建, 保存的对应的数据库中的记录
	 * @param identifyCode
	 * @param dataList
	 *            数据
	 * @param removeList
	 * @return
	 */
	public static Map<String, Set<String>> createCSVFile(String identifyCode, List<String> heads, List<LinkedHashMap<String, Object>> dataList, String outPutPath, String filename, List<String> removeList) {
		FileOutputStream out = null;
		OutputStreamWriter osw = null;
		BufferedWriter bw = null;
		Map<String, Set<String>> extra = new HashMap<>();
		Set<String> attachfiles = new HashSet<>();
		Set<String> pdffiles = new HashSet<>();
		Set<String> headimg = new HashSet<>();
		try {
			if(!new File(outPutPath).exists()) {
				new File(outPutPath).mkdirs();
			}
			out = new FileOutputStream(new File(outPutPath + File.separator + filename));
			// 输出字符流按照 指定的 编码格式进行编码
			osw = new OutputStreamWriter(out, "UTF-8");
			bw = new BufferedWriter(osw);

			// 写入首行
			for (String key : heads) {
				bw.append(key).append(",");
			}
			bw.append("\r\n");

			
			// 写入 content
			if (dataList != null && !dataList.isEmpty()) {
				for (LinkedHashMap<String, Object> entities : dataList) {
					boolean ignoreFlag = false;
					String entityTitle = (String) entities.get("title");
					// 对于部分正文部分内容极少或没有的数据, 不予处理
					String entityContent = (String) entities.get("content");

					String id = (String)entities.get("id");
					if(null != removeList && removeList.size() > 1) {
						if(removeList.contains(id)) {
							logger.warn("---------------->" + id + " is Duplicate news!!!");
							System.out.println("==========================================================================================---------------->" + id + "is Duplicate news!!!");
							ignoreFlag = true;
						}
					}

					for(String blackword : blackList) {
						if(entityTitle.contains(blackword) || entityContent.contains(blackword)) {
							logger.warn("-------------->" + entityTitle + "is in the blackList");
							ignoreFlag = true;
						}
					}
					if(ignoreFlag) continue;

					//entityContent = entityContent.replaceAll("<(.*?)>", "").trim();
					if(entityContent == null || entityContent.replaceAll("<(.*?)>", "").trim().length() < 100) {
						continue;
					}
					String tbname = filename.substring(0, filename.lastIndexOf("_"));
					// 1. 获取所有的数据
					// 根据 id 获取对应的出版名称, 一个 entities 仅仅对应一个 出版名称
					String publicname = "";
					for (Map.Entry<String, Object> entity : entities.entrySet()) {
						if(tbname.contains("wx") && "id".equals(entity.getKey())) {
							publicname = csvUtils.dao.getPublicnameById(tbname, (String)entity.getValue());
						}
						// 处理附件名, 即处理图片
						if ("attchfiles".equals(entity.getKey()) && !StringUtils.isEmpty(entity.getValue())) {
							if(!StringUtils.isEmpty(identifyCode) && identifyCode.equalsIgnoreCase("jh")) {
								addAttachFiles(entity.getKey(), attachfiles, extra, ((String)entity.getValue()).replace("${RootPath}", ""));
							} else {
								addAttachFiles(entity.getKey(), attachfiles, extra, ((String)entity.getValue()));
							}
						}
						// 处理 pdf 附件
						if("pdffiles".equals(entity.getKey()) && !StringUtils.isEmpty(entity.getValue())) {
							addAttachFiles(entity.getKey(), pdffiles, extra, (String)entity.getValue());
							String entityVal = (String) entity.getValue();
							entityVal = "/wxgzh" + entityVal.substring(entityVal.lastIndexOf("/"), entityVal.length());
							entities.put(entity.getKey(), entityVal);
						}
						if("headimg".equals(entity.getKey()) && !StringUtils.isEmpty(entity.getValue()) && tbname.contains("wx")) {
							addAttachFiles(entity.getKey(), headimg, extra, (String)entity.getValue());
							String entityVal = (String) entity.getValue();
							entityVal = "//upload/image/20180329" + entityVal.replace("${RootPath}", "");
							entities.put(entity.getKey(), entityVal);
						}
						
						// 处理日期
						if (entity.getValue() instanceof Date) {
							String entityDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(entity.getValue());
							if (entityDate == null) {
								entityDate = "\"\"";
							} else {
								entityDate = dealEntityVal(entityDate, identifyCode, tbname, publicname);
							}
							bw.append(entityDate).append(",");
						} else {
							// 常规处理
							String entityVal = (String) entity.getValue();
							if (entityVal == null || entityVal == "") {
								entityVal = "\"\"";
							} else {
								entityVal = dealEntityVal(entityVal, identifyCode, filename, publicname);
							}
							bw.append(entityVal).append(",");
						}
					}
					bw.append("\r\n");
				}

			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (bw != null) {
				try {
					bw.close();
					bw = null;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (osw != null) {
				try {
					osw.close();
					osw = null;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (out != null) {
				try {
					out.close();
					out = null;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return extra;
		
	}

	/**
	 * 添加额外的附件信息
	 * @param key 附件的名称(image cover pdf)
	 * @param list 最终的集合
	 * @param extra 包含所有的附件信息的集合(加的运算, 有新的附件就会加到之前的集合中去)
	 * @param attachfiles 附件的属性值(xxxxx.pdf)
	 * @return
	 */
	public static Map<String, Set<String>> addAttachFiles(String key, Set<String> list, Map<String, Set<String>> extra, String attachfiles) {
		if(attachfiles.contains(",")) {
			// 使用 , 进行分割附件
			String[] split = attachfiles.split(",");
			for(String str : split) {
				list.add(str.substring(str.indexOf("/"), str.length()));
			}
			extra.put(key, list);
		} else {
			list.add(attachfiles);
			extra.put(key, list);
		}
		
		return extra;
	}
	
	// 首字母实现大写
	public static String upperCase(String str) {
	    char[] ch = str.toCharArray();
	    if (ch[0] >= 'a' && ch[0] <= 'z') {
	        ch[0] = (char) (ch[0] - 32);
	    }
	    return new String(ch);
	}
	
	/**
	 * 按照 csv 文件的规则进行内部数据的调整
	 * 
	 * @param entityVal 
	 * @param identifyCode 
	 * @param filename 
	 * @param publicname 
	 * @return
	 */
	private static String dealEntityVal(String entityVal, String identifyCode, String tbname, String publicname) {
		entityVal = StringUtils.replace(entityVal, "\"", "\"\"");
		entityVal = StringUtils.replace(entityVal, entityVal, "\"" + entityVal + "\"");
		entityVal = entityVal.replaceAll("\\n", "");
		entityVal = entityVal.replaceAll("\\r", "");
		entityVal = entityVal.replaceAll("\\t", "");
		if(!StringUtils.isEmpty(identifyCode) && identifyCode.equalsIgnoreCase("jh") && entityVal.contains("${RootPath}")) {
			
			/**
			 *  处理结果:
			 *  	- domain
			 *  		- yyyymm
			 *  			- dd
			 *  				- image
			 *  					- attchfilename
			 */
			// 20180820171332242/weixin/chatnumber/201808/01/cover/02ebd81a4dd0f0e90bfe0b0ad1763e24.jpeg
			//String str = "20180820171332242/weixin.sogou.com/cover/02ebd81a4dd0f0e90bfe0b0ad1763e24.jpeg
			
			// D:/temp/20180820171636060/news.ifeng.com/image/2210bfa9afca0a375cfdcc981cd23a76.jpeg
			// /news/news.ifeng.com/201808/20/image/944ed23f8c563cf49d85cf72dccbaaed.jpg
			String day = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
			String ym = File.separator + day.replace("-", "").substring(0, 6);
			String d = File.separator + day.replace("-", "").substring(6, 8);
			
			
			/*if(!tbname.contains("wx")) {
				Matcher m = Pattern.compile("(?<=\\$\\{RootPath\\}/).*?(?=/image/)").matcher(entityVal);
				if(m.find()) {
					String rep = m.group();
					// 替换域名到指定的格式
					entityVal = entityVal.replace(rep, "news" + File.separator + rep+ ym  + d);
				}
			} else {
				// 替换域名到指定的格式
				//entityVal = entityVal.replace("weixin.sogou.com", "weixin" + File.separator + getChatnumberBypublicname(publicname)+ ym  + d);
				entityVal = entityVal.replace("weixin.sogou.com", "weixin" + File.separator + "weixin.sogou.com"+ ym  + d);
				
			}*/
			// 替换前缀为空即可
			entityVal = entityVal.replace("${RootPath}", "");
			
		}
		return entityVal;
	}

	// 获取前一天的日期, 由于外文网与中国有时差
	public static String getPrevDay(Date crawlerdate) {
		Calendar c = Calendar.getInstance(); 
		c.setTime(crawlerdate); 
		
		int day=c.get(Calendar.DATE); 
		c.set(Calendar.DATE,day-1); 

		String prevDay=new SimpleDateFormat("yyyy-MM-dd").format(c.getTime()); 
		return prevDay; 
	}
	// 获取后一天的日期, 由于外文网与中国有时差
	public static String getNextDay(Date crawlerdate) {
		Calendar c = Calendar.getInstance();
		c.setTime(crawlerdate);

		int day=c.get(Calendar.DATE);
		c.set(Calendar.DATE,day+1);

		String nextDay=new SimpleDateFormat("yyyy-MM-dd").format(c.getTime());
		return nextDay;
	}
	
	// 获取指定的日期的前 n 天的日期
	public static String getNumberDaysBefore(Date crawlerdate, Integer number) {
		Calendar c = Calendar.getInstance();
		c.setTime(crawlerdate);

		int day=c.get(Calendar.DATE);
		c.set(Calendar.DATE,day - number);

		String targetDay = new SimpleDateFormat("yyyy-MM-dd").format(c.getTime());
		return targetDay;
	}

	/**
	 * 复制目标路径下的所有文件到目标路径下
	 * 
	 * @param path
	 *            数据的来源路径, 即你想拷贝哪个路径下面的文件
	 * @param copyPath
	 *            目标路径, 即你想把数据拷贝到什么地方
	 * @throws IOException
	 */
	public static void copy(String path, String copyPath) throws IOException {
		File file = new File(copyPath);
		File rootFile = file.getParentFile();
		if(!rootFile.exists()) {
			rootFile.mkdirs();
			file.createNewFile();
		}
		File filePath = new File(path);
		DataInputStream read;
		DataOutputStream write;
		if (filePath.isDirectory()) {
			File[] list = filePath.listFiles();
			for (int i = 0; i < list.length; i++) {
				String newPath = path + File.separator + list[i].getName();
				String newCopyPath = copyPath + File.separator + list[i].getName();
				File newFile = new File(copyPath);
				if (!newFile.exists()) {
					newFile.mkdir();
				}
				copy(newPath, newCopyPath);
			}
		} else if (filePath.isFile()) {
			read = new DataInputStream(new BufferedInputStream(new FileInputStream(path)));
			write = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
			byte[] buf = new byte[1024 * 512];
			int n = -1;
			while ((n = read.read(buf)) != -1) {
				write.write(buf, 0, n);
			}
			read.close();
			write.close();
		} else {
			//logger.error("请输入正确的文件名或路径名 ----------------> " + copyPath);
		}
	}
	
	/**
	   *  复制单个文件文件到指定的位置
	   * @param fromPath
	   * @param toPath
	   * @throws IOException
	   */
	  public static String copySingleFile(String fromPath, String toPath) throws IOException {
		  File file = new File(toPath);
		  File rootFile = file.getParentFile();
		  if(!rootFile.exists()) {
		      rootFile.mkdirs();
		      file.createNewFile();
		  }
		  File filePath = new File(fromPath);
		  DataInputStream read;
		  DataOutputStream write;
	     if (filePath.isFile()) {
	          read = new DataInputStream(new BufferedInputStream(new FileInputStream(fromPath)));
	          write = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
	          byte[] buf = new byte[1024 * 1024 * 5];
	          int n = -1;
	          while ((n = read.read(buf)) != -1) {
	              try {
					write.write(buf, 0, n);
					logger.info("copying " + fromPath + " to " + toPath);
				} catch (Exception e) {
					e.printStackTrace();
				}
	          }
	      read.close();
	      write.close();
		  } else {
			  //logger.error("请输入正确的文件名或路径名 ----------------> " + fromPath);
	      }
	      return toPath;
	  }

	public static boolean compareDate(String begindate, String enddate) {
		int begin = Integer.parseInt(begindate.replace("-", ""));
		int end = Integer.parseInt(enddate.replace("-", ""));
		return (begin - end) < 0;
	}
	
	public static String getChatnumberBypublicname(String publicname) {
		if(StringUtils.isEmpty(publicname) || !chatnumberMap.containsKey(publicname)) {
			return "temp";
		}
		return chatnumberMap.get(publicname);
	}
	
	private static List<String> list = new ArrayList<>();
	
	/**
	 * 获取指定的路径下所有的制定后缀的文件名称
	 * @param path
	 * @param suffix
	 * @return
	 */
	public static List<String> getFilesEndsWith(String path, String suffix) {

        File file = new File(path);
        if(file.exists() && file.isDirectory()) {
            File[] files = file.listFiles();
            for(File f : files) {
                if(f.isFile() && f.getAbsolutePath().toLowerCase().endsWith(suffix)) {
                	String fPath = f.getAbsolutePath().toString();
                    String pathImg = fPath.substring(fPath.lastIndexOf(File.separator) + 1, fPath.length());
                    list.add(pathImg);
                } else if(f.isDirectory()) {
                    getFilesEndsWith(f.getAbsolutePath(), suffix);
                }
            }
        }

        return list;
    }
	
	public static void main(String[] args) throws IOException {
		copy("D:\\Users\\anhuifeng\\volumes\\root\\upload\\20190719084030774", "D:\\Users\\anhuifeng");
	}

	/**
	 * 读取文件的内容生成 List 集合
	 * @return
	 */
	public static List<String> getPdfNameList() {
		List<String> list = new ArrayList<>();
		try {
			File file = new File(pdfTempTxtPath);
			if(!file.exists()) {
				file.getParentFile().mkdirs();
				file.createNewFile();
			}
			InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(file), "UTF-8");
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
			String lineStr = null;
			while((lineStr = bufferedReader.readLine()) != null){
				if(lineStr.contains("/")) {
					lineStr = lineStr.substring(lineStr.lastIndexOf("/") + 1, lineStr.length());
				}
				list.add(lineStr);
			}
			bufferedReader.close();
			inputStreamReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return list;
	}

	/**
	 * 清空临时文件
	 */
	public static void clearPdfTempText() {
		File file = new File(pdfTempTxtPath);
        try {
            if(!file.exists()) {
                file.createNewFile();
            }
            FileWriter fileWriter =new FileWriter(file);
            fileWriter.write("");
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
		logger.info("----------->" + pdfTempTxtPath + " cleaned!!!");
	}
	
	private static List<String> names = new ArrayList<>();
	
	/**
	 * 获取指定的文件夹下面的所有的文件名称的集合
	 */
	public static List<String> getFileNameUnderDirectory(String path) {
		File file = new File(path);
		if(file.exists() && file.isDirectory()) {
    		File[] files = file.listFiles();
    		
    		for(File f : files) {
    			if(f.isFile()) {
    				names.add(f.getName());
    			} else if(f.isDirectory()) {
    				getFileNameUnderDirectory(f.getAbsolutePath());
    			}
    		}
    	}
		
		return names;
	}
	
}
