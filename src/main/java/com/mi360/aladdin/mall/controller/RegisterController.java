package com.mi360.aladdin.mall.controller;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mi360.aladdin.mall.util.CaptchaUtil;
import com.mi360.aladdin.mall.util.WebUtil;
import com.mi360.aladdin.message.email.service.EmailVerifyService;
import com.mi360.aladdin.user.service.PcUserService;
import com.mi360.aladdin.util.MapUtil;
import com.mi360.aladdin.util.MapUtil.MapData;
import com.radiadesign.catalina.session.SessionUserAuthInfo;

/**
 * 注册
 * 
 * @author ek
 *
 */
@Controller
@RequestMapping(value = "/register")
public class RegisterController {
	private Logger logger = Logger.getLogger(this.getClass());
	@Autowired
	private PcUserService userService;
	@Autowired
	private EmailVerifyService emailVerifyService;

	/**
	 * 页面
	 */
	@RequestMapping()
	public String index(String requestId) {
		return "register/register";
	}

	/**
	 * 提交
	 * 
	 * @param iv
	 *            邀请码
	 * @param captcha
	 *            验证码
	 * @param password
	 *            密码
	 * @param username
	 *            用户名
	 */
	@RequestMapping("/submit")
	@ResponseBody
	public String submit(String requestId, String iv, String captcha, String password, String username) {
		boolean captchaPassed = CaptchaUtil.validate(captcha);
		if (!captchaPassed) {
			return "captcha_error";
		}
		MapData serviceData = null;
		if (username.matches("^1\\d{10}$")) {
			serviceData = MapUtil.newInstance(userService.existPhone(requestId, username));
			logger.info(serviceData.dataString());
			if (serviceData.getBoolean("result")) {
				return "username_exists";
			}
			Integer ivInt = null;
			if (iv != null) {
				try {
					ivInt = Integer.valueOf(iv);
				} catch (Exception e) {
				}
			}
			MapData serviceData2 = MapUtil
					.newInstance(userService.createPc(requestId, ivInt, password, username, null));
			logger.info(serviceData2.dataString());
			int errcode = serviceData2.getErrcode();
			if (errcode != 0) {
				return "error";
			}
			Integer luckNum = serviceData2.getInteger("luckNum");
			String mqId = serviceData2.getString("mqId");
			Integer userId = serviceData2.getInteger("iv");
			SessionUserAuthInfo sessionUserAuthInfo = new SessionUserAuthInfo();
			sessionUserAuthInfo.setUserId(userId);
			sessionUserAuthInfo.setMqId(mqId);
			sessionUserAuthInfo.setLuckNum(luckNum);
			WebUtil.login(sessionUserAuthInfo);
			return "success_phone";
		} else if (username.matches("^.*@.*\\..*")) {
			serviceData = MapUtil.newInstance(userService.existEmail(requestId, username));
			logger.info(serviceData.dataString());
			if (serviceData.getBoolean("result")) {
				return "username_exists";
			}
			Integer ivInt = null;
			if (iv != null) {
				try {
					ivInt = Integer.valueOf(iv);
				} catch (Exception e) {
				}
			}
			MapData serviceData2 = MapUtil
					.newInstance(userService.createPc(requestId, ivInt, password, null, username));
			logger.info(serviceData2.dataString());
			int errcode = serviceData2.getErrcode();
			if (errcode != 0 || errcode != 0) {
				return "error";
			}
			Integer luckNum = serviceData2.getInteger("luckNum");
			String mqId = serviceData2.getString("mqId");
			Integer userId = serviceData2.getInteger("iv");
			SessionUserAuthInfo sessionUserAuthInfo = new SessionUserAuthInfo();
			sessionUserAuthInfo.setUserId(userId);
			sessionUserAuthInfo.setMqId(mqId);
			sessionUserAuthInfo.setLuckNum(luckNum);
			WebUtil.login(sessionUserAuthInfo);
			return "success_email";
		} else {
			return "username_error";
		}
	}

	/**
	 * 确认 - 邮箱
	 * 
	 * @param code
	 *            校验码
	 * @throws Exception 
	 */
	@RequestMapping("/confirm/email/{code}")
	public String confirmEmail(String requestId, @PathVariable String code) throws Exception {
		MapData serviceData = MapUtil.newInstance(emailVerifyService.verify(requestId, code, "REG"));
		logger.info(serviceData.dataString());
		if (serviceData.getErrcode() != 0) {

			throw new Exception();
//			return "404.html";
		}
		boolean passed = serviceData.getBoolean("result");
		if (passed) {
			return "register/confirm/success-email";
		} else {

			throw new Exception();
//			return "404.html";
		}
	}

	/**
	 * 成功 - 手机
	 */
	@RequestMapping("/success/phone")
	public String successPhone(String requestId) {
		return "register/success-phone";
	}

	/**
	 * 成功 - 邮箱
	 */
	@RequestMapping("/success/email")
	public String successEmail(String requestId) {
		return "register/success-email";
	}
}
