package com.mss.crawler.spiderjson.model;

import java.io.Serializable;

public class ListContentModel implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * 新闻标题
     */
    private String title;
    /**
     * 新闻的链接
     */
    private String url;
    /**
     * 新闻的发布日期
     */
    private String pubdate;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPubdate() {
        return pubdate;
    }

    public void setPubdate(String pubdate) {
        this.pubdate = pubdate;
    }
}
