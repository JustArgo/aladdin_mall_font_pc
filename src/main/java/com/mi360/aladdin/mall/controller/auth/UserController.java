package com.mi360.aladdin.mall.controller.auth;

import java.util.Date;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mi360.aladdin.account.service.PcAccountService;
import com.mi360.aladdin.mall.util.WebUtil;
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
		MapData serviceData5 = MapUtil
				.newInstance(verticalSettlementService.findDailyTopSales(requestId, now, 5));
		logger.info(serviceData5.dataString());
		modelMap.addAttribute("dailyTopSales", serviceData5.getObject("result"));
		return "user/index";
	}

}
