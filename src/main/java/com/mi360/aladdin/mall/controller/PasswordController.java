package com.mi360.aladdin.mall.controller;

import java.util.Date;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mi360.aladdin.mall.util.CaptchaUtil;
import com.mi360.aladdin.mall.util.ExpireKey;
import com.mi360.aladdin.mall.util.WebUtil;
import com.mi360.aladdin.message.email.service.EmailVerifyService;
import com.mi360.aladdin.message.sms.service.SmsCodeVerifyService;
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
	@Autowired
	private SmsCodeVerifyService smsCodeVerifyService;
	@Autowired
	private EmailVerifyService emailVerifyService;
	@Value("${host_name}")
	private String hostName;
	private static final String PASSWORD_FIND_USERNAME_KEY = "passwordFindVertify";
	private static final String PASSWORD_FIND_SMS_WAIT_SECOND_KEY = "passwordFindSmsWaitSecondKey";
	private static final String PASSWORD_FIND_RESET_KEY = "passwordFindResetKey";
	private static final String PASSWORD_FIND_RESET_SUBMIT_KEY = "passwordFindResetSubmitKey";

	/**
	 * 页面
	 */
	@RequestMapping("/find")
	public String index(String requestId) {
		return "password/find";
	}

	/**
	 * 找回登陆密码 - 验证用户名
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
			ExpireKey expireKey = new ExpireKey(600000);// 10分钟后过期
			expireKey.setAttribution(username);
			WebUtil.getSession().setAttribute(PASSWORD_FIND_USERNAME_KEY, expireKey);
			return hostName + "/password/find/vertify";
		}
	}

	/**
	 * 找回登陆密码 - 验证身份页面
	 * 
	 * @param username
	 *            用户名
	 * @param value
	 *            密匙
	 */
	@RequestMapping("/find/vertify")
	public String vertify(String requestId, ModelMap modelMap) throws Exception {
		ExpireKey usernameKey = (ExpireKey) WebUtil.getSession().getAttribute(PASSWORD_FIND_USERNAME_KEY);
		if (usernameKey == null || usernameKey.hasExpired()) {
			throw new Exception();
		}
		String username = (String) usernameKey.getAttribution();
		MapData serviceData = MapUtil.newInstance(userService.findSimpleUserInfoByUsername(requestId, username));
		if (serviceData.getErrcode() != 0) {
			throw new Exception();
		}
		MapData result = serviceData.getResult();
		String nickname = result.getString("nickname");
		modelMap.addAttribute("nickname", nickname);
		if (username.matches("^1\\d{10}$")) {
			ExpireKey smsSecond = (ExpireKey) WebUtil.getSession().getAttribute(PASSWORD_FIND_SMS_WAIT_SECOND_KEY);
			long second = 0;
			if (smsSecond != null && !smsSecond.hasExpired()) {
				Date now = new Date();
				Date expire = smsSecond.getExpire();
				second = (expire.getTime() - now.getTime()) / 1000;
				second = second <= 0 ? 0 : second;
			}
			modelMap.addAttribute("sms_wait_second", second);
			String phone = result.getString("phone");
			StringBuilder sBuilder = new StringBuilder(phone);
			phone = sBuilder.replace(3, 7, "****").toString();
			modelMap.addAttribute("phone", phone);
			return "password/find-vertify-phone";
		} else if (username.matches("^.*@.*\\..*")) {
			String pEmail = result.getString("email");
			String email = pEmail;
			StringBuilder sBuilder = new StringBuilder(email);
			int atIdx = sBuilder.indexOf("@");
			int dotIdx = sBuilder.indexOf(".");
			sBuilder.replace(atIdx + 1, dotIdx, "***").replace(atIdx / 2, atIdx, "****");
			email = sBuilder.replace(3, 7, "****").toString();
			modelMap.addAttribute("email", email);
			if (MapUtil.newInstance(emailVerifyService.send(requestId, pEmail, "PWD")).getErrcode() != 0) {
				throw new Exception();
			}
			return "password/find-vertify-email";
		} else {
			throw new Exception();
		}
	}

	/**
	 * 找回登陆密码 - 发送找回登陆密码短信验证
	 */
	@RequestMapping("/find/sms")
	@ResponseBody
	public String sms(String requestId) throws Exception {
		ExpireKey usernameKey = (ExpireKey) WebUtil.getSession().getAttribute(PASSWORD_FIND_USERNAME_KEY);
		if (usernameKey == null) {
			throw new Exception();
		}
		ExpireKey smsSecond = (ExpireKey) WebUtil.getSession().getAttribute(PASSWORD_FIND_SMS_WAIT_SECOND_KEY);
		if (smsSecond != null && !smsSecond.hasExpired()) {
			return "error";
		}
		String username = (String) usernameKey.getAttribution();
		if (!username.matches("^1\\d{10}$")) {
			throw new Exception();
		} else {
			MapData serviceData = MapUtil.newInstance(smsCodeVerifyService.send(requestId, username, "PWD"));
			if (serviceData.getErrcode() == 0) {
				smsSecond = new ExpireKey(60000);// 一分钟过期
				WebUtil.getSession().setAttribute(PASSWORD_FIND_SMS_WAIT_SECOND_KEY, smsSecond);
				return "success";
			} else {
				return "error";
			}
		}
	}

	/**
	 * 找回登陆密码 - 重置登录密码页面
	 * 
	 * @param captcha
	 *            验证码
	 */
	@RequestMapping("/find/reset")
	public String reset(String requestId, String captcha) throws Exception {
		ExpireKey resetKey = (ExpireKey) WebUtil.getSession().getAttribute(PASSWORD_FIND_RESET_KEY);
		if (resetKey == null || resetKey.hasExpired()) {
			throw new Exception();
		}
		ExpireKey usernameKey = (ExpireKey) WebUtil.getSession().getAttribute(PASSWORD_FIND_USERNAME_KEY);
		if (usernameKey == null) {
			throw new Exception();
		}
		ExpireKey expireKey = new ExpireKey(600000);// 10分钟后过期
		expireKey.setAttribution(usernameKey.getAttribution());// 用户名
		WebUtil.getSession().setAttribute(PASSWORD_FIND_RESET_SUBMIT_KEY, expireKey);
		return "password/find-reset";
	}

	/**
	 * 找回登陆密码 - 重置登录密码提交
	 * 
	 * @param password
	 *            登录密码
	 */
	@RequestMapping("/find/reset/submit")
	public String resetSubmit(String requestId, String password) throws Exception {
		ExpireKey resetKey = (ExpireKey) WebUtil.getSession().getAttribute(PASSWORD_FIND_RESET_SUBMIT_KEY);
		if (resetKey == null || resetKey.hasExpired()) {
			throw new Exception();
		}
		String username = (String) resetKey.getAttribution();
		MapData serviceData = MapUtil.newInstance(userService.resetLoginPassword(requestId, username, password));
		if (serviceData.getErrcode() != 0) {
			throw new Exception();
		}
		WebUtil.getSession().removeAttribute(PASSWORD_FIND_RESET_SUBMIT_KEY);
		return "password/find-complete";
	}

	/**
	 * 找回登陆密码 - 验证短信验证码
	 * 
	 * @param captcha
	 *            短信验证码
	 */
	@RequestMapping("/find/sms/vertify")
	@ResponseBody
	public String smsVertify(String requestId, String captcha) throws Exception {
		ExpireKey usernameKey = (ExpireKey) WebUtil.getSession().getAttribute(PASSWORD_FIND_USERNAME_KEY);
		if (usernameKey == null) {// 只是获取一下手机号码
			throw new Exception();
		}
		String username = (String) usernameKey.getAttribution();
		if (!username.matches("^1\\d{10}$")) {
			throw new Exception();
		} else {
			MapData serviceData = MapUtil.newInstance(smsCodeVerifyService.verify(requestId, username, captcha, "PWD"));
			if (serviceData.getErrcode() == 0) {
				boolean passed = serviceData.getBoolean("result");
				if (passed) {
					ExpireKey expireKey = new ExpireKey(600000);// 10分钟后过期
					WebUtil.getSession().setAttribute(PASSWORD_FIND_RESET_KEY, expireKey);
					return "success";
				} else {
					return "captcha_error";
				}
			} else {
				throw new Exception();
			}
		}
	}

	/**
	 * 找回登陆密码 - 通过邮箱重置登录密码页面
	 * 
	 * @param code
	 *            找回登陆密码邮箱验证码
	 */
	@RequestMapping("/find/email/vertify/{code}")
	public String emailVertify(String requestId, @PathVariable String code) throws Exception {
		MapData serviceData = MapUtil.newInstance(emailVerifyService.getEmail(requestId, code, "PWD"));
		if (serviceData.getErrcode() != 0) {
			throw new Exception();
		}
		logger.info(serviceData.dataString());
		ExpireKey expireKey = new ExpireKey(600000);// 10分钟后过期
		expireKey.setAttribution(serviceData.getString("result"));// 用户名
		WebUtil.getSession().setAttribute(PASSWORD_FIND_RESET_SUBMIT_KEY, expireKey);
		return "password/find-reset";
	}

}
