package com.mss.crawler.spiderjson.downloader;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.mss.crawler.spiderjson.util.CasperjsProgramManager;
import com.mss.crawler.spiderjson.util.SpiderConstants;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.downloader.AbstractDownloader;
import us.codecraft.webmagic.selector.PlainText;

/**
 * 支持 JS 动态渲染并支持代理的下载器
 * @author wgshb
 *
 */
public class CrawlerPhantomJSDownloader extends AbstractDownloader {
    private Logger logger = LoggerFactory.getLogger(getClass());
    public Page download(Request request, Task task, String cookie) {
        Site site = null;
        if (task != null) {
            site = task.getSite();
        }
        Set<Integer> acceptStatCode;
        String domain = "";
        String userAgent = "";
        boolean isUserGzip = true;
        int retryTimes = 3;
        String charset = "utf-8";
        if (site != null) {
            acceptStatCode = site.getAcceptStatCode();
            domain = String.valueOf(site.getDomain());
//            userAgent = site.getUserAgent();
            userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.100 Safari/537.36";
            isUserGzip = site.isUseGzip();
            retryTimes = site.getRetryTimes();
            charset = site.getCharset();
//            headers = site.getHeaders();
        } else {
            acceptStatCode = Sets.newHashSet(200, 500);
        }

        logger.debug("downloading page {}", request.getUrl());
        int statusCode = 0;
        String result = null;
        try {

            List<String> paramList = new ArrayList<>();
            paramList.add(request.getUrl());
            if(!StringUtils.isEmpty(userAgent)) {
                paramList.add(URLEncoder.encode(userAgent.replaceAll(" ", "%20"), "utf-8"));
            }
            paramList.add(domain);
            paramList.add(String.valueOf(isUserGzip));
            paramList.add(String.valueOf(retryTimes));
            cookie = cookie != null ? URLEncoder.encode(cookie.replaceAll(" ", "%20"), "utf-8") : "";
            paramList.add(cookie);

            result = CasperjsProgramManager.launch("casperjsDownload.js", charset, paramList);

            statusCode = Integer.parseInt(StringUtils.substringBefore(result, "\r\n").trim());
            request.putExtra(SpiderConstants.STATUS_CODE, statusCode);
            if (statusAccept(acceptStatCode, statusCode)) {
                Page page = handleResponse(request, result);
                onSuccess(request);
                return page;
            } else {
                logger.warn("code error {}\t,{}", statusCode, request.getUrl());
                return null;
            }
        } catch (Exception e) {
            logger.warn("download page {} error {} msg {}", request.getUrl(), e, result);
            onError(request);
            return null;
        } finally {
            request.putExtra(SpiderConstants.STATUS_CODE, statusCode);
        }
    }

    protected boolean statusAccept(Set<Integer> acceptStatCode, int statusCode) {
        return acceptStatCode.contains(statusCode);
    }

    protected Page handleResponse(Request request, String content) throws IOException {
        Page page = new Page();
        page.setRawText(content);
        page.setUrl(new PlainText(request.getUrl()));
        page.setRequest(request);
        page.setStatusCode(200);
        return page;
    }

    @Override
    public Page download(Request request, Task task) {
        return download(request, task, null);
    }

    @Override
    public void setThread(int threadNum) {

    }
}
