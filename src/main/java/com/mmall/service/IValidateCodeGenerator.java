package com.mmall.service;
import com.mmall.common.ServerResponse;
import com.mmall.entity.ValidateCode;
import org.springframework.web.context.request.ServletWebRequest;

import javax.servlet.http.HttpSession;

/**
 * 校验图片的生成器接口
 *
 */
public interface IValidateCodeGenerator {
	/**
	 * 生成图片
	 * @param request
	 * @return
	 */
	ValidateCode generate(ServletWebRequest request);

	/**
	 * 发生手机验证
	 * @param mobile
	 * @param code
	 */
	void mobile(String mobile,String code);

	/**
	 * 校验验证码
	 * @throws Exception
	 */
	ServerResponse validate(String imageCode , HttpSession session);
}
