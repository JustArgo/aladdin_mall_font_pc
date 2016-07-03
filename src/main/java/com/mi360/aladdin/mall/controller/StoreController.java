package com.mi360.aladdin.mall.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mi360.aladdin.account.service.AccountService;
import com.mi360.aladdin.data.dictionary.service.DataDictionaryService;
import com.mi360.aladdin.data.dictionary.service.DataDictionaryService.ConfigKey;
import com.mi360.aladdin.entity.order.GoodsReturn;
import com.mi360.aladdin.entity.order.MoneyReturn;
import com.mi360.aladdin.entity.order.Order;
import com.mi360.aladdin.entity.order.OrderPayment;
import com.mi360.aladdin.entity.order.OrderPayment.PayChannel;
import com.mi360.aladdin.entity.order.OrderProduct;
import com.mi360.aladdin.logistics.domain.ExpressCompany;
import com.mi360.aladdin.logistics.service.ILogisticsService;
import com.mi360.aladdin.logistics.vo.LogisticsFormVo;
import com.mi360.aladdin.logistics.vo.LogisticsInfoVo;
import com.mi360.aladdin.mall.util.QiNiuUtil;
import com.mi360.aladdin.mall.util.WebUtil;
import com.mi360.aladdin.order.service.IOrderProductService;
import com.mi360.aladdin.order.service.IOrderService;
import com.mi360.aladdin.product.domain.ProductSku;
import com.mi360.aladdin.product.service.IProductSkuService;
import com.mi360.aladdin.store.service.IStoreService;
import com.mi360.aladdin.supplier.service.ISupplierService;
import com.mi360.aladdin.user.service.UserService;
import com.mi360.aladdin.util.MapUtil;
import com.mi360.aladdin.util.MapUtil.MapData;


@Controller
@RequestMapping("/store")
public class StoreController {

	private Logger logger = Logger.getLogger(StoreController.class);
	
	public static final int DEFAULT_PAGE_SIZE = 4;
	
	@Autowired
	private IStoreService storeService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private IOrderService orderService;
	
	@Autowired
	private IOrderProductService orderProductService;
	
	@Autowired
	private IProductSkuService productSkuService;
	
	@Autowired
	private AccountService accountService;
	
	@Autowired
	private DataDictionaryService dataDictionaryService;
	
	@Autowired
	private ISupplierService supplierService;
	
	@Autowired
	private ILogisticsService logisticsService;
	
	@RequestMapping("/")
	public String index(String requestId, Model model){
		
		Map<String,Object> principal = WebUtil.getCurrentUserInfo();
		String mqId = (String)principal.get("mqId");
		
		Map<String,Object> map = storeService.getStore(requestId, mqId);
		logger.info(map);
		if((Integer)map.get("errcode")==0){
			
			Map<String,Object> store = (Map<String,Object>)map.get("result");
			if(store!=null){
				
				Map<String,Object> userInfo = userService.findWxUserByMqId(requestId, mqId);
				MapData data = MapUtil.newInstance(userInfo);
				Map<String,Object> user = (Map<String, Object>) data.getObject("result");
				
				model.addAttribute("userHeadImg",user.get("headimgurl"));
				model.addAttribute("userName",user.get("nickname")); 
				
				Map<String,Object> productCountMap = storeService.getProductCountInStore(requestId, mqId);
				Map<String,Object> monthIncomeMap = storeService.getMonthIncome(requestId, mqId);
				Map<String,Object> monthSellCountMap = storeService.getMonthSellCount(requestId, mqId);
				
				model.addAttribute("productCount",productCountMap.get("result")==null?0:productCountMap.get("result"));
				model.addAttribute("monthIncome",monthIncomeMap.get("result")==null?0:productCountMap.get("result"));
				model.addAttribute("monthSellCount",monthSellCountMap.get("result")==null?0:monthSellCountMap.get("result"));
				model.addAttribute("todayVisit",0);
				
				return "store/index";
			}else{
				return "store/no-store";
			}
		}
		
		return "500";
		
	}
	
	/**
	 * 开店
	 */
	@RequestMapping("/open")
	public String openStore(String requestId, Model model){
		
		Map<String,Object> principal = WebUtil.getCurrentUserInfo();
		String mqId = (String)principal.get("mqId");
		
		Map<String,Object> map = storeService.getStore(requestId, mqId);
		logger.info("map:"+map);
		if((Integer)map.get("errcode")==0){
			Map<String,Object> store = (Map<String,Object>)map.get("result");
			if(store!=null){
				return "store/index";
			}else{

				return "store/open-store";
			}
		}
		
		return "store/open-store";
	}
	
	@RequestMapping("/create")
	public String createStore(String requestId, String title, String logoPath, String abstraction, Model model){
		
		Map<String,Object> principal = WebUtil.getCurrentUserInfo();
		String mqId = (String)principal.get("mqId");
		
		Map<String,Object> map = storeService.createStore(requestId, mqId, title, logoPath, abstraction);
		logger.info("map:"+map);
		if((Integer)map.get("errcode")==0){
			Integer storeId = (Integer)map.get("result");
			model.addAttribute("storeId",storeId);
			
			Map<String,Object> userInfo = userService.findWxUserByMqId(requestId, mqId);
			MapData data = MapUtil.newInstance(userInfo);
			Map<String,Object> user = (Map<String, Object>) data.getObject("result");
			
			model.addAttribute("userHeadImg",user.get("headimgurl"));
			model.addAttribute("userName",user.get("nickname")); 
			
			model.addAttribute("productCount",0);
			model.addAttribute("monthIncome",0);
			model.addAttribute("monthSellCount",0);
			model.addAttribute("todayVisit",0);
					
		}
		
		return "store/no-products";
		
	}
	
	/**
	 * 点击店铺设置
	 */
	@RequestMapping("/setting")
	public String setting(String requestId, Model model){
		
		Map<String,Object> principal = WebUtil.getCurrentUserInfo();
		String mqId = (String)principal.get("mqId");
		
		Map<String,Object> storeMap = storeService.getStore(requestId, mqId);
		logger.info("storeMap:"+storeMap);
		if((Integer)storeMap.get("errcode")==0){
			Map<String,Object> store = (Map<String, Object>) storeMap.get("result");
			model.addAttribute("logoPath",store.get("logoPath"));
			model.addAttribute("title",store.get("title"));
			model.addAttribute("abstraction",store.get("abstraction"));
		}
		
		return "store/store-setting";
		
	}
	
	/**
	 * 更新店铺信息
	 */
	@RequestMapping("/update")
	public String updateStore(String requestId, String title, String logoPath, String abstraction, Model model){
		
		Map<String,Object> principal = WebUtil.getCurrentUserInfo();
		String mqId = (String)principal.get("mqId");
		
		Map<String,Object> map = storeService.updateStoreInfo(requestId, mqId, title, abstraction, logoPath);
		logger.info("map:"+map);
		if((Integer)map.get("errcode")==0){
			Integer storeId = (Integer)map.get("result");
			model.addAttribute("storeId",storeId);
		}
		
		return "store/index";
		
	}
	
	
	/**
	 * 店铺商品管理
	 */
	@RequestMapping("/products")
	public String products(String requestId,  String tab, Integer page, Model model){
		
		if(tab==null){
			tab="sale";
		}
		if(page==null){
			page=1;
		}
		
		Map<String,Object> principal = WebUtil.getCurrentUserInfo();
		String mqId = (String)principal.get("mqId");
		
		Map<String,Object> productCountMap = storeService.getProductCountInStore(requestId, mqId);
		model.addAttribute("productCount",((Integer)productCountMap.get("result")+DEFAULT_PAGE_SIZE-1)/DEFAULT_PAGE_SIZE);
		
		Map<String,Object> histProductCountMap = storeService.getHistProductCountInStore(requestId, mqId);
		model.addAttribute("histProductCount",((Integer)histProductCountMap.get("result")+DEFAULT_PAGE_SIZE-1)/DEFAULT_PAGE_SIZE);
		
		model.addAttribute("tab",tab);
		model.addAttribute("page",page);
		
		if("sale".equals(tab)){
			
			Map<String,Object> map = storeService.getProductInStore(requestId, mqId, (page-1)*DEFAULT_PAGE_SIZE, DEFAULT_PAGE_SIZE);
			logger.info("map:"+map);
			if((Integer)map.get("errcode")==0){
				List<Map<String,Object>> productList = (List<Map<String, Object>>) map.get("result");
				model.addAttribute("productList",productList);
			}
			
			Map<String,Object> histProductMap = storeService.getHistProductInStore(requestId, mqId, 0, DEFAULT_PAGE_SIZE);
			logger.info("histProductMap:"+histProductMap);
			if((Integer)histProductMap.get("errcode")==0){
				List<Map<String,Object>> histProductList = (List<Map<String,Object>>)histProductMap.get("result");
				model.addAttribute("histProductList",histProductList);
			}
			
		}else if("history".equals(tab)){
		
			Map<String,Object> map = storeService.getProductInStore(requestId, mqId, 0, DEFAULT_PAGE_SIZE);
			logger.info("map:"+map);
			if((Integer)map.get("errcode")==0){
				List<Map<String,Object>> productList = (List<Map<String, Object>>) map.get("result");
				model.addAttribute("productList",productList);
			}
			
			Map<String,Object> histProductMap = storeService.getHistProductInStore(requestId, mqId, (page-1)*DEFAULT_PAGE_SIZE, DEFAULT_PAGE_SIZE);
			logger.info("histProductMap:"+histProductMap);
			if((Integer)histProductMap.get("errcode")==0){
				List<Map<String,Object>> histProductList = (List<Map<String,Object>>)histProductMap.get("result");
				model.addAttribute("histProductList",histProductList);
			}
			
		}
		
		return "store/products";
		
	}
	
	/**
	 * 销售统计
	 */
	@RequestMapping("/sale-calc")
	public String saleCalc(String requestId, Model model){
		
		Map<String,Object> principal = WebUtil.getCurrentUserInfo();
		String mqId = (String)principal.get("mqId");
		
		Map<String,Object> map = storeService.getOrder(requestId, mqId, null, null, 0, DEFAULT_PAGE_SIZE);
		logger.info("map:"+map);
		if((Integer)map.get("errcode")==0){
			List<Map<String,Object>> orderList = (List<Map<String, Object>>) map.get("result");
			model.addAttribute("orderList");
		}
		
		Map<String,Object> saleIncomeMap = storeService.getIncomeCalc(requestId, mqId, null, null, 0, DEFAULT_PAGE_SIZE);
		logger.info("saleIncomeMap:"+saleIncomeMap);
		if((Integer)saleIncomeMap.get("errcode")==0){
			List<Map<String,Object>> distributionList = (List<Map<String, Object>>) saleIncomeMap.get("result");
			model.addAttribute("distributionList",distributionList);
		}
		
		return "store/sale-statistics";
		
	}
	
	/**
	 * 订单管理
	 */
	@RequestMapping("/order")
	public String orderIndex(String requestId, Model model){
		
		Map<String,Object> principal = WebUtil.getCurrentUserInfo();
		String mqId = (String)principal.get("mqId");
		
		Map<String,Object> allOrderMap = storeService.getOrder(requestId, mqId, null, null, 0, DEFAULT_PAGE_SIZE);
		logger.info("allOrderMap:"+allOrderMap);
		
		Map<String,Object> noPayOrderMap = storeService.selectNoPayedOrder(requestId, mqId, 0, DEFAULT_PAGE_SIZE);
		logger.info("noPayOrderMap:"+noPayOrderMap);
		
		Map<String,Object> noSendOrderMap = storeService.selectNoSendOrder(requestId, mqId, 0, DEFAULT_PAGE_SIZE);
		logger.info("noSendOrderMap:"+noSendOrderMap);
		
		Map<String,Object> waitForCommentMap = storeService.selectWaitForCommentList(requestId, mqId, 0, DEFAULT_PAGE_SIZE);
		logger.info("waitForCommentMap:"+waitForCommentMap);
		
		Map<String,Object> returnMoneyMap = storeService.selectReturnMoneyList(requestId, mqId, 0, DEFAULT_PAGE_SIZE);
		logger.info("returnMoneyMap:"+returnMoneyMap);
		
		Map<String,Object> returnGoodsMap = storeService.selectReturnGoodsList(requestId, mqId, 0, DEFAULT_PAGE_SIZE);
		logger.info("returnGoodsMap:"+returnGoodsMap);
		
		
		model.addAttribute("allOrder",allOrderMap.get("result"));
		model.addAttribute("noPayOrder",noPayOrderMap.get("result"));
		model.addAttribute("noSendOrder",noSendOrderMap.get("result"));
		model.addAttribute("waitForComment",waitForCommentMap.get("result"));
		model.addAttribute("returnMoney",returnMoneyMap.get("result"));
		model.addAttribute("returnGoods",returnGoodsMap.get("result"));
		
		return "store/order-index";
		
	}
	
	/**
	 * 置顶商品
	 * @param requestId
	 * @return
	 */
	@RequestMapping("/top-products")
	@ResponseBody
	public Map<String,Object> topProducts(String requestId, Integer[] productIds){
		
		
		Map<String,Object> principal = WebUtil.getCurrentUserInfo();
		String mqId = (String)principal.get("mqId");
		
		return storeService.topProductInStore(requestId, mqId, productIds);
		
	}
	
	/**
	 * 店长推荐
	 */
	@RequestMapping("/recommend")
	@ResponseBody
	public Map<String,Object> recommend(String requestId, Integer[] productIds){
		
		Map<String,Object> principal = WebUtil.getCurrentUserInfo();
		String mqId = (String)principal.get("mqId");
		
		return storeService.recommendProducts(requestId, mqId, productIds);
		
	}
	
	/**
	 * 取消代理
	 */
	@RequestMapping("/cancel-proxy")
	@ResponseBody
	public Map<String,Object> cancelProxy(String requestId, Integer[] productIds){
		
		Map<String,Object> principal = WebUtil.getCurrentUserInfo();
		String mqId = (String)principal.get("mqId");
		
		return storeService.delProductsFromStore(requestId, mqId, productIds);
		
	}
	
	/**
	 * 店铺浏览 自己浏览自己的店铺
	 */
	@RequestMapping("/browse")
	public String browse(String requestId, Model model, Integer page){
		
		if(page==null){
			page = 1;
		}
		
		final int RECOMMEND_PAGE = 3;
		final int NOT_RECOMMEND_PAGE = 8;
		
		Map<String,Object> principal = WebUtil.getCurrentUserInfo();
		String mqId = (String)principal.get("mqId");
		
		Map<String,Object> storeMap = storeService.getStore(requestId, mqId);
		if((Integer)storeMap.get("errcode")==0){
			model.addAttribute("store",storeMap.get("result"));
		}
		
		int notRecommendCount = storeService.getNotRecommendProductCount(requestId, mqId);
		int recommendCount = storeService.getRecommendProductCount(requestId, mqId);
		
		int notRecommendTotal = (notRecommendCount+NOT_RECOMMEND_PAGE-1)/NOT_RECOMMEND_PAGE;
		int recommendTotal = (recommendCount+RECOMMEND_PAGE-1)/RECOMMEND_PAGE;
		
		List<Map<String,Object>> notRecommendProductList = storeService.getNotRecommendProduct(requestId, mqId, (page-1)*NOT_RECOMMEND_PAGE, NOT_RECOMMEND_PAGE);
		List<Map<String,Object>> recommendProductList = storeService.getRecommendProduct(requestId, mqId, (page-1)*RECOMMEND_PAGE, RECOMMEND_PAGE);
		
		model.addAttribute("notRecommendProductList",notRecommendProductList);
		model.addAttribute("recommendProductList",recommendProductList);
		
		model.addAttribute("page",page);
		model.addAttribute("total",notRecommendTotal>recommendTotal?notRecommendTotal:recommendTotal);
			
//		Map<String,Object> productsMap = storeService.getProductInStore(requestId, mqId, 0, 12);
//		if((Integer)productsMap.get("errcode")==0){
//			model.addAttribute("productList",productsMap.get("result"));
//		}
		
		return "store/store-browse";
		
	}
	
	/**
	 * 查看别人的店铺
	 */
	@RequestMapping("/view")
	public String view(String requestId, Integer storeId, Integer page, Model model){
		
		final int VIEW_PAGE_SIZE = 12;
		
		if(page==null){
			page = 1;
		}
		
		Map<String,Object> storeMap = storeService.getStoreByStoreId(requestId, storeId);
		
		Map<String,Object> productCountMap = storeService.getProductCountInStoreByStoreId(requestId, storeId);
		
		Map<String,Object> productList = storeService.getProductInStoreByStoreId(requestId, storeId, (page-1), VIEW_PAGE_SIZE);
		
		model.addAttribute("store",storeMap.get("result"));
		model.addAttribute("page",page);
		model.addAttribute("total",((Integer)productCountMap.get("result")+VIEW_PAGE_SIZE-1)/VIEW_PAGE_SIZE);
		model.addAttribute("productList",productList.get("result"));
		model.addAttribute("storeId",storeId);
		
		return "store/view";
		
	}
	
	@RequestMapping("/order-detail")
	public String orderDetail(String requestId, String orderCode, Integer orderID, Model model){
		
		
		Order order = null;

		if (!StringUtils.isBlank(orderCode)) {
			order = orderService.getOrderByOrderCode(orderCode, requestId);
		} else if (orderID != null) {
			order = orderService.getOrderByID(orderID, requestId);
		}

		if (order == null) {
			return "404";
		}

		Order parentOrder = null;
		String orderStatus = order.getOrderStatus();
		String payStatus = order.getPayStatus();
		String returnMoneyStatus = order.getReturnMoneyStatus();
		String returnGoodsStatus = order.getReturnGoodsStatus();
		String shippingStatus = order.getShippingStatus();
		String commentStatus = order.getCommentStatus();
		String recName = "";
		String recMobile = "";
		String address = "";

		String pFee = null;
		String pSum = null;

		List<Map<String, Object>> childOrderVo = new ArrayList<Map<String, Object>>();

		// 区分子订单 和 父订单
		if (order.getParentID() == 0) {
			parentOrder = order;
			pFee = parentOrder.getpFee().toString();
			pSum = parentOrder.getpSum().toString();
		} else {
			parentOrder = orderService.getOrderByOrderCode(order.getParentCode(), requestId);
			pFee = order.getPostFee().toString();
			pSum = order.getOrderSum().toString();
		}

		OrderPayment orderPayment = orderService.getOrderPaymentByOrderCode(parentOrder.getOrderCode(), requestId);
		recName = order.getRecName();
		recMobile = order.getRecMobile();
		address = StringUtils.defaultString(order.getProvince(), "") + StringUtils.defaultString(order.getCity(), "")
				+ StringUtils.defaultString(order.getDistrict(), "") + StringUtils.defaultString(order.getAddress(), "");

		if (orderPayment != null) {
			model.addAttribute("payChannel", OrderPayment.PayChannel.valueOf(orderPayment.getPayChannel()).getName());
			model.addAttribute("payTime", orderPayment.getPayTime());
			model.addAttribute("payNum", orderPayment.getPayNum());
		}
		model.addAttribute("recName", recName);
		model.addAttribute("recMobile", recMobile);
		model.addAttribute("address", address);
		model.addAttribute("pFee", pFee);
		model.addAttribute("pSum", pSum);
		model.addAttribute("createTime", parentOrder.getCreateTime());
		model.addAttribute("invoiceName", parentOrder.getInvoiceName());
		model.addAttribute("notes", parentOrder.getNotes());
		model.addAttribute("logistics", parentOrder.getLogistics());
		model.addAttribute("logisticsNum", parentOrder.getLogisticsNum());

		// 如果parentID!=0 则 代表子订单 优先处理
		if (order.getParentID() != 0) {

			// 程序走到这里代表是子订单 则order代表子订单 再查找出本个子订单对应的订单商品
			List<OrderProduct> orderProductList = orderProductService.getOrderProductByOrderID(order.getID(), requestId);

			Map<String, Object> childOrderMap = new HashMap<String, Object>();
			// 根据orderProductList重新组装前台对象 组装成Map 包含skuImg productName skuPrice
			// buyNum
			List<Map<String, Object>> childOrderProductVo = new ArrayList<Map<String, Object>>();
			for (OrderProduct op : orderProductList) {
				Map<String, Object> orderProductMap = new HashMap<String, Object>();

				ProductSku sku = productSkuService.getSkuByID(op.getSkuID(), requestId);
				orderProductMap.put("skuImg", QiNiuUtil.getDownloadUrl(sku.getSkuImg()));
				orderProductMap.put("productName", op.getProductName());
				orderProductMap.put("skuPrice", op.getSellPrice());
				List<String> skuStrs = productSkuService.getSkuStr(sku.getID(), requestId);
				orderProductMap.put("skuStrs", skuStrs);
				orderProductMap.put("productID", sku.getProductID());
				orderProductMap.put("buyNum", op.getBuyNum());
				childOrderProductVo.add(orderProductMap);
			}

			// 物流信息针对子订单
			model.addAttribute("logistics", order.getLogistics());
			model.addAttribute("logisticsNum", order.getLogisticsNum());
			model.addAttribute("sendTime", order.getSendTime());

			childOrderMap.put("supName", orderProductList.get(0).getSupName());
			childOrderMap.put("orderCode", order.getOrderCode());
			childOrderMap.put("childOrderProductVo", childOrderProductVo);

			childOrderVo.add(childOrderMap);
			model.addAttribute("childOrderVo", childOrderVo);
			model.addAttribute("orderCode", parentOrder.getOrderCode());

			// 查看订单状态
			if ("COM".equals(orderStatus)) {// 已完成
				return "store/order-detail-com";
			} else if ("CAN".equals(orderStatus)) {// 已取消
				return "store/order-detail-can";
			} else if ("PAY".equals(payStatus) && "NOA".equals(returnMoneyStatus) && "NOT".equals(shippingStatus)) {// 付完款
				return "store/order-detail-dfh";
			} else if (!"NOA".equals(returnMoneyStatus)) {// 和退款相关的 都转到
				MoneyReturn moneyReturn = orderService.getNewestMoneyReturnByChildOrderCode(orderCode, requestId);
				return "redirect:return-money-detail?moneyReturnID=" + moneyReturn.getID();
			} else if ("PAY".equals(payStatus) && "NOA".equals(returnMoneyStatus) && "HAV".equals(shippingStatus)) {// 已发货
				return "store/order-detail-yfh";
			}

		}

		// 找出所有的子订单和订单商品
		List<Order> childOrderList = orderService.getChildOrdersByParentOrderCode(orderCode, requestId);
		for (int i = 0; i < childOrderList.size(); i++) {

			Map<String, Object> childOrderMap = new HashMap<String, Object>();

			Order childOrder = childOrderList.get(i);
			List<OrderProduct> orderProductList = orderProductService.getOrderProductByOrderID(childOrder.getID(), requestId);
			// 根据orderProductList重新组装前台对象 组装成Map 包含skuImg productName skuPrice
			// buyNum
			List<Map<String, Object>> childOrderProductVo = new ArrayList<Map<String, Object>>();
			for (OrderProduct op : orderProductList) {
				Map<String, Object> orderProductMap = new HashMap<String, Object>();

				ProductSku sku = productSkuService.getSkuByID(op.getSkuID(), requestId);
				orderProductMap.put("skuImg", QiNiuUtil.getDownloadUrl(sku.getSkuImg()));
				orderProductMap.put("productName", op.getProductName());
				orderProductMap.put("skuPrice", op.getSellPrice());
				List<String> skuStrs = productSkuService.getSkuStr(sku.getID(), requestId);
				orderProductMap.put("skuStrs", skuStrs);
				orderProductMap.put("skuId", sku.getID());
				orderProductMap.put("productID", sku.getProductID());
				orderProductMap.put("buyNum", op.getBuyNum());
				childOrderProductVo.add(orderProductMap);
			}

			childOrderMap.put("supName", orderProductList.get(0).getSupName());
			childOrderMap.put("orderCode", childOrder.getOrderCode());
			childOrderMap.put("childOrderProductVo", childOrderProductVo);
			childOrderMap.put("orderSum", childOrder.getOrderSum());
			childOrderMap.put("postFee", childOrder.getPostFee());

			childOrderVo.add(childOrderMap);
		}

		model.addAttribute("childOrderVo", childOrderVo);
		model.addAttribute("orderCode", orderCode);

		//如果是待付款
		if("ING".equals(orderStatus) && "NOT".equals(payStatus)){
			
			// 计算剩余时间
			String remainTime = remainTime(order.getCreateTime(), requestId);
			if (remainTime.equals("OUT_OF_DATE")) {

				// 如果orderStatus 还没有变为CAN 则变成CAN
				if (!order.getOrderStatus().equals("CAN")) {
					order.setOrderStatus("CAN");
					orderService.updateOrder(order, requestId);
					for (int i = 0; i < childOrderList.size(); i++) {
						childOrderList.get(i).setOrderStatus("CAN");
						orderService.updateOrder(childOrderList.get(i), requestId);
					}
				}

				return "order/out_of_date";
			}
			if (!remainTime.equals("1分钟内")) {
				remainTime = "剩" + remainTime;
			}
			
			Map<String, Object> remainingMap = accountService.getRemainingSum(requestId, parentOrder.getMqID());

			if ((Integer) remainingMap.get("errcode") != 0) {
				logger.info(remainingMap);
			} else {
				logger.info("remainingMap-->" + remainingMap);
				Long remainingSum = (Long) remainingMap.get("result");
				
				model.addAttribute("remainingSum",remainingSum==null?0L:remainingSum);
				
				if (remainingSum.compareTo(parentOrder.getpSum()) < 0) {
					model.addAttribute("remainNotEnough", "not enough");
				}
			}
			
			model.addAttribute("remainTime",remainTime);
			
			return "store/order-detail-dfk";
		}
		
		
		
		return "store/order-detail";
		
		
	}
	
	
	@RequestMapping("/order-detail-com")
	public String orderDetailCom(String requestId, String orderCode, Model model){
		
		Order order = orderService.getOrderByOrderCode(orderCode, requestId);
		
		model.addAttribute("orderCode",orderCode);
		
		OrderPayment orderPayment = orderService.getOrderPaymentByOrderCode(order.getParentCode(), requestId);
		String recName = order.getRecName();
		String recMobile = order.getRecMobile();
		String address = StringUtils.defaultString(order.getProvince(), "") + StringUtils.defaultString(order.getCity(), "")
				+ StringUtils.defaultString(order.getDistrict(), "") + StringUtils.defaultString(order.getAddress(), "");

		if (orderPayment != null) {
			model.addAttribute("payChannel", OrderPayment.PayChannel.valueOf(orderPayment.getPayChannel()).getName());
			model.addAttribute("payTime", orderPayment.getPayTime());
		}
		model.addAttribute("recName", recName);
		model.addAttribute("recMobile", recMobile);
		model.addAttribute("address", address);
		
		model.addAttribute("orderSum",order.getOrderSum());
		model.addAttribute("postFee",order.getPostFee());
		
		//发票抬头
		model.addAttribute("invoiceName",order.getInvoiceName());
		
		List<OrderProduct> orderProductList = orderProductService.getOrderProductByOrderID(order.getID(), requestId);
		List<Map<String,Object>> childOrderProductVo = new ArrayList<Map<String,Object>>(); 
		if(orderProductList!=null && orderProductList.size()>0){
			model.addAttribute("supName",orderProductList.get(0).getSupName());
		}
		for(OrderProduct op:orderProductList){
			Map<String, Object> orderProductMap = new HashMap<String, Object>();
			ProductSku sku = productSkuService.getSkuByID(op.getSkuID(), requestId);
			orderProductMap.put("skuImg", QiNiuUtil.getDownloadUrl(sku.getSkuImg()));
			orderProductMap.put("productName", op.getProductName());
			orderProductMap.put("skuPrice", op.getSellPrice());
			List<String> skuStrs = productSkuService.getSkuStr(sku.getID(), requestId);
			orderProductMap.put("skuStrs", skuStrs);
			orderProductMap.put("productID", sku.getProductID());
			orderProductMap.put("buyNum", op.getBuyNum());
			childOrderProductVo.add(orderProductMap);
		}
		model.addAttribute("childOrderProductVo",childOrderProductVo);
		
		return "store/order-detail-com";
		
	}
	
	
	@RequestMapping("/order-detail-can")
	public String orderDetailCan(String requestId, String orderCode, Model model){
		
		Order order = orderService.getOrderByOrderCode(orderCode, requestId);
		
		model.addAttribute("orderCode",orderCode);
		
		OrderPayment orderPayment = orderService.getOrderPaymentByOrderCode(order.getParentCode(), requestId);
		String recName = order.getRecName();
		String recMobile = order.getRecMobile();
		String address = StringUtils.defaultString(order.getProvince(), "") + StringUtils.defaultString(order.getCity(), "")
				+ StringUtils.defaultString(order.getDistrict(), "") + StringUtils.defaultString(order.getAddress(), "");

		if (orderPayment != null) {
			model.addAttribute("payChannel", OrderPayment.PayChannel.valueOf(orderPayment.getPayChannel()).getName());
			model.addAttribute("payTime", orderPayment.getPayTime());
		}
		model.addAttribute("recName", recName);
		model.addAttribute("recMobile", recMobile);
		model.addAttribute("address", address);
		
		model.addAttribute("orderSum",order.getOrderSum());
		model.addAttribute("postFee",order.getPostFee());
		
		//发票抬头
		model.addAttribute("invoiceName",order.getInvoiceName());
		
		List<OrderProduct> orderProductList = orderProductService.getOrderProductByOrderID(order.getID(), requestId);
		List<Map<String,Object>> childOrderProductVo = new ArrayList<Map<String,Object>>(); 
		if(orderProductList!=null && orderProductList.size()>0){
			model.addAttribute("supName",orderProductList.get(0).getSupName());
		}
		for(OrderProduct op:orderProductList){
			Map<String, Object> orderProductMap = new HashMap<String, Object>();
			ProductSku sku = productSkuService.getSkuByID(op.getSkuID(), requestId);
			orderProductMap.put("skuImg", QiNiuUtil.getDownloadUrl(sku.getSkuImg()));
			orderProductMap.put("productName", op.getProductName());
			orderProductMap.put("skuPrice", op.getSellPrice());
			List<String> skuStrs = productSkuService.getSkuStr(sku.getID(), requestId);
			orderProductMap.put("skuStrs", skuStrs);
			orderProductMap.put("productID", sku.getProductID());
			orderProductMap.put("buyNum", op.getBuyNum());
			childOrderProductVo.add(orderProductMap);
		}
		model.addAttribute("childOrderProductVo",childOrderProductVo);
		
		return "store/order-detail-can";
		
	}
	
	/**
	 * 查询已发货订单详情
	 * @param requestId
	 * @param orderCode
	 * @param model
	 * @return
	 */
	@RequestMapping("/order-detail-yfh")
	public String orderDetailYfh(String requestId, String orderCode, Model model){
		
		Order order = orderService.getOrderByOrderCode(orderCode, requestId);
		
		model.addAttribute("orderCode",orderCode);
		
		OrderPayment orderPayment = orderService.getOrderPaymentByOrderCode(order.getParentCode(), requestId);
		String recName = order.getRecName();
		String recMobile = order.getRecMobile();
		String address = StringUtils.defaultString(order.getProvince(), "") + StringUtils.defaultString(order.getCity(), "")
				+ StringUtils.defaultString(order.getDistrict(), "") + StringUtils.defaultString(order.getAddress(), "");

		if (orderPayment != null) {
			model.addAttribute("payChannel", OrderPayment.PayChannel.valueOf(orderPayment.getPayChannel()).getName());
			model.addAttribute("payTime", orderPayment.getPayTime());
		}
		model.addAttribute("recName", recName);
		model.addAttribute("recMobile", recMobile);
		model.addAttribute("address", address);
		
		model.addAttribute("orderSum",order.getOrderSum());
		model.addAttribute("postFee",order.getPostFee());
		
		//发票抬头
		model.addAttribute("invoiceName",order.getInvoiceName());
		
		List<OrderProduct> orderProductList = orderProductService.getOrderProductByOrderID(order.getID(), requestId);
		List<Map<String,Object>> childOrderProductVo = new ArrayList<Map<String,Object>>(); 
		if(orderProductList!=null && orderProductList.size()>0){
			model.addAttribute("supName",orderProductList.get(0).getSupName());
		}
		for(OrderProduct op:orderProductList){
			Map<String, Object> orderProductMap = new HashMap<String, Object>();
			ProductSku sku = productSkuService.getSkuByID(op.getSkuID(), requestId);
			orderProductMap.put("skuImg", QiNiuUtil.getDownloadUrl(sku.getSkuImg()));
			orderProductMap.put("productName", op.getProductName());
			orderProductMap.put("skuPrice", op.getSellPrice());
			List<String> skuStrs = productSkuService.getSkuStr(sku.getID(), requestId);
			orderProductMap.put("skuStrs", skuStrs);
			orderProductMap.put("productID", sku.getProductID());
			orderProductMap.put("buyNum", op.getBuyNum());
			childOrderProductVo.add(orderProductMap);
		}
		model.addAttribute("childOrderProductVo",childOrderProductVo);
		
		//查询物流信息
		//LogisticsFormVo logisticsFormVo = logisticsService.getLogisticsFormByCompanyID(order.getLogisticsNum(), order.getLogisticsID(), requestId);
		
		LogisticsFormVo logisticsFormVo2 = new LogisticsFormVo();
		logisticsFormVo2.setLogisticsCode("sdfd");
		logisticsFormVo2.setLogisticsName("顺丰公司");
		logisticsFormVo2.setLogisticsNum("23424");
		LogisticsInfoVo logisticsInfoVo1 = new LogisticsInfoVo();
		logisticsInfoVo1.setContext("躺下街道1巷4号");
		logisticsInfoVo1.setTime("2014年5月18号");

		LogisticsInfoVo logisticsInfoVo2 = new LogisticsInfoVo();
		logisticsInfoVo2.setContext("上舍街道");
		logisticsInfoVo2.setTime("2013年8月13号");
		
		List<LogisticsInfoVo> infoList = new ArrayList<LogisticsInfoVo>();
		infoList.add(logisticsInfoVo1);
		infoList.add(logisticsInfoVo2);
		
		logisticsFormVo2.setLogisticsInfos(infoList);
		
		model.addAttribute("logisticsFormVo",logisticsFormVo2);
		
		
		return "store/order-detail-yfh";
		
	}
	
	@RequestMapping("/order-detail-dfh")
	public String orderDetailDfh(String requestId, String orderCode, Model model){
		
		Order order = orderService.getOrderByOrderCode(orderCode, requestId);
		
		model.addAttribute("orderCode",orderCode);
		
		OrderPayment orderPayment = orderService.getOrderPaymentByOrderCode(order.getParentCode(), requestId);
		String recName = order.getRecName();
		String recMobile = order.getRecMobile();
		String address = StringUtils.defaultString(order.getProvince(), "") + StringUtils.defaultString(order.getCity(), "")
				+ StringUtils.defaultString(order.getDistrict(), "") + StringUtils.defaultString(order.getAddress(), "");

		if (orderPayment != null) {
			model.addAttribute("payChannel", OrderPayment.PayChannel.valueOf(orderPayment.getPayChannel()).getName());
			model.addAttribute("payTime", orderPayment.getPayTime());
		}
		model.addAttribute("recName", recName);
		model.addAttribute("recMobile", recMobile);
		model.addAttribute("address", address);
		
		model.addAttribute("orderSum",order.getOrderSum());
		model.addAttribute("postFee",order.getPostFee());
		
		//发票抬头
		model.addAttribute("invoiceName",order.getInvoiceName());
		
		List<OrderProduct> orderProductList = orderProductService.getOrderProductByOrderID(order.getID(), requestId);
		List<Map<String,Object>> childOrderProductVo = new ArrayList<Map<String,Object>>(); 
		if(orderProductList!=null && orderProductList.size()>0){
			model.addAttribute("supName",orderProductList.get(0).getSupName());
		}
		for(OrderProduct op:orderProductList){
			Map<String, Object> orderProductMap = new HashMap<String, Object>();
			ProductSku sku = productSkuService.getSkuByID(op.getSkuID(), requestId);
			orderProductMap.put("skuImg", QiNiuUtil.getDownloadUrl(sku.getSkuImg()));
			orderProductMap.put("productName", op.getProductName());
			orderProductMap.put("skuPrice", op.getSellPrice());
			List<String> skuStrs = productSkuService.getSkuStr(sku.getID(), requestId);
			orderProductMap.put("skuStrs", skuStrs);
			orderProductMap.put("productID", sku.getProductID());
			orderProductMap.put("buyNum", op.getBuyNum());
			childOrderProductVo.add(orderProductMap);
		}
		model.addAttribute("childOrderProductVo",childOrderProductVo);
		
		return "store/order-detail-dfh";
		
	}
	
	@RequestMapping("/order-detail-dpl")
	public String orderDetailDpl(String requestId, Integer orderID, Integer orderProductID, Model model){
		
		Order order = orderService.getOrderByID(orderID, requestId);
		
		model.addAttribute("orderCode",order.getOrderCode());
		
		OrderPayment orderPayment = orderService.getOrderPaymentByOrderCode(order.getParentCode(), requestId);
		String recName = order.getRecName();
		String recMobile = order.getRecMobile();
		String address = StringUtils.defaultString(order.getProvince(), "") + StringUtils.defaultString(order.getCity(), "")
				+ StringUtils.defaultString(order.getDistrict(), "") + StringUtils.defaultString(order.getAddress(), "");

		if (orderPayment != null) {
			model.addAttribute("payChannel", OrderPayment.PayChannel.valueOf(orderPayment.getPayChannel()).getName());
			model.addAttribute("payTime", orderPayment.getPayTime());
		}
		model.addAttribute("recName", recName);
		model.addAttribute("recMobile", recMobile);
		model.addAttribute("address", address);
		
		model.addAttribute("orderSum",order.getOrderSum());
		model.addAttribute("postFee",order.getPostFee());
		
		//发票抬头
		model.addAttribute("invoiceName",order.getInvoiceName());
		
		OrderProduct orderProduct = orderProductService.getOrderProductByID(orderProductID, requestId);
		
		List<Map<String,Object>> childOrderProductVo = new ArrayList<Map<String,Object>>(); 
		if(orderProduct!=null){
			model.addAttribute("supName",orderProduct.getSupName());
		}
		Map<String, Object> orderProductMap = new HashMap<String, Object>();
		ProductSku sku = productSkuService.getSkuByID(orderProduct.getSkuID(), requestId);
		orderProductMap.put("skuImg", QiNiuUtil.getDownloadUrl(sku.getSkuImg()));
		orderProductMap.put("productName", orderProduct.getProductName());
		orderProductMap.put("skuPrice", orderProduct.getSellPrice());
		List<String> skuStrs = productSkuService.getSkuStr(sku.getID(), requestId);
		orderProductMap.put("skuStrs", skuStrs);
		orderProductMap.put("productID", sku.getProductID());
		orderProductMap.put("buyNum", orderProduct.getBuyNum());
		childOrderProductVo.add(orderProductMap);
		model.addAttribute("childOrderProductVo",childOrderProductVo);
		
		return "store/order-detail-dpl";
		
	}
	
	@RequestMapping("/return-money-detail")
	public String returnMoneyDetail(String requestId, Integer moneyReturnID, Model model) {

		Map<String,Object> principal = WebUtil.getCurrentUserInfo();
		// if(principal==null)principal = new Principal("2","");
		String mqID = (String)principal.get("mqId");

		MoneyReturn moneyReturn = orderService.getMoneyReturnByID(moneyReturnID, requestId);

		Order order = orderService.getOrderByOrderCode(moneyReturn.getOrderCode(), requestId);
		model.addAttribute("recName",order.getRecName());
		model.addAttribute("recMobile",order.getRecMobile());
		model.addAttribute("address",order.getAddress());
		
		model.addAttribute("payTime",order.getPayTime());
		model.addAttribute("orderSum",order.getOrderSum());
		model.addAttribute("invoiceName",order.getInvoiceName());
		
		OrderPayment orderPayment = orderService.getOrderPaymentByOrderCode(order.getParentCode(), requestId);
		List<Order> childOrderList = new ArrayList<Order>();
		childOrderList.add(order);
		model.addAttribute("childOrder",orderService.wrapperOrder(requestId, childOrderList));
		model.addAttribute("payChannel",PayChannel.valueOf(orderPayment.getPayChannel()).getName());
		model.addAttribute("postFee",order.getPostFee());
		
		
		model.addAttribute("orderCode",order.getOrderCode());
		model.addAttribute("moneyReturn", moneyReturn);
		// 退款原因
		if (moneyReturn.getReturnReason().equals("SJ#")) {
			model.addAttribute("returnReason", "少件/漏发");
		} else if (moneyReturn.getReturnReason().equals("FP#")) {
			model.addAttribute("returnReason", "发票问题");
		} else {
			model.addAttribute("returnReason", MoneyReturn.ReturnReason.valueOf(moneyReturn.getReturnReason()).getValue());
		}

		if ("STK".equals(moneyReturn.getStatus())) {// 此处auditStatus==null // 则下面必然不会==null
			return "store/returnR-examine";
		} else if ("NO#".equals(moneyReturn.getStatus())) {// 商家审核不通过
			return "store/returnR-reject";
		} else if ("TKZ".equals(moneyReturn.getStatus())) {// 审核通过 正在退款中
			return "store/returnR-pass";
		} else if ("COM".equals(moneyReturn.getStatus())) {// 后台确认退款
			return "store/returnR-success";
		} else if ("FAI".equals(moneyReturn.getStatus())) {
			return "store/returnR-fail";
		}

		return "404";
	}
	
	/**
	 * 根据订单商品的id查看 其退货详情
	 * 
	 * @param requestId
	 * @param orderProductID
	 * @param model
	 * @return
	 */
	@RequestMapping("/return-goods-detail-orderproductid")
	public String returnGoodsDetailWithOrderProductID(String requestId, Integer orderProductID, Model model) {

		Map<String,Object> principal = WebUtil.getCurrentUserInfo();
		// if(principal==null)principal = new Principal("2","");
		String mqID = (String)principal.get("mqId");

		GoodsReturn goodsReturn = orderService.getGoodsReturnByOrderProductID(orderProductID, requestId);

		if (goodsReturn == null) {
			logger.info("查找的退款单不存在");
			return "404";
		}
		
		Order order = orderService.getOrderByOrderCode(goodsReturn.getOrderCode(), requestId);
		model.addAttribute("orderRecName",order.getRecName());
		model.addAttribute("orderRecMobile",order.getRecMobile());
		model.addAttribute("address",order.getAddress());
		
		model.addAttribute("orderCode",order.getOrderCode());
		model.addAttribute("payTime",order.getPayTime());
		model.addAttribute("orderSum",order.getOrderSum());
		model.addAttribute("invoiceName",order.getInvoiceName());
		
		OrderPayment orderPayment = orderService.getOrderPaymentByOrderCode(order.getParentCode(), requestId);
		if(orderPayment!=null){
			model.addAttribute("payChannel",PayChannel.valueOf(orderPayment.getPayChannel()).getName());
		}
		
		
		model.addAttribute("goodsReturn", goodsReturn);
		// 退货原因
		if (goodsReturn.getReturnReason().equals("SJ#")) {
			model.addAttribute("returnReason", "少件/漏发");
		} else if (goodsReturn.getReturnReason().equals("FP#")) {
			model.addAttribute("returnReason", "发票问题");
		} else {
			model.addAttribute("returnReason", GoodsReturn.ReturnReason.valueOf(goodsReturn.getReturnReason()).getValue());
		}

		model.addAttribute("applySum", goodsReturn.getApplySum() / 100.0);
		model.addAttribute("returnDesc", goodsReturn.getReturnDesc());

		String status = goodsReturn.getStatus();

		if ("STH".equals(status)) {
			return "store/returnG-examine";
		} else if ("STG".equals(status)) { // 退货审核通过

			// 查出供应商的地址 放在 页面上
			Map<String, Object> receAdd = supplierService.getReceAddByID(goodsReturn.getSupID(), requestId);
			logger.info("receAdd-->" + receAdd);
			if (receAdd != null) {
				model.addAttribute("fullAddress", receAdd.get("returnAddress"));
				model.addAttribute("recName", receAdd.get("returnContactName"));
				model.addAttribute("recMobile", receAdd.get("returnContactPhone"));
			}

			// TODO 查找物流公司
			List<ExpressCompany> expressCompanyList = logisticsService.getAllLogisticsCompany(requestId);
			model.addAttribute("expressCompanyList", expressCompanyList);

			return "store/returnG-pass";
		} else if ("THZ".equals(status)) { // 用户填写了退货物流信息之后
			return "store/returnG-takeG";
		} else if ("NO#".equals(status)) {
			return "store/returnG-reject";
		} else if ("DTK".equals(status)) {
			return "store/returnG-dtk";
		} else if ("TKZ".equals(status)) {
			return "store/returnG-tkz";
		} else if ("JTK".equals(status)) {
			return "store/returnG-jtk";
		} else if ("FAI".equals(status)) {
			return "store/returnG-fail";
		} else if ("COM".equals(status)) {
			return "store/returnG-success";
		}

		return "404";
	}
	
	/**
	 * 计算订单还有多久关闭 返回59分钟 或 1分钟内
	 * 
	 * @param createTime
	 *            订单的创建时间
	 * @return
	 */
	private String remainTime(Date createTime, String requestId) {

		MapData data = MapUtil.newInstance(dataDictionaryService.getBaseConfigByConfigKey(requestId, ConfigKey.order_close_time.getCode()));
		MapData result = data.getResult();

		Integer period_seconds = 60 * 60;

		if (result != null) {
			period_seconds = result.getInteger("configValue");
			logger.info("period_seconds:" + period_seconds);
		}
		String retStr = "";

		long remainMills = createTime.getTime() + period_seconds * 1000 - System.currentTimeMillis();

		if (remainMills <= 0) {
			retStr = "OUT_OF_DATE";
			return retStr;
		}

		long remainDay = remainMills / (24 * 60 * 60 * 1000);
		long remainHour = (remainMills - remainDay * (24 * 60 * 60 * 1000)) / (60 * 60 * 1000);
		long remainMinute = (remainMills - remainDay * (24 * 60 * 60 * 1000) - remainHour * (60 * 60 * 1000)) / (60 * 1000);

		if (remainDay > 0) {
			retStr += remainDay + "天";
			if (remainHour >= 0) {
				retStr += remainHour + "小时";
			}
			if (remainMinute >= 0) {
				retStr += remainMinute + "分钟";
			}
		} else {
			if (remainHour > 0) {
				retStr += remainHour + "小时";
				if (remainMinute >= 0) {
					retStr += remainMinute + "分钟";
				}
			} else {
				if (remainMinute > 0) {
					retStr += remainMinute + "分钟";
				} else {
					retStr += "1分钟内";
				}
			}
		}

		return retStr;
	}
	
}
