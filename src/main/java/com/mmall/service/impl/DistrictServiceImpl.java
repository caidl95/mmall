package com.mmall.service.impl;

import java.util.List;

import com.mmall.common.ServerResponse;
import com.mmall.dao.DistrictMapper;
import com.mmall.pojo.District;
import com.mmall.service.IDistrictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 处理省/市/区数据的业务层实现类
 */
@Service
public class DistrictServiceImpl implements IDistrictService {
	
	@Autowired
	private DistrictMapper distrctMapper;

	@Override
	public ServerResponse<List<District>> getByParent(String parent) {
		return ServerResponse.createBySuccess(findByParent(parent));
	}

	@Override
	public ServerResponse<District> getByCode(String code) {
		return ServerResponse.createBySuccess(findByCode(code));
	}
	
	/**
	 * 获取所有省/某省所有市/某市所有区的列表
	 * @param parent 获取省列表时，使用86；获取市列表时，使用省的代号；获取区列表时，使用市的代号
	 * @return 所有省/某省所有市/某市所有区的列表
	 */
	private List<District> findByParent(String parent) {
		return distrctMapper.findByParent(parent);
	}
	
	/**
	 * 根据代号获取省/市/区的信息
	 * @param code 省/市/区的代号
	 * @return 匹配的省/市/区的信息，如果没有匹配的信息，则返回null
	 */
	private District findByCode(String code) {
		return distrctMapper.findByCode(code);
	}

}




