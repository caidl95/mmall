package com.mmall.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录拦截器
 */
public class LoginInterceptor implements HandlerInterceptor {

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		HttpSession session = request.getSession();
		if (session.getAttribute(Const.CURRENT_USER) == null) {
			response.sendRedirect("/web/login.html");
			return false;
		}
		return true;
	}

}
