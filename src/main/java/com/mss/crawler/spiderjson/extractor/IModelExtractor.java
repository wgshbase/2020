package com.mss.crawler.spiderjson.extractor;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.selector.Selector;

/**
 * 模型抽取接口
 * @author wdw
 *
 */
public interface IModelExtractor {
	
	/**
	 *获取目标对象抽取规则
	 * @return
	 */
	public List<Pattern> getTargetUrlPatterns();

	/**
	 * 获取辅助页面抽取规则
	 * @return
	 */
	public List<Pattern> getHelpUrlPatterns();

	/**
	 * 获取目标URL所在区域选择器
	 * @return
	 */
	public Selector getTargetUrlRegionSelector();
	
	/**
	 * 获取辅助URL所在区域选择器
	 * @return
	 */
	public Selector getHelpUrlRegionSelector();
	 
	/**
	 * 处理抽取页面对象
	 * @param page
	 * @return
	 */
	public Object process(Page page,Map<String,Object> context);

}
