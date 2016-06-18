package com.mi360.aladdin.mall.controller;

import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mi360.aladdin.account.service.AccountService;
import com.mi360.aladdin.mall.Principal;
import com.mi360.aladdin.mall.util.QRCodeUtil;
import com.mi360.aladdin.mall.util.QiNiuUtil;
import com.mi360.aladdin.mall.util.WebUtil;
import com.mi360.aladdin.product.service.IProductCollectService;
import com.mi360.aladdin.receadd.domain.Address;
import com.mi360.aladdin.receadd.service.IAddressService;
import com.mi360.aladdin.unionpay.service.UnionpayService;
import com.mi360.aladdin.user.service.UserService;
import com.mi360.aladdin.util.MapUtil;
import com.mi360.aladdin.util.MapUtil.MapData;
import com.mi360.aladdin.vertical.distribution.service.VerticalDistributionService;
import com.mi360.aladdin.vertical.settlement.service.VerticalSettlementService;

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
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	@Value("${host_name}")
	private String hostName;
	@Autowired
	private AccountService accountService;
	@Autowired
	private UserService userService;
	@Autowired
	private IProductCollectService productCollectService;
	@Autowired
	private VerticalDistributionService verticalDistributionService;
	@Autowired
	private VerticalSettlementService verticalSettlementService;

	@Autowired
	private IAddressService addressService;
	@Autowired
	private UnionpayService unionpayService;

	/**
	 * 个人中心
	 */
	@RequestMapping(value = "/index", method = RequestMethod.GET)
	public String index(String requestId, ModelMap modelMap) {
		Principal principal = WebUtil.getCurrentPrincipal();
		String mqId = principal.getMqId();
		MapData data = MapUtil.newInstance(accountService.getRemainingSum(requestId, mqId));
		logger.info(data.dataString());
		MapData data2 = MapUtil.newInstance(userService.findSimpleUserInfo(requestId, mqId));
		logger.error(data2.dataString());
		MapData data3 = MapUtil.newInstance(verticalDistributionService.countMemberAll(requestId, mqId));
		logger.error(data3.dataString());
		modelMap.addAttribute("remainingSum", data.getObject("result") == null ? 0 : data.getObject("result"));
		modelMap.addAttribute("userInfo", data2.getObject("result") == null ? 0 : data2.getObject("result"));
		modelMap.addAttribute("memberCount", data3.getObject("result") == null ? 0 : data3.getObject("result"));
		return "user/index";
	}

	/**
	 * 我的财富
	 */
	@RequestMapping(value = "/wealth", method = RequestMethod.GET)
	public String wealth(String requestId, ModelMap modelMap) {
		Principal principal = WebUtil.getCurrentPrincipal();
		MapData data = MapUtil.newInstance(accountService.getAccountInfo(requestId, principal.getMqId()));
		logger.info(data.dataString());
		modelMap.addAttribute("accountInfo", data.getObject("result"));
		return "user/wealth";
	}

	/**
	 * 余额明细
	 */
	@RequestMapping(value = "/wealthDetail", method = RequestMethod.GET)
	public String wealthDetail(String requestId, ModelMap modelMap, Integer tabIdx) {
		modelMap.put("tabIdx", tabIdx == null ? 0 : tabIdx);
		return "user/wealthDetail";
	}

	/**
	 * 余额明细查询
	 */
	@RequestMapping(value = "/wealthDetail/query", method = RequestMethod.POST)
	@ResponseBody
	public Object wealthDetailQuery(String requestId, ModelMap modelMap, String accountType, int page, int pageSize) {
		Principal principal = WebUtil.getCurrentPrincipal();
		MapData data = MapUtil.newInstance(accountService.getAccountDetail(requestId, principal.getMqId(), accountType, page, pageSize));
		logger.info(data.dataString());
		return data.getObject("result");
	}

	/**
	 * 我要推广
	 * 
	 * @return
	 */
	@RequestMapping(value = "/generalize", method = RequestMethod.GET)
	public String generalize(String requestId, ModelMap modelMap) {
		Principal principal = WebUtil.getCurrentPrincipal();
		modelMap.addAttribute("hostName", hostName);
		modelMap.addAttribute("id", principal.getUserId());
		return "user/generalize";
	}

	/**
	 * 退出
	 * 
	 * @param requestId
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping(value = "/logout", method = RequestMethod.GET)
	@ResponseBody
	public void logout(String requestId, HttpServletResponse response) throws Exception {
		WebUtil.getSession().setAttribute(Principal.ATTRIBUTE_KEY, null);
		response.setContentType("text/html;charset=utf-8");
		response.getWriter().println("<h2 style=\"text-align:center;\">已退出</h2>");
	}

	/**
	 * 分享二维码
	 * 
	 * @param requestId
	 * @param response
	 * @param modelMap
	 * @throws Exception
	 */
	@RequestMapping(value = "/qrCode", method = RequestMethod.GET)
	public void qrCode(String requestId, HttpServletResponse response, ModelMap modelMap) throws Exception {
		Principal principal = WebUtil.getCurrentPrincipal();
		int userId = principal.getUserId();
		response.setContentType("image/jpeg");
		OutputStream os = response.getOutputStream();
		String content = hostName + "?iv=" + userId;
		QRCodeUtil.encode(os, content);
		os.flush();
		os.close();
	}

	/**
	 * 提现
	 * 
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/withDraw", method = RequestMethod.GET)
	public String withDraw(String requestId, ModelMap modelMap) throws Exception {
		Principal principal = WebUtil.getCurrentPrincipal();
		MapData data = MapUtil.newInstance(accountService.getRemainingSum(requestId, principal.getMqId()));
		logger.info(data.dataString());
		if (data.getErrcode() != 0) {
			throw data.getException();
		}
		MapData resultData = MapUtil.newInstance(unionpayService.bankCardQuery(requestId, WebUtil.getCurrentPrincipal().getMqId()));
		logger.info(resultData.getData());
		if (resultData.getErrcode() != 0) {
			throw resultData.getException();
		}
		modelMap.addAttribute("remainingSum", data.getObject("result"));
		modelMap.addAttribute("bankCardList",resultData.getObject("result"));
		return "user/withDraw";
	}

	/**
	 * 充值
	 * 
	 * @return
	 */
	@RequestMapping(value = "/recharge", method = RequestMethod.GET)
	public String recharge(String requestId, ModelMap modelMap, Integer tabIdx) {
		modelMap.put("tabIdx", tabIdx == null ? 0 : tabIdx);
		return "user/recharge";
	}

	/**
	 * 充值记录
	 * 
	 * @return
	 */
	@RequestMapping(value = "/recharge/query", method = RequestMethod.POST)
	@ResponseBody
	public Object rechargeQuery(String requestId, ModelMap modelMap, int page, int pageSize) {
		Principal principal = WebUtil.getCurrentPrincipal();
		MapData data = MapUtil.newInstance(accountService.getRechargeHist(requestId, principal.getMqId(), page, pageSize));
		logger.info(data.dataString());
		return data.getObject("result");
	}

	/**
	 * 充值记录明细
	 * 
	 * @return
	 */
	@RequestMapping(value = "/recharge/detail", method = RequestMethod.GET)
	public String rechargeDetail(String requestId, ModelMap modelMap, long id) throws Exception {
		MapData data = MapUtil.newInstance(accountService.getRechargeDetail(requestId, id));
		logger.info(data.dataString());
		if (data.getErrcode() != 0) {
			data.getException();
		}
		modelMap.addAttribute("rechargeDetail", data.getObject("result"));
		return "user/rechargeDetail";
	}

	/**
	 * 提交充值申请
	 * 
	 * @return
	 * @throws ParseException
	 */
	@RequestMapping(value = "/applyOfflineRecharge", method = RequestMethod.POST)
	public String applyOfflineRecharge(String requestId, ModelMap modelMap, double sum, String externalOrderId, String externalOrderTime, String phone, String remark) throws ParseException {
		String sumString = String.valueOf(sum);
		if (sumString.substring(sumString.indexOf('.'), sumString.length()).length() > 3) {
			modelMap.addAttribute("result", 1);
		} else {
			Principal principal = WebUtil.getCurrentPrincipal();
			// externalOrderTime = externalOrderTime.replace("T", " ");
			// Date externalOrderDate = sdf.parse(externalOrderTime);
			MapData data = MapUtil.newInstance(accountService.applyOfflineRecharge(requestId, principal.getMqId(), principal.getLuckNum(), (long) (sum * 100), externalOrderId, null, phone, remark));
			logger.info(data.dataString());
			if (data.getErrcode() == 0) {
				modelMap.addAttribute("result", 0);
			} else {
				modelMap.addAttribute("result", 1);
			}
		}
		modelMap.addAttribute("money", sum);
		return "user/applyRechargeResult";

	}

	/**
	 * 提交提现申请
	 * 
	 * @param money
	 *            提现金额（分）
	 * @return
	 */
	@RequestMapping(value = "/applyWithDraw", method = RequestMethod.POST)
	public String applyWithDraw(String requestId, ModelMap modelMap, double money) {
		if (logger.isDebugEnabled()) {
			logger.debug("提交提现申请/user/applyWithDraw 开始,requestId=" + requestId);
		}
		String moneyString = String.valueOf(money);
		if (moneyString.substring(moneyString.indexOf('.'), moneyString.length()).length() > 3) {
			modelMap.addAttribute("result", 1);
		} else {
			Principal principal = WebUtil.getCurrentPrincipal();

			MapData data = MapUtil.newInstance(accountService.applyWithDraw(requestId, (int) (money * 100), principal.getMqId()));
			System.out.println(data.dataString());
			logger.info(data.dataString());
			if (data.getErrcode() == 0) {
				modelMap.addAttribute("result", 0);
			} else {
				// 判断一下
				if (data.getErrcode() == AccountService.ApplyWithDrawErrcode.e210605.getCode()) {
					if (logger.isDebugEnabled()) {
						logger.debug("提交提现申请/user/applyWithDraw失败,超过微信限额,转到银行卡提现页面");
					}
					// 查询所有银行卡信息
					List<Map<String, Object>> allBank = userService.getAllBank(requestId);
					modelMap.addAttribute("allBank", allBank);
					// 查询当前用户的银行卡信息
					List<Map<String, Object>> userBanks = userService.getUserBanks(requestId, principal.getMqId());
					// {user_id=1621f314a9574a4e8918a3e38a33f85f,
					// account_name=张三, bank_name=ICBC, id=42,
					// account_no=543543}
					modelMap.addAttribute("userBanks", userBanks);
					// 加载省份信息
					List<Address> provinces = addressService.getSubAddress(100, requestId);
					modelMap.addAttribute("provinces", provinces);
					// 加载第一个城市信息
					List<Address> cities = new ArrayList<Address>();
					cities = addressService.getSubAddress(10, requestId);
					modelMap.addAttribute("cities", cities);
					modelMap.addAttribute("money", moneyString);
					MapData remainingSumdata = MapUtil.newInstance(accountService.getRemainingSum(requestId, principal.getMqId()));
					modelMap.addAttribute("remainingSum", remainingSumdata.getObject("result") == null ? 0 : remainingSumdata.getObject("result"));
					return "user/withDrawUsingBankcard";
				} else if (data.getErrcode() == AccountService.ApplyWithDrawErrcode.e210601.getCode()) {
					modelMap.addAttribute("errmsg", AccountService.ApplyWithDrawErrcode.e210601.getMsg());

				}
				modelMap.addAttribute("result", 1);
			}
		}
		modelMap.addAttribute("money", money);
		if (logger.isDebugEnabled()) {
			logger.debug("提交提现申请/user/applyWithDraw 完成");
		}
		return "user/applyWithDrawResult";
	}

	/**
	 * 提交提现到银行卡申请
	 * 
	 * @param money
	 *            提现金额（分）
	 * @return
	 */
	@RequestMapping(value = "/applyWithDrawUsingBankcard", method = RequestMethod.POST)
	public String applyWithDrawUsingBankcard(String requestId, ModelMap modelMap, double money, int bankCardId) {
		if (logger.isDebugEnabled()) {
			logger.debug("提交提现到银行卡申请 /user/applyWithDrawUsingBankcard 开始,requestId=" + requestId);
		}
		String moneyString = String.valueOf(money);
		if (moneyString.substring(moneyString.indexOf('.'), moneyString.length()).length() > 3) {
			modelMap.addAttribute("result", 1);
		} else {
			Principal principal = WebUtil.getCurrentPrincipal();

			MapData data = MapUtil
					.newInstance(accountService.applyWithDrawUsingBankcard(requestId, (int) (money * 100), principal.getMqId(),bankCardId));
			System.out.println(data.dataString());
			logger.info(data.dataString());
			if (data.getErrcode() == 0) {
				modelMap.addAttribute("result", 0);
			} else {
				modelMap.addAttribute("result", 1);
			}
		}
		modelMap.addAttribute("money", money);
		if (logger.isDebugEnabled()) {
			logger.debug("提交提现到银行卡申请 /user/applyWithDrawUsingBankcard 完成");
		}
		return "user/applyWithDrawResult";
	}

	/**
	 * 我的收藏
	 * 
	 * @return
	 */
	@RequestMapping(value = "/collect", method = RequestMethod.GET)
	public String collect(String requestId, ModelMap modelMap) {
		Principal principal = WebUtil.getCurrentPrincipal();
		String mqId = principal.getMqId();
		modelMap.addAttribute("counts", productCollectService.getProductCollectCountByMqID(mqId, requestId));
		return "user/collect";
	}

	/**
	 * 我的收藏查询
	 * 
	 * @return
	 */
	@RequestMapping(value = "/collect/query")
	@ResponseBody
	public Object collectQuery(String requestId, int start, int pageSize) {
		Principal principal = WebUtil.getCurrentPrincipal();
		List<Map<String, Object>> data = productCollectService.getProductCollectListByMqID(principal.getMqId(), start, pageSize, requestId);
		for (Map<String, Object> map : data) {
			map.put("imgPath", QiNiuUtil.getDownloadUrl((String) map.get("imgPath")));
		}
		return data;
	}

	/**
	 * 我的销售
	 * 
	 * @return
	 */
	@RequestMapping(value = "/sales", method = RequestMethod.GET)
	public String sales(String requestId, ModelMap modelMap) {
		Principal principal = WebUtil.getCurrentPrincipal();
		String mqId = principal.getMqId();
		MapData data = MapUtil.newInstance(verticalSettlementService.countSales(requestId, mqId));
		logger.info(data.dataString());
		modelMap.addAttribute("counts", data.getObject("result"));
		return "user/sales";
	}

	/**
	 * 我的销售查询
	 * 
	 * @return
	 */
	@RequestMapping(value = "/sales/query", method = RequestMethod.POST)
	@ResponseBody
	public Object salesQuery(String requestId, ModelMap modelMap, int page, int pageSize) {
		Principal principal = WebUtil.getCurrentPrincipal();
		String mqId = principal.getMqId();
		MapData data = MapUtil.newInstance(verticalSettlementService.findSales(requestId, mqId, page, pageSize));
		logger.info(data.dataString());
		return data.getObject("result");
	}

	/**
	 * 我的团队
	 * 
	 * @return
	 */
	@RequestMapping(value = "/team", method = RequestMethod.GET)
	public String team(String requestId, ModelMap modelMap, Integer tabIdx) {
		Principal principal = WebUtil.getCurrentPrincipal();
		String mqId = principal.getMqId();
		MapData data = MapUtil.newInstance(verticalDistributionService.findMemberCount(requestId, mqId));
		logger.info(data.dataString());
		modelMap.put("tabIdx", tabIdx == null ? 0 : tabIdx);
		modelMap.put("counts", data.getObject("result"));
		return "user/team";
	}

	/**
	 * 我的团队查询
	 * 
	 * @return
	 */
	@RequestMapping(value = "/team/query", method = RequestMethod.POST)
	@ResponseBody
	public Object teamQuery(String requestId, ModelMap modelMap, int levelNum, int page, int pageSize) {
		Principal principal = WebUtil.getCurrentPrincipal();
		String mqId = principal.getMqId();
		MapData data = MapUtil.newInstance(verticalDistributionService.findMemberByLevelNum(requestId, mqId, levelNum, page, pageSize));
		logger.info(data.dataString());
		return data.getObject("result");
	}

	/**
	 * 点击头像 查看个人信息
	 */
	@RequestMapping("/info-index")
	public String infoIndex(String requestId, Integer isRefresh, Model model) {
		Principal principal = WebUtil.getCurrentPrincipal();
		String mqId = principal.getMqId();
		MapData data = MapUtil.newInstance(userService.findUserByMqId(requestId, mqId));
		MapData result = data.getResult();
		String headImage = result.getString("headImage");
		String nickName = result.getString("nickName");
		String luckNum = result.getString("luckNum");

		logger.info(data.dataString());

		if (headImage != null && !headImage.equals("")) {
			model.addAttribute("headImage", headImage);
		}
		model.addAttribute("isRefresh", isRefresh);
		model.addAttribute("nickName", nickName);
		model.addAttribute("luckNum", luckNum);
		return "user/info-index";
	}

	@RequestMapping("/refresh-wx-info")
	@ResponseBody
	public Map<String, Object> refreshWxInfo(String requestId) {

		Map<String, Object> retMap = new HashMap<String, Object>();
		retMap.put("errcode", 0);

		Principal principal = WebUtil.getCurrentPrincipal();
		String mqId = principal.getMqId();
		try {
			userService.flushWxUserInfoByMqId(requestId, mqId);
		} catch (Exception e) {
			logger.info(e);
			retMap.put("errcode", 10000);
			retMap.put("errmsg", "刷新失败");
		}

		return retMap;

	}
}
