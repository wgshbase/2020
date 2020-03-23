package com.mss.crawler.spiderjson.util;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Semaphore;

/**
 * 管理Casperjs的启动和执行
 * Created by ucs_yuananyun on 2016/6/23.
 */
@Component
public class CasperjsProgramManager {


    private static String casperJsPath;

    private static ResourceBundle resource = ResourceBundle.getBundle("casperjs");

    static{
        casperJsPath = resource.getString("path");
    }

    private static Logger logger = LoggerFactory.getLogger(CasperjsProgramManager.class);
    public static String launch(String jsFileName, List<String> params) {
        return launch(jsFileName, null, params);
    }
    public static String launch(String jsFileName, String pageCharset,List<String> params) {
        if (StringUtils.isBlank(jsFileName)) {
            logger.error("待执行的js文件名不能为空！");
            return null;
        }
        BufferedReader br = null;
        try {
            if(pageCharset==null) pageCharset = "utf-8";
//            String path = CasperjsProgramManager.class.getResource("/").getPath();
//            path = path.substring(1, path.lastIndexOf("/") + 1);
            /*String path = System.getProperty("user.dir");
            if(!path.endsWith(File.separator)) {
            	path = path + File.separator;
            }*/

            String path = casperJsPath;
            // 操作系统
            String os = System.getProperties().getProperty("os.name");
            
            String casperJsPath = "";
            String phantomJsPath = "";
            if (StringUtils.startsWithIgnoreCase(os, "win")) {
                casperJsPath = path + "casperjs/bin/casperjs.exe";
                phantomJsPath = path + "phantomjs/window/phantomjs.exe";
            } else {
                path = "/" + path;
                casperJsPath = path + "casperjs/bin/casperjs";
                phantomJsPath = path + "phantomjs/linux/phantomjs";
            }
            logger.info("CasperJs程序地址:{}", casperJsPath);
            ProcessBuilder processBuilder = new ProcessBuilder(casperJsPath, jsFileName);
            if (params != null) {
                for (String param : params) {
                    processBuilder.command().add(param);
                }
            }
            processBuilder.command().add("--output-encoding="+pageCharset);
            processBuilder.command().add("--web-security=no");
            processBuilder.command().add("--ignore-ssl-errors=true");

            processBuilder.directory(new File(path + "casperjs/js"));
            processBuilder.environment().put("PHANTOMJS_EXECUTABLE", phantomJsPath);

            Process p = processBuilder.start();
            InputStream is = p.getInputStream();
            br = new BufferedReader(new InputStreamReader(is, pageCharset));
            StringBuffer sbf = new StringBuffer();
            String tmp = "";
            while ((tmp = br.readLine()) != null) {
//                System.out.println("this:" + tmp);
                sbf.append(tmp).append("\r\n");
                /*if(!org.springframework.util.StringUtils.isEmpty(sbf.toString())) {
                	// System.out.println("--------->" + sbf.toString());
                	break;
                }*/
            }
            br.close();
            p.destroy();
            return sbf.toString();
        } catch (IOException ex) {
            logger.error(ex.getMessage());
            return null;
        } finally {
            if(br != null)
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

}
