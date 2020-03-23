package io.renren.modules.spider.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import io.renren.modules.spider.utils.CSVUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import io.renren.modules.spider.entity.News;
import io.renren.modules.spider.service.IImport2CloudService;

/**
 *  将数据导出并上传到 云 的控制器
 * @author wgshb
 *
 */
@RequestMapping("/news")
@Controller
public class Import2CloudController {

	private Logger LOG = LogManager.getLogger(Import2CloudController.class);

	@Value("${crawler.weixin.pdf.path}")
	private String pdfpath;

	@Value("${crawler.news.content.similarity.factor}")
	private double similarityFactor;
	
	@Autowired
	private IImport2CloudService import2CloudService;
	
	@ResponseBody
	@RequestMapping("/import2Cloud")
	public String import2Cloud(@RequestParam Map<String, Object> params) {
		String[] tables = {"news_cn", "news_en", "news_wx"};
		String begindate = (String) params.get("begindate");
		String enddate = (String) params.get("enddate");
		String identifyCode = (String) params.get("identifyCode");
		
		try {
			Date date = new SimpleDateFormat("yyyy-MM-dd").parse(begindate);
		} catch (ParseException e) {
			// 出错, 设置日期为当天
			begindate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
			LOG.error("The begindate you input is bad format, We have make it today by default");
		}
		try {
			Date date2 = new SimpleDateFormat("yyyy-MM-dd").parse(enddate);
		} catch (ParseException e) {
			// 出错, 设置日期为当天
			enddate = CSVUtils.getNextDay(new Date());
			LOG.error("The enddate you input is bad format, We have make it tomorrow by default");
		}
		
		// 0. 数据库的重复记录进行去重处理(修改为按照权重进行去重)
		List<String> removeList = null;
		removeList = import2CloudService.removeDulpRecords(tables, similarityFactor);

		// 对于极个别的 pdf 不存在的情况, 做出预警机制
		// 1. 获取所有的 pdf 的文件名称的集合
		List<String> pdfs = CSVUtils.getPdfNameList();
		
		// 2. 当前的表中对应的准备打包的数据对应的所有的  pdf 值
		List<News> newsList = import2CloudService.getNewsPdfs(begindate, enddate);
		
		// 3. 对于不存在的情况, 打印结果到指定的文件
		for(News news : newsList) {
			// 不存在对应的 pdf 文件
			if(!pdfs.contains(news.getId() + ".pdf")) {
				// 将对应的记录的 pdffiles 字段设置为空
				import2CloudService.clearNotExistPdf(news);
				LOG.error("No pdffiles found!!! ---------------> " + news.getId());
			}
		}
		
		// 4. 本地的验证结束, 清空对应的文件的内容
		//CSVUtils.clearPdfTempText();
		
		try {
            import2CloudService.importByDateWithExtra(identifyCode, begindate, enddate, tables, removeList);
		} catch (Exception e) {
			LOG.error("Cant not find the zip from the given path!!");
			e.printStackTrace();
			return "2";
		}
		return "1";
	}
	
}
