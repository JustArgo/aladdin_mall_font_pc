package com.mi360.aladdin.mall.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hummingbird.common.ext.SessionUserAuthInfo;
import com.mi360.aladdin.mall.util.CaptchaUtil;
import com.mi360.aladdin.mall.util.WebUtil;
import com.mi360.aladdin.user.service.PcUserService;
import com.mi360.aladdin.util.MapUtil;
import com.mi360.aladdin.util.MapUtil.MapData;

/**
 * 首页控制器
 * 
 * @author JSC
 *
 */
@Controller
@RequestMapping(value = "/login")
public class LoginController {
	@Autowired
	private PcUserService userService;

	/**
	 * 提交
	 * 
	 * @param password
	 *            密码
	 * @param username
	 *            用户名
	 */
	@RequestMapping(value = "/submit")
	@ResponseBody
	public String authentication(String requestId, String captcha, String password, String username) {
		boolean captchaPassed = CaptchaUtil.validate(captcha);
		if (!captchaPassed) {
			return "captcha_error";
		}
		MapData serviceData = null;
		if (username.matches("^1\\d{10}$")) {
			serviceData = MapUtil.newInstance(userService.loginAuthentication(requestId, password, username, null));
		} else if (username.matches("^.*@.*\\..*")) {
			serviceData = MapUtil.newInstance(userService.loginAuthentication(requestId, password, null, username));
		} else {
			return "username_password_error";
		}
		if (serviceData.getErrcode() == 0) {
			String mqId = serviceData.getString("result");
			MapData serviceData2 = MapUtil.newInstance(userService.findSimpleUserInfo(requestId, mqId));
			if (serviceData2.getErrcode() == 0) {
				MapData resultData = serviceData2.getResult();
				int userId = resultData.getInteger("id");
				int luckNum = resultData.getInteger("luckNum");
				Map<String, Object> loginUserInfo = new SessionUserAuthInfo().toMap();
				loginUserInfo.put("userId", String.valueOf(userId));
				loginUserInfo.put("mqId", mqId);
				loginUserInfo.put("luckNum", luckNum);
				WebUtil.login(loginUserInfo);
				return "success";
			}
		}
		return "username_password_error";
	}

	/**
	 * 页面
	 */
	@RequestMapping()
	public String index(String requestId) {
		return "login";
	}
}
