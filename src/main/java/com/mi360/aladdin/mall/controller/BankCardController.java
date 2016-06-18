package com.mi360.aladdin.mall.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.chanjar.weixin.common.util.StringUtils;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mi360.aladdin.entity.account.BankCard;
import com.mi360.aladdin.entity.account.BankCode;
import com.mi360.aladdin.entity.order.Order;
import com.mi360.aladdin.mall.Principal;
import com.mi360.aladdin.mall.util.WebUtil;
import com.mi360.aladdin.order.service.IOrderService;
import com.mi360.aladdin.unionpay.service.UnionpayService;
import com.mi360.aladdin.util.MapUtil;
import com.mi360.aladdin.util.MapUtil.MapData;

/**
 * 银行卡
 * 
 * @author JSC
 *
 */
@Controller
@RequestMapping("/bankCard")
public class BankCardController extends BaseWxController {
	private Logger logger = Logger.getLogger(this.getClass());
	@Autowired
	private UnionpayService unionpayService;
	
	@Autowired
	private IOrderService orderService;
	
	/**
	 * 银行卡管理
	 * 
	 * @return
	 */
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public String bankCard(String requestId) {
		return "/bankCard/list";
	}

	/**
	 * 银行卡管理查询
	 * 
	 * @return
	 */
	@RequestMapping(value = "/query", method = RequestMethod.POST)
	@ResponseBody
	public Object bankCardQuery(String requestId, int page, int pageSize) throws Exception {
		Principal principal = WebUtil.getCurrentPrincipal();
		MapData resultData = MapUtil.newInstance(unionpayService.bankCardQuery(requestId, principal.getMqId()));
		logger.info(resultData.getData());
		if (resultData.getErrcode() != 0) {
			throw resultData.getException();
		}
		return resultData.getObject("result");
	}
	
	/**
	 * 添加银行卡，步骤1
	 */
	@RequestMapping("/add/1")
	public String add(String requestId, String orderCode,String addType , Model model){
		model.addAttribute("addType",addType);
		if (AddType.pay==AddType.valueOf(addType)) {
			model.addAttribute("orderCode",orderCode);
		}
		return "bankCard/add1";
	}

	/**
	 * 添加银行卡，步骤2
	 */
	@RequestMapping("/add/2")
	public String add2(String requestId, String accNo, String orderCode, String addType, Model model){
		
		model.addAttribute("addType",addType);
		if (AddType.pay==AddType.valueOf(addType)) {
			model.addAttribute("orderCode",orderCode);
		}
		
		Map<String, Object> retMap = unionpayService.openQuery(requestId, null, new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()), new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()), accNo);
		logger.info("retMap:"+retMap);
		
		//代表未开通
		String cardTypeName = "信用卡";//BankCard.cardType.valueOf(retMap.get("payCardType"));
		String payCardType  = "unknown";
		String bankName = "";
		
		if((Integer)retMap.get("errcode")==0 || (Integer)retMap.get("errcode")==1){
			
		}
		
		Map<String,Object> bankMap = unionpayService.selectBankNameByIssInsCode(requestId, (String)retMap.get("issInsCode"));
		if((Integer)bankMap.get("errcode")==0){
			bankName = (String)bankMap.get("bankName");
		}
		
		if(retMap.get("payCardType")!=null){
			payCardType = (String)retMap.get("payCardType");
		}
		
		if("unknown".equals(payCardType)){
			//查找出所有的银行
			List<BankCode> bankCodeList = unionpayService.getAllBankCode(requestId);
			model.addAttribute("bankCodeList",bankCodeList);
		}
		
		if("01".equals(retMap.get("payCardType"))){
			cardTypeName = "储蓄卡";
		}else if("02".equals(retMap.get("payCardType"))){
			cardTypeName = "信用卡";
		}
		
		if((Integer)retMap.get("errcode")==0){
			model.addAttribute("isOpen","true");
		}else if((Integer)retMap.get("errcode")==1){
			model.addAttribute("isOpen","false");
		}else{
			model.addAttribute("isOpen","unknown");
		}
		
		String regex = "(.{4})";
		String accNoStr = accNo.replaceAll(regex,"$1 ");
		
		model.addAttribute("accNoStr",accNoStr);
		model.addAttribute("accNo",accNo);
		model.addAttribute("payCardType",payCardType);
		model.addAttribute("cardTypeName",cardTypeName);
		model.addAttribute("issInsCode",(String)retMap.get("issInsCode"));
		model.addAttribute("bankName",bankName);
		
		//BankCard bankCard = bankCardService.getBankCardByAccNo();
		
		return "bankCard/add2";
	}
	
	@RequestMapping("/del")
	public String del(String requestId,int id) throws Exception{
		MapData resultData=MapUtil.newInstance(unionpayService.deleteBankCard(requestId, WebUtil.getCurrentPrincipal().getMqId(), id));
		logger.info(resultData.getData());
		if (resultData.getErrcode()!=0) {
			throw resultData.getException();
		}
		return "redirect:list";
	}
	
	/**
	 * 判断当前用户是否已绑定该银行卡
	 * @param requestId
	 * @param accNo
	 * @return
	 */
	@RequestMapping("/exists-bankcard")
	@ResponseBody
	public Map<String,Object> existsBankCard(String requestId, String accNo){
		
		Principal principal = WebUtil.getCurrentPrincipal();
		String mqID = principal.getMqId();
		
		Map<String,Object> retMap = new HashMap<String,Object>();
		BankCard bankCard = unionpayService.existsBankCard(requestId, mqID, accNo);
		if(bankCard!=null){
			retMap.put("errcode",0);
			retMap.put("cardId",bankCard.getId());
		}else{
			retMap.put("errcode", 10000);
		}
		
		return retMap;
		
	}
	
	@RequestMapping("/bind-card")
	@ResponseBody
	public Map<String,Object> bindCard(String requestId, String isOpen, String payCardType, String accNo, String name, String idCard, String phoneNo, String expired, String cvn2, String issInsCode, String bankName){
		
		
		Principal principal = WebUtil.getCurrentPrincipal();
		String mqID = principal.getMqId();
		
		Map<String,Object> retMap = new HashMap<String,Object>();
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		try{
			if(!"01".equals(payCardType) && !"02".equals(payCardType)){//既不是储蓄卡也不是信用卡
				retMap.put("errcode", 10001);
				retMap.put("errmsg", "本平台暂时只支持绑定储蓄卡和信用卡");
				return retMap;
			}
			
			logger.info("expired:"+expired);
			if(StringUtils.isNotBlank(expired)){
				expired = expired.substring(3,5)+expired.substring(0,2);
				logger.info("expired:"+expired);
			}
			
			if("true".equals(isOpen)){
				try{
					
					BankCard existsBankCard = unionpayService.existsBankCard(requestId, mqID, accNo);
					
					if(existsBankCard==null){
						BankCard bankCard = new BankCard();
						bankCard.setCardNum(accNo);
						bankCard.setHolderName(name);
						bankCard.setInsertTime(new Date());
						bankCard.setPayCardType(payCardType);
						bankCard.setIssInsCode(issInsCode);
						bankCard.setBankName(bankName);
						bankCard.setMqId(mqID);
						bankCard.setPhone(phoneNo);
						bankCard.setSafeCode(cvn2);
						bankCard.setIsDefault(0);
						bankCard.setIdCard(idCard);
						bankCard.setStatus("OK#");
						int cardId = unionpayService.addBankCard(requestId, bankCard);
						if(cardId!=0){
							retMap.put("errcode",0);
							retMap.put("cardId", cardId);
						}else{
							retMap.put("errcode", 10001);
							retMap.put("errmsg","绑定失败");
						}
					}else{
						retMap.put("errcode", 0);
						retMap.put("cardId", existsBankCard.getId());
					}
					
				}catch(Exception e){
					logger.error(e.getMessage(),e);
					retMap.put("errcode",10000);
					retMap.put("errmsg","绑定银行卡失败");
					return retMap;
				}
			}else{
				logger.info("");
				Map<String,Object> openMap = unionpayService.openCardBack(requestId, null, sdf.format(new Date()), sdf.format(new Date()), payCardType, accNo, phoneNo, null, cvn2, expired);//前端expired需要做处理
				logger.info("openMap:"+openMap);
				if((Integer)openMap.get("errcode")==0){//如果开通成功 还要判断是否存在该银行卡 因为有可能用户一开始绑定  后来关闭认证支付 再到本平台 
					try{
						
						BankCard existsBankCard = unionpayService.existsBankCard(requestId, mqID, accNo);
						
						if(existsBankCard==null){
							BankCard bankCard = new BankCard();
							bankCard.setCardNum(accNo);
							bankCard.setHolderName(name);
							bankCard.setInsertTime(new Date());
							bankCard.setPayCardType(payCardType);
							bankCard.setIssInsCode(issInsCode);
							bankCard.setBankName(bankName);
							bankCard.setMqId(mqID);
							bankCard.setPhone(phoneNo);
							bankCard.setSafeCode(cvn2);
							bankCard.setIsDefault(0);
							bankCard.setIdCard(idCard);
							bankCard.setStatus("OK#");
							int cardId = unionpayService.addBankCard(requestId, bankCard);
							if(cardId!=0){
								retMap.put("errcode",0);
								retMap.put("cardId", cardId);
							}else{
								retMap.put("errcode", 10001);
								retMap.put("errmsg","绑定失败");
							}
						}else{
							retMap.put("errcode", 0);
							retMap.put("cardId", existsBankCard.getId());
						}
					}catch(Exception e){
						logger.error(e.getMessage(),e);
						retMap.put("errcode",10000);
						retMap.put("errmsg","绑定银行卡失败");
						return retMap;
					}
				}else{
					return openMap;
				}
			}
		}catch(Exception e){
			logger.error(e.getMessage(),e);
			retMap.put("errcode", 10000);
			retMap.put("errmsg", "开通失败");
		}
		
		return retMap;
		
	}
	
	@RequestMapping("/verify-code")
	public String verifyCode(String requestId, String orderCode, Integer cardId, Model model){
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		
		BankCard bankCard = unionpayService.getBankCardById(requestId, cardId);
		String accNo = bankCard.getCardNum();
		String phoneNo = bankCard.getPhone();
		
		Order parentOrder = orderService.getOrderByOrderCode(orderCode, requestId);
		String txnTime = sdf.format(parentOrder.getCreateTime());
		
		Map<String, Object> retMap = unionpayService.sendPaySMSCode(requestId, null, orderCode, txnTime, parentOrder.getpSum().toString(), phoneNo, accNo);
		logger.info(retMap);
		model.addAttribute("phoneNo",phoneNo);
		phoneNo = phoneNo.substring(0, 4)+"****"+phoneNo.substring(8);
		model.addAttribute("phoneNo2",phoneNo);
		model.addAttribute("orderCode",orderCode);
		model.addAttribute("txnTime",txnTime);
		model.addAttribute("txnAmt",parentOrder.getpSum());
		model.addAttribute("accNo",accNo);
		
		return "bankCard/verify-code";
	}
	
	@RequestMapping("/send-smscode")
	@ResponseBody
	public Map<String, Object> sendSmsCode(String requestId, String orderCode, String phoneNo, String txnTime, String txnAmt, String accNo, Model model){
		
		Map<String, Object> retMap = new HashMap<String,Object>();
		
		try{
			retMap = unionpayService.sendPaySMSCode(requestId, null, orderCode, txnTime, txnAmt, phoneNo, accNo);
		}catch(Exception e){
			retMap.put("errcode",10001);
			retMap.put("errmsg","系统繁忙,请稍候重试");
		}
		logger.info(retMap);
		return retMap;
		
	}
	
	/**
	 * 银联付款
	 * @param requestId
	 * @param orderCode
	 * @param txnAmt
	 * @param accNo
	 * @param smsCode
	 * @return
	 */
	@RequestMapping("pay-with-sms")
	@ResponseBody
	public Map<String, Object> payWithSms(String requestId, String orderCode, String txnAmt, String accNo, String smsCode){
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		
		Map<String, Object> retMap = new HashMap<String,Object>();
		
		try{
			retMap = unionpayService.pay(requestId, null, orderCode, sdf.format(new Date()), txnAmt, accNo, smsCode);
		}catch(Exception e){
			logger.error(e.getMessage(),e);
			retMap.put("errcode", 10000);
			retMap.put("errmsg", "网络异常,请稍候再试");
		}
		
		logger.info(retMap);
		
		return retMap;
		
	}
	
	/**
	 * 添加银行卡行为
	 * @author hsf
	 *
	 */
	public enum AddType{
		/**支付*/
		pay,
		/**添加*/
		add,
		/**提现*/
		withDraw;
	}
}
