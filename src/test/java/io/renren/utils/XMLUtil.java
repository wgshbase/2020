package io.renren.utils;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**   
 * 封装了XML转换成object，object转换成XML的代码   
 *    
 * @author mscall
 *    
 */  
public class XMLUtil {
	/**   
     * 将对象直接转换成String类型的 XML输出   
     *    
     * @param obj   
     * @return   
     */    
    public static String convertToXml(Object obj) {    
        // 创建输出流    
        StringWriter sw = new StringWriter();    
        try {    
            // 利用jdk中自带的转换类实现    
            JAXBContext context = JAXBContext.newInstance(obj.getClass());    
    
            Marshaller marshaller = context.createMarshaller();    
            // 格式化xml输出的格式    
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,    
                    Boolean.TRUE);    
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8"); 
            // 将对象转换成输出流形式的xml    
            marshaller.marshal(obj, sw);    
        } catch (JAXBException e) {    
            e.printStackTrace();    
        }    
        return sw.toString();    
    }    
    
    /**   
     * 将对象根据路径写入指定的xml文件里
     *    
     * @param obj   
     * @param path   
     * @return   
     */    
    public static void convertToXml(Object obj, String path) {    
        try {    
            // 利用jdk中自带的转换类实现    
            JAXBContext context = JAXBContext.newInstance(obj.getClass());    
    
            Marshaller marshaller = context.createMarshaller();    
            // 格式化xml输出的格式    
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,    
                    Boolean.TRUE);    
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "GBK");
            // 将对象转换成输出流形式的xml    
            // 创建输出流    
            FileWriter fw = null;    
            try {    
                fw = new FileWriter(path);    
            } catch (IOException e) {    
                e.printStackTrace();    
            }    
            marshaller.marshal(obj, fw);    
        } catch (JAXBException e) {    
            e.printStackTrace();    
        }    
    }    
    
    /**   
     * 将String类型的xml转换成对象   
     */    
    public static Object convertXmlStrToObject(Class<?> clazz, String xmlStr) {    
        Object xmlObject = null;    
        try {
            JAXBContext context = JAXBContext.newInstance(clazz);    
            // 进行将Xml转成对象的核心接口    
            Unmarshaller unmarshal = context.createUnmarshaller();
            StringReader sr = new StringReader(xmlStr);    
            xmlObject = unmarshal.unmarshal(sr);
        } catch (Exception e) {
            e.printStackTrace();    
        }    
        return xmlObject;    
    }    
    
    /**   
     * 将file类型的xml转换成对象   
     */    
    public static Object convertXmlFileToObject(Class<?> clazz, String xmlPath) {    
        Object xmlObject = null;    
        try {
            JAXBContext context = JAXBContext.newInstance(clazz);    
            Unmarshaller unmarshaller = context.createUnmarshaller();      
            InputStreamReader isr=new InputStreamReader(new FileInputStream(xmlPath),"GBK");
            xmlObject = unmarshaller.unmarshal(isr);    
        } catch (Exception e) {    
            e.printStackTrace();    
        }    
        return xmlObject;    
    }   
}
