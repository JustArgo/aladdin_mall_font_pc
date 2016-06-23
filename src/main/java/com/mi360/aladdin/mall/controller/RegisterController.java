package com.mi360.aladdin.mall.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mi360.aladdin.mall.Principal;
import com.mi360.aladdin.mall.util.CaptchaUtil;
import com.mi360.aladdin.mall.util.WebUtil;
import com.mi360.aladdin.message.email.service.EmailVerifyService;
import com.mi360.aladdin.user.service.PcUserService;
import com.mi360.aladdin.util.MapUtil;
import com.mi360.aladdin.util.MapUtil.MapData;

/**
 * 注册
 * 
 * @author ek
 *
 */
@Controller
@RequestMapping(value = "/register")
public class RegisterController {
	@Autowired
	private PcUserService userService;

	/**
	 * 页面
	 */
	@RequestMapping()
	public String index(String requestId) {
		return "register";
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
			int errcode = serviceData2.getErrcode();
			if (errcode != 0) {
				return "error";
			}
			Integer luckNum = serviceData2.getInteger("luckNum");
			String mqId = serviceData2.getString("mqId");
			Integer userId = serviceData2.getInteger("iv");
			Principal principal = new Principal(userId, mqId, null, luckNum);
			WebUtil.login(principal);
			return "success_phone";
		} else if (username.matches("^.*@.*\\..*")) {
			serviceData = MapUtil.newInstance(userService.existEmail(requestId, username));
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
			int errcode = serviceData2.getErrcode();
			if (errcode != 0 || errcode != 0) {
				return "error";
			}
			Integer luckNum = serviceData2.getInteger("luckNum");
			String mqId = serviceData2.getString("mqId");
			Integer userId = serviceData2.getInteger("iv");
			Principal principal = new Principal(userId, mqId, null, luckNum);
			WebUtil.login(principal);
			return "success_email";
		} else {
			return "username_error";
		}
	}

	@RequestMapping("/success/phone")
	public String successPhone(String requestId) {
		return "success-phone";
	}

	@RequestMapping("/success/email")
	public String successEmail(String requestId) {
		return "success-phone";
	}
}
