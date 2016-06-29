package com.mi360.aladdin.mall.controller;

import java.util.Date;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
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
	@Autowired
	private SmsCodeVerifyService smsCodeVerifyService;

	private static final String REGISTER_PHONE_KEY = "registerPhoneKey";
	private static final String PASSWORD_FIND_SMS_WAIT_SECOND_KEY = "passwordFindSmsWaitSecondKey";

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
			WebUtil.getSession().setAttribute(REGISTER_PHONE_KEY, username);
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
		MapData serviceData = MapUtil.newInstance(emailVerifyService.getEmail(requestId, code, "REG"));
		logger.info(serviceData.dataString());
		if (serviceData.getErrcode() != 0) {
			throw new Exception();
		}
		MapData serviceData2 = MapUtil.newInstance(userService.registerActivation(requestId, serviceData.getString("result")));
		if (serviceData2.getErrcode() == 0) {
			return "register/success-email";
		}else {
			throw new Exception();
		}
	}

	/**
	 * 成功 - 手机
	 */
	@RequestMapping("/vertify/phone")
	public String successPhone(String requestId, ModelMap modelMap) {
		String phone = (String) WebUtil.getSession().getAttribute(REGISTER_PHONE_KEY);
		StringBuilder sBuilder = new StringBuilder(phone);
		phone = sBuilder.replace(3, 7, "****").toString();
		modelMap.addAttribute("phone", phone);
		ExpireKey smsSecond = (ExpireKey) WebUtil.getSession().getAttribute(PASSWORD_FIND_SMS_WAIT_SECOND_KEY);
		long second = 0;
		if (smsSecond != null && !smsSecond.hasExpired()) {
			Date now = new Date();
			Date expire = smsSecond.getExpire();
			second = (expire.getTime() - now.getTime()) / 1000;
			second = second <= 0 ? 0 : second;
		}
		modelMap.addAttribute("sms_wait_second", second);
		return "register/vertify-phone";
	}

	/**
	 * 成功 - 邮箱
	 */
	@RequestMapping("/vertify/email")
	public String successEmail(String requestId) {
		return "register/vertify-email";
	}

	/**
	 * 发送短信
	 * 
	 */
	@RequestMapping("/sms")
	@ResponseBody
	public String sms(String requestId) {
		ExpireKey smsSecond = (ExpireKey) WebUtil.getSession().getAttribute(PASSWORD_FIND_SMS_WAIT_SECOND_KEY);
		if (smsSecond != null && !smsSecond.hasExpired()) {
			return "error";
		}
		String phone = (String) WebUtil.getSession().getAttribute(REGISTER_PHONE_KEY);
		MapData serviceData = MapUtil.newInstance(smsCodeVerifyService.send(requestId, phone, "REG"));
		if (serviceData.getErrcode() == 0) {
			smsSecond = new ExpireKey(60000);// 一分钟过期
			WebUtil.getSession().setAttribute(PASSWORD_FIND_SMS_WAIT_SECOND_KEY, smsSecond);
			return "success";
		} else {
			return "error";
		}
	}

	@RequestMapping("/sms/vertify")
	@ResponseBody
	public String smsVertify(String requestId, String captcha) throws Exception {
		String phone = (String) WebUtil.getSession().getAttribute(REGISTER_PHONE_KEY);
		MapData serviceData = MapUtil.newInstance(smsCodeVerifyService.verify(requestId, phone, captcha, "REG"));
		if (serviceData.getErrcode() == 0) {
			boolean passed = serviceData.getBoolean("result");
			if (passed) {
				MapData serviceData2 = MapUtil.newInstance(userService.registerActivation(requestId, phone));
				logger.info(serviceData2.dataString());
				if (serviceData2.getErrcode() == 0) {
					return "success";
				}else {
					return "error";
				}
			} else {
				return "captcha_error";
			}
		} else {
			throw new Exception();
		}
	}
	
	@RequestMapping("/success/phone")
	public String successPhone(String requestId){
		return "register/success-phone";
	}
}
