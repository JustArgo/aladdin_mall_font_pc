package com.mi360.aladdin.mall.controller;



import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;


/**
 * 用户
 * 
 * @author JSC
 *
 */
@Controller
@RequestMapping("/user")
public class UserController extends BaseWxController {
	private Logger logger = Logger.getLogger(this.getClass());
	
	@RequestMapping("/password/find")
	public String passwordFind(String requestId){
		return 
	}
}
