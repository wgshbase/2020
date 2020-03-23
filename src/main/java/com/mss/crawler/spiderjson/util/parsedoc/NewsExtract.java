package com.mss.crawler.spiderjson.util.parsedoc;

import com.baidu.aip.nlp.AipNlp;

import java.io.*;
import java.util.*;

import org.springframework.util.StringUtils;

/**
 * Created by wgshb on 2018/7/20 16:15
 */
public class NewsExtract {

    //设置APPID/AK/SK
    public static final String APP_ID = "10282489";
    public static final String API_KEY = "gcYKYxPNb1aL7jCVBQU1qDwn";
    public static final String SECRET_KEY = "uvzoVop4fLxmmU9jlgGbVDTrLXPx8fKN";
    public static AipNlp CLIENT;

    static {
        // 初始化一个AipNlp
        CLIENT = new AipNlp(APP_ID, API_KEY, SECRET_KEY);
        // 可选：设置网络连接参数
        CLIENT.setConnectionTimeoutInMillis(2000);
        CLIENT.setSocketTimeoutInMillis(60000);
    }


    public static Set<String> readText2Set(String filePath){
        Set<String> stopWordSet = new HashSet<String>();

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath),"GBK"));

            String line = "";
            while(!StringUtils.isEmpty(line = reader.readLine())) {
                stopWordSet.add(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stopWordSet;
    }
}


