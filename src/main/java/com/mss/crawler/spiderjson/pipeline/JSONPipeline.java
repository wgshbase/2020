package com.mss.crawler.spiderjson.pipeline;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;
import us.codecraft.webmagic.utils.FilePersistentBase;
 
/**
 * 主对象内容json形式存储管道 
 * @author wangdw
 *
 */

public class JSONPipeline extends FilePersistentBase implements Pipeline {
 
    private Logger logger = LoggerFactory.getLogger(getClass());
    public static final String JSON_LABEL = "json";
    public JSONPipeline() {
        setPath("D:/data/webmagic/");
    }
 
    public JSONPipeline(String path) {
        setPath(path);
    }
    
    @Override
    public void process(ResultItems resultItems, Task task) {
    	String fileStorePath = this.path + PATH_SEPERATOR + task.getUUID() + PATH_SEPERATOR;
        List<String> objectNames = resultItems.get("objectNames");
        if(objectNames!=null){
	        for(String objectName:objectNames){
	        	Map<String,Object> obj = (Map<String,Object>)resultItems.get(objectName);
	        	if(obj!=null){
	        		obj.put("url", resultItems.getRequest().getUrl());
	        		writeToFile(obj,fileStorePath);
	        	}
	        }
        }
    }
    
    private void writeToFile(Map<String,Object> obj,String fileStorePath){
    	String json = JSON.toJSONString(obj);
    	try {
            String filename;
            filename = fileStorePath + obj.get("filename") + ".json";
            PrintWriter printWriter = new PrintWriter(new FileWriter(getFile(filename)));
            printWriter.write(json);
            printWriter.close();
        } catch (IOException e) {
            logger.warn("write file error", e);
        }
    }
}