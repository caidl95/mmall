package com.mmall.dao;

import java.util.List;
import com.mmall.pojo.District;

/**
 * 处理省/市/区数据的持久层接口
 */
public interface DistrictMapper {

	/**
	 * 获取所有省/某省所有市/某市所有区的列表
	 * @param parent 获取省列表时，使用86；获取市列表时，使用省的代号；获取区列表时，使用市的代号
	 * @return 所有省/某省所有市/某市所有区的列表
	 */
	List<District> findByParent(String parent);
	
	/**
	 * 根据代号获取省/市/区的信息
	 * @param code 省/市/区的代号
	 * @return 匹配的省/市/区的信息，如果没有匹配的信息，则返回null
	 */
	District findByCode(String code);
	
}




