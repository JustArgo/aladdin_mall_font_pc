package com.mi360.aladdin.mall.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mi360.aladdin.account.service.AccountService;
import com.mi360.aladdin.account.service.PcAccountService;
import com.mi360.aladdin.mall.Principal;
import com.mi360.aladdin.mall.util.WebUtil;

@Controller
@RequestMapping("/wealth")
public class WealthController {

	Logger logger = Logger.getLogger(this.getClass());

	private static final int DEFAULT_PAGE_SIZE = 10;
	
	private static final int THIRTY_DAY = 24*30*60*60*1000;
	
	private static final String CASH_TYPE = "KYY";
	
	private static final String FROZEN_TYPE = "DJY";

	@Autowired
	private PcAccountService pcAccountService;
	
	@Autowired
	private AccountService accountService;
	
	
	

	/**
	 * 查看我的财富
	 * 
	 * @param requestID
	 * @param model
	 * @return
	 */
	@RequestMapping("/wealth-index")
	public String wealthIndex(String requestId, Model model, Integer pageIndex) {

		String mqID = getAccountInfo(requestId, model);
		System.out.println("mqID"+mqID);
		if(pageIndex==null){
			pageIndex = 1;
		}
		
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		
		List<Map<String, Object>> resultList = (List<Map<String, Object>>)pcAccountService.getAccountDetailByDate(requestId, mqID, CASH_TYPE, new Date(new Date().getTime() - THIRTY_DAY), new Date(), pageIndex, DEFAULT_PAGE_SIZE).get("result");
		
		
		// 5、查询出我的近一个月的收支明细
		if(resultList != null){
			list.addAll(resultList);
		}
		
		model.addAttribute("cashList", list);

		return "/wealth/wealth-index";
	}

	private String getAccountInfo(String requestId, Model model) {
		Map<String,Object> principal = WebUtil.getCurrentUserInfo();
		String mqID = (String)principal.get("mqId");
		
		Map<String, Object>  accountMap = (Map<String, Object>) pcAccountService.getAccountInfo(requestId, mqID).get("result");
		
		if(accountMap!=null){
			// 1 、查询出我的可用余额
			model.addAttribute("remainingSum", accountMap.get("remainingSum")==null?0:accountMap.get("remainingSum"));
			// 2、查询出我的累计收入
			model.addAttribute("totalEaring", accountMap.get("totalEaring")==null?0:accountMap.get("totalEaring"));
			// 3、查询出我的累计提现
			model.addAttribute("totalWithdraw", accountMap.get("totalWithdraw")==null?0:accountMap.get("totalWithdraw"));
			// 4、查询出我的冻结余额
			model.addAttribute("frozenSum", accountMap.get("frozenSum")==null?0:accountMap.get("frozenSum"));
		}else{
			model.addAttribute("remainingSum", 0);
			model.addAttribute("totalEaring", 0);
			model.addAttribute("totalWithdraw", 0);
			model.addAttribute("frozenSum", 0);
		}
		return mqID;
	}
	
	/**
	 * 查询我的充值记录
	 * 
	 * @param requestID
	 * @param model
	 * @return
	 */
	@RequestMapping("/recharge-list")
	public String rechargeList(String requestId, Model model, Integer pageIndex, HttpServletRequest request) {

		String mqID = getAccountInfo(requestId, model);
		
		
		if(pageIndex==null){
			pageIndex = 1;
		}
		
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		
		Map<String, Object> map = accountService.getRechargeHist(requestId, mqID, pageIndex, DEFAULT_PAGE_SIZE);
		System.out.println("map"+map);
		List<Map<String, Object>> resultList = (List<Map<String, Object>>)map.get("result");
		
		
		// 5、查询我的充值记录
		if(resultList != null){
			list.addAll(resultList);
		}
		
		model.addAttribute("rechargeList", list);

		return "/wealth/recharge-list";
	}
	
	/**
	 * 查询我的冻结余额记录
	 * 
	 * @param requestID
	 * @param model
	 * @return
	 */
	@RequestMapping("/frozen-list")
	public String frozenList(String requestId, Model model, Integer pageIndex, HttpServletRequest request) {

		String mqID = getAccountInfo(requestId, model);
		
		if(pageIndex==null){
			pageIndex = 1;
		}
		
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		
		List<Map<String, Object>> resultList = (List<Map<String, Object>>)pcAccountService.getAccountDetailByDate(requestId, mqID, FROZEN_TYPE, new Date(new Date().getTime() - THIRTY_DAY), new Date(), pageIndex, DEFAULT_PAGE_SIZE).get("result");
		
		
		// 5、查询我的冻结余额记录
		if(resultList != null){
			list.addAll(resultList);
		}
		
		model.addAttribute("frozenList", list);

		return "/wealth/frozen-list";
	}
	
	/**
	 * 访问线下充值页面
	 * 
	 * @param requestID
	 * @param model
	 * @return
	 */
	@RequestMapping("/off-recharge-index")
	public String offRechargeIndex(String requestId, Model model) {
		
		Map<String,Object> principal = WebUtil.getCurrentUserInfo();
		model.addAttribute("luckNum", principal.get("luckNum"));

		return "/wealth/off-recharge-index";
	}
	
	/**
	 * 线下充值成功页面
	 * 
	 * @param requestID
	 * @param model
	 * @return
	 */
	@RequestMapping("/off-recharge-result")
	public String offRechargeResult(String requestId, Model model) {
		

		return "/wealth/off-recharge-result";
	}
	
	/**
	 * 提交线下充值申请
	 * 
	 * @param requestID
	 * @param model
	 * @return
	 */
	@RequestMapping("/off-recharge")
	@ResponseBody
	public String offRecharge(String requestId, Model model, String sum, String externalOrderId, String phone,
			String remark) {
		String mqID = getAccountInfo(requestId, model);
		long sumLong = NumberUtils.toLong(String.valueOf(NumberUtils.toDouble(sum)*100));
		System.out.println(sumLong);
		
		Map<String, Object> map = accountService.applyOfflineRecharge(requestId, mqID, (Integer)WebUtil.getCurrentUserInfo().get("luckNum"), sumLong, externalOrderId, null, phone, remark);
		
		System.out.println("map" +map);

		return "success";
	}
	
	
	/**
	 * 提交线下充值申请
	 * 
	 * @param requestID
	 * @param model
	 * @return
	 */
	@RequestMapping("/withdraw")
	public String withdraw(String requestId, Model model, String sum, String externalOrderId, String phone,
			String remark) {
		
		String mqID = (String)WebUtil.getCurrentUserInfo().get("mqId");
		Map<String, Object> map = accountService.getRemainingSum(requestId, mqID);

		model.addAttribute("remainingSum", map.get("result"));

		return "/wealth/withdraw";
	}
	
	/**
	 * 提交微信提现申请
	 * 
	 * @param requestID
	 * @param model
	 * @return
	 */
	@RequestMapping("/withdraw-alipay")
	public String withdrawByAlipay(String requestId, Model model, String alipaySum, String alipayAccount, String alipayName,
			String payPwd1) {
		
		String mqID = (String)WebUtil.getCurrentUserInfo().get("mqId");
		
		long sumLong = NumberUtils.toLong(String.valueOf(NumberUtils.toDouble(alipaySum)*100));
		
//		Map<String, Object> map = accountService.applyOfflineRecharge(requestId, mqID, (Integer)WebUtil.getCurrentUserInfo().get("luckNum"), sumLong, externalOrderId, null, phone, remark);
		
//		System.out.println("map" +map);

		return "success";
	}
	
	/**
	 * 提交微信提现申请
	 * 
	 * @param requestID
	 * @param model
	 * @return
	 */
	@RequestMapping("/withdraw-wx")
	public String withdrawByWX(String requestId, Model model, String wxSum, String wxAccount,
			String payPwd2) {
		
		String mqID = (String)WebUtil.getCurrentUserInfo().get("mqId");
		
		long sumLong = NumberUtils.toLong(String.valueOf(NumberUtils.toDouble(wxSum)*100));
		
		//Map<String, Object> map = accountService.applyOfflineRecharge(requestId, mqID, (Integer)WebUtil.getCurrentUserInfo().get("luckNum"), sumLong, externalOrderId, null, phone, remark);
		
//		System.out.println("map" +map);

		return "success";
	}
	
}
