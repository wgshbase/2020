package com.mss.crawler.spiderjson.extractor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.mss.crawler.model.DomModel;
import com.mss.crawler.spiderjson.config.InterFileExtractorConfig;
import com.mss.crawler.spiderjson.util.ExtractorUtils;

import us.codecraft.webmagic.selector.Selector;


/**
 * 对象的属性列抽取器
 * @author wangdw <br>
 * @since 0.2.0
 */
public class FieldExtractor extends Extractor {
	
	public static String  EXCLUDEEXPTYPE_CSSQUERY="cssquery";
	
	public static String  EXCLUDEEXPTYPE_REGEX="regex";
	
	public static String  EXCLUDEEXPTYPE_XPATH="xpath";

	/**
	 * 视频类型
	 */
	private String videoType;

	/**
	 * 视频名称
	 */
	private String videoTitle;

	/**
	 * 抽取表达式
	 */
	private String fieldExtractExp;
	
	private String domain;

	/**
	 * 列名称
	 */
    private String fieldName;
    
    /**
     * 排除区域表达式，用于剔除广告等不需要内容的区域
     */
    private Map<String,List<String>> excludeExp;
    
    /**
	 * dom元素
	 */
	private DomModel removeDom; 
   /* *//**
     * 排除区域表达式类型
     *//*
    private String excludeExpType;*/
    /**
     * 资源抽取器集合
     */
    private List<ResourceExtractor> resourceExtractors;
	/**
	 * 视频的保存路径
	 */
	private String videoStorePath;
	/**
	 * 视频封面
	 */
	private String videoHeadimg;
	/**
	 * 视频打包路径
	 */
	private String zippath;

    public FieldExtractor(String fieldName, Selector selector, Source source, boolean notNull, boolean multi,DomModel removeDom,Map<String,List<String>> excludeExp,List<InterFileExtractorConfig> interFileExtractorConfigs) {
        super(selector, source, notNull, multi);
        this.fieldName = fieldName;
        //this.excludeExpType = excludeExpType;
        this.removeDom = removeDom;
        this.excludeExp = excludeExp;
        if(interFileExtractorConfigs!=null&&interFileExtractorConfigs.size()>0){
        	resourceExtractors = new ArrayList<ResourceExtractor>();
        	for(InterFileExtractorConfig cfg : interFileExtractorConfigs){
        		Selector fileSelector = ExtractorUtils.getSelector(source.name(),cfg.getResourceRegionExp());
        		resourceExtractors.add(new ResourceExtractor(fileSelector, source, cfg.isNotNull(), cfg.isMulti(), cfg.getResourceName(), cfg.getResourceUrlPatterns(), cfg.getExcludeUrlPatterns(),cfg.getTargetFileName(),cfg.getTargetUrlPattern()));
        	}
        }
        
    }

    String getFieldName() {
        return fieldName;
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
    
	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public Map<String,List<String>> getExcludeExp() {
		return excludeExp;
	}

	public List<ResourceExtractor> getResourceExtractors() {
		return resourceExtractors;
	}

	public DomModel getRemoveDom() {
		return removeDom;
	}

	public String getVideoType() {
		return videoType;
	}

	public void setVideoType(String videoType) {
		this.videoType = videoType;
	}

	public String getFieldExtractExp() {
		return fieldExtractExp;
	}

	public void setFieldExtractExp(String fieldExtractExp) {
		this.fieldExtractExp = fieldExtractExp;
	}

	public String getVideoTitle() {
		return videoTitle;
	}

	public void setVideoTitle(String videoTitle) {
		this.videoTitle = videoTitle;
	}

	public String getVideoStorePath() {
		return videoStorePath;
	}

	public void setVideoStorePath(String videoStorePath) {
		this.videoStorePath = videoStorePath;
	}

	public String getVideoHeadimg() {
		return videoHeadimg;
	}

	public void setVideoHeadimg(String videoHeadimg) {
		this.videoHeadimg = videoHeadimg;
	}

	public String getZippath() {
		return zippath;
	}

	public void setZippath(String zippath) {
		this.zippath = zippath;
	}
}
