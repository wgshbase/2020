package com.mss.crawler.spiderjson.util;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;

/**
* Author: WuLC
* Date:   2016-05-25 09:18:09
* Last modified by:   WuLC
* Last Modified time: 2016-05-25 14:50:52
* Email: liangchaowu5@gmail.com
******************************************************
* Function: combine TextRank and TF-IDF to extract keywords 
* Input: path of the directory of the corpus
* Output: keywords extracted for each document
*/


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.junit.Test;

import com.alibaba.fastjson.JSON;
import com.mss.crawler.spiderjson.util.parsedoc.ReadDir;
import com.mss.crawler.spiderjson.util.parsedoc.ReadFile;
import com.mss.crawler.spiderjson.util.parsedoc.TFIDF;
import com.mss.crawler.spiderjson.util.parsedoc.TextRank;


/**
 *  新闻关键字的抽取的步骤
 *  1. 生成对应的 语料 corpus XXX.xml 文件
 *  2. 该 xml 借助 对应的 corpus.txt 生成对应的关键字并添加到 新闻的 keywords 字段中
 * @author wgshb
 *
 */
public class KeywordsUtils 
{
	private static String basepath = System.getProperty("user.dir") + File.separator;
	// 项目启动的时候预加载这个数据, 作为类似常量的参考
	private static HashMap<String, Float> dirFilesIDF = (HashMap<String, Float>) readTxtFile2Map(basepath +  "corpus.txt");
	
	/**
	 * 根据对应的新闻的标题和正文生成对应的关键字
	 * 
	 */
	public static String getKeywordsByTitleAndContent(String title, String content) {
		// 生成对应的语料
		boolean  flag = createXmlFile(title, content);
		
		// 生成的语料结合总的 corpus 集合生成对应的 关键字
		if(flag) {
			Map<String, List<String>> fileKeywords = textRankTFIDFVoteDir(dirFilesIDF,basepath + "test" + File.separator + DigestUtils.md5Hex(JSON.toJSONString(title.trim())) + ".xml");
			// 删除生成的临时文件
			deleteFile(basepath + "test" + File.separator + DigestUtils.md5Hex(JSON.toJSONString(title.trim())) + ".xml");
			for(Map.Entry<String, List<String>> entry : fileKeywords.entrySet()) {
				System.out.println("-------------------" + entry.getKey() + "====" + entry.getValue());
				String keywords = formatTags(entry.getValue().toString());

				return keywords;
			}
		}
		
		return "";
	}

	/**
	 * 通过文件路径删除对应的文件
	 * @param fileName
	 * @return
	 */
	public static boolean deleteFile(String fileName) {
		File file = new File(fileName);
		// 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
		if (file.exists() && file.isFile()) {
			if (file.delete()) {
				System.out.println("删除单个文件" + fileName + "成功！");
				return true;
			} else {
				System.out.println("删除单个文件" + fileName + "失败！");
				return false;
			}
		} else {
			System.out.println("删除单个文件失败：" + fileName + "不存在！");
			return false;
		}
	}

	// 根据 id 生成包含新闻的 title 和 content 的 xml 语料, 用于产生关键字
	private static boolean createXmlFile(String title, String content) {
		
		String targetPath = basepath + "test" + File.separator;
		// 文件不存在直接创建
		if(!new File(targetPath).exists()) {
			new File(targetPath).mkdirs();
		}
		// 清空目标文件夹下面的所有文件
		//removeBeforeCreate(targetPath);
		
		String id = DigestUtils.md5Hex(JSON.toJSONString(title.trim()));
		Document doc = DocumentHelper.createDocument();//创建document
        Element articleEle = doc.addElement("article");//添加根元素
        articleEle.addComment("文档的根article已经创建。");//添加注释
        articleEle.addElement("id").addText(id);
        articleEle.addElement("title").addText(title);
        articleEle.addElement("content").addCDATA(content.replaceAll("<(.*?)>", "").replaceAll("</>", "").replace("]]>", "").replace("<![CDATA[", ""));

        try {
            OutputFormat format = OutputFormat.createPrettyPrint();
            format.setEncoding("UTF-8");
            
        	XMLWriter writer = new XMLWriter(new FileWriter(new File(targetPath + id + ".xml")),format);
        	writer.write(doc);
        	writer.flush();
        	writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
		return true;
	}

	// 生产新的预料参考文件
	@Test
	public void testCreateNewCorpus() {
		HashMap<String, Float> dirFilesIDF = TFIDF.idfForDir(basepath + "corpus");
		saveMap2Test(dirFilesIDF, basepath + "corpus.txt");
	}
	
	// 读取本地文件到 map 集合
	public static Map<String,Float> readTxtFile2Map(String filePath) {

		//存放内容的map对象
		Map<String,Float> filemaps = new HashMap<String,Float>();
		try {
			File file = new File(filePath);
			if (file.isFile() && file.exists()) { // 判断文件是否存在
				InputStreamReader read = new InputStreamReader(new FileInputStream(file));// 考虑到编码格式
				BufferedReader bufferedReader = new BufferedReader(read);
				String lineTxt = null;
				while ((lineTxt = bufferedReader.readLine()) != null) {//按行读取
					// System.out.println(“lineTxt=” + lineTxt);
					if(!"".equals(lineTxt)){
					String key = lineTxt.split(":")[0];//对行的内容进行分析处理后再放入map里。
					Float value = Float.parseFloat(lineTxt.split(":")[1]);//对行的内容进行分析处理后再放入map里。
					// System.out.println(reds);
					filemaps.put(key, value);//放入map
				}
			}
				read.close();//关闭InputStreamReader 
				bufferedReader.close();//关闭BufferedReader 
			} else {
				System.out.println("找不到指定的文件");
			}
		} catch (Exception e) {
			System.out.println("读取文件内容出错");
			e.printStackTrace();
		}
		return filemaps;
	}

	// 保存 map 到本地的文件
	private static String saveMap2Test(HashMap<String, Float> map, String filePath) {
		OutputStreamWriter outFile = null;
		FileOutputStream fos = null;

		try {
			fos = new FileOutputStream(filePath);
			outFile = new OutputStreamWriter(fos);
			StringBuilder sb = new StringBuilder();
			for (String key : map.keySet()) {
				sb.append(key + ":" + map.get(key) + "\n");
			}
			outFile.write(sb.toString());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				outFile.flush();
				outFile.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return null;
	}
	
	/**
	 * 对于关键字进行进一步的处理
	 * @param keywords
	 * @return
	 */
	public static String formatTags(String keywords) {
		if(keywords.startsWith("[")) {
			keywords = keywords.substring(1, keywords.length());
		}
		
		if(keywords.endsWith("]")) {
			keywords = keywords.substring(0, keywords.length() - 1);
		}
		
		keywords = keywords.replace("@", " ").replace("&", " ").replace("nbsp", "").replace("#", " ").replace("_", "").replace("'", "").replace("$", "").trim();
		
		
		StringBuilder sb = new StringBuilder();
		String[] split = keywords.split(", ");
		
		Set<String> temps = new LinkedHashSet<String>();
		
		for(String spl : split) {
			temps.add(spl.trim());
		}
		
		for(String temp : temps) {
			if(temp.matches("(.*?)[a-z]+(.*?)") || temp.length() == 1 || temp.endsWith("-") || temp.startsWith("-")) {
				temp = "";
			} 
			if(temp != "") {
				sb.append(temp + ";");
			}
		}
		
		String result = sb.toString();
		
		while(result.contains(";;")) {
			result = result.replace(";;", ";");
		}
		
		if(result.endsWith(";")) {
			result = result.substring(0, result.length() - 1);
		} 
		if(result.startsWith(";")) {
			result = result.substring(1, result.length());
		}
		return result;
	}

	private static int keywordsNumber = 5;
	private static int keywordCandidateNum = 10;
	
	/**
	 * set the number of keywords to extract 
	 * @param number(int): number of keywords to extract 
	 */
	public static void setKeywordsNumber(int number)
	{
		keywordsNumber = number;
		keywordCandidateNum = 2 * number;
	}
	
   
	/**
	 * integrate the results generated by TextRank and TF-IDF, choose those words that co-occure in both 
	 * results, if the number of co-occuring words is not enough, choose the left part from the results of TF-IDF
	 * @param dirPath(String): path of the directory of the corpus
 	 * @return(Map<String,List<String>>): keywords of each document of the corpus
	 */
	public static Map<String,List<String>> textRankTFIDFVoteDir(HashMap<String, Float> dirFilesIDF,String dirPath)
	{	
	
		Map<String, List<String>> result = new HashMap<String,List<String>>();
		// 返回指定路径下的所有的语料
		List<String> fileList = ReadDir.readDirFileNames(dirPath);
		Map<String, String> resultmap = new HashMap<String, String>();
		int count =0;
		for(String file:fileList)
		{
			//System.out.println("    count:" + (count++));
			try {
				resultmap = ReadFile.loadFile(file);
				String content = resultmap.get("content");
				content = content.replace("————————————————————本文来源于国外网站,以下是机器翻译内容,向下阅读可查看原文————————————————————", "");
				if(content.contains("————————————————————原文————————————————————")) {
					content = content.substring(0, content.indexOf("————————————————————原文————————————————————"));
				}
				String title = resultmap.get("title");
				List<String> keywords= textRankTFIDFVote(dirFilesIDF,title, content);
				result.put(file, keywords);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	/**
	 * integrate the results generated by TextRank and TF-IDF, choose those words that co-occure in both 
	 * results, if the number of co-occuring words is not enough, choose the left part from the results of TF-IDF
	 * @param (
	 * @return(Map<String,List<String>>): keywords of each document of the corpus
	 */
	public static List<String> textRankTFIDFVote(HashMap<String, Float> dirFilesIDF,String title, String content)
	{	
		
		TFIDF.setKeywordsNumber(keywordCandidateNum);
		Map<String,Float> tfidfKeywords = TFIDF.getTFIDF(title, content,dirFilesIDF);
		
		List<String>  trKeyword = TextRank.getKeyword(title,content);
		
			List<String> temp = new ArrayList<String>();
			for(String keyword:tfidfKeywords.keySet())
			{
				if (trKeyword.contains(keyword) /*&& (words.contains(keyword))*/) {
					temp.add(keyword);
				}
				if (temp.size()== keywordsNumber)
					break;
			}
			if (temp.size()!= keywordsNumber){
				for(String keyword:tfidfKeywords.keySet())
				{
					if (!temp.contains(keyword) /*&& (words.contains(keyword))*/) {
						temp.add(keyword);
					}
					if (temp.size()>=keywordsNumber)
						break;
				}
			}
	
		return temp;
	}

}
