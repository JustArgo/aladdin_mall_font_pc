package com.mi360.aladdin.mall.controller.auth;

import java.util.Date;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mi360.aladdin.account.service.PcAccountService;
import com.mi360.aladdin.mall.util.CaptchaUtil;
import com.mi360.aladdin.product.service.IProductCollectService;
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
	public Integer saveInfo(String requestId, String headImage, Integer sex, String nickname) {
		// String mqId=WebUtil.getCurrentSessionUserAuthInfo().getMqId();
		String mqId = "790b664ff0b946a5adf6488a1ae8e6cb";
		MapData serviceData = MapUtil.newInstance(userService.savePc(requestId, mqId, headImage, nickname, sex));
		return serviceData.getErrcode();
	}

	/**
	 * 修改登录密码
	 * 
	 * @param prePassword
	 *            原始登陆密码
	 * @param password
	 *            新登陆密码
	 */
	@RequestMapping("/save/loginPassword")
	@ResponseBody
	public Integer saveLoginPassword(String requestId, String prePassword, String password) {
		// String mqId=WebUtil.getCurrentSessionUserAuthInfo().getMqId();
		String mqId = "790b664ff0b946a5adf6488a1ae8e6cb";
		MapData serviceData = MapUtil
				.newInstance(userService.modifyLoginPassword(requestId, mqId, prePassword, password));
		return serviceData.getErrcode();
	}

	/**
	 * 修改支付密码
	 * 
	 * @param prePassword
	 *            原始支付密码
	 * @param password
	 *            新支付密码
	 */
	@RequestMapping("/save/paymentPassword")
	@ResponseBody
	public Integer savePaymentPassword(String requestId, String prePaymentPassword, String paymentPassword) {
		// String mqId=WebUtil.getCurrentSessionUserAuthInfo().getMqId();
		String mqId = "790b664ff0b946a5adf6488a1ae8e6cb";
		MapData serviceData = MapUtil
				.newInstance(userService.modifyPaymentPassword(requestId, mqId, prePaymentPassword, paymentPassword));
		return serviceData.getErrcode();
	}

	/**
	 * 设置支付密码
	 * 
	 * @param loginPassword
	 *            登陆密码
	 * @param paymentPassword
	 *            支付密码
	 */
	@RequestMapping("/paymentPassword")
	public String paymentPassword(String requestId, String loginPassword, String paymentPassword) {
		// String mqId=WebUtil.getCurrentSessionUserAuthInfo().getMqId();
		String mqId = "790b664ff0b946a5adf6488a1ae8e6cb";
		MapData serviceData = MapUtil.newInstance(userService.existPaymentPassword(requestId, mqId));
		logger.info(serviceData.dataString());
		if (serviceData.getBoolean("result")) {
			return "redirect:/user";
		}
		return "user/first-set-payment-password";
	}

	@RequestMapping("/save/paymentPassword/first")
	@ResponseBody
	public Integer savePaymentPasswordFirst(String requestId, String loginPassword, String paymentPassword,
			String captcha) {
		if (!CaptchaUtil.validate(captcha)) {
			return -1;
		}
		// String mqId=WebUtil.getCurrentSessionUserAuthInfo().getMqId();
		String mqId = "790b664ff0b946a5adf6488a1ae8e6cb";
		MapData serviceData = MapUtil
				.newInstance(userService.firstSetPaymentPassword(requestId, mqId, loginPassword, paymentPassword));
		logger.info(serviceData.dataString());
		return serviceData.getErrcode();
	}

	@RequestMapping("/level")
	public String level(String requestId, ModelMap modelMap) {
		// String mqId=WebUtil.getCurrentSessionUserAuthInfo().getMqId();
		String mqId = "ee9de73cf5a24e1597d916e61bd89365";
		MapData serviceData = MapUtil.newInstance(userService.findSimpleUserInfo(requestId, mqId));
		logger.info(serviceData.dataString());
		MapData resultData = serviceData.getResult();
		modelMap.addAttribute("userInfo", resultData.getData());
		if (resultData.getInteger("isGoldType") == 1) {
			MapData serviceData2 = MapUtil.newInstance(verticalDistributionService.levelupInfo(requestId, mqId));
			logger.info(serviceData2.dataString());
			modelMap.addAttribute("levelInfo", serviceData2.getObject("result"));
		}
		return "user/level";
	}

	@RequestMapping("/sales")
	public String sales(String requestId, ModelMap modelMap, Integer page, Integer pageSize) {
		if (page == null || page < 1) {
			page = 1;
		}
		if (pageSize == null || pageSize < 1) {
			pageSize = 15;
		}
		// String mqId=WebUtil.getCurrentSessionUserAuthInfo().getMqId();
		String mqId = "ee9de73cf5a24e1597d916e61bd89365";
		MapData serviceData = MapUtil.newInstance(verticalSettlementService.findSales(requestId, mqId, page, pageSize));
		logger.info(serviceData.dataString());
		modelMap.addAttribute("sales", serviceData.getObject("result"));
		return "user/sales";
	}

	@RequestMapping("/collects")
	public String collects(String requestId) {
		return "user/collects";
	}

	@RequestMapping("/team")
	public String team(String requestId) {
		return "user/team";
	}
}
