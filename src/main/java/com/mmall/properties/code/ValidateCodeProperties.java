package com.mmall.properties.code;

/**
 * 可配置验证码的分层
 * @author hy
 *
 */
public class ValidateCodeProperties {
	
	private ImageCodeProperties image = new ImageCodeProperties();

	private SmsCodeProperties sms = new SmsCodeProperties();
	
	public ImageCodeProperties getImage() {
		return image;
	}

	public void setImage(ImageCodeProperties image) {
		this.image = image;
	}

	public SmsCodeProperties getSms() {
		return sms;
	}

	public void setSms(SmsCodeProperties sms) {
		this.sms = sms;
	}
	
}
