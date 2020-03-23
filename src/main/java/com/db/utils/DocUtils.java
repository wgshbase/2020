package com.db.utils;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import sun.misc.BASE64Encoder;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class DocUtils {

    private Configuration configuration = null;

    public DocUtils() {
        configuration = new Configuration();
        configuration.setDefaultEncoding("utf-8");
    }

    public void createDocByFos(Map<String,Object> dataMap, OutputStream fos) throws UnsupportedEncodingException {
        //dataMap 要填入模本的数据文件
        //设置模本装置方法和路径,FreeMarker支持多种模板装载方法。可以重servlet，classpath，数据库装载，
        //这里我们的模板是放在template包下面
        configuration.setClassForTemplateLoading(this.getClass(), "/static/template");
        Template t = null;
        try {
            //test.ftl为要装载的模板
            t = configuration.getTemplate("2.ftl");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Writer out = null;
        OutputStreamWriter oWriter = new OutputStreamWriter(fos, "UTF-8");
        //这个地方对流的编码不可或缺，使用main（）单独调用时，应该可以，但是如果是web请求导出时导出后word文档就会打不开，并且包XML文件错误。主要是编码格式不正确，无法解析。
        //out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile)));
        out = new BufferedWriter(oWriter);

        try {
            t.process(dataMap, out);
            out.close();
            fos.close();
        } catch (TemplateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //System.out.println("---------------------------");
    }

    public void createDoc(Map<String,Object> dataMap, String fileName) throws UnsupportedEncodingException {
        //dataMap 要填入模本的数据文件
        //设置模本装置方法和路径,FreeMarker支持多种模板装载方法。可以重servlet，classpath，数据库装载，
        //这里我们的模板是放在template包下面
        configuration.setClassForTemplateLoading(this.getClass(), "/static/template");
        Template t = null;
        try {
            //test.ftl为要装载的模板
            t = configuration.getTemplate("2.ftl");
        } catch (IOException e) {
            e.printStackTrace();
        }
        //输出文档路径及名称
        File outFile = new File(fileName);
        Writer out = null;
        FileOutputStream fos = null;
        try {
            if(!outFile.getParentFile().exists()) {
                outFile.getParentFile().mkdirs();
            }
            if(!outFile.exists()) {
                outFile.createNewFile();
            }
            fos = new FileOutputStream(outFile);
            OutputStreamWriter oWriter = new OutputStreamWriter(fos, "UTF-8");
            //这个地方对流的编码不可或缺，使用main（）单独调用时，应该可以，但是如果是web请求导出时导出后word文档就会打不开，并且包XML文件错误。主要是编码格式不正确，无法解析。
            //out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile)));
            out = new BufferedWriter(oWriter);
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            t.process(dataMap, out);
            out.close();
            fos.close();
        } catch (TemplateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //System.out.println("---------------------------");
    }

    /**
     * 由于word中图片为base64编码， 故将图片转化为base64编码后的字符串
     * @return String
     */
    public static String getImageStr(String imgFile) {
        // String imgFile = new File(this.getClass().getResource("/").getPath())
        // + "/com/export/doc/1.jpg";
        InputStream in = null;
        byte[] data = null;
        try {
            // 创建照片的字节输入流
            in = new FileInputStream(imgFile);
            // 创建一个长度为照片总大小的内存空间
            data = new byte[in.available()];
            // 从输入流中读取文件大小的字节，并将其存储在缓冲区数组 data中
            in.read(data);
            // 关闭输入流
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        BASE64Encoder encoder = new BASE64Encoder();
        // 进行base64加密
        return encoder.encode(data);
    }

    public static void main(String[] args) throws UnsupportedEncodingException {
        Map<String,Object> dataMap = new HashMap<>();
        dataMap.put("pubdate", "文件夹监测任务");
        dataMap.put("url", "保密部");
        dataMap.put("attachfiles", "201907112006");
        dataMap.put("filepath", "HYPERLINK \"F:\\\\111\\\\2019-057.pdf\"");
        dataMap.put("titleTr", 100);
        dataMap.put("contentTr", 2);
        dataMap.put("title", 1);
        dataMap.put("content", 3);

        DocUtils docUtils = new DocUtils();
        docUtils.createDoc(dataMap, "D:/2.doc");


        System.out.println("123123.txt".substring("123123.txt".lastIndexOf(".") + 1));
    }

}
