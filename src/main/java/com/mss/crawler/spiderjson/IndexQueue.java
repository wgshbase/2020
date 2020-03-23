package com.mss.crawler.spiderjson;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 索引词队列
 * @author wangdw
 *
 */
public class IndexQueue {
	
	private BlockingQueue<String> queue = new LinkedBlockingQueue<String>();
	
    public void push(String index) {
        queue.add(index);
    }

    public String poll() {
        return queue.poll();
    }

}
