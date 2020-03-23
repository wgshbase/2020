package com.mss.crawler.spiderjson;

import us.codecraft.webmagic.Request;

/**
 * Listener of Spider on page processing. Used for monitor and such on.
 *
 * @author code4crafer@gmail.com
 * @since 0.5.0
 */
public interface SpiderNextAction {
	
    public Request getNextReq();
}
