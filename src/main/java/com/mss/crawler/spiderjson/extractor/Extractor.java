package com.mss.crawler.spiderjson.extractor;

import us.codecraft.webmagic.selector.Selector;

/**
 * The object contains 'ExtractBy' information.
 * @author code4crafter@gmail.com <br>
 * @since 0.2.0
 */
public class Extractor {

    protected Selector selector;

    protected final Source source;

    protected final boolean notNull;

    protected final boolean multi;

    public enum Source {Html, Url, RawHtml, RawText, NewsText, DateText, Regex, Json, HtmlText, movie}

    public Extractor(Selector selector, Source source, boolean notNull, boolean multi) {
        this.selector = selector;
        this.source = source;
        this.notNull = notNull;
        this.multi = multi;
    }

    Selector getSelector() {
        return selector;
    }

    Source getSource() {
        return source;
    }

    boolean isNotNull() {
        return notNull;
    }

    boolean isMulti() {
        return multi;
    }

    void setSelector(Selector selector) {
        this.selector = selector;
    }
}
