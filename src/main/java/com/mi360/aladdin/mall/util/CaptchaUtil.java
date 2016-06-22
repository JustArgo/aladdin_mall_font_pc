package com.mi360.aladdin.mall.util;

import java.awt.image.BufferedImage;

import com.octo.captcha.service.captchastore.FastHashMapCaptchaStore;
import com.octo.captcha.service.image.DefaultManageableImageCaptchaService;
import com.octo.captcha.service.image.ImageCaptchaService;

/**
 * 验证码工具
 * 
 * @author ek
 *
 */
public class CaptchaUtil {
	private static ImageCaptchaService instance;

	private CaptchaUtil() {
	}

	static {
		instance = new DefaultManageableImageCaptchaService(new FastHashMapCaptchaStore(),
				new ImageCaptchaEngineExtend(), 180, 100000, 75000);
	}

	/**
	 * 生成
	 * 
	 * @return 验证码图片
	 */
	public static BufferedImage createImg() {
		return instance.getImageChallengeForID(WebUtil.getSession().getId(), WebUtil.getRequest().getLocale());
	}

	/**
	 * 验证
	 * 
	 * @return 验证码是否正确
	 */
	public static boolean validate(String code) {
		return instance.validateResponseForID(WebUtil.getSession().getId(), code);
	}
}
