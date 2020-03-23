package io.renren.modules.spider.dao;

import io.renren.modules.spider.entity.SpiderProjectEntity;
import io.renren.modules.sys.dao.BaseDao;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * InnoDB free: 3971072 kB
 * 
 * @author wangdawei
 * @email dawei.happy@gmail.com
 * @date 2018-09-06 20:24:12
 */
@Mapper
public interface SpiderProjectDao extends BaseDao<SpiderProjectEntity> {
	
	public List<SpiderProjectEntity> queryListByIds(Long[] ids);
	

	/**
	 * 查询所有的
	 * @return
	 */
	List<SpiderProjectEntity> getProjects();

	
}
