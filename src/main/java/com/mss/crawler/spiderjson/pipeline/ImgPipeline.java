package com.mss.crawler.spiderjson.pipeline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import com.mss.crawler.spiderjson.util.BadFormatImage;
import org.apache.commons.lang3.StringUtils;
import org.json.HTTP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mss.crawler.spiderjson.ResourceFile;
import com.mss.crawler.spiderjson.util.HttpFileUtil;

import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;
import us.codecraft.webmagic.utils.FilePersistentBase;
 
 
/**
 * 图片文件下载
 * @author wangdw
 *
 */

public class ImgPipeline extends FilePersistentBase implements Pipeline {
 
    private Logger logger = LoggerFactory.getLogger(getClass());

    public ImgPipeline() {
        setPath("D:/data/webmagic/");
    }
 
    public ImgPipeline(String path) {
        setPath(path);
    }
 
    @Override
    public void process(ResultItems resultItems, Task task) {
    	String fileStorePath = this.path + PATH_SEPERATOR + task.getSite().getDomain() + PATH_SEPERATOR;
            List<ResourceFile> rfs = (List<ResourceFile>)resultItems.get("resources");
            if(rfs!=null){
            	 Map<String,Object> resultMap = new HashMap<>();
            	 // 包含错误图片类型的集合
	            List<String> oldTypeImages = new ArrayList<>();
	            // 包含真实图片类型的集合
	            List<String> realTypeImages = new ArrayList<>();
	            // 包含图片的尺寸超限的集合
	            List<BadFormatImage> badFormatImages = new ArrayList<>();
	            for (ResourceFile entry : rfs) {
	                String extName=entry.getFileExtName();
	                String newFileName = entry.getNewFileName();
	                StringBuffer imgFileNameNew = new StringBuffer(fileStorePath);
	                imgFileNameNew.append(entry.getRelativePath());
	                imgFileNameNew.append(PATH_SEPERATOR);
	                imgFileNameNew.append(newFileName);	            
	                entry.setRealPath(imgFileNameNew.toString());
	                //这里通过httpclient下载之前抓取到的图片网址，并放在对应的文件中
	                try {
	                	String downUrl = HttpFileUtil.formatDownloadUrl(entry);
	                	resultMap = HttpFileUtil.getInstance().getFileTo(downUrl, imgFileNameNew.toString(), resultMap);
	                	if(resultMap.size() > 0) {
	                		if(!StringUtils.isEmpty((String)resultMap.get("oldTypeImage"))) {
								 if(!oldTypeImages.contains((String)resultMap.get("oldTypeImage"))) {
									 System.out.println("----------------> " + (String)resultMap.get("oldTypeImage") + " is bad type!");
								 	oldTypeImages.add((String)resultMap.get("oldTypeImage"));
									 realTypeImages.add((String)resultMap.get("realTypeImage"));
								 }
			                }

			                if(null != resultMap.get("badFormatImage")) {
				                	badFormatImages.add((BadFormatImage) resultMap.get("badFormatImage"));
			                }
	                	}
	                } catch (IOException e) {
		                e.printStackTrace();
	                }
	            }
	            if(oldTypeImages.size() > 0) {
		            resultItems.put("oldTypeImages", oldTypeImages);
		            resultItems.put("realTypeImages",realTypeImages);
	            }
	            if(badFormatImages.size() > 0) {
	            	resultItems.put("badFormatImages", badFormatImages);
	            }
            }
      
    }
}