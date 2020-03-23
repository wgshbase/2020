package io.renren.modules.spider.service.impl;

import io.renren.modules.spider.dao.SpiderProjectDao;
import io.renren.modules.spider.entity.SpiderProjectEntity;
import io.renren.modules.spider.service.SpiderProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Service("spiderProjectService")
public class SpiderProjectServiceImpl implements SpiderProjectService {
	@Autowired
	private SpiderProjectDao spiderProjectDao;
	
	@Override
	public SpiderProjectEntity queryObject(Long id){
		return spiderProjectDao.queryObject(id);
	}
	
	@Override
	public List<SpiderProjectEntity> queryList(Map<String, Object> map){
		return spiderProjectDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return spiderProjectDao.queryTotal(map);
	}
	
	@Override
	public void save(SpiderProjectEntity spiderProject){
		spiderProjectDao.save(spiderProject);
	}
	
	@Override
	public void update(SpiderProjectEntity spiderProject){
		spiderProjectDao.update(spiderProject);
	}
	
	@Override
	public void delete(Long id){
		spiderProjectDao.delete(id);
	}
	
	@Override
	public void deleteBatch(Long[] ids){
		spiderProjectDao.deleteBatch(ids);
	}

	@Override
	public List<SpiderProjectEntity> queryListByIds(Long[] ids) {
		return spiderProjectDao.queryListByIds(ids);
	}
	
	@Override
	public List<SpiderProjectEntity> getProjects() {
		return spiderProjectDao.getProjects();
	}
	
}
