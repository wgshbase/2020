package io.renren.modules.spider.controller;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.renren.common.annotation.SysLog;
import io.renren.common.utils.PageUtils;
import io.renren.common.utils.Query;
import io.renren.common.utils.R;
import io.renren.modules.spider.entity.SpiderInfo;
import io.renren.modules.spider.service.ISpiderInfoService;
import io.renren.modules.sys.controller.AbstractController;

/**
 * 
 * 采集模板控制器
 * @author wangdw
 * @email dawei.happy@gmail.com
 * @date 2016年10月31日 上午10:40:10
 */
@RestController
@RequestMapping("/spiderInfo")
public class SpiderInfoController extends AbstractController {
	@Autowired
	private ISpiderInfoService spiderInfoService;
	
	
	/**
	 * 查询所有采集模板信息
	 */
	@RequestMapping("/list")
	@RequiresPermissions("sys:user:list")
	public R list(@RequestParam Map<String, Object> params){
		
		//查询列表数据
		Query query = new Query(params);
		List<SpiderInfo> userList = spiderInfoService.queryList(query);
		int total = spiderInfoService.queryTotal(query);
		
		PageUtils pageUtil = new PageUtils(userList, total, query.getLimit(), query.getPage());
		
		return R.ok().put("page", pageUtil);
	}	
	
	
	/**
	 * 获取采集模板信息
	 */
	@RequestMapping("/info/{spiderInfoId}")
	public R info(@PathVariable("spiderInfoId") String spiderInfoId){
		SpiderInfo info = spiderInfoService.getById(spiderInfoId);
		return R.ok().put("spiderInfo", info);
	}
	
	/**
	 * 保存采集模板信息
	 */
	@SysLog("保存采集模板信息")
	@RequestMapping("/save")
	public R save(@RequestBody SpiderInfo spiderInfo){
		//ValidatorUtils.validateEntity(spiderInfo, AddGroup.class);
		
		String id = spiderInfo.getId();
		if(StringUtils.isEmpty(id)) {
			id = "";
		}
		SpiderInfo f_spiderInfo = spiderInfoService.getById(id);
		if(f_spiderInfo==null){
			spiderInfoService.save(spiderInfo);
		}else{
			try{
				spiderInfoService.update(spiderInfo);
			}catch(Exception e){
				e.printStackTrace();
				return R.error(e.getMessage());
			}
		}
		return R.ok();
	}
	
	/**
	 * 修改采集模板信息
	 */
	@SysLog("修改采集模板信息")
	@RequestMapping("/update")
	public R update(@RequestBody SpiderInfo spiderInfo){
		//ValidatorUtils.validateEntity(user, UpdateGroup.class);
		try{
			spiderInfoService.update(spiderInfo);
			}catch(Exception e){
				e.printStackTrace();
				return R.error(e.getMessage());
			}
		
		return R.ok();
	}
	
	/**
	 * 删除采集模板信息
	 */
	@SysLog("删除采集模板信息")
	@RequestMapping("/delete")
	public R delete(@RequestBody String[] spiderInfoIds){
		
		spiderInfoService.deleteById(spiderInfoIds);
		
		return R.ok();
	}
}
