package com.mi360.aladdin.mall.controller.auth;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
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
	public String index(String requestId) {
		String mqId=WebUtil.g
		userService.findSimpleUserInfo(requestId, mqId);
		return "user/index";
	}

}
