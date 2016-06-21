package com.mi360.aladdin.mall.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

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
public class LoginController {
	@Autowired
	private PcUserService userService;

	/**
	 * 登陆验证
	 * 
	 * @param password
	 *            密码
	 * @param username
	 *            用户名
	 */
	@RequestMapping(value = "loginAuthentication")
	@ResponseBody
	public boolean loginAuthentication(String requestId, String password, String username) {
		MapData serviceData=null;
		if (username.matches("^1\\d{10}$")) {
			serviceData = MapUtil
					.newInstance(userService.loginAuthentication(requestId, password, username, null));
		} else if (username.matches("^.*@.*\\..*")) {
			serviceData = MapUtil
					.newInstance(userService.loginAuthentication(requestId, password, null, username));
		} else {
			return false;
		}
		if (serviceData.getErrcode() != 0) {
			return false;
		} else {
			String mqId = serviceData.getString("result");
			// TODO 登陆添加session信息
			return true;
		}
	}
}
