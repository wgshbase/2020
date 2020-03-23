package io.renren.modules.spider.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mss.crawler.common.UUIDGenerator;

import io.renren.modules.spider.dao.SpiderInfoDao;
import io.renren.modules.spider.entity.SpiderInfo;
import io.renren.modules.spider.service.ISpiderInfoService;



/**
 * 采集模板服务类
 * 
 * @author wangdw
 * @email dawei.happy@gmail.com
 * @date 2016年9月18日 上午9:46:09
 */
@Service("spiderInfoService")
public class SpiderInfoServiceImpl implements ISpiderInfoService {
	@Autowired
	private SpiderInfoDao spiderInfoDao;

	@Override
	public List<SpiderInfo> queryList(Map<String, Object> query) {
		return spiderInfoDao.queryList(query);
	}

	@Override
	public void deleteById(String[] ids) {
		spiderInfoDao.deleteBatch(ids);
		
	}

	@Override
	public SpiderInfo getById(String id) {
		return spiderInfoDao.queryObject(id);
	}

	@Override
	public void save(SpiderInfo spiderInfo) {
		spiderInfo.setId(UUIDGenerator.generate());
		spiderInfoDao.save(spiderInfo);
	}

	@Override
	public void update(SpiderInfo spiderInfo) throws Exception {
		String jsonData = spiderInfo.getJsonData().replaceAll("&amp;", "&");
		spiderInfo.setJsonData(jsonData);
		spiderInfoDao.update(spiderInfo);
	}

	@Override
	public int queryTotal(Map<String, Object> map) {
		return spiderInfoDao.queryTotal(map);
	}
}
