package com.mss.crawler.spiderjson.util.parsedoc;


import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReadFile 
{
	public static Map<String, String> loadFile(String filePath)
	{

	    Map<String, String> resultMap = new HashMap<String, String>();

		File f = new File(filePath);
		if(!f.isFile())
		{
			System.out.println("The input "+filePath+" is not a file or the file doesn't exist");
			System.exit(0);
		}
		Map<String, String> xmlMap = null;
		try {
			xmlMap = xmlToMap(f);
		} catch (Exception e) {
			e.printStackTrace();
		} 
		String content = new String();
		String title = new String();
		if(!StringUtils.isEmpty(xmlMap.get("content"))) {
			content = xmlMap.get("content").replaceAll("<.*?>", "");
			resultMap.put("content", content);
		} else {
			System.out.println("--------------------------------> no content --------------" + filePath);
		}
		if(!StringUtils.isEmpty(xmlMap.get("title"))) {
			title = xmlMap.get("title").replaceAll("<.*?>", "");
			resultMap.put("title", title);
		}

/*define your own way of loading the your file's content, the following commented two lines 
is an example of loading the content of a XML file with the Class ParseXML in the same package*/
		//ParseXML parser = new ParseXML();
		//content = parser.parseXML(filePath, "content");
		
		return resultMap;
	}


	/**
	 * 将一个文件解析为 map 的集合
	 *
	 * @param
	 * @return
	 * @throws IOException
	 * @throws DocumentException
	 */
	public static Map<String, String> xmlToMap(File file) throws IOException, DocumentException {
		Map<String, String> map = new HashMap<String, String>();
		try {
			SAXReader reader = new SAXReader();
			InputStream in = new FileInputStream(file);
			Document document = reader.read(in);
			Element root = document.getRootElement();
			List<Element> list = root.elements();
			List<Element> records = root.elements("RECODE");

			for(Element el : records) {
			    System.out.println(el.getName() + "---------" + el.getText());
			}

			for (Element e : list) {
				String str = e.getText();
				if(str.replaceFirst("<!\\[CDATA\\[", "").contains("<![CDATA[")) {
					str = "<![CDATA[" + str.replace("<![CDATA[", "").replace("]]>", "") + "]]>";
				}
				map.put(e.getName(), str);
			}
			in.close();
		} catch (Exception e) {
			System.out.println(file.getPath() + " <------------------------ BAD");
			e.printStackTrace();
		}
		return map;
	}

}
