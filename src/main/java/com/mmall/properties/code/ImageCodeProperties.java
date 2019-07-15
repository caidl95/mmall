
package com.mmall.properties.code;

/**
 * 图形验证配置的实体类
 * @author hy
 *
 */
public class ImageCodeProperties extends SmsCodeProperties{
	
	public ImageCodeProperties() {
		//初始化长度为4覆盖父类默认值
		setLength(4);
	}

	/**
	 * 默认配置图形验证背景的宽
	 */
	private int width = 67;
	
	/**
	 * 默认配置图形验证背景的高
	 */
	private int height = 23;

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	

}
