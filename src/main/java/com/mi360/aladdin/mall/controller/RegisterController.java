package com.mi360.aladdin.mall.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mi360.aladdin.mall.util.CaptchaUtil;
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

	@RequestMapping()
	public String index(String requestId) {
		return "register";
	}

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
			userService.createPc(requestId, ivInt, password, username, null);
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
			userService.createPc(requestId, ivInt, password, null, username);
		} else {
			return "username_error";
		}
		return "success";
	}
}
