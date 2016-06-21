package com.mi360.aladdin.mall.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mi360.aladdin.mall.Principal;
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
	 * 登陆验证
	 * 
	 * @param password
	 *            密码
	 * @param username
	 *            用户名
	 */
	@RequestMapping(value = "/authentication")
	@ResponseBody
	public boolean authentication(String requestId, String password, String username) {
		MapData serviceData = null;
		if (username.matches("^1\\d{10}$")) {
			serviceData = MapUtil.newInstance(userService.loginAuthentication(requestId, password, username, null));
		} else if (username.matches("^.*@.*\\..*")) {
			serviceData = MapUtil.newInstance(userService.loginAuthentication(requestId, password, null, username));
		} else {
			return false;
		}
		if (serviceData.getErrcode() == 0) {
			String mqId = serviceData.getString("result");
			MapData serviceData2 = MapUtil.newInstance(userService.findSimpleUserInfo(requestId, mqId));
			if (serviceData2.getErrcode() == 0) {
				MapData resultData = serviceData2.getResult();
				int userId = resultData.getInteger("id");
				int luckNum = resultData.getInteger("luckNum");
				Principal principal = new Principal(userId, mqId, null, luckNum);
				WebUtil.login(principal);
				return true;
			}
		}
		return false;
	}
	
	@RequestMapping(value = "/page")
	public String page(String requestId) {
		return "login";
	}
}
