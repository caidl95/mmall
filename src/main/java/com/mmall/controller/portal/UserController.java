package com.mmall.controller.portal;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.controller.BaseController;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.service.IValidateCodeGenerator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpSession;
import java.util.Random;

/**
 * 用户模块
 */
@RestController
@RequestMapping("/users")
public class UserController extends BaseController {

	@Autowired
	private IUserService userService;

	@Autowired
	private IValidateCodeGenerator validateCodeGenerator;

	/**
	 * 用户登录
	 */
	@PostMapping("/login")
	public ServerResponse login(HttpSession session,String username, String password,String imageCode){
		ServerResponse result = validateCodeGenerator.validate(imageCode,session);
		if (!result.isSuccess())
			return result;
		ServerResponse<User> response = userService.login(username,password);
		if (response.isSuccess())
			session.setAttribute(Const.CURRENT_USER,response.getData());
		return response;
	}

	/**
	 * 手机登录
	 */
	@PostMapping("/mobile")
	public ServerResponse<User> mobile(HttpSession session ,String phone,String code){
		 String sessionPhone = (String)session.getAttribute(Const.SESSION_KEY_PHONE);
		 String sessionCode = (String)session.getAttribute(Const.SESSION_KEY_PHONE_CODE);
		 ServerResponse result = userService.mobileLogin(phone,code,sessionPhone,sessionCode);
		 if (result.isSuccess()){
		 	session.removeAttribute(Const.SESSION_KEY_PHONE_CODE);
		 	session.removeAttribute(Const.SESSION_KEY_PHONE);
		 	session.setAttribute(Const.CURRENT_USER,result.getData());
		 }
		 return result;
	}

	/**
	 * 登出
	 */
	@PostMapping("/logout")
	public ServerResponse<String> logout(HttpSession session){
		session.removeAttribute(Const.CURRENT_USER);
		return ServerResponse.createBySuccess();
	}

	/**
	 * 注册用户
	 */
	@PostMapping("/reg")
	public ServerResponse<String> register(User user){
		return userService.register(user);
	}

	/**
	 * 实时校验，用户名是否存在
	 */
	@PostMapping("/check_valid")
	public ServerResponse<String> checkValid(String str,String type){
		return userService.checkValid(str,type);
	}

	/**
	 * 获取用户登录信息
	 */
	@GetMapping("/get_user")
	public ServerResponse<User> getUserInfo(HttpSession session){
		return ServerResponse.createBySuccess(getUserFromSession(session));
	}

	/**
	 * 获取忘记密码提示问题
	 */
	@GetMapping("/forget_get_question")
	public ServerResponse<String> forgetGetQuestion(String username){
		return userService.selectQuestion(username);
	}

	/**
	 * 效验忘记密码的问题与答案
	 */
	@PostMapping("/forget_check_answer")
	public ServerResponse<String> forgetCheckAnswer(String username,String question,String answer){
		return userService.checkAnswer(username,question,answer);
	}

	/**
	 * 忘记密码中的重置密码功能
	 */
	@PostMapping("forget_rest_password")
	public ServerResponse<String> forgetRestPassword(String username,String passwordNew,String forgetToken){
		return userService.forgetRestPassword(username,passwordNew,forgetToken);
	}

	/**
	 * 登录状态时重置密码
	 */
	@PostMapping("/reset_password")
	public ServerResponse<String> resetPassword(HttpSession session,String passwordOld,String passwordNew){
		return userService.resetPassword(passwordOld,passwordNew,getUserFromSession(session));
	}

	/**
	 * 更新用户信息
	 */
	@GetMapping("/update_information")
	public ServerResponse<User> updateInformation(HttpSession session,User user){
		User currentUser = getUserFromSession(session);
		user.setId(currentUser.getId());
		user.setUsername(currentUser.getUsername());
		ServerResponse response = userService.updateInformation(user);
		if (response.isSuccess())
			session.setAttribute(Const.CURRENT_USER,response.getData());
		return response;
	}

	/**
	 * 获取用户登录的详细信息
	 */
	@GetMapping("/get_information")
	public ServerResponse<User> getInformation(HttpSession session){
		return userService.getInformation(getUidFromSession(session));
	}

}







