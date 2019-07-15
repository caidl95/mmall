package com.mmall.properties.code;

/**
 * 短信验证码的可配置类
 * @author hy
 *
 */
public class SmsCodeProperties {
	
	/**
	 * 默认配置验证码的长度
	 */
	private int length = 6;
	
	/**
	 * 默认配置验证码的有效时间
	 */
	private int expireIn = 60;
	
	/**
	 * 可配置需要用到短信验证的路径
	 */
	private String url;
	
	public int getLength() {
		return length;
	}
	public void setLength(int length) {
		this.length = length;
	}
	public int getExpireIn() {
		return expireIn;
	}
	public void setExpireIn(int expireIn) {
		this.expireIn = expireIn;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	
	
}
