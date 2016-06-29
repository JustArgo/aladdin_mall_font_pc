package com.mi360.aladdin.mall.controller.auth;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mi360.aladdin.mall.util.WebUtil;
import com.mi360.aladdin.user.service.PcUserService;
import com.mi360.aladdin.util.MapUtil;
import com.mi360.aladdin.util.MapUtil.MapData;

/**
 * 用户
 * 
 * @author JSC
 *
 */
@Controller
@RequestMapping("/user")
public class UserController {
	private Logger logger = Logger.getLogger(this.getClass());
	@Autowired
	private PcUserService userService;

	@RequestMapping()
	public String index(String requestId, ModelMap modelMap) {
		// String mqId=WebUtil.getCurrentSessionUserAuthInfo().getMqId();
		MapData serviceData = MapUtil
				.newInstance(userService.findSimpleUserInfo(requestId, "790b664ff0b946a5adf6488a1ae8e6cb"));
		logger.info(serviceData.dataString());
		modelMap.addAttribute("userInfo", serviceData.getObject("result"));
		return "user/index";
	}

}
