package com.mi360.aladdin.mall.controller.auth;

import java.util.Date;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mi360.aladdin.account.service.PcAccountService;
import com.mi360.aladdin.mall.util.WebUtil;
import com.mi360.aladdin.product.service.IProductCollectService;
import com.mi360.aladdin.product.service.IProductService;
import com.mi360.aladdin.user.service.PcUserService;
import com.mi360.aladdin.util.MapUtil;
import com.mi360.aladdin.util.MapUtil.MapData;
import com.mi360.aladdin.vertical.distribution.service.PcVerticalDistributionService;
import com.mi360.aladdin.vertical.settlement.service.PcVerticalSettlementService;

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
	@Autowired
	private PcAccountService accountService;
	@Autowired
	private PcVerticalDistributionService verticalDistributionService;
	@Autowired
	private PcVerticalSettlementService verticalSettlementService;
	@Autowired
	private IProductCollectService productCollectService;

	@RequestMapping()
	public String index(String requestId, ModelMap modelMap) {
		// String mqId=WebUtil.getCurrentSessionUserAuthInfo().getMqId();
		String mqId = "790b664ff0b946a5adf6488a1ae8e6cb";
		MapData serviceData = MapUtil.newInstance(userService.findSimpleUserInfo(requestId, mqId));
		logger.info(serviceData.dataString());
		modelMap.addAttribute("userInfo", serviceData.getObject("result"));
		MapData serviceData2 = MapUtil.newInstance(accountService.getRemainingSum(requestId, mqId));
		logger.info(serviceData2.dataString());
		modelMap.addAttribute("remainingSum", serviceData2.getObject("result"));
		MapData serviceData3 = MapUtil.newInstance(verticalDistributionService.countMemberAll(requestId, mqId));
		logger.info(serviceData3.dataString());
		modelMap.addAttribute("countMemberAll", serviceData3.getObject("result"));
		Date now = new Date();
		Date fiveDateAgo = new Date(now.getTime() - 432000000);// 5天前
		MapData serviceData4 = MapUtil
				.newInstance(verticalSettlementService.findDailySales(requestId, mqId, fiveDateAgo, now));
		logger.info(serviceData4.dataString());
		modelMap.addAttribute("dailySales", serviceData4.getObject("result"));
		MapData serviceData5 = MapUtil.newInstance(verticalSettlementService.findDailyTopSales(requestId, now, 5));
		logger.info(serviceData5.dataString());
		modelMap.addAttribute("dailyTopSales", serviceData5.getObject("result"));
		modelMap.addAttribute("productCollects",
				productCollectService.getProductCollectListByMqID(mqId, 0, 10, requestId));
		return "user/index";
	}

	@RequestMapping("/info")
	public String info(String requestId, ModelMap modelMap) {
		// String mqId=WebUtil.getCurrentSessionUserAuthInfo().getMqId();
		String mqId = "790b664ff0b946a5adf6488a1ae8e6cb";
		MapData serviceData = MapUtil.newInstance(userService.findSimpleUserInfo(requestId, mqId));
		logger.info(serviceData.dataString());
		modelMap.addAttribute("userInfo", serviceData.getObject("result"));
		return "user/info";
	}

	@RequestMapping("/save/info")
	@ResponseBody
	public String saveInfo(String requestId, String headImage, Integer sex, String nickname) {
		// String mqId=WebUtil.getCurrentSessionUserAuthInfo().getMqId();
		String mqId = "790b664ff0b946a5adf6488a1ae8e6cb";
		MapData serviceData = MapUtil.newInstance(userService.savePc(requestId, mqId, headImage, nickname, sex));
		if (serviceData.getErrcode() == 0) {
			return "success";
		} else {
			return "error";
		}
	}

	@RequestMapping("/save/loginPassword")
	@ResponseBody
	public String saveLoginPassword(String requestId, String prePassword, String password) {
		// String mqId=WebUtil.getCurrentSessionUserAuthInfo().getMqId();
		String mqId = "790b664ff0b946a5adf6488a1ae8e6cb";
		MapData serviceData = MapUtil
				.newInstance(userService.modifyLoginPassword(requestId, mqId, prePassword, password));
		int errcode = serviceData.getErrcode();
		if (errcode == 0) {
			return "success";
		} else if (errcode == 210603) {
			return "wrong";
		} else if (errcode == 210602) {
			return "wrong_tree_times";
		} else {
			return "error";
		}
	}

	@RequestMapping("/save/paymentPassword")
	@ResponseBody
	public String savePaymentPassword(String requestId, String prePaymentPassword, String paymentPassword) {
		// String mqId=WebUtil.getCurrentSessionUserAuthInfo().getMqId();
		String mqId = "790b664ff0b946a5adf6488a1ae8e6cb";
		MapData serviceData = MapUtil
				.newInstance(userService.modifyPaymentPassword(requestId, mqId, prePaymentPassword, paymentPassword));
		int errcode = serviceData.getErrcode();
		if (errcode == 0) {
			return "success";
		} else if (errcode == 210603) {
			return "wrong";
		} else if (errcode == 210602) {
			return "wrong_tree_times";
		} else {
			return "error";
		}
	}

}
