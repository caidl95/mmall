package com.mmall.service.impl;

import java.util.Date;
import java.util.List;
import com.mmall.common.ServerResponse;
import com.mmall.dao.AddressMapper;
import com.mmall.pojo.Address;
import com.mmall.pojo.District;
import com.mmall.service.IAddressService;
import com.mmall.service.IDistrictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 处理收货地址数据的业务层实现类
 */
@Service
public class AddressServiceImpl implements IAddressService {
	
	@Autowired
	private AddressMapper addressMapper;
	@Autowired
	private IDistrictService districtService;

	@Override 
	public ServerResponse addNew(Address address, String username) {
		// 查询用户的收货地址的数量：countByUid(Integer uid)，参数值来自address.getUid();
		Integer count = countByUid(address.getUid());
		// 判断数量是否为0
		// 是：当前将增加第1条收货地址，则：address.setIsDefault(1)
		// 否：当前增加的不是第1条收货地址，则：address.setIsDefault(0)
		address.setIsDefault(count == 0 ? 1 : 0);
		// 处理district
		String district = getDistrict(
				address.getProvince(), address.getCity(), address.getArea());
		address.setDistrict(district);
		// 增加收货地址数据
		Integer rows = addressMapper.insert(address);
		if (rows != 1)
			return ServerResponse.createByErrorMessage("增加收货地址数据时出现未知错误！");
		return ServerResponse.createBySuccess();
	}
	
	@Override
	@Transactional
	public ServerResponse<String> delete(Integer uid, Integer aid){
		// 根据aid查询即将删除的数据
		Address result = findByAid(aid);
		// 判断查询结果是否为null
		if (result == null)
			return ServerResponse.createByErrorMessage("删除收货地址失败！尝试访问的数据不存在！");
		// 检查数据归属是否不正确
		if (!result.getUid().equals(uid))
			return ServerResponse.createByErrorMessage("删除收货地址失败！数据归属错误！");
		// 执行删除根据id删除收货地址数据
		Integer rows = addressMapper.deleteByAid(aid);
		if (rows != 1)
			return ServerResponse.createByErrorMessage("删除收货地址时出现未知错误！");
		// 检查此前的查询结果中的isDefault是否为0,0为非默认
		if (result.getIsDefault().equals(0))
			return ServerResponse.createBySuccess("删除成功！");
		// 获取当前用户的收货地址数据的数量
		Integer count = countByUid(uid);
		// 判断数量是否为0,为0则为无数据
		if (count.equals(0))
			return ServerResponse.createBySuccess("删除成功！");
		// 获取当前用户最后修改的收货地址数据
		Address lastModified = findLastModified(uid);
		// 全部设置为非默认,将指定用户的所有收货地址设置为非默认
		rows = addressMapper.updateNonDefault(uid);
		if (rows < 1)
			return ServerResponse.createByErrorMessage("设置非默认收货地址时出现未知错误！");
		// 将最后修改的数据设置为默认。
		rows = addressMapper.updateDefault(lastModified.getAid());
		if (rows != 1)
			return ServerResponse.createByErrorMessage("设置默认收货地址时出现未知错误！");
		return ServerResponse.createBySuccess("删除成功！");
	}

	@Override
	@Transactional
	public ServerResponse<String> setDefault(Integer uid, Integer aid) {
		// 根据aid查询数据
		Address result = findByAid(aid);
		// 判断数据是否为null
		if (result == null)
			// 是：AddressNotFoundException
			return ServerResponse.createByErrorMessage("设置默认收货地址失败！尝试访问的数据不存在！");
		// 判断参数uid与查询结果中的uid是否不一致
		if (!result.getUid().equals(uid))
			// 是：AccessDeniedException
			return ServerResponse.createByErrorMessage("设置默认收货地址失败！数据归属错误！");
		// 全部设置为非默认,将指定用户的所有收货地址设置为非默认
		Integer rows = addressMapper.updateNonDefault(uid);
		if (rows < 1)
			return ServerResponse.createByErrorMessage("设置非默认收货地址时出现未知错误！");
		// 将指定的收货地址设置为默认
		rows = addressMapper.updateDefault(aid);
		if (rows != 1)
			return ServerResponse.createByErrorMessage("设置默认收货地址时出现未知错误！");
		return ServerResponse.createBySuccess("设置默认收货地址成功！");
	}

	@Override
	public ServerResponse<List<Address>> getByUid(Integer uid) {
		return ServerResponse.createBySuccess(findByUid(uid));
	}

	@Override
	public ServerResponse<Address> getByAid(Integer aid) {
		return ServerResponse.createBySuccess(findByAid(aid));
	}

	/**
	 * 统计指定用户的收货地址数据的数量
	 * @param uid 用户的id
	 * @return 用户的收货地址数据的数量
	 */
	private Integer countByUid(Integer uid) {
		return addressMapper.countByUid(uid);
	}
	
	/**
	 * 根据用户id查询收货地址列表
	 * @param uid 用户id
	 * @return 该用户的收货地址列表
	 */
	private List<Address> findByUid(Integer uid) {
		return addressMapper.findByUid(uid);
	}
	
	/**
	 * 根据收货地址id查询匹配的数据
	 * @param aid 收货地址id
	 * @return 匹配的数据，如果没有匹配的数据，则返回null
	 */
	private Address findByAid(Integer aid) {
		return addressMapper.findByAid(aid);
	}
	
	/**
	 * 获取某用户最后修改的收货地址数据
	 * @param uid 用户的id
	 * @return 该用户最后修改的收货地址数据
	 */
	private Address findLastModified(Integer uid) {
		return addressMapper.findLastModified(uid);
	}
	
	/**
	 * 根据省、市、区的代号，获取名称
	 * @param province 省的代号
	 * @param city 市的代号
	 * @param area 区的代号
	 * @return 省、市、区的代号对应的名称
	 */
	private String getDistrict(
			String province, String city, String area) {
		ServerResponse response = districtService.getByCode(province);
		District p = (District) response.getData();
		response = districtService.getByCode(city);
		District c = (District) response.getData();
		response = districtService.getByCode(area);
		District a = (District) response.getData();
		String provinceName = p == null ? "NULL" : p.getName();
		String cityName = c == null ? "NULL" : c.getName();
		String areaName = a == null ? "NULL" : a.getName();
		StringBuffer result = new StringBuffer();
		result.append(provinceName);
		result.append(",");
		result.append(cityName);
		result.append(",");
		result.append(areaName);
		return result.toString();
	}

}



