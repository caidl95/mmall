package com.mmall.entity;

import org.joda.time.LocalDateTime;

import java.awt.image.BufferedImage;


/**
 * 图片验证实体类
 * @author hy
 *
 */
public class ImageCode extends ValidateCode{
	
	private BufferedImage  image;
	
	
	public ImageCode() {
		super();
	}

	public ImageCode(BufferedImage image, String code, LocalDateTime expireTime) {
		super(code,expireTime);
		this.image = image;
	}
	
	public ImageCode(BufferedImage image, String code, int expireIn) {
		super(code,expireIn);
		this.image = image;
	}
	
	public BufferedImage getImage() {
		return image;
	}

	public void setImage(BufferedImage image) {
		this.image = image;
	}

	
	
	
	
	
}
