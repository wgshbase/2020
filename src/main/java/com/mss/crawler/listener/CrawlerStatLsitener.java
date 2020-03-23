package com.mss.crawler.listener;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mss.crawler.common.FileUtils;

import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.SpiderListener;

/**
 * 爬虫统计信息存储
 * @author wwq
 *
 */
public class CrawlerStatLsitener implements SpiderListener {

	private String runInfoPath;
	
	private String domainUuid;
	
	private String crawlerStatFileSuffix = "_stats.txt";
	
	private String crawlerFailUrlSuffix = "_crawlerFail.txt";
	
	private String crawlerSuccUrlSuffix = "_succUrl.txt";
	
	private String notParseModelFileSuffix = "_NotParseModel.txt";
		
	private JSONObject crawlerStatsJson = new JSONObject();
    
    private volatile AtomicInteger successCount = new AtomicInteger(0);
    
    private volatile AtomicInteger failCount = new AtomicInteger(0);
	
    private volatile AtomicInteger dataCount = new AtomicInteger(0);
    
    public int getDataCount() {
    	return dataCount.get();
    }
    
    /**
     * 初始化文件路径
     * @param crawlerStatFilePath
     * @param crawlerFailUrlPath
     * @param crawlerSuccUrlPath
     */
	public CrawlerStatLsitener(String runInfoPath, String domainUuid) {
		this.runInfoPath = runInfoPath;
		this.domainUuid = domainUuid;
		//初始化统计信息
		initStatInfo();
	}
	
	private void initStatInfo() {
		Object obj = FileUtils.readFile(getFilePath(crawlerStatFileSuffix));
    	if(obj != null){
    		crawlerStatsJson = JSONObject.parseObject(obj.toString());
    	}
    	if(!crawlerStatsJson.isEmpty()) {
        	if(crawlerStatsJson.get("successCount") != null) {
        		successCount.set(Integer.parseInt(crawlerStatsJson.getString("successCount")));
        	}
        	if(crawlerStatsJson.get("dataCount") != null) {
        		dataCount.set(Integer.parseInt(crawlerStatsJson.getString("dataCount")));
        	}
        	if(crawlerStatsJson.get("failCount") != null) {
        		failCount.set(Integer.parseInt(crawlerStatsJson.getString("failCount")));
        	}
        }
	}
	
	private String getFilePath(String suffix) {
		return runInfoPath + FileUtils.PATH_SEPERATOR + domainUuid + suffix; 
	}
	
	public CrawlerStatLsitener() {
		
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void onSuccess(Request request) {
		crawlerStatsJson.put("successCount", successCount.incrementAndGet());
		
		//将爬取成功URL写入文件
		if("detail".equals(request.getExtra("requestType"))) {
			if("succ".equals(request.getExtra("status"))) {
				if(request.getExtra("dataSize") != null) {
					crawlerStatsJson.put("dataCount", dataCount.addAndGet(Integer.parseInt(request.getExtra("dataSize")+"")));
				} else {
					crawlerStatsJson.put("dataCount", dataCount.incrementAndGet());
				}
				FileUtils.print(JSON.toJSONString(request), getFilePath(crawlerSuccUrlSuffix), true);
			} else {
				onError(request);
			}
		} 
		
		//将爬取成功数量写入文件
		FileUtils.print(crawlerStatsJson.toString(), getFilePath(crawlerStatFileSuffix), false);
		
		if(request.getExtra("NotParseModel") != null && request.getExtra("NotParseModel") instanceof List) {
			FileUtils.printList((List)request.getExtra("NotParseModel"), getFilePath(notParseModelFileSuffix), true);
		}
	}

	@Override
	public void onError(Request request) {		
		crawlerStatsJson.put("failCount", failCount.incrementAndGet());
		//将爬取失败数量写入文件
		FileUtils.print(crawlerStatsJson.toString(), getFilePath(crawlerStatFileSuffix), false);
		//将爬取失败请求写入文件
		FileUtils.print(JSON.toJSONString(request), getFilePath(crawlerFailUrlSuffix), true);
	}
}
