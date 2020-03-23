package com.mss.crawler.spiderjson.scheduler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mss.crawler.spiderjson.model.WeixinModelPageProcessor;

/**
 * 重复主键去重过滤器
 * @author wangdw
 *
 */
public class FileCacheDoubleRemvoeFilter{
	
	protected Logger logger = LoggerFactory.getLogger(WeixinModelPageProcessor.class);
	
	private String filePath = System.getProperty("java.io.tmpdir");

	//持久化文件名
    private String fileUrlAllName = "urls.txt";

    private PrintWriter fileUrlWriter;

    private AtomicBoolean inited = new AtomicBoolean(false);

    private HashSet<String> urls;
    
    public FileCacheDoubleRemvoeFilter(String filePath) {
        if (!filePath.endsWith("/") && !filePath.endsWith("\\")) {
            filePath += "/";
        }
        this.filePath = filePath;
        this.init();
	}

    private void flush() {
        fileUrlWriter.flush();
    }

    private void init() {
        File file = new File(filePath);
        if (!file.exists()) {
            file.mkdirs();
        }
        readFile();
        initWriter();
        inited.set(true);
        logger.info("init cache scheduler success");
    }

    private void initWriter() {
        try {
            fileUrlWriter = new PrintWriter(new FileWriter(getFileName(fileUrlAllName), true));
        } catch (IOException e) {
            throw new RuntimeException("init cache scheduler error", e);
        }
    }

    private void readFile() {
        try {
            urls = new LinkedHashSet<String>();
            readUrlFile();
            // initDuplicateRemover();
        } catch (FileNotFoundException e) {
            //init
            logger.info("init cache file " + getFileName(fileUrlAllName));
        } catch (IOException e) {
            logger.error("init file error", e);
        }
    }

    private void readUrlFile() throws IOException {
        String line;
        BufferedReader fileUrlReader = null;
        try {
            fileUrlReader = new BufferedReader(new FileReader(getFileName(fileUrlAllName)));
            while ((line = fileUrlReader.readLine()) != null) {
                urls.add(line.trim());
            }
        } finally {
            if (fileUrlReader != null) {
                IOUtils.closeQuietly(fileUrlReader);
            }
        }
    }
	    
    public void close() throws IOException {
		fileUrlWriter.close();
	}

    private String getFileName(String filename) {
        return filePath + filename;
    }

    /**
     * 判断是否包含主键
     * @param key
     * @return
     */
    public boolean isContains(String key){
    	return this.urls.contains(key);
    }
    /**
     * 将主键压入堆栈
     * @param key
     */
    public void push(String key){
    	this.urls.add(key);
    	this.fileUrlWriter.println(key);
    	this.flush();
    }
}
