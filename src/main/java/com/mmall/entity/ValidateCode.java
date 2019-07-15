package com.mmall.entity;


import org.joda.time.LocalDateTime;

/**
 * 短信验证实体类
 * @author hy
 *
 */
public class ValidateCode {
	
	
	private String code;
	
	private LocalDateTime expireTime;

	
	
	public ValidateCode() {
		super();
	}

	public ValidateCode(String code, LocalDateTime expireTime) {
		super();
		this.code = code;
		this.expireTime = expireTime;
	}
	
	public ValidateCode(String code, int expireIn) {
		super();
		this.code = code;
		this.expireTime = LocalDateTime.now().plusSeconds(expireIn);
	}
	
	
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public LocalDateTime getExpireTime() {
		return expireTime;
	}

	public void setExpireTime(LocalDateTime expireTime) {
		this.expireTime = expireTime;
	}
	//判断当前时间是否在有效期
	public boolean isExpried() {
		return LocalDateTime.now().isAfter(expireTime);
	}
	
	
	
}
