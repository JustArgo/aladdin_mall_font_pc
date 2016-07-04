package com.mi360.aladdin.mall.controller;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/alipay")
public class AliPayController {
	
	private Logger logger = Logger.getLogger(AliPayController.class);
	
	/**
	 * 支付宝支付 成功后 前台回调地址
	 * @return
	 * 2016年7月2日
	 */
	@RequestMapping("/directpay/redirect")
	public String redirect(String requestId, String is_success, String out_trade_no, String trade_status, String total_fee, Model model){
		
		logger.info("requestId:"+requestId+" is_success:"+is_success+" out_trade_no:"+out_trade_no+" trade_status:"+trade_status+" total_fee:"+total_fee);
		
		model.addAttribute("orderCode",out_trade_no);
		
		if("T".equals(is_success) && "TRADE_SUCCESS".equals(trade_status)){
			
			model.addAttribute("pSum",Double.valueOf(total_fee)*100);
			return "order/pay-success";
			
		}else{
			return "order/pay-fail";
		}
		
		
	}
	
}
