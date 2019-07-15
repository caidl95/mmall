package com.mmall.properties;

import com.mmall.properties.code.ValidateCodeProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;


/**
 * 配置文件读取类
 * ConfigurationProperties(prefix = "mmall.security")表示这个类会读取所有以hy.security开头的配置项
 * @author hy
 *
 */
@ConfigurationProperties(prefix = "mmall.security")
public class SecurityProperties {

	private ValidateCodeProperties code = new ValidateCodeProperties();


	public ValidateCodeProperties getCode() {
		return code;
	}

	public void setCode(ValidateCodeProperties code) {
		this.code = code;
	}

	
	
}
