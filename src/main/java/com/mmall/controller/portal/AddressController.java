package com.mmall.controller.portal;


import javax.servlet.http.HttpSession;
import com.mmall.common.ServerResponse;
import com.mmall.controller.BaseController;
import com.mmall.pojo.Address;
import com.mmall.pojo.User;
import com.mmall.service.IAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/addresses")
public class AddressController extends BaseController {

	@Autowired
	private IAddressService addressService;

	/**
	 * 新增收货地址
	 */
	@RequestMapping("/add_new")
	public ServerResponse<String> addNew(Address address, HttpSession session) {
		User user =  getUserFromSession(session);
		// 将uid封装到address中
		address.setUid(user.getId());
		// 调用业务层对象执行：addressService.addNew(address, username);
		return addressService.addNew(address, user.getUsername());
	}

	/**
	 * 获取当前用户下地址
	 */
	@GetMapping("/")
	public ServerResponse<List<Address>> getByUid(HttpSession session) {
		Integer uid = getUidFromSession(session);
		return addressService.getByUid(uid);
	}

	/**
	 * 设定成默认地址
	 */
	@RequestMapping("/{aid}/set_default")
	public ServerResponse<String> setDefault(@PathVariable("aid") Integer aid, HttpSession session) {
		Integer uid = getUidFromSession(session);
		return addressService.setDefault(uid, aid);
	}

	/**
	 * 删除地址
	 */
	@RequestMapping("/{aid}/delete")
	public ServerResponse<String> delete(@PathVariable("aid") Integer aid, HttpSession session) {
		Integer uid = getUidFromSession(session);
		return addressService.delete(uid, aid);
	}

}












