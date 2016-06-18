package com.mi360.aladdin.mall.controller;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mi360.aladdin.account.service.AccountService;
import com.mi360.aladdin.entity.order.GoodsReturn;
import com.mi360.aladdin.entity.order.MoneyReturn;
import com.mi360.aladdin.entity.order.Order;
import com.mi360.aladdin.entity.order.OrderCashback;
import com.mi360.aladdin.entity.order.OrderCashbackHist;
import com.mi360.aladdin.entity.order.OrderCashbackHist.FlowDirection;
import com.mi360.aladdin.entity.order.OrderCashbackHist.Status;
import com.mi360.aladdin.entity.order.OrderPayment;
import com.mi360.aladdin.entity.order.OrderProduct;
import com.mi360.aladdin.entity.other.Currency;
import com.mi360.aladdin.mq.service.MqService;
import com.mi360.aladdin.order.service.IOrderProductService;
import com.mi360.aladdin.order.service.IOrderService;
import com.mi360.aladdin.product.service.IProductService;
import com.mi360.aladdin.unionpay.service.UnionpayService;
import com.mi360.aladdin.user.service.UserService;
import com.mi360.aladdin.util.MapUtil;
import com.mi360.aladdin.util.MapUtil.MapData;

@Controller
@RequestMapping("/unionpay")
public class UnionPayController {

	private Logger logger = Logger.getLogger(this.getClass());
	
	private static final String ENCODING = "utf-8";
	
	@Autowired
	private IOrderService orderService;
	
	@Autowired
	private IOrderProductService orderProductService;
	
	@Autowired
	private UnionpayService unionPayService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private MqService mqService;
	
	@Autowired
	private IProductService productService;
	
	@Autowired
	private AccountService accountService;
	
	@RequestMapping("/notify")
	@ResponseBody
	public String notify(String requestId, HttpServletRequest req){
		
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		
		Map<String, String> reqParam = this.getAllRequestParam(req);
		
		logger.info("reqParam:"+reqParam);
		
		String encoding="UTF-8";
		
		Map<String, String> valideData = null;
		if (null != reqParam && !reqParam.isEmpty()) {
			Iterator<Entry<String, String>> it = reqParam.entrySet().iterator();
			valideData = new HashMap<String, String>(reqParam.size());
			while (it.hasNext()) {
				Entry<String, String> e = it.next();
				String key = (String) e.getKey();
				String value = (String) e.getValue();
				try {
					value = new String(value.getBytes(ENCODING), ENCODING);
				} catch (UnsupportedEncodingException e1) {
					e1.printStackTrace();
				}
				valideData.put(key, value);
			}
		}
		
		String txnType = valideData.get("txnType");
		logger.info("txnType："+txnType);
		logger.info("valideData:"+valideData);
		
		Map<String, Object> retMap = unionPayService.validate(requestId, valideData, encoding);
		logger.info("valideRet:"+retMap);
		if(retMap!=null && (Integer)retMap.get("errcode")==0){
			logger.info("验证签名成功");
			//改变订单状态
			
			int currentYear = Calendar.getInstance().get(Calendar.YEAR);
			
			if("04".equals(txnType)){//退款
				String returnCode = valideData.get("reqReserved");
				if(returnCode!=null && returnCode.indexOf(":")>=0){
					String orderCode = valideData.get("orderId");
					String returnType = returnCode.substring(0,returnCode.indexOf(":"));
					if("TH".equals(returnType)){//退货的退款
						
						GoodsReturn goodsReturn = orderService.getGoodsReturnByReturnCode(returnCode.substring(returnCode.indexOf(":")+1), requestId);
						OrderProduct orderProduct = orderProductService.getOrderProductByID(goodsReturn.getOrderProductID(), requestId);
						
						String respCode = valideData.get("respCode");
						String txnAmt   = valideData.get("txnAmt");
						String queryId  = valideData.get("valideData");
						String traceTime = currentYear+valideData.get("traceTime");
						
						if("00".equals(respCode)){//退货成功
							
							goodsReturn.setStatus("COM");
							goodsReturn.setRealReturnSum(Long.valueOf(txnAmt));
							goodsReturn.setThirdReturnNo(queryId);
							
							try {
								goodsReturn.setReturnMoneyTime(sdf.parse(traceTime));
							} catch (ParseException e) {
								logger.error(e.getMessage(),e);
							}
							orderProduct.setReturnGoodsStatus("COM");
							
							//获取用户 的openId 准备发送消息
							MapData userData = MapUtil.newInstance(userService.findSimpleUserInfo(requestId, goodsReturn.getMqID()));
							MapData userResult = userData.getResult();
							String openId = userResult.getString("openId");
							
							try{
								//发送退货队列
								mqService.goodsReturn(requestId, goodsReturn.getReturnCode());
								
								//发送同意退款消息
								Map<String,String> msgParam = new HashMap<String,String>();
								msgParam.put("orderCode", orderCode);
								mqService.sendWxMessage(requestId, openId, "", "57138030ecbd6", msgParam);
								
							}catch(Exception e){
								logger.error(e.getMessage(),e);
							}
							
							//TODO 扣除指定金额
							OrderCashback orderCashback = orderService.getOrderCashbackByOrderCode(requestId, goodsReturn.getOrderCode());
							if(orderCashback!=null){
								orderCashback.setTotalMoney(orderCashback.getTotalMoney()-orderProduct.getSellPrice()*orderProduct.getBuyNum());
								orderService.updateOrderCashback(requestId, orderCashback);
							}
							
							/*更新返现记录*/
							OrderCashbackHist orderCashbackHist  = orderService.selectFxByOrderCode(requestId, goodsReturn.getOrderCode());
							orderCashbackHist.setRemainMoney(orderCashbackHist.getRemainMoney()-orderProduct.getSellPrice()*goodsReturn.getProductCount());
							orderService.updateOrderCashbackHist(requestId, orderCashbackHist);
							/*返现退款扣除返现金额记录*/
							OrderCashbackHist orderCashbackHist2 = new OrderCashbackHist();
							orderCashbackHist2 .setFlowDirection(FlowDirection.out.getCode());
							orderCashbackHist2.setInsertTime(new Date());
							orderCashbackHist2.setMqId(goodsReturn.getMqID());
							orderCashbackHist2.setOrderCode(goodsReturn.getOrderCode());
							orderCashbackHist2.setReturnCode(goodsReturn.getReturnCode());
							orderCashbackHist2.setMoney(orderProduct.getSellPrice()*goodsReturn.getProductCount());
							orderCashbackHist2.setType(com.mi360.aladdin.entity.order.OrderCashbackHist.Type.th.getCode());
							orderService.addOrderCashbackHist(requestId, orderCashbackHist2);
							
							orderService.updateGoodsReturn(goodsReturn, requestId);
							orderProductService.updateOrderProduct(orderProduct, requestId);
							
						}else{
							goodsReturn.setStatus("JTK");
							
							//修改对应的订单商品的状态
							orderProduct.setReturnGoodsStatus("JTK");
							orderService.updateGoodsReturn(goodsReturn, requestId);
							orderProductService.updateOrderProduct(orderProduct, requestId);
						}
						
						
					}else{//退款
						
						MoneyReturn moneyReturn = orderService.getMoneyReturnByReturnCode(returnCode.substring(returnCode.indexOf(":")+1), requestId);
						Order childOrder = orderService.getOrderByOrderCode(moneyReturn.getOrderCode(), requestId);
						
						
						String respCode = valideData.get("respCode");
						String txnAmt   = valideData.get("txnAmt");
						String queryId  = valideData.get("valideData");
						String traceTime = currentYear+valideData.get("traceTime");
						if("00".equals(respCode)){//退款成功
							
							moneyReturn.setStatus("COM");
							moneyReturn.setRealReturnSum(Long.valueOf(txnAmt));
							moneyReturn.setThirdReturnNo(queryId);
							try {
								moneyReturn.setReturnTime(sdf.parse(traceTime));
							} catch (ParseException e) {
								logger.error(e.getMessage(),e);
							}
							
							childOrder.setReturnMoneyStatus("COM");
								
							
							
							/** 删除全返记录 **/
							orderService.deleteOrderCashbackByOrderCode(requestId, moneyReturn.getOrderCode());
							
							
							
							/*更新返现记录*/
							OrderCashbackHist orderCashbackHist = orderService.selectFxByOrderCode(requestId, childOrder.getOrderCode());
							orderCashbackHist.setRemainMoney(0L);
							orderCashbackHist.setStatus(Status.com.getCode());
							orderService.updateOrderCashbackHist(requestId, orderCashbackHist);
							/*返现退款扣除返现金额记录*/
							OrderCashbackHist orderCashbackHist2 = new OrderCashbackHist();
							orderCashbackHist2 .setFlowDirection(FlowDirection.out.getCode());
							orderCashbackHist2.setInsertTime(new Date());
							orderCashbackHist2.setMqId(moneyReturn.getMqID());
							orderCashbackHist2.setOrderCode(childOrder.getOrderCode());
							orderCashbackHist2.setReturnCode(moneyReturn.getReturnCode());
							orderCashbackHist2.setMoney(childOrder.getOrderSum());
							orderCashbackHist2.setType(com.mi360.aladdin.entity.order.OrderCashbackHist.Type.tk.getCode());
							orderService.addOrderCashbackHist(requestId, orderCashbackHist2);
							
							
						}else{//退款失败
							moneyReturn.setStatus("FAI");
							childOrder.setReturnMoneyStatus("FAI");
						}
						orderService.updateMoneyReturn(requestId, moneyReturn);
						orderService.updateOrder(childOrder, requestId);
						
						
						
					}
				}
			}else if("01".equals(txnType)){//支付
				String orderCode = valideData.get("orderId");
				String txnAmt =    valideData.get("txnAmt");
				String traceTime = currentYear+valideData.get("traceTime");
				String queryId   = valideData.get("queryId");
				String currencyCode = valideData.get("currencyCode");
				Order parentOrder = orderService.getOrderByOrderCode(orderCode, requestId);
				logger.info(orderCode+" "+txnAmt+" "+traceTime+" "+queryId+" "+currencyCode+" "+parentOrder);
				if("PAY".equals(parentOrder.getPayStatus())){
					logger.error("该订单已付过款");
					return "ok";
				}
				if(!parentOrder.getpSum().toString().equals(txnAmt)){
					logger.error("交易金额与实际金额不符");
					return "ok";
				}
				try {
					parentOrder.setPayTime(sdf.parse(traceTime));
				} catch (ParseException e) {
					logger.error(e.getMessage(),e);
				}
				
				List<Order> childOrderList = orderService.getChildOrdersByParentOrderCode(orderCode, requestId);
				for (int i = 0; i < childOrderList.size(); i++) {
					Order childOrderTmp = childOrderList.get(i);
					childOrderTmp.setPayStatus("PAY");
					childOrderTmp.setOrderStatus("ING");
					childOrderTmp.setPaySum(childOrderTmp.getOrderSum());
					childOrderTmp.setPayTime(parentOrder.getPayTime());
					orderService.updateOrder(childOrderTmp, requestId);
				}

				// 这个只是主订单 还要设置子订单的状态
				parentOrder.setPaySum(Long.valueOf(txnAmt));
				parentOrder.setPayStatus("PAY");
				parentOrder.setOrderStatus("ING");
				orderService.updateOrder(parentOrder, requestId);
				
				// 新增订单支付对象
				OrderPayment orderPayment = new OrderPayment();

				orderPayment.setCreateTime(new Date());
				orderPayment.setMoneyType(Currency.getByCode(currencyCode).toString());
				orderPayment.setMqID(parentOrder.getMqID());
				orderPayment.setOrderCode(orderCode);
				orderPayment.setPayChannel(OrderPayment.PayChannel.UNI.toString());
				orderPayment.setPayNum(queryId);
				orderPayment.setPayStatus("PAY");
				orderPayment.setPaySum(Long.valueOf(txnAmt));
				orderPayment.setPayTime(parentOrder.getPayTime());

				orderService.addOrderPayment(orderPayment, requestId);

				try {
					// 发送消息 开始分佣
					mqService.orderPaid(requestId, parentOrder.getOrderCode());
					logger.info("发送分佣消息队列成功");
				} catch (Exception e) {
					logger.info("发送分佣消息队列失败");
				}

				Map<String, String> msgParam = new HashMap<String, String>();

				try {
					MapData data = MapUtil.newInstance(userService.findWxUserByMqId(requestId, parentOrder.getMqID()));
					MapData result = data.getResult();
					String openId = "";
					if(result!=null){
						openId = result.getString("openId");
					}
					mqService.sendWxMessage(requestId, openId, "", "57137ee9b2a4e", msgParam);
					logger.info("发送银行卡支付消息成功");
				} catch (Exception e) {
					logger.info("发送银行卡支付消息失败");
					logger.error(e.getMessage(),e);
				}
				
				productService.updateSellCount(orderCode, requestId);
			}else if ("12".equals(txnType)) {//代付
				MapData resultData=MapUtil.newInstance(accountService.enterprisePayUnionpayNotify(requestId, valideData));
				logger.info(resultData.getData());
			}
			
			
			
		}else{
			logger.error("验证签名失败");
		}
		
		return "ok";
		
	}
	
	/**
	 * 获取请求参数中所有的信息
	 * 
	 * @param request
	 * @return
	 */
	private static Map<String, String> getAllRequestParam(final HttpServletRequest request) {
		Map<String, String> res = new HashMap<String, String>();
		Enumeration<?> temp = request.getParameterNames();
		if (null != temp) {
			while (temp.hasMoreElements()) {
				String en = (String) temp.nextElement();
				String value = request.getParameter(en);
				res.put(en, value);
				//在报文上送时，如果字段的值为空，则不上送<下面的处理为在获取所有参数数据时，判断若值为空，则删除这个字段>
				//System.out.println("ServletUtil类247行  temp数据的键=="+en+"     值==="+value);
				if (null == res.get(en) || "".equals(res.get(en))) {
					res.remove(en);
				}
			}
		}
		return res;
	}
	
}
