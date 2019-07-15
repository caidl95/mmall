package com.mmall.service;

import java.util.List;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Address;

/**
 * 处理收货地址数据的业务层接口
 */
public interface IAddressService {

	/**
	 * 增加新的收货地址
	 * @param address 收货地址数据
	 * @param username 当前登录的用户名

	 */
	ServerResponse addNew(Address address, String username);
	
	/**
	 * 根据id删除收货地址数据
	 * @param uid 当前登录的用户的id
	 * @param aid 将删除的收货地址数据的id
	 */
	ServerResponse<String> delete(Integer uid, Integer aid);

	
	/**
	 * 设置默认收货地址
	 * @param uid 当前登录的用户的id
	 * @param aid 将要设置为默认收货地址的数据的id
	 */
	ServerResponse<String> setDefault(Integer uid, Integer aid);

	/**
	 * 根据用户id查询收货地址列表
	 * @param uid 用户id
	 * @return 该用户的收货地址列表
	 */
	ServerResponse<List<Address>> getByUid(Integer uid);
	
	/**
	 * 根据收货地址id查询收货地址数据
	 * @param aid 收货地址id
	 * @return 匹配的收货地址数据，如果没有匹配的数据，则返回null
	 */
	ServerResponse<Address> getByAid(Integer aid);
	
}




