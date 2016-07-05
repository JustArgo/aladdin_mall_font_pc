package com.mi360.aladdin.mall.controller;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hummingbird.common.ext.SessionUserAuthInfo;
import com.mi360.aladdin.interaction.wx.service.WxInteractionService;
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
	private Logger logger=Logger.getLogger(this.getClass());
	@Autowired
	private PcUserService userService;

	@Value("${host_name}")
	private String hostName;
	
	@Value("${wx_host_name}")
	private String wxHostName;
	
	@Autowired
	private WxInteractionService wxInteractionService;
	
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
				logger.info("登录用户信息："+loginUserInfo);
				WebUtil.login(loginUserInfo);
				return "success";
			}
		}
		return "username_password_error";
	}
	
	/**
	 * 提交
	 * 
	 * @param uuid
	 *        随机字符串，用来管理用户微信登录
	 */
	@RequestMapping(value = "/wxlogin")
	public String wxAuthentication(String requestId, String uuid) {
		
		// 从用户服务读取
		String mqId = "";
		Integer userId = null;
		Integer luckNum = null;
		
		// 根据uuid查询用户服务redis缓存信息
		Map<String,Object> userInfoMap = wxInteractionService.getSanUserInfo(requestId, uuid);
		
		
		// 如果读取到赋值给以下变量，读取不到，
		if(userInfoMap!=null){
			
			mqId = (String) userInfoMap.get("mqId");
			userId = (Integer) userInfoMap.get("userId");
			luckNum = (Integer) userInfoMap.get("luckNum");
			
		}else{
			return "fail";
		}
		// return "logintimeout",提示用户已经操作超时，请重新刷新二维码；
		
		// 从用户服务读取
		
		
		Map<String, Object> loginUserInfo = new SessionUserAuthInfo().toMap();
		loginUserInfo.put("userId", String.valueOf(userId));
		loginUserInfo.put("mqId", mqId);
		loginUserInfo.put("luckNum", luckNum);
		logger.info("登录用户信息："+loginUserInfo);
		WebUtil.login(loginUserInfo);
		return "redirect:/user";
	}

	@RequestMapping("/iswxlogin")
	@ResponseBody
	public Map<String,Object> isWxScanLogin(String requestId, String uuid){
		
		Map<String,Object> retMap = new HashMap<String,Object>();
		
		Map<String,Object> userInfoMap = wxInteractionService.getSanUserInfo(requestId, uuid);
		if(userInfoMap!=null){
			retMap.put("errcode", 0);
		}else{
			retMap.put("errcode", 10000);
		}
		
		return retMap;
		
	}
	
	/**
	 * 页面
	 */
	@RequestMapping()
	public String index(String requestId, Model model) {
		model.addAttribute("wxHostName",wxHostName);
		return "login";
	}
}
