package com.mmall.service.impl;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Random;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.ServletRequestUtils;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import com.mmall.entity.ImageCode;
import com.mmall.properties.SecurityProperties;
import com.mmall.service.IValidateCodeGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.ServletWebRequest;

import javax.servlet.http.HttpSession;


/**
 *  校验图片的生成器实现类
 */
@Component
public class ValidateCodeGeneratorImpl implements IValidateCodeGenerator {

	@Autowired
	private SecurityProperties securityProperties;


	// 短信发送接口的http地址，请咨询客服
	private String url = "http://api01.monyun.cn:7901/sms/v2/std/single_send";
	//private String url = "http://api02.monyun.cn:7901/sms/v2/std/";
	//http://api01.monyun.cn:7901/sms/v2/std/single_send
	/**
	 * 编码格式。发送编码格式统一用UTF-8
	 */
	//private static final String ENCOKING = "UTF-8";

	//账号
	private String userid = "E107RE";

	//密码
	private String pwd = "zE3FyQ";

	private String APIKey = "a85dfcbfd03270999a06f6235db65636";

	//String msg = "【汉宇科技】您的验证码是："+code;
	/**
	      * 发送短信
	      *
	      * @param account
	      *            account
	      * @param pswd
	      *            pswd
	      * @param mobile
	      *            手机号码
	      * @param content
	      *            短信发送内容
	      */
	@Override
	public void mobile(String mobile, String code) {
		System.out.println("向手机"+mobile+"发送验证码"+code);
		String content = "您的验证码是"+code+"请在2分钟内验证";
		NameValuePair[] data = { new NameValuePair("userid",userid),
				new NameValuePair("pwd", pwd),
				new NameValuePair("mobile",mobile),
				new NameValuePair("content",content),
				new NameValuePair("APIKey",APIKey)
		};
		doPost(url,data);
	}

	/**
	 *基于HttpClient的post函数
	 * PH
	 * @param url
	 * 提交的URL
	 * @param data
	 * 提交NameValuePair参数
	 * @return 提交响应
	 */
	private void doPost(String url, NameValuePair[] data) {
		HttpClient client =new HttpClient();
		PostMethod method=new PostMethod(url);
		// method.setRequestHeader("ContentType",
		// "application/x-www-form-urlencoded;charset=UTF-8");
		method.setRequestBody(data);
		// client.getParams().setContentCharset("UTF-8");
		client.getParams().setConnectionManagerTimeout(10000);
		try{
			client.executeMethod(method);
			String str = method.getResponseBodyAsString();
			System.err.println(str);
		}catch(Exception e){
			e.printStackTrace();
		}
	}


	/**
	 * 随机生成图片
	 * @param request
	 * @return
	 */
	@Override
	public ImageCode generate(ServletWebRequest request) {
		//request中如果没有width/height/length/expireIn 则在配置文件里获取
		int width = ServletRequestUtils.getIntParameter(request.getRequest(), "width", securityProperties.getCode().getImage().getWidth());
		int height =ServletRequestUtils.getIntParameter(request.getRequest(), "height", securityProperties.getCode().getImage().getHeight());
		BufferedImage image = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
		// 获取图形上下文
		Graphics g = image.getGraphics();
		//生成随机类
		Random random = new Random();
		// 设定背景色
		g.setColor(getRandColor(200,250));
		g.fillRect(0, 0, width, height);
		//设定字体
		g.setFont(new Font("Times New Roman",Font.ITALIC,20));
		// 随机产生168条干扰线，使图象中的认证码不易被其它程序探测到
		g.setColor(getRandColor(160,200));
		for(int i=0;i<155;i++) {
			int x = random.nextInt(width);
			int y = random.nextInt(height);
			int x1 = random.nextInt(12);
			int y1 = random.nextInt(12);
			g.drawLine(x, y,x + x1,y + y1);
		}
		//取随机产生的码
		String sRand = "";
		//4代表4位验证码,如果要生成更多位的认证码,则加大数值
		for(int i=0;i<securityProperties.getCode().getImage().getLength();i++) {
			String rand = String.valueOf(random.nextInt(10));
			sRand += rand;
			// 将认证码显示到图象中
			g.setColor(new Color(20 + random.nextInt(110),20 + random.nextInt(110), 20 +random.nextInt(110)));
			// 设置随便码在背景图图片上的位置
			g.drawString(rand, 13 * i +6, 16);
		}
		// 释放图形上下文
		g.dispose();
		return new ImageCode(image,sRand,securityProperties.getCode().getImage().getExpireIn());
	}

	/**
	 * 生成随机背景条纹
	 */
	private Color getRandColor(int i, int j) {
		Random random = new Random();
		if (i > 255) i = 255;
		if (j > 255) j = 255;
		int r = i + random.nextInt(j - i);
		int g = i + random.nextInt(j - i);
		int b = i + random.nextInt(j - i);
		return new Color(r, g, b);
	}


	public ServerResponse validate(String code , HttpSession session){
		ImageCode codeInSession = (ImageCode) session.getAttribute(Const.SESSION_KEY_IMAGE);
		if (StringUtils.isBlank(code))
			return ServerResponse.createByErrorMessage("验证码的值不能为空");
		if (codeInSession == null)
			return ServerResponse.createByErrorMessage("验证码不存在");
		if (codeInSession.isExpried()) {
			session.removeAttribute(Const.SESSION_KEY_IMAGE);
			return ServerResponse.createByErrorMessage("验证码已过期！");
		}
		if (!StringUtils.equals(codeInSession.getCode(),code))
			return ServerResponse.createByErrorMessage("验证码不匹配！");
		session.removeAttribute(Const.SESSION_KEY_IMAGE);
		return ServerResponse.createBySuccess();
	}

}
