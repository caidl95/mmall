package com.mmall.controller;

import com.mmall.common.Const;
import com.mmall.entity.ImageCode;
import com.mmall.pojo.User;
import com.mmall.service.IValidateCodeGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.ServletWebRequest;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Random;

/**
 *
 */
@RestController
@RequestMapping("/code")
public class BaseController {

    @Autowired
    private IValidateCodeGenerator validateCodeGenerator;

    /**
     * 获取验证码
     */
    @GetMapping("/image")
    public void createCode(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ImageCode imageCode = (ImageCode) validateCodeGenerator.generate(new ServletWebRequest(request,response));
        request.getSession().setAttribute(Const.SESSION_KEY_IMAGE, imageCode);
        ImageIO.write(imageCode.getImage(),"JPEG",response.getOutputStream());
    }

    /**
     * 手机短信验证
     */
    @PostMapping("/mobile")
    public void mobile(String mobile,HttpSession session){
        String code = getCode();
        validateCodeGenerator.mobile(mobile,code);
        session.setAttribute(Const.SESSION_KEY_PHONE_CODE,code);
        session.setAttribute(Const.SESSION_KEY_PHONE,mobile);
    }

    /**
     * 生成六位数验证码
     */
    protected String getCode(){
        Random random =new Random();
        String code ="";
        for (int i = 0 ;i <6 ;i++){
            code += String.valueOf(random.nextInt(10));
        }
        return code;
    }

    /**
     * 获取当前用户id
     */
    protected final Integer getUidFromSession(HttpSession session) {
       User user = (User)session.getAttribute(Const.CURRENT_USER);
        return user.getId();
    }

    /**
     * 获取当前用户
     */
    protected final User getUserFromSession(HttpSession session){
        return (User)session.getAttribute(Const.CURRENT_USER);
    }

    /**
     * 判断当前用户是否是管理员
     */
    protected final boolean getRoleAdminFromSession(HttpSession session){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if (user.getRole() == Const.Role.ROLE_ADMIN){
            return true;
        }else {
            return false;
        }
    }


}
