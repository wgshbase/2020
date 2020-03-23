package io.renren.modules.spider.utils;

import org.apache.log4j.Logger;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.springframework.util.StringUtils;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Author wgsh
 * @Date wgshb on 2018/12/24 9:34
 */
public class XMLUtils {

	private static Logger logger = Logger.getLogger(CSVUtils.class);

	/**
	 * 将数据库的记录转化为 xml 文件
	 * @param identifyCode 导出的目标标识符
	 * @param dataList 导出的数据
	 * @param outPutPath 目标路径
	 * @param filename 导出的文件名称
	 * @param removeList 黑名单需要移出的数据
	 * @param fieldsProperties
	 * @return
	 */
	public static Map<String,Set<String>> createXMLFile(String identifyCode, List<LinkedHashMap<String, Object>> dataList, String outPutPath, String filename, List<String> removeList, Map<String, String> fieldsProperties) {
		FileOutputStream out = null;
		XMLWriter writer = null;
		Map<String, Set<String>> extra = new HashMap<>();
		Set<String> attachfiles = new HashSet<>();
		Set<String> pdffiles = new HashSet<>();
		Set<String> headimg = new HashSet<>();
		try {

			File xmlFile = new File(outPutPath + File.separator + filename);
			if(!xmlFile.exists()) {
				xmlFile.getParentFile().mkdirs();
				xmlFile.createNewFile();
			}

			out = new FileOutputStream(xmlFile.getAbsolutePath());
			OutputFormat format = OutputFormat.createPrettyPrint();
			format.setEncoding("utf-8");
			writer = new XMLWriter(out, format);

			org.dom4j.Document doc = DocumentHelper.createDocument();
			// 1. 创建根节点
			org.dom4j.Element root = doc.addElement("entities");

			// 写入 content
			if (dataList != null && !dataList.isEmpty()) {
				// dataList 代表所有的数据项, 而对应的 entities 代表的是一个对象, 即一条数据库的记录
				for (LinkedHashMap<String, Object> entities : dataList) {
					boolean ignoreFlag = false;
					String entityTitle = (String) entities.get("title");
					// 对于部分正文部分内容极少或没有的数据, 不予处理
					String entityContent = (String) entities.get("content");

					String id = (String)entities.get("id");
					if(null != removeList && removeList.size() > 1) {
						if(removeList.contains(id)) {
							logger.warn("---------------->" + id + "is Duplicate news!!!");
							System.out.println("==========================================================================================---------------->" + id + "is Duplicate news!!!");
							ignoreFlag = true;
						}
					}

					for(String blackword : CSVUtils.blackList) {
						if(entityTitle.contains(blackword) || entityContent.contains(blackword)) {
							logger.warn("-------------->" + entityTitle + "is in the blackList");
							ignoreFlag = true;
						}
					}
					if(ignoreFlag) continue;

					if(entityContent == null || entityContent.replaceAll("<(.*?)>", "").trim().length() < 100) {
						continue;
					}

					// 添加单一的元素
					Element object = root.addElement("entity");

					String tbname = filename.substring(0, filename.indexOf("-"));
					// 1. 获取所有的数据
					for (Map.Entry<String, Object> entity : entities.entrySet()) {
						// 处理附件名, 即处理图片
						if ("attchfiles".equals(entity.getKey()) && !StringUtils.isEmpty(entity.getValue())) {
							if(!StringUtils.isEmpty(identifyCode) && identifyCode.equalsIgnoreCase("jh")) {
								CSVUtils.addAttachFiles(entity.getKey(), attachfiles, extra, ((String)entity.getValue()).replace("${RootPath}", ""));
							} else {
								CSVUtils.addAttachFiles(entity.getKey(), attachfiles, extra, ((String)entity.getValue()));
							}
						}
						// 处理 pdf 附件
						if("pdffiles".equals(entity.getKey()) && !StringUtils.isEmpty(entity.getValue())) {
							CSVUtils.addAttachFiles(entity.getKey(), pdffiles, extra, (String)entity.getValue());
							String entityVal = (String) entity.getValue();
							entityVal = "/wxgzh" + entityVal.substring(entityVal.lastIndexOf("/"), entityVal.length());
							entities.put(entity.getKey(), entityVal);
						}
						if("headimg".equals(entity.getKey()) && !StringUtils.isEmpty(entity.getValue()) && tbname.contains("wx")) {
							CSVUtils.addAttachFiles(entity.getKey(), headimg, extra, (String)entity.getValue());
							String entityVal = (String) entity.getValue();
							entityVal = "//upload/image/20180329" + entityVal.replace("${RootPath}", "");
							entities.put(entity.getKey(), entityVal);
						}

						if (entity.getValue() instanceof Date) {
							String entityDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(entity.getValue());
							entities.put(entity.getKey(), entityDate);
						}
						// 字段处理
						for(Map.Entry<String, String> entry : fieldsProperties.entrySet()) {
							//
							if(fieldsProperties.containsValue(entity.getKey())) {
								if(entry.getValue().endsWith(entity.getKey())) {
									Element field = object.addElement(entry.getKey());
									if(null != entity.getValue() && ((String)entity.getValue()).contains("${RootPath}")) {
										field.addCDATA(((String)entity.getValue()).replace("${RootPath}",""));
									} else {
										field.addCDATA((String)entity.getValue());
									}
									break;
								}

							}
						}
					}

				}

			}
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

		return extra;

	}
}
