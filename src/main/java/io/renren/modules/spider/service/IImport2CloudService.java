package io.renren.modules.spider.service;


import java.io.FileNotFoundException;
import java.util.List;

import io.renren.modules.spider.entity.News;


/**
 * 导出数据到服务接口
 * 
 * @author mfq
 * @email 774638327@qq.com
 * @date 2018年4月2日 
 */
public interface IImport2CloudService {

	/**
	 *  导入数据到云存储
	 *  1)导出数据库数据——例子：
	 *  2)导出图片——文件包
	 *  3)按照日期，生成文件包      20180404.zip
	 *  目录结构
	 *  20180404
	 *     news_cn_20180404.csv
	 *     news_en_20180404.csv
	 *     news_wx_20180404.csv
	 *     domain例：www.baidu.com
	 *       images/**		
	 *       pdf/**			
	 *  4)上传到云上
	 * @param date  如果日期为null，导出全部数据， 如果不为空，导出和参数日期相同的采集日期的数据
	 * @param enddate
	 * @param tables 包括：news_cn,news_en,news_wx
	 * @param removeList
	 * @throws FileNotFoundException
	 */
    // void importByDate(String date, String enddate, String[] tables) throws FileNotFoundException;

	void importByDateWithExtra(String identifyCode, String begindate, String enddate, String[] tables, List<String> removeList) throws FileNotFoundException;

	// 返回指定的时间区间内部的所有的 pdf 的集合
	List<News> getNewsPdfs(String begindate, String enddate);

	// 将对应的 pdffiles 文件不存在的属性值设置为空
	void clearNotExistPdf(News news);

	// 去除数据库的重复记录
	List<String> removeDulpRecords(String[] tables, double similarityFactor);

}
