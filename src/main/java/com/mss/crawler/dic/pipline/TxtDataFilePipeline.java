package com.mss.crawler.dic.pipline;

import java.io.File;
import java.util.Calendar;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mss.crawler.common.AppendFile;

import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;
import us.codecraft.webmagic.utils.FilePersistentBase;

/**
 * Store results to files in JSON format.<br>
 *
 * @author code4crafter@gmail.com <br>
 * @since 0.2.0
 */
public class TxtDataFilePipeline extends FilePersistentBase implements Pipeline {

    private Logger logger = LoggerFactory.getLogger(getClass());


    public TxtDataFilePipeline() {
        setPath("D://data//webmagic");
    }

    public TxtDataFilePipeline(String path) {
        setPath(path);
    }

    @Override
    public void process(ResultItems resultItems, Task task) {
        
        StringBuilder sbPath = new StringBuilder(this.path);
        sbPath.append(task.getSite().getDomain()).append("/").append(task.getUUID()).append("/");
        
        
        String fname = "file_"+DateUtils.formatDate(Calendar.getInstance().getTime(), "yyyyMMdd");   
        String text =  resultItems.get("text");
        if(!StringUtils.isEmpty(text)){
	        StringBuilder content = new StringBuilder(text);
	        content.append(System.getProperty("line.separator"));
	        try{  
	        	 File file = new File(sbPath.toString());
	        	 if(!file.exists()){
	        		  file.mkdirs();
	        	 }
	        	 
	            String fullFileName = sbPath+fname+".txt";    
	            file = new File(fullFileName);  
	            if(!file.exists()){
	            	file.createNewFile();
	            }           
	            AppendFile.method1(fullFileName, content.toString(),"UTF8");  
	        }catch(Exception e){  
	            e.printStackTrace();
	        }  
        }
       
    }
}
