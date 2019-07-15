package com.mmall.dao;

import java.util.List;
import com.mmall.pojo.Address;

/**
 * 处理收货地址的持久层接口
 */
public interface AddressMapper {

	/**
	 * 增加收货地址数据
	 * @param address 收货地址数据
	 * @return 受影响的行数
	 */
	Integer insert(Address address);
	
	/**
	 * 根据id删除收货地址数据
	 * @param aid 将删除的收货地址数据的id
	 * @return 受影响的行数
	 */
	Integer deleteByAid(Integer aid);

	/**
	 * 将指定用户的所有收货地址设置为非默认
	 * @param uid 用户的id
	 * @return 受影响的行数
	 */
	Integer updateNonDefault(Integer uid);
	
	/**
	 * 将指定的收货地址设置为默认
	 * @param aid 收货地址id
	 * @return 受影响的行数
	 */
	Integer updateDefault(Integer aid);

	/**
	 * 统计指定用户的收货地址数据的数量
	 * @param uid 用户的id
	 * @return 用户的收货地址数据的数量
	 */
	Integer countByUid(Integer uid);
	
	/**
	 * 根据用户id查询收货地址列表
	 * @param uid 用户id
	 * @return 该用户的收货地址列表
	 */
	List<Address> findByUid(Integer uid);
	
	/**
	 * 根据收货地址id查询匹配的数据
	 * @param aid 收货地址id
	 * @return 匹配的数据，如果没有匹配的数据，则返回null
	 */
	Address findByAid(Integer aid);

	/**
	 * 获取某用户最后修改的收货地址数据
	 * @param uid 用户的id
	 * @return 该用户最后修改的收货地址数据
	 */
	Address findLastModified(Integer uid);
	
}






