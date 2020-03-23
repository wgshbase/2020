package io.renren.modules.spider.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface INlpService {
	/**
	 * 	人名发布 
	 * @param list
	 * @return
	 */
	boolean  personReload(List<String> list);
	/**
	 * 	地名发布 
	 * @param list
	 * @return
	 */
	boolean  placeReload(List<String> list);
	/**
	 * 	机构发布 
	 * @param list
	 * @return
	 */
	boolean  organReload(List<String> list);
	/**
	 * 	关键词发布 
	 * @param list
	 * @return
	 */
	boolean  kewordReload(List<String> list);
    
	/** 
     * 词法分析 
     *
     * */
    HashMap<String, Object> lexer(String text);
    
    /**
     * 实体识别
     * 自动抽取人名地名机构名
     * @param text
     * @return
     */
    Map<String, Set<String>>  recognize(String text);
    Map<String, Set<String>>  recognizeAndMaxword(String text);

    /** 自动摘要 */
    Map<String, Object> autoSummary(String text);

    /** 情感倾向分析 */
    Map<String, Object> sentimentClassify(String text);

    /** 关键词自动抽取 */
    HashMap<String, Object> keyword(String title, String content);
    
    HashMap<String, Object> keyword(String content);

    /** 文本分类 */
    Map<String, Object> classify(String title, String content);
    
}
