package com.mi360.aladdin.mall.controller.auth;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mi360.aladdin.account.service.PcAccountService;
import com.mi360.aladdin.mall.util.CaptchaUtil;
import com.mi360.aladdin.mall.util.QiNiuUtil;
import com.mi360.aladdin.mall.util.WebUtil;
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
		// String mqId=(String)WebUtil.getCurrentUserInfo().get("mqId");
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
	public String paymentPassword(String requestId, String loginPassword, String paymentPassword, Integer fromPay,
			String location, ModelMap modelMap) {
		// String mqId=WebUtil.getCurrentSessionUserAuthInfo().getMqId();
		String mqId = "790b664ff0b946a5adf6488a1ae8e6cb";
		MapData serviceData = MapUtil.newInstance(userService.existPaymentPassword(requestId, mqId));
		logger.info(serviceData.dataString());
		if (serviceData.getBoolean("result")) {
			return "redirect:/user";
		}
		modelMap.addAttribute("fromPay", fromPay);
		modelMap.addAttribute("location", location);
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
		MapData serviceData2 = MapUtil.newInstance(verticalSettlementService.countSales(requestId, mqId));
		logger.info(serviceData2.dataString());
		modelMap.addAttribute("total", (int) Math.ceil(serviceData2.getDouble("result") / pageSize));
		modelMap.addAttribute("page", page);
		return "user/sales";
	}

	@RequestMapping("/collects")
	public String collects(String requestId, ModelMap modelMap, Integer page, Integer pageSize) {
		if (page == null || page < 1) {
			page = 1;
		}
		if (pageSize == null || pageSize < 1) {
			pageSize = 15;
		}
		// String mqId=WebUtil.getCurrentSessionUserAuthInfo().getMqId();
		String mqId = "d9afefcc54ec4a2ca6ca099e8cbd2413";
		List<Map<String, Object>> data = productCollectService.getProductCollectListByMqID(mqId, (page - 1) * 15, page * 15, requestId);
		logger.info("========"+data);
		for (Map<String, Object> map : data) {
			map.put("imgPath", QiNiuUtil.getDownloadUrl((String) map.get("imgPath")));
		}
		modelMap.addAttribute("productCollects",data);
		modelMap.addAttribute("total", (int) Math
				.ceil(((double) productCollectService.getProductCollectCountByMqID(mqId, requestId)) / pageSize));
		modelMap.addAttribute("page", page);
		return "user/collects";
	}

	@RequestMapping("/team")
	public String team(String requestId, ModelMap modelMap) {
		String mqId = "6313b50f20754261846c10fb23c6d33b";
		modelMap.addAttribute("mqId", mqId);
		MapData serviceData = MapUtil
				.newInstance(verticalDistributionService.findDirectlyMember(requestId, mqId, false, null, null));
		logger.info(serviceData.dataString());
		modelMap.addAttribute("directlyMembers", serviceData.getObject("result"));
		return "user/team";
	}

	@RequestMapping("/team/query")
	@ResponseBody
	public Object teamQuery(String requestId, String mqId) {
		MapData serviceData = MapUtil
				.newInstance(verticalDistributionService.findDirectlyMember(requestId, mqId, false, null, null));
		return serviceData.getObject("result");
	}

	@RequestMapping("/logout")
	@ResponseBody
	public String logout(String requestId) {
		WebUtil.login(null);
		return "success";
	}
	
	@RequestMapping("/wealth")
	public String wealth(String requestId){
		return "user/wealth";
	}
}
