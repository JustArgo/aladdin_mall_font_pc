package com.mi360.aladdin.mall.controller;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mi360.aladdin.mall.util.CaptchaUtil;
import com.mi360.aladdin.mall.util.PwdKey;
import com.mi360.aladdin.mall.util.WebUtil;
import com.mi360.aladdin.user.service.PcUserService;
import com.mi360.aladdin.util.MapUtil;
import com.mi360.aladdin.util.MapUtil.MapData;

/**
 * 密码
 * 
 * @author JSC
 *
 */
@Controller
@RequestMapping("/password")
public class PasswordController {
	private Logger logger = Logger.getLogger(this.getClass());
	@Autowired
	private PcUserService userService;
	@Value("${host_name}")
	private String hostName;

	/**
	 * 页面
	 */
	@RequestMapping("/find")
	public String index(String requestId) {
		return "password/find";
	}

	/**
	 * 验证用户名
	 * 
	 * @param username
	 *            用户名
	 * @param captcha
	 *            验证码
	 */
	@RequestMapping("/find/check")
	@ResponseBody
	public String check(String requestId, String username, String captcha) {
		boolean captchaPassed = CaptchaUtil.validate(captcha);
		if (!captchaPassed) {
			return "captcha_error";
		}
		MapData serviceData = null;
		if (username.matches("^1\\d{10}$")) {
			serviceData = MapUtil.newInstance(userService.existPhone(requestId, username));
		} else if (username.matches("^.*@.*\\..*")) {
			serviceData = MapUtil.newInstance(userService.existEmail(requestId, username));
		} else {
			return "username_not_exists";
		}
		logger.info(serviceData.dataString());
		if (serviceData.getErrcode() != 0) {
			return "error";
		}
		if (!serviceData.getBoolean("result")) {
			return "username_not_exists";
		} else {
			PwdKey pwdKey = new PwdKey(username);
			WebUtil.getSession().setAttribute("pwdKey", pwdKey);
			return hostName + "/password/find/vertify/" + pwdKey.getValue();
		}
	}

	/**
	 * 验证身份页面
	 * 
	 * @param username
	 *            用户名
	 * @param value
	 *            密匙
	 */
	@RequestMapping("/find/vertify/{value}")
	public String vertify(String requestId, @PathVariable String value, ModelMap modelMap) {
		logger.info(WebUtil.getSession().getId());
		PwdKey pwdKey = (PwdKey) WebUtil.getSession().getAttribute("pwdKey");
		if (!pwdKey.vertify(value)) {
			return "404.html";
		}
		String username = pwdKey.getUsername();
		MapData serviceData = MapUtil.newInstance(userService.findSimpleUserInfoByUsername(requestId, username));
		if (serviceData.getErrcode() != 0) {
			return "404.html";
		}
		MapData result = serviceData.getResult();
		String nickname = result.getString("nickname");
		String phone = result.getString("phone");
		StringBuilder sBuilder = new StringBuilder(phone);
		phone = sBuilder.replace(3, 7, "****").toString();
		modelMap.addAttribute("nickname", result.getString("nickname"));
		modelMap.addAttribute("phone", phone);
		return "password/find-vertify";
	}
}
