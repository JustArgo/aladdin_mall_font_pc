package com.mi360.aladdin.mall.controller;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mi360.aladdin.account.service.AccountService;
import com.mi360.aladdin.data.dictionary.service.DataDictionaryService;
import com.mi360.aladdin.data.dictionary.service.DataDictionaryService.ConfigKey;
import com.mi360.aladdin.entity.account.BankCard;
import com.mi360.aladdin.entity.order.GoodsReturn;
import com.mi360.aladdin.entity.order.MoneyReturn;
import com.mi360.aladdin.entity.order.Order;
import com.mi360.aladdin.entity.order.OrderPayment;
import com.mi360.aladdin.entity.order.OrderPayment.PayChannel;
import com.mi360.aladdin.entity.order.OrderProduct;
import com.mi360.aladdin.interaction.wx.service.WxInteractionService;
import com.mi360.aladdin.logistics.domain.ExpressCompany;
import com.mi360.aladdin.logistics.service.ILogisticsService;
import com.mi360.aladdin.logistics.vo.LogisticsFormVo;
import com.mi360.aladdin.logistics.vo.LogisticsInfoVo;
import com.mi360.aladdin.mall.Principal;
import com.mi360.aladdin.mall.controller.BankCardController.AddType;
import com.mi360.aladdin.mall.util.QiNiuUtil;
import com.mi360.aladdin.mall.util.WebUtil;
import com.mi360.aladdin.mq.service.MqService;
import com.mi360.aladdin.order.service.IOrderProductService;
import com.mi360.aladdin.order.service.IOrderService;
import com.mi360.aladdin.product.domain.Product;
import com.mi360.aladdin.product.domain.ProductSku;
import com.mi360.aladdin.product.service.IPostFeeService;
import com.mi360.aladdin.product.service.IProductService;
import com.mi360.aladdin.product.service.IProductSkuService;
import com.mi360.aladdin.receadd.domain.Address;
import com.mi360.aladdin.receadd.domain.ReceiveAddress;
import com.mi360.aladdin.receadd.service.IAddressService;
import com.mi360.aladdin.receadd.service.IManageReceAddService;
import com.mi360.aladdin.shopcar.domain.ShopCarProduct;
import com.mi360.aladdin.shopcar.service.IShopCarService;
import com.mi360.aladdin.supplier.domain.Supplier;
import com.mi360.aladdin.supplier.service.ISupplierService;
import com.mi360.aladdin.unionpay.service.UnionpayService;
import com.mi360.aladdin.user.service.UserService;
import com.mi360.aladdin.util.MapUtil;
import com.mi360.aladdin.util.MapUtil.MapData;

@Controller
@RequestMapping("/order")
public class OrderController {

	Logger logger = Logger.getLogger(this.getClass());

	private static final int DEFAULT_PAGE_SIZE = 4;
	
	@Value("${host_name}")
	private String hostName;

	@Value("${qiniu.space}")
	protected String qiNiuSpace;
	
	@Value("${qiniu.domain}")
	protected String qiNiuDomain;
	
	@Autowired
	private WxInteractionService wxInteractionService;

	@Autowired
	private IShopCarService shopCarService;

	@Autowired
	private IProductSkuService productSkuService;

	@Autowired
	private IOrderService orderService;

	@Autowired
	private IOrderProductService orderProductService;

	@Autowired
	private IProductService productService;

	@Autowired
	private IManageReceAddService manageReceAddService;

	@Autowired
	private IPostFeeService postFeeService;

	@Autowired
	private IAddressService addressService;

	@Autowired
	private ISupplierService supplierService;

	@Autowired
	private AccountService accountService;

	@Autowired
	private UserService userService;

	@Autowired
	private ILogisticsService logisticsService;

	@Autowired
	private DataDictionaryService dataDictionaryService;
	
	@Autowired
	private UnionpayService unionPayService;
	
	@Autowired
	private MqService mqService;

	
	@RequestMapping("/placeOrder")
	public String placeOrder(String requestId, String orderCode, String payType, Integer[] skuIds, Integer[] buyNums, Long[] skuPrices, Long[] supplierAmounts,
			Long pFee, Long pSum, String invoiceName, Integer invoiceID, Integer receaddID, String notes, Model model) {

		Principal principal = WebUtil.getCurrentPrincipal();
		String mqID = principal.getMqId();

		if (skuIds != null) {
			List<Map<String, Object>> noUpProductNameList = productService.checkCanPlaceOrder(skuIds, requestId);
			if (noUpProductNameList.size() > 0) {
				model.addAttribute("noUpProductNameList", noUpProductNameList);
				return "order/product_down";
			}
		} else if (orderCode != null) { // 检查 商品是否已下架
			skuIds = orderService.getSkuIdsByOrderCode(requestId, orderCode);
			List<Map<String, Object>> noUpProductNameList = productService.checkCanPlaceOrder(skuIds, requestId);
			if (noUpProductNameList.size() > 0) {
				model.addAttribute("noUpProductNameList", noUpProductNameList);
				return "order/product_down";
			}
		}

		if (orderCode == null || "".equals(orderCode.trim())) {
			orderCode = orderService.placeOrder(mqID, "NOR", skuIds, buyNums, skuPrices, pFee, pSum, invoiceName, invoiceID, notes, receaddID, requestId);
			// 清除购物车中的某些商品
			shopCarService.removeShopCarProduct(mqID, skuIds, requestId);
		}

		if (payType.equals(OrderPayment.PayChannel.WXP.toString())) {
			return "redirect:wxPay?orderCode=" + orderCode;
		} else if (payType.equals(OrderPayment.PayChannel.SUM.toString())) {
			return "redirect:remainPay?orderCode=" + orderCode;
		} else if(payType.equals(OrderPayment.PayChannel.UNI.toString())){
			return "redirect:unionPay?orderCode=" + orderCode;
		}

		return "redirect:unionPay?orderCode=" + orderCode;

	}

	@RequestMapping("/remainPay")
	public String remainPay(String requestId, String orderCode, Model model) {

		Principal principal = WebUtil.getCurrentPrincipal();
		String mqID = principal.getMqId();

		String openId = principal.getOpenId();
		logger.info("remainPay-->openId: " + openId);

		try {

			Order order = orderService.getOrderByOrderCode(orderCode, requestId);

			MapData data = MapUtil.newInstance(accountService.remainingPlay(requestId, mqID, order.getpSum(), order.getOrderCode()));
			logger.info(data.dataString());
			if (data.getErrcode() == 0) {
				MapData result = data.getResult();
				String payNum = result.getString("payNum");
				Date payTime = result.getDate("payTime");

				List<Order> childOrderList = orderService.getChildOrdersByParentOrderCode(orderCode, requestId);

				// 这个只是主订单 还要设置子订单的状态
				order.setPaySum(order.getpSum());
				order.setPayStatus("PAY");
				order.setOrderStatus("ING");

				order.setPayTime(payTime);
				orderService.updateOrder(order, requestId);
				OrderPayment orderPayment = new OrderPayment();

				// 新增订单支付对象
				orderPayment.setCreateTime(new Date());
				orderPayment.setMoneyType("SUM");
				orderPayment.setMqID(order.getMqID());
				orderPayment.setOrderCode(orderCode);
				orderPayment.setPayChannel(OrderPayment.PayChannel.SUM.toString());

				// 支付单号 由remainingPlay返回
				orderPayment.setPayNum(payNum);
				orderPayment.setPayStatus("PAY");
				orderPayment.setPaySum(order.getPaySum());
				orderPayment.setPayTime(order.getPayTime());

				// 查询该该用户是否为全返类型的用户

				for (int i = 0; i < childOrderList.size(); i++) {
					Order childOrderTmp = childOrderList.get(i);
					childOrderTmp.setPayStatus("PAY");
					childOrderTmp.setOrderStatus("ING");
					childOrderTmp.setPaySum(childOrderTmp.getOrderSum());
					childOrderTmp.setPayTime(order.getPayTime());
					orderService.updateOrder(childOrderTmp, requestId);

				}

				orderService.addOrderPayment(orderPayment, requestId);

				try {

					// 发送消息 开始分佣
					mqService.orderPaid(requestId, order.getOrderCode());
					logger.info("发送分佣消息成功");
				} catch (Exception e) {
					logger.info("发送分佣消息失败");
				}

				Map<String, String> msgParam = new HashMap<String, String>();

				try {
					mqService.sendWxMessage(requestId, openId, "", "57137ee9b2a4e", msgParam);
					logger.info("发送余额支付消息成功");
				} catch (Exception e) {
					logger.info("发送余额支付消息失败");
				}

				// 更新商品销量
				productService.updateSellCount(order.getOrderCode(), requestId);

			} else {
				logger.error(data.getString("errmsg"));
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return "redirect:pay-result?orderCode=" + orderCode;
	}

	@RequestMapping("/unionPay")
	public String unionPay(String requestId, ModelMap model, String orderCode) {
		System.out.println("unionPay--");
		
		Principal principal = WebUtil.getCurrentPrincipal();
		String mqID = principal.getMqId();
		
		Order parentOrder = orderService.getOrderByOrderCode(orderCode, requestId);
		
		model.addAttribute("orderCode",orderCode);
		
		Map<String,Object> retMap = unionPayService.bankCardQuery(requestId, mqID);
		List<BankCard> bankCardList = new ArrayList<BankCard>();
		if((Integer)retMap.get("errcode")==0){
			bankCardList = (List<BankCard>) retMap.get("result");
		}
		
		//选择所有的银行卡
		model.addAttribute("pSum",parentOrder.getpSum());
		model.addAttribute("bankCardList",bankCardList);
		model.addAttribute("addType",AddType.pay.name());
		return "order/selectBankCard";
	}
	
	
	@RequestMapping("/wxPay")
	@ResponseBody
	public Map<String, String> wxPay(String requestId, String orderCode) {

		Principal principal = WebUtil.getCurrentPrincipal();
		// if(principal==null) principal=new Principal("2", "");
		String openID = principal.getOpenId();

		Map<String, String> unifiedOrderResult = new HashMap<String, String>();

		try {
			Order order = orderService.getOrderByOrderCode(orderCode, requestId);

			List<Order> childOrderList = orderService.getChildOrdersByParentOrderCode(orderCode, requestId);

			String firstProductName = "";

			List<OrderProduct> orderProductList = orderProductService.getOrderProductByOrderID(childOrderList.get(0).getID(), requestId);
			firstProductName = orderProductList.get(0).getProductName();

			Set<Integer> productNumSet = new HashSet<Integer>();
			if (childOrderList.size() == 1) {// 如果只有一个子订单 则 里面的订单商品 即使有多个
												// 也可能对应的是 同一个商品
				for (int i = 0; i < orderProductList.size(); i++) {
					productNumSet.add(orderProductList.get(i).getProductID());
				}
			}

			Map<String, String> parameters = new HashMap<String, String>();
			parameters.put("openid", openID);
			if (productNumSet.size() == 1) {
				parameters.put("body", firstProductName);
			} else {
				parameters.put("body", firstProductName + "等商品");
			}
			parameters.put("out_trade_no", orderCode);
			parameters.put("total_fee", order.getpSum() + "");

			unifiedOrderResult = wxInteractionService.unifiedOrder(requestId, parameters);
			unifiedOrderResult.put("orderCode", orderCode);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			unifiedOrderResult.put("errmsg", "下单失败,请重新操作");
		}

		return unifiedOrderResult;

	}

	@RequestMapping("/previewOrder")
	public String previewOrder(String requestId, String orderCode, String mode, Integer[] skuIds, Integer[] buyNums, Integer receaddID, Model model,
			HttpServletResponse response) {

		Principal principal = WebUtil.getCurrentPrincipal();
		String mqID = principal.getMqId();
		if (StringUtils.isNotBlank(orderCode)) {
			model.addAttribute("orderCode", orderCode);
		}

		Map<String, Object> supplierHouseOrderProducts = this.getSupplierHouseOrderProducts(skuIds, buyNums, requestId);

		// 这个list代表 多个供应商-商品的集合 一个Map代表从某个供应商买了多少东西 Map包含两个key
		// 1 供应商的名字
		// 2 在该供应商下购买的商品Vo的集合
		List<Map<String, Object>> supplierProducts = new ArrayList<Map<String, Object>>();

		if (mode == null || "".equals(mode.trim())) {
			supplierProducts = this.getShopCarProducts(skuIds, buyNums, requestId);
		} else if (mode.equals("buyNow")) {

			model.addAttribute("mode", mode);

			Map<String, Object> buyNowMap = new HashMap<String, Object>();
			ProductSku sku = productSkuService.getSkuByID(skuIds[0], requestId);
			Product product = productService.queryProduct(sku.getProductID(), requestId);
			Supplier supplier = supplierService.getSupplier(product.getSupplyID(), requestId);

			List<Map<String, Object>> productVoList = new ArrayList<Map<String, Object>>();
			// 由于是立即购买 所以只买了一个商品
			Map<String, Object> singleProduct = new HashMap<String, Object>();
			singleProduct.put("skuID", sku.getID());
			singleProduct.put("imgPath", sku.getSkuImg());
			singleProduct.put("productName", product.getProductName());
			singleProduct.put("skuStrs", productSkuService.getSkuStr(sku.getID(), requestId));
			singleProduct.put("skuPrice", sku.getSkuPrice());
			singleProduct.put("skuQuality", buyNums[0]);

			productVoList.add(singleProduct);

			buyNowMap.put("supName", supplier.getName());
			buyNowMap.put("supId", supplier.getID());
			buyNowMap.put("shopCarProducts", productVoList);

			supplierProducts.add(buyNowMap);

		}

		Long totalPrice = 0L;
		Long totalPostFee = 0L; // 单位为分

		// 查找最新的3条发票抬头
		Map<String, Object> commonInvoice = userService.selectCommonInvoice(requestId, mqID);
		model.addAttribute("invoice", commonInvoice.get("result"));

		for (int i = 0; i < supplierProducts.size(); i++) {

			Map<String, Object> map = supplierProducts.get(i);
			List<Map<String, Object>> sameSupplierOrderProductList = (List<Map<String, Object>>) map.get("shopCarProducts");

			for (Map<String, Object> eachMap : sameSupplierOrderProductList) {
				totalPrice += (Long) eachMap.get("skuPrice") * (Integer) eachMap.get("skuQuality");
			}
		}

		model.addAttribute("totalPrice", totalPrice);
		model.addAttribute("supplierProducts", supplierProducts);

		ReceiveAddress receiveAddress = null;
		if (receaddID == null) {
			receiveAddress = manageReceAddService.getFirstUsefulAddress(mqID, requestId);
		} else {
			receiveAddress = manageReceAddService.getAddress(receaddID, requestId);
		}

		
		//查询所有的收货地址
		List<ReceiveAddress> allUsableAddress = manageReceAddService.listUsableAddress(mqID, requestId);
		if(allUsableAddress!=null && allUsableAddress.size()>0){
			for(int i=0;i<allUsableAddress.size();i++){
				if(receiveAddress.getID().equals(allUsableAddress.get(i).getID())){
					allUsableAddress.remove(i);
				}
			}
		}
		
		List<Map<String,Object>> remainAddressList = new ArrayList<Map<String,Object>>();
		if(allUsableAddress!=null && allUsableAddress.size()>0){
			for(int i=0;i<allUsableAddress.size();i++){
				Map<String,Object> map = new HashMap<String,Object>();
				map.put("simpleAddress",manageReceAddService.getFullAddress(allUsableAddress.get(i), requestId).replace(allUsableAddress.get(i).getAddress(), ""));
				map.put("address", allUsableAddress.get(i));
				remainAddressList.add(map);
			}
		}
		
		
		model.addAttribute("remainAddressList",remainAddressList);
		
		for (int i = 0; i < supplierProducts.size(); i++) {

			Long supplierPostFee = 0L;

			if (receiveAddress != null) {
				for (Entry<String, Object> entry : supplierHouseOrderProducts.entrySet()) {
					String supplierHouse = entry.getKey();
					Integer supID = Integer.valueOf(supplierHouse.substring(0, supplierHouse.indexOf(":")));
					if (supID.equals((Integer) supplierProducts.get(i).get("supId"))) {
						List<OrderProduct> orderProductList = (List<OrderProduct>) entry.getValue();
						supplierPostFee += (Long) (postFeeService.calcPostFeeByOrderProductList(orderProductList, receiveAddress.getCountryID(),
								receiveAddress.getProvinceID(), receiveAddress.getCityID(), receiveAddress.getDistrictID(), requestId)
								.get("childOrderPostFee"));
					}
				}
			}

			// 计算一次邮费
			List<Map<String, Object>> shopCarProducts = (List<Map<String, Object>>) supplierProducts.get(i).get("shopCarProducts");

			double supplierAmount = 0.0;
			for (int j = 0; j < shopCarProducts.size(); j++) {
				Integer skuID = (Integer) shopCarProducts.get(j).get("skuID");
				Integer productID = productSkuService.getSkuByID(skuID, requestId).getProductID();
				if (receiveAddress != null) {
					Long postFee = postFeeService.calcPostFee(productID, (Integer) shopCarProducts.get(j).get("skuQuality"), receiveAddress.getCountryID(),
							receiveAddress.getProvinceID(), receiveAddress.getCityID(), receiveAddress.getDistrictID(), requestId);
					// supplierPostFee += postFee;
				}
				supplierAmount += (Integer) shopCarProducts.get(j).get("skuQuality") * (Long) shopCarProducts.get(j).get("skuPrice");

			}
			// 如果供应商对应的小计 不含运费 则 去掉以下这一行
			supplierAmount += supplierPostFee;
			supplierProducts.get(i).put("postFee", supplierPostFee);
			supplierProducts.get(i).put("supplierAmount", supplierAmount);
			totalPostFee += (supplierPostFee);
		}
		if (receiveAddress != null) {
			model.addAttribute("receaddID", receiveAddress.getID());
			model.addAttribute("recName", receiveAddress.getRecName());
			model.addAttribute("recMobile", receiveAddress.getRecMobile());
			model.addAttribute("addressPrefix",manageReceAddService.getFullAddress(receiveAddress, requestId).replace(receiveAddress.getAddress(), ""));
			model.addAttribute("fullAddress", receiveAddress.getAddress());
		}
		model.addAttribute("totalPostFee", totalPostFee);
		model.addAttribute("totalPrice", totalPrice + totalPostFee);

		// 查找出用户余额
		Map<String, Object> remainingMap = accountService.getRemainingSum(requestId, mqID);

		if ((Integer) remainingMap.get("errcode") != 0) {
			logger.info(remainingMap);
		} else {
			logger.info("remainingMap-->" + remainingMap);
			Long remainingSum = (Long) remainingMap.get("result");
			
			model.addAttribute("remainingSum",remainingSum);
			
			if (remainingSum.compareTo(totalPrice + totalPostFee) < 0) {
				model.addAttribute("remainNotEnough", "not enough");
			}
		}

		return "order-generate";

	}

	/*
	 * @RequestMapping("/viewOrder") public String viewOrder(String
	 * requestId,Integer orderID, Model model){
	 * 
	 * Principal principal = WebUtil.getCurrentPrincipal();
	 * //if(principal==null) principal=new Principal("2", ""); String mqID =
	 * principal.getMqId();
	 * 
	 * //假设参数有一个orderID Order order = orderService.getOrderByID(orderID,
	 * requestId); order = orderService.setReceAdd(mqID, order, requestId);
	 * 
	 * List<Order> childOrders =
	 * orderService.getChildOrdersByParentOrderID(orderID, requestId);
	 * 
	 * List<Map<String,Object>> viewList = new ArrayList<Map<String,Object>>();
	 * 
	 * 
	 * for(int i=0;i<childOrders.size();i++){
	 * 
	 * Order childOrder = childOrders.get(i);
	 * 
	 * //设置子订单的收货地址相关信息 childOrder.setRecName(order.getRecName());
	 * childOrder.setRecMobile(order.getRecMobile());
	 * childOrder.setAddress(order.getAddress());
	 * childOrder.setCountry(order.getCountry());
	 * childOrder.setProvince(order.getProvince());
	 * childOrder.setCity(order.getCity());
	 * childOrder.setDistrict(order.getDistrict());
	 * 
	 * // 一个订单商品对应多个子订单 List<OrderProduct> orderProducts =
	 * orderProductService.getOrderProductByOrderID(childOrder.getID(),
	 * requestId);
	 * 
	 * for(OrderProduct orderProduct:orderProducts){ //查找对应的商品 Product product =
	 * productService.queryProduct(orderProduct.getProductID(),requestId);
	 * 
	 * //查找对应的sku ProductSku productSku =
	 * productSkuService.getSkuByID(orderProduct.getSkuID(),requestId);
	 * 
	 * 
	 * Map<String,Object> orderProductMap = new HashMap<String,Object>();
	 * orderProductMap.put("supName", orderProduct.getSupName()); //设置供应商名字
	 * orderProductMap.put("sellDesc", product.getSellDesc()); //商品描述
	 * 
	 * List<String> skuStrs =
	 * productSkuService.getSkuStr(orderProduct.getSkuID(),requestId);
	 * 
	 * orderProductMap.put("skuStrs", skuStrs); //sku描述 尺码:39 颜色:红色
	 * orderProductMap.put("skuPrice", productSku.getSkuPrice()); //sku价格
	 * orderProductMap.put("skuImg", productSku.getSkuImg()); //sku图片
	 * orderProductMap.put("buyNum", orderProduct.getBuyNum()); //该sku的购买数量 //
	 * ReceiveAddress receiveAddress =
	 * manageReceAddService.getDefaultAddress(mqID, requestId);
	 * if(order.getRecName()==null){ orderProductMap.put("postFee", 0); }else{
	 * 
	 * Long postFee = postFeeService.calcPostFee(orderProduct.getProductID(),
	 * orderProduct.getBuyNum(), receiveAddress.getCountryID(),
	 * receiveAddress.getProvinceID(), receiveAddress.getCountryID(),
	 * receiveAddress.getDistrictID(), requestId); if(postFee==0){
	 * orderProductMap.put("postFee", "包邮"); }else{
	 * orderProductMap.put("postFee", postFee); }
	 * 
	 * }
	 * 
	 * viewList.add(orderProductMap); }
	 * 
	 * }
	 * 
	 * // //下单的时候不一定要填电话号码 model.addAttribute("recName",order.getRecName());
	 * model.addAttribute("recMobile",order.getRecMobile());
	 * model.addAttribute("fullAddress",order.getFullAddress());
	 * model.addAttribute("viewList",viewList);
	 * model.addAttribute("productNum",childOrders.size());
	 * model.addAttribute("orderSum",order.getpSum());
	 * 
	 * return "order-generate"; }
	 */

	private Map<String, Object> getSupplierHouseOrderProducts(Integer[] skuIds, Integer[] buyNums, String requestId) {

		Map<String, Object> supplierHouseMap = new HashMap<String, Object>();

		for (int i = 0; i < skuIds.length; i++) {
			ProductSku sku = productSkuService.getSkuByID(skuIds[i], requestId);
			Product product = productService.queryProduct(sku.getProductID(), requestId);
			Supplier supplier = supplierService.getSupplier(product.getSupplyID(), requestId);

			Integer houseID = sku.getHouseID();

			if (supplierHouseMap.get(supplier.getID() + ":" + houseID) == null) {
				List<OrderProduct> orderProductList = new ArrayList<OrderProduct>();
				OrderProduct orderProduct = new OrderProduct();
				orderProduct.setProductID(product.getID());
				orderProduct.setBuyNum(buyNums[i]);
				orderProduct.setSellPrice(sku.getSkuPrice());
				orderProductList.add(orderProduct);
				supplierHouseMap.put(supplier.getID() + ":" + houseID, orderProductList);
			} else {
				OrderProduct orderProduct = new OrderProduct();
				orderProduct.setProductID(product.getID());
				orderProduct.setBuyNum(buyNums[i]);
				orderProduct.setSellPrice(sku.getSkuPrice());
				List<OrderProduct> orderProductList = (List<OrderProduct>) supplierHouseMap.get(supplier.getID() + ":" + houseID);
				orderProductList.add(orderProduct);
				supplierHouseMap.put(supplier.getID() + ":" + houseID, orderProductList);
			}

		}

		return supplierHouseMap;
	}

	/**
	 * 点击立即购买
	 * 
	 * @param userID
	 * @param productID
	 * @param skuID
	 * @param buyNum
	 * @param model
	 * @return
	 */
	@RequestMapping("/buyNow")
	public String order(String requestId, Integer productID, Integer skuID, Integer buyNum, Long skuPrice, Model model) {

		Principal principal = WebUtil.getCurrentPrincipal();
		String mqID = principal.getMqId();

		return "redirect:previewOrder?mode=buyNow&skuIds=" + skuID + "&buyNums=" + buyNum;
	}

	/**
	 * 在购物车点击结算
	 */
	@RequestMapping("settle")
	public String settle(String requestId, Integer[] skuIDs, Integer[] buyNums, Long[] skuPrices) {

		Principal principal = WebUtil.getCurrentPrincipal();
		// if(principal==null) principal=new Principal("2", "");
		String mqID = principal.getMqId();

		List<ShopCarProduct> shopCarProducts = shopCarService.getShopCarProducts(mqID, requestId);
		for (int i = 0; i < skuIDs.length; i++) {
			for (int j = 0; j < shopCarProducts.size(); j++) {
				if (skuIDs[i].equals(Integer.valueOf(shopCarProducts.get(j).getSkuID()))) {
					if (!buyNums[i].equals(shopCarProducts.get(j).getQuality())) {
						ShopCarProduct shopCarProduct = shopCarProducts.get(j);
						shopCarProduct.setQuality(buyNums[i]);
						shopCarService.updateShopCarProduct(mqID, shopCarProduct, requestId);
					}
				}
			}
		}

		List<Integer> skuIDList = new ArrayList<Integer>();
		for (int i = 0; i < skuIDs.length; i++) {
			skuIDList.add(skuIDs[i]);
		}

		Set<Integer> deletedSkuIDs = new HashSet<Integer>();

		for (int i = 0; i < shopCarProducts.size(); i++) {
			if (!skuIDList.contains(shopCarProducts.get(i).getSkuID())) {
				deletedSkuIDs.add(shopCarProducts.get(i).getSkuID());
			}
		}

		if (deletedSkuIDs.size() != 0) {
			Integer[] deletedSkuIDArray = new Integer[deletedSkuIDs.size()];
			deletedSkuIDs.toArray(deletedSkuIDArray);
			shopCarService.removeShopCarProduct(mqID, deletedSkuIDArray, requestId);
		}

		return "redirect:previewOrder";

	}

	@RequestMapping("chooseReceAdd")
	public String chooseReceAdd(String requestId, Model model) {

		Principal principal = WebUtil.getCurrentPrincipal();
		// if(principal==null) principal=new Principal("2", "");
		String mqID = principal.getMqId();

		List<ReceiveAddress> adds = manageReceAddService.listUsableAddress(mqID, requestId);
		for (int i = 0; i < adds.size(); i++) {
			adds.get(i).setAddress(manageReceAddService.getFullAddress(adds.get(i), requestId));
		}
		model.addAttribute("adds", adds);
		return "orderReceAdd/manage";
	}

	/**
	 * 编辑用户收货地址 可能是更新也可能是新增
	 * 
	 * @param ID
	 * @param model
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/order_edit_rece_add")
	public String edit(String requestId, Integer ID, Model model) throws Exception {

		List<Address> provinces = addressService.getSubAddress(100, requestId);
		model.addAttribute("provinces", provinces);
		List<Address> cities = new ArrayList<Address>();
		List<Address> districts = new ArrayList<Address>();

		if (ID != null) {

			ReceiveAddress address = manageReceAddService.getAddress(ID, requestId);
			model.addAttribute("add", address);
			cities = addressService.getSubAddress(address.getProvinceID(), requestId);
			districts = addressService.getSubAddress(address.getCityID(), requestId);

		} else {
			cities = addressService.getSubAddress(10, requestId);
			districts = addressService.getSubAddress(1010, requestId);
		}
		model.addAttribute("cities", cities);
		model.addAttribute("districts", districts);
		return "orderReceAdd/edit";

	}

	/**
	 * 新增用户收货地址
	 * 
	 * @param receiveAddress
	 * @param model
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/order_add_rece_address")
	public String add(String requestId, ReceiveAddress receiveAddress, Model model) throws Exception {

		Principal principal = WebUtil.getCurrentPrincipal();
		// if(principal==null) principal=new Principal("2", "");
		String mqID = principal.getMqId();

		if (receiveAddress != null) {
			receiveAddress.setMqID(mqID);
		}

		manageReceAddService.addAddress(receiveAddress, requestId);

		// 返回地址列表页面
		return "redirect:chooseReceAdd";
	}

	/**
	 * 删除用户收货地址
	 * 
	 * @param ID
	 * @param model
	 * @return
	 */
	@RequestMapping("/order_del_rece_add")
	public String del(String requestId, int ID, Model model) {

		manageReceAddService.deleteAddress(ID, requestId);

		return "redirect:chooseReceAdd";
	}

	/**
	 * 更新用户收货地址
	 * 
	 * @param receiveAddress
	 * @param model
	 * @return
	 */
	@RequestMapping("/order_update_rece_add")
	public String update(String requestId, ReceiveAddress receiveAddress, Model model) {

		manageReceAddService.updateAddress(receiveAddress, requestId);

		return "redirect:chooseReceAdd";
	}

	/**
	 * 获得signature 作为前台wx.config
	 * 
	 * @return
	 */
	@RequestMapping("/getConfig")
	@ResponseBody
	public Map<String, String> getConfig(String requestId, String url) {

		Map<String, String> retMap = new HashMap<String, String>();
		retMap = wxInteractionService.getConfig(requestId, url);

		return retMap;
	}

	/**
	 * 统一下单通知
	 * 
	 * @param retXml
	 * @return
	 * @throws IOException
	 */
	@RequestMapping("/unifiedorder_notify")
	public void unifiedorder_notify(String requestId, HttpServletRequest req, HttpServletResponse resp) throws IOException {
		logger.error("674----->");
		resp.setContentType("application/xml");

		String retXml = "";
		InputStream is;
		try {
			is = req.getInputStream();
			retXml = IOUtils.toString(is);
		} catch (IOException e2) {
			logger.error("683-->");
			e2.printStackTrace();
		}
		logger.error("685-->");
		SortedMap<String, String> retMap = new TreeMap<String, String>();

		Pattern pattern = Pattern.compile("<([^/]\\S*?)>(.*?)</(\\S*?)>");
		Matcher m = pattern.matcher(retXml.replace("<xml>", "").replace("</xml>", ""));
		while (m.find()) {
			retMap.put(m.group(1), m.group(2).replace("<![CDATA[", "").replace("]]>", ""));
		}
		String return_code = retMap.get("return_code");

		if (return_code.equals("SUCCESS")) {
			String result_code = retMap.get("result_code");
			// 先验证签名
			String sign = retMap.get("sign");
			retMap.remove("sign");
			String createSign = wxInteractionService.createSign(retMap);

			// 验证签名

			if (sign.equals(createSign)) {
				String orderCode = retMap.get("out_trade_no");
				Long total_fee = Long.valueOf(retMap.get("total_fee"));
				String payTime = retMap.get("time_end");
				String fee_type = retMap.get("fee_type");
				String transaction_id = retMap.get("transaction_id");

				Order order = orderService.getOrderByOrderCode(orderCode, requestId);
				if (order != null) {
					if (result_code.equals("SUCCESS")) {

						if ("PAY".equals(order.getPayStatus())) {
							resp.getOutputStream().print("<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>");
							logger.error("订单已支付---->");
						}
						if (!order.getpSum().equals(total_fee)) {
							logger.fatal("支付金额不等于订单总额");
							resp.getOutputStream()
									.print("<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[支付金额有误,数据可能遭到篡改]]></return_msg></xml>");
						} else {
							// 设置订单的支付时间和支付金额 还有支付状态
							SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
							try {
								Date pay_time = sdf.parse(payTime);
								order.setPayTime(pay_time);
							} catch (ParseException e) {
								e.printStackTrace();
							}
							logger.error("732----->");
							List<Order> childOrderList = orderService.getChildOrdersByParentOrderCode(orderCode, requestId);
							for (int i = 0; i < childOrderList.size(); i++) {
								Order childOrderTmp = childOrderList.get(i);
								childOrderTmp.setPayStatus("PAY");
								childOrderTmp.setOrderStatus("ING");
								childOrderTmp.setPaySum(childOrderTmp.getOrderSum());
								childOrderTmp.setPayTime(order.getPayTime());
								orderService.updateOrder(childOrderTmp, requestId);
							}

							// 这个只是主订单 还要设置子订单的状态
							order.setPaySum(total_fee);
							order.setPayStatus("PAY");
							order.setOrderStatus("ING");
							orderService.updateOrder(order, requestId);
							OrderPayment orderPayment = new OrderPayment();

							// 新增订单支付对象
							orderPayment.setCreateTime(new Date());
							orderPayment.setMoneyType(fee_type);
							orderPayment.setMqID(order.getMqID());
							orderPayment.setOrderCode(orderCode);
							orderPayment.setPayChannel(OrderPayment.PayChannel.WXP.toString());
							orderPayment.setPayNum(transaction_id);
							orderPayment.setPayStatus("PAY");
							orderPayment.setPaySum(total_fee);
							orderPayment.setPayTime(order.getPayTime());

							orderService.addOrderPayment(orderPayment, requestId);

							MapData data = MapUtil.newInstance(userService.findWxUserByMqId(requestId, order.getMqID()));
							MapData result = data.getResult();
							String openId = result.getString("openId");
							Map<String, String> msgParam = new HashMap<String, String>();
							// TODO

							logger.error("769---->");
							data = MapUtil.newInstance(userService.findUserByMqId(requestId, order.getMqID()));
							result = data.getResult();
							Integer isGoldType = result.getInteger("isGoldType");
							if (isGoldType == 1) {// 如果已经是金牌用户

							}
							logger.error("776----->");
							// TODO 发送消息 开始分佣
							mqService.orderPaid(requestId, order.getOrderCode());
							mqService.sendWxMessage(requestId, openId, "", "57137ee9b2a4e", msgParam);

							// 更改商品的销量
							productService.updateSellCount(orderCode, requestId);
							logger.error("783----->");
						}

					} else {
						logger.error("通信错误");
					}
				} else {
					logger.error("订单不存在");
				}

			} else {
				logger.fatal("签名失败,数据可能遭到篡改");
				resp.getOutputStream().print("<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[签名失败]]></return_msg></xml>");
			}
		} else {
			logger.error("通信错误,请检查网络状态");
		}

		resp.getOutputStream().print("<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>");

	}

	/**
	 * 点击 某个订单的退货按钮 跳转到这里 然后查找出相关的信息 再跳转到申请退货页面 避免微信地址太长
	 * 
	 * @param o
	 *            订单号 orderCode
	 * @param p
	 *            订单商品ID orderProductID
	 */
	@RequestMapping("/rg")
	public String returnGoods(String requestId, String o, Integer p, Model model) {

		OrderProduct orderProduct = orderProductService.getOrderProductByID(p, requestId);
		model.addAttribute("refundLimit", (orderProduct.getBuyNum() * orderProduct.getSellPrice()) / 100.0);// 支付多少钱
																											// 不退运费
																											// 最多退款多少钱
		
		
		Order order = orderService.getOrderByID(orderProduct.getOrderID(), requestId);
		
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
		
		model.addAttribute("orderCode", o);
		model.addAttribute("orderProductID", p);
		return "order/return-goods";

	}

	@RequestMapping("/r2")
	public String returnGoods2(String requestId, Integer p, Model model) {

		OrderProduct orderProduct = orderProductService.getOrderProductByID(p, requestId);
		Order order = orderService.getOrderByID(orderProduct.getOrderID(), requestId);
		model.addAttribute("refundLimit", (orderProduct.getBuyNum() * orderProduct.getSellPrice()) / 100.0);// 支付多少钱
																											// 不退运费
																											// 最多退款多少钱
		
		
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
		model.addAttribute("orderCode", order.getOrderCode());
		model.addAttribute("orderProductID", p);
		return "order/return-goods";

	}

	@RequestMapping("/query-logistics")
	public String queryLogistics(String requestId, String num, Integer logisticsId, Model model){
		
		LogisticsFormVo logisticsFormVo = logisticsService.getLogisticsFormByCompanyID(num, logisticsId, requestId);
		
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
		
		return "order/logistics";
		
	}
	
	/**
	 * 点击某个已支付订单的退款按钮 经过这里 再跳转到申请退款的页面
	 */
	@RequestMapping("/return-money")
	public String returnMoney(String requestId, String orderCode, Model model) {

		Order order = orderService.getOrderByOrderCode(orderCode, requestId);
		model.addAttribute("refundLimit", order.getOrderSum() / 100.0);// 支付多少钱
																		// 最多退款多少钱
		
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
		
		model.addAttribute("orderCode", orderCode);
		
		return "order/return-money";

	}

	/**
	 * 在退货页面 点击 申请退货
	 * 
	 * @param requestID
	 * @param orderCode
	 * @param returnReason
	 *            退货原因
	 * @param refundFee
	 *            退款金额
	 * @param returnDesc
	 *            退货说明
	 * @return
	 */
	@RequestMapping("/apply-return-goods")
	public String applyReturnGoods(String requestId, String orderCode, Integer modify, Integer orderProductID, String returnReason, Long refundFee,
			String returnDesc, String[] imgs, Model model) {

		Principal principal = WebUtil.getCurrentPrincipal();
		// if(principal==null) principal=new Principal("2", "");
		String mqID = principal.getMqId();
		String openId = principal.getOpenId();

		try {

			logger.info("imgs:" + imgs);

			if (imgs != null && imgs.length > 0) {
				this.fetch(imgs);

				for (int i = 0; i < imgs.length; i++) {
					if (imgs[i].indexOf("#old") < 0) {
						imgs[i] = "http://" + qiNiuDomain + "/return_img_" + imgs[i];
					}
				}
			}

			GoodsReturn goodsReturn = orderService.applyReturnGoods(mqID, orderCode, orderProductID, refundFee, returnReason, returnDesc, imgs, requestId);

			if (modify == null || modify != 1) {
				try{
					Map<String, String> msgParam = new HashMap<String, String>();
					msgParam.put("orderCode", goodsReturn.getOrderCode());
					mqService.sendWxMessage(requestId, openId, null, "5713808f49538", msgParam);
					logger.info("发送申请退货队列");
				}catch(Exception e){
					logger.info(e.getMessage(),e);
				}
							
			}

			logger.info("goodsReturn.returnImgs:" + goodsReturn.getReturnImgs());

			model.addAttribute("goodsReturn", goodsReturn);
			if (goodsReturn.getReturnReason().equals("SJ#")) {
				model.addAttribute("returnReason", "少件/漏发");
			} else if (goodsReturn.getReturnReason().equals("FP#")) {
				model.addAttribute("returnReason", "发票问题");
			} else {
				model.addAttribute("returnReason", GoodsReturn.ReturnReason.valueOf(goodsReturn.getReturnReason()).getValue());
			}
			
			Order order = orderService.getOrderByID(goodsReturn.getOrderID(), requestId);
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
			
			model.addAttribute("orderCode",orderCode);
			
			
		} catch (RuntimeException e) {
			logger.error(e.getMessage(), e);
			// 返回错误页面
			return "404";
		}

		return "order/returnG-examine";
	}

	@RequestMapping("/modify-return-goods")
	public String modifyReturnGoods(String requestId, Integer goodsReturnID, Model model) {

		GoodsReturn goodsReturn = orderService.getGoodsReturnByID(goodsReturnID, requestId);
		OrderProduct orderProduct = orderProductService.getOrderProductByID(goodsReturn.getOrderProductID(), requestId);

		model.addAttribute("imgs", goodsReturn.getReturnImgs());

		Order order = orderService.getOrderByID(goodsReturn.getOrderID(), requestId);
		model.addAttribute("orderCode", goodsReturn.getOrderCode());
		model.addAttribute("orderProductID", goodsReturn.getOrderProductID());
		model.addAttribute("returnReasonCode", goodsReturn.getReturnReason());
		
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
		
		if (goodsReturn.getReturnReason().equals("SJ#")) {
			model.addAttribute("returnReason", "少件/漏发");
		} else if (goodsReturn.getReturnReason().equals("FP#")) {
			model.addAttribute("returnReason", "发票问题");
		} else {
			model.addAttribute("returnReason", GoodsReturn.ReturnReason.valueOf(goodsReturn.getReturnReason()).getValue());
		}
		model.addAttribute("refundLimit", (orderProduct.getBuyNum() * orderProduct.getSellPrice()) / 100.0);
		model.addAttribute("applySum", goodsReturn.getApplySum() / 100.0);
		model.addAttribute("returnDesc", goodsReturn.getReturnDesc());
		return "order/modify-returnG";

	}

	/**
	 * 申请退款 点击 提交申请 按钮
	 * 
	 * @param requestID
	 * @param orderCode
	 *            订单编号
	 * @param returnReason
	 *            退款原因
	 * @param refundFee
	 *            退款金额
	 * @param returnDesc
	 *            退款描述
	 * @param model
	 * @return
	 */
	@RequestMapping("/apply-return-money")
	public String applyReturnMoney(String requestId, String orderCode, Integer modify, String returnReason, Long refundFee, String returnDesc, Model model) {

		Principal principal = WebUtil.getCurrentPrincipal();
		String mqID = principal.getMqId();
		String openId = principal.getOpenId();

		MoneyReturn moneyReturn = orderService.applyReturnMoney(mqID, orderCode, refundFee, returnReason, returnDesc, requestId);

		if (modify == null || modify != 1) {
			try{
				Map<String, String> msgParam = new HashMap<String, String>();
				msgParam.put("orderCode", moneyReturn.getOrderCode());
				mqService.sendWxMessage(requestId, openId, null, "57138005e04c7", msgParam);
				logger.info("发送申请退款队列");
			}catch(Exception e){
				logger.info(e.getMessage(),e);
			}
		}

		model.addAttribute("moneyReturn", moneyReturn);
		if (moneyReturn.getReturnReason().equals("SJ#")) {
			model.addAttribute("returnReason", "少件/漏发");
		} else if (moneyReturn.getReturnReason().equals("FP#")) {
			model.addAttribute("returnReason", "发票问题");
		} else {
			model.addAttribute("returnReason", MoneyReturn.ReturnReason.valueOf(moneyReturn.getReturnReason()).getValue());
		}
		
		Order order = orderService.getOrderByOrderCode(orderCode, requestId);
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
		
		model.addAttribute("orderCode",orderCode);
		
		System.out.println("return now");
		
		return "order/returnR-examine";
	}

	/**
	 * 点击 修改退款申请 只有在 前一次申请 还没有审核的情况下 才能 修改
	 * 
	 * @param requestID
	 * @param moneyReturnID
	 * @param model
	 * @return
	 */
	@RequestMapping("/modify-return-money")
	public String modifyReturnMoney(String requestId, Integer moneyReturnID, Model model) {

		MoneyReturn moneyReturn = orderService.getMoneyReturnByID(moneyReturnID, requestId);
		Order order = orderService.getOrderByID(moneyReturn.getOrderID(), requestId);
		model.addAttribute("orderCode", moneyReturn.getOrderCode());
		model.addAttribute("returnReasonCode", moneyReturn.getReturnReason());
		
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
		
		System.out.println("order:"+order);
		
		if (moneyReturn.getReturnReason().equals("SJ#")) {
			model.addAttribute("returnReason", "少件/漏发");
		} else if (moneyReturn.getReturnReason().equals("FP#")) {
			model.addAttribute("returnReason", "发票问题");
		} else {
			model.addAttribute("returnReason", MoneyReturn.ReturnReason.valueOf(moneyReturn.getReturnReason()).getValue());
		}
		model.addAttribute("refundLimit", order.getPaySum() / 100.0);
		model.addAttribute("applySum", moneyReturn.getApplySum() / 100.0);
		model.addAttribute("returnDesc", moneyReturn.getReturnDesc());
		return "order/modify-returnR";

	}

	/**
	 * 查看我的订单
	 * 
	 * @param requestID
	 * @param model
	 * @return
	 */
	@RequestMapping("/order-index")
	public String orderIndex(String requestId, Model model, Integer pageIndex, Integer pageSize, String orderType, String tab, HttpServletRequest request) {

		Principal principal = WebUtil.getCurrentPrincipal();
		// if(principal==null)principal = new Principal("2","");
		String mqID = principal.getMqId();

		String appId = (String) request.getAttribute("appId");
		// 1 查找出所有的订单商品
		List<Map<String, Object>> childOrderList = orderService.getOrderListByMqID(mqID, 1, 8, requestId);

		// 查找出待评论的商品 默认第1页 每页8条
		List<Map<String, Object>> waitForCommentList = orderService.selectWaitForCommentList(mqID, 0, DEFAULT_PAGE_SIZE, requestId);

		// 查找出待评论的
		List<Map<String, Object>> returnGoodsList = orderService.selectReturnGoodsList(mqID, 0, DEFAULT_PAGE_SIZE, requestId);

		List<Map<String, Object>> noPayedOrderList = orderService.selectNoPayedOrder(mqID, 0, DEFAULT_PAGE_SIZE, requestId);
		logger.info("noPayedOrderList:" + noPayedOrderList);

		List<Map<String, Object>> noSendOrderList = orderService.selectNoSendOrder(mqID, 0, DEFAULT_PAGE_SIZE, requestId);
		logger.info("noSendOrderList:" + noSendOrderList);

		//查询退款相关的
		List<Map<String,Object>> returnMoneyList = orderService.selectReturnMoneyList(mqID, 0, DEFAULT_PAGE_SIZE, requestId);
		
		model.addAttribute("noPayedOrderList", noPayedOrderList);
		model.addAttribute("noSendOrderList", noSendOrderList);
		model.addAttribute("childOrderList", childOrderList);
		
		for(int i=0;i<childOrderList.size();i++){
			
			if(childOrderList.get(i).get("status").equals("DFK")){
				
				childOrderList.get(i).put("remainTime", remainTime((Date) childOrderList.get(i).get("createTime"), requestId));
				
			}
			
		}
		
		
		for(int i=0;i<noPayedOrderList.size();i++){
			
			
			noPayedOrderList.get(i).put("remainTime", remainTime((Date) noPayedOrderList.get(i).get("createTime"), requestId));
			
			
		}
		
		
		model.addAttribute("waitForCommentList", waitForCommentList);
		model.addAttribute("returnMoneyList", returnMoneyList);
		model.addAttribute("returnGoodsList", returnGoodsList);
		model.addAttribute("tab", tab);

		return "/order/order-index";
	}

	@RequestMapping("/return-goods-detail")
	public String returnGoodsDetail(String requestId, Integer goodsReturnID, Model model) {

		Principal principal = WebUtil.getCurrentPrincipal();
		// if(principal==null)principal = new Principal("2","");
		String mqID = principal.getMqId();

		GoodsReturn goodsReturn = orderService.getGoodsReturnByID(goodsReturnID, requestId);
		Map<String, Object> supReceAdd = supplierService.getReceAddByID(goodsReturn.getSupID(), requestId);
		logger.info("supReceAdd-->" + supReceAdd);
		if (supReceAdd != null) {
			model.addAttribute("fullAddress", supReceAdd.get("returnAddress"));
			model.addAttribute("recName", supReceAdd.get("returnContactName"));
			model.addAttribute("recMobile", supReceAdd.get("returnContactPhone"));
		}
		model.addAttribute("goodsReturn", goodsReturn);

		model.addAttribute("applySum", goodsReturn.getApplySum() / 100.0);
		model.addAttribute("returnDesc", goodsReturn.getReturnDesc());

		// 退货原因
		if (goodsReturn.getReturnReason().equals("SJ#")) {
			model.addAttribute("returnReason", "少件/漏发");
		} else if (goodsReturn.getReturnReason().equals("FP#")) {
			model.addAttribute("returnReason", "发票问题");
		} else {
			model.addAttribute("returnReason", GoodsReturn.ReturnReason.valueOf(goodsReturn.getReturnReason()).getValue());
		}

		Order order = orderService.getOrderByID(goodsReturn.getOrderID(), requestId);
		model.addAttribute("orderCode", goodsReturn.getOrderCode());
		model.addAttribute("returnReasonCode", goodsReturn.getReturnReason());
		
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
		
		
		String status = goodsReturn.getStatus();

		if ("STH".equals(status)) {
			return "order/returnG-examine";
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

			return "order/returnG-pass";
		} else if ("THZ".equals(status)) { // 用户填写了退货物流信息之后
			return "order/returnG-takeG";
		} else if ("NO#".equals(status)) {
			return "order/returnG-reject";
		} else if ("DTK".equals(status)) {
			return "order/returnG-dtk";
		} else if ("TKZ".equals(status)) {
			return "order/returnG-tkz";
		} else if ("JTK".equals(status)) {
			return "order/returnG-jtk";
		} else if ("FAI".equals(status)) {
			return "order/returnG-reject";
		} else if ("COM".equals(status)) {
			return "order/returnG-success";
		}

		return "404";
	}

	@RequestMapping("/return-money-detail")
	public String returnMoneyDetail(String requestId, Integer moneyReturnID, Model model) {

		Principal principal = WebUtil.getCurrentPrincipal();
		// if(principal==null)principal = new Principal("2","");
		String mqID = principal.getMqId();

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
			return "order/returnR-examine";
		} else if ("NO#".equals(moneyReturn.getStatus())) {// 商家审核不通过
			return "order/returnR-reject";
		} else if ("TKZ".equals(moneyReturn.getStatus())) {// 审核通过 正在退款中
			return "order/returnR-pass";
		} else if ("COM".equals(moneyReturn.getStatus())) {// 后台确认退款
			return "order/returnR-success";
		} else if ("FAI".equals(moneyReturn.getStatus())) {
			return "order/returnR-reject";
		}

		return "404";
	}

	/**
	 * 当用户支付完成之后返回支付结果
	 * 
	 * @param requestId
	 * @param orderCode
	 *            主订单号
	 * @return
	 */
	@RequestMapping("/pay-result")
	public String payResult(String requestId, String orderCode, String isUnion, Model model) {

		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		
		model.addAttribute("orderCode", orderCode);
		Order order = orderService.getOrderByOrderCode(orderCode, requestId);
		logger.info("order:"+order);
		if (order.getPayStatus().equals("PAY")) {
			model.addAttribute("payResult", "success");
		} else {
			if("true".equals(isUnion)){
				Map<String,Object> retMap = unionPayService.queryPayResult(requestId, null, orderCode, sdf.format(order.getCreateTime()));
				logger.info(retMap);
				if((Integer)retMap.get("errcode")==0){
					model.addAttribute("payResult", "success");
				}else{
					model.addAttribute("payResult", "fail");
				}
			}else{
				model.addAttribute("payResult", "fail");
			}
		}
		return "order/pay-result";
	}

	/**
	 * 查看 待发货或待评论 订单详情
	 * 
	 * @param orderCode
	 *            订单号
	 */
	@RequestMapping("/order-detail")
	public String orderDetail(String requestId, String orderCode, Integer orderID, Model model) {

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
			childOrderMap.put("childOrdreProductVo", childOrderProductVo);

			childOrderVo.add(childOrderMap);
			model.addAttribute("childOrderVo", childOrderVo);
			model.addAttribute("orderCode", parentOrder.getOrderCode());

			// 查看订单状态
			if ("COM".equals(orderStatus)) {// 已完成
				return "order/child-order-detail";
			} else if ("CAN".equals(orderStatus)) {// 已取消

				return "order/child-order-detail";

			} else if ("PAY".equals(payStatus) && "NOA".equals(returnMoneyStatus) && "NOT".equals(shippingStatus)) {// 付完款
																													// 没有申请退款
																													// 也没有发货
																													// (待发货)

				return "order/child-order-detail";

			} else if (!"NOA".equals(returnMoneyStatus)) {// 和退款相关的 都转到
															// return-money-detail
				logger.info("1165: ");
				MoneyReturn moneyReturn = orderService.getNewestMoneyReturnByChildOrderCode(orderCode, requestId);
				return "redirect:return-money-detail?moneyReturnID=" + moneyReturn.getID();
			} else if ("PAY".equals(payStatus) && "NOA".equals(returnMoneyStatus) && "HAV".equals(shippingStatus)) {// 已发货

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
				orderProductMap.put("productID", sku.getProductID());
				orderProductMap.put("buyNum", op.getBuyNum());
				childOrderProductVo.add(orderProductMap);
			}

			childOrderMap.put("supName", orderProductList.get(0).getSupName());
			childOrderMap.put("orderCode", childOrder.getOrderCode());
			childOrderMap.put("childOrdreProductVo", childOrderProductVo);

			childOrderVo.add(childOrderMap);
		}

		model.addAttribute("childOrderVo", childOrderVo);
		model.addAttribute("orderCode", orderCode);

		return "order/order-detail";
	}

	/**
	 * 查看待付款订单详情
	 */
	@RequestMapping("/order-nopay-detail")
	public String orderNoPayDetail(String requestId, String orderCode, Model model) {

		if (StringUtils.isBlank(orderCode)) {
			return "404";
		}

		// 子订单的列表 一个Map代表一个子订单
		List<Map<String, Object>> childOrderVo = new ArrayList<Map<String, Object>>();

		Order order = orderService.getOrderByOrderCode(orderCode, requestId);

		if (order == null) {
			return "404";
		}

		List<Order> childOrderList = orderService.getChildOrdersByParentOrderID(order.getID(), requestId);

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
				orderProductMap.put("buyNum", op.getBuyNum());
				List<String> skuStrs = productSkuService.getSkuStr(sku.getID(), requestId);
				orderProductMap.put("skuStrs", skuStrs);
				childOrderProductVo.add(orderProductMap);
			}

			childOrderMap.put("supName", orderProductList.get(0).getSupName());

			childOrderMap.put("childOrdreProductVo", childOrderProductVo);

			childOrderVo.add(childOrderMap);
		}

		// 剩余时间
		model.addAttribute("remainTime", remainTime);

		// 收货人信息
		model.addAttribute("recName", order.getRecName());
		model.addAttribute("recMobile", order.getRecMobile());
		String address = StringUtils.defaultString(order.getProvince(), "") + StringUtils.defaultString(order.getCity(), "")
				+ StringUtils.defaultString(order.getDistrict(), "") + StringUtils.defaultString(order.getAddress(), "");
		model.addAttribute("address", address);

		// 主订单和子订单相关信息
		model.addAttribute("orderCode", order.getOrderCode());
		model.addAttribute("childOrderVo", childOrderVo);

		// 备注和金额
		model.addAttribute("notes", order.getNotes());
		model.addAttribute("pFee", order.getpFee());
		model.addAttribute("pSum", order.getpSum());

		// 创建时间和发票抬头
		model.addAttribute("createTime", order.getCreateTime());
		model.addAttribute("invoiceName", order.getInvoiceName());

		// 订单超时
		if (remainTime.equals("OUT_OF_DATE")) {
			if (order.getOrderStatus().equals("ING")) {
				order.setOrderStatus("CAN");
				orderService.updateOrder(order, requestId);
				for (Order childOrder : childOrderList) {
					childOrder.setOrderStatus("CAN");
					orderService.updateOrder(childOrder, requestId);
				}
			}
			return "order/out_of_date";
		}

		return "order/nopay-detail";
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

	/**
	 * 根据skuIds 和 buyNums 返回对应的供应商-购买商品的列表
	 * 
	 * @param skuIds
	 * @param buyNums
	 * @param requestID
	 * @return
	 */
	private List<Map<String, Object>> getShopCarProducts(Integer[] skuIds, Integer[] buyNums, String requestID) {

		Map<Integer, Supplier> supplierMap = new HashMap<Integer, Supplier>();
		Map<Integer, List<Map<String, Object>>> supplierShopProductsMap = new HashMap<Integer, List<Map<String, Object>>>();

		Supplier supplier = null;
		List<Map<String, Object>> shopCarProductsList = new ArrayList<Map<String, Object>>();

		for (int i = 0; i < skuIds.length; i++) {

			ProductSku sku = productSkuService.getSkuByID(skuIds[i], requestID);
			Product product = productService.queryProduct(sku.getProductID(), requestID);

			List<Map<String, Object>> sameSupplierShopCarProducts = supplierShopProductsMap.get(product.getSupplyID());
			if (sameSupplierShopCarProducts == null) {
				// 此处相当于某个供应商 有一个list来跟它对应
				sameSupplierShopCarProducts = new ArrayList<Map<String, Object>>();
				supplierShopProductsMap.put(product.getSupplyID(), sameSupplierShopCarProducts);
			}

			// 此处采用map起到缓存作用 不用每次都到数据库去查对应的供应商
			supplier = supplierMap.get(product.getSupplyID());
			if (supplier == null) {
				supplier = supplierService.getSupplier(product.getSupplyID(), requestID);
				supplierMap.put(supplier.getID(), supplier);
			}

			List<String> skuStrs = productSkuService.getSkuStr(skuIds[i], requestID);

			Map<String, Object> map = new HashMap<String, Object>();

			// 1 存储skuID
			map.put("skuID", sku.getID());
			// 2 存储sku对应的图片
			map.put("imgPath", sku.getSkuImg());
			// 3 存储sku对应的商品描述
			map.put("productName", product.getProductName());
			// 4 存储sku对应的sku属性
			map.put("skuStrs", skuStrs);
			// 5 存储sku对应的价格
			map.put("skuPrice", sku.getSkuPrice());
			// 6 存储sku对应的购买数量
			map.put("skuQuality", buyNums[i]);

			sameSupplierShopCarProducts.add(map);

		}

		for (Entry<Integer, Supplier> entry : supplierMap.entrySet()) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("supName", entry.getValue().getName());
			map.put("supId", entry.getValue().getID());
			map.put("shopCarProducts", supplierShopProductsMap.get(entry.getKey()));
			shopCarProductsList.add(map);
		}

		return shopCarProductsList;

	}

	@RequestMapping("/cancel")
	public String cancelOrder(String requestId, String orderCode, Model model) {

		if (orderCode != null && !"".equals(orderCode)) {
			Order order = orderService.getOrderByOrderCode(orderCode, requestId);
			if (order != null) {

				if (!"CAN".equals(order.getOrderStatus())) {
					order.setOrderStatus("CAN");
					List<Order> childOrderList = orderService.getChildOrdersByParentOrderCode(orderCode, requestId);
					for (int i = 0; i < childOrderList.size(); i++) {
						childOrderList.get(i).setOrderStatus("CAN");
						orderService.updateOrder(childOrderList.get(i), requestId);

						List<OrderProduct> orderProductList = orderProductService.getOrderProductByOrderID(childOrderList.get(i).getID(), requestId);
						for (int j = 0; j < orderProductList.size(); j++) {
							OrderProduct orderProduct = orderProductList.get(j);
							Integer buyNum = orderProduct.getBuyNum();

							ProductSku sku = productSkuService.getSkuByID(orderProduct.getSkuID(), requestId);
							if (sku.getSkuStock() == null) {
								sku.setSkuStock(buyNum);
							} else {
								sku.setSkuStock(sku.getSkuStock() + buyNum);
							}
							productSkuService.updateSku(sku, requestId);
							productSkuService.clearSkuListByProductID(orderProduct.getProductID(), requestId);
						}

					}
					orderService.updateOrder(order, requestId);
				}

			}
		}

		return "order/cancel";
	}

	@RequestMapping("/pay-again")
	public String payAgain(String requestId, String orderCode, Model model) {

		Principal principal = WebUtil.getCurrentPrincipal();
		// if(principal==null)principal = new Principal("2","");
		String mqID = principal.getMqId();

		Order parentOrder = orderService.getOrderByOrderCode(orderCode, requestId);
		if (parentOrder == null) {
			return "404";
		}

		model.addAttribute("orderCode", orderCode);

		List<Integer> skuIdsList = new ArrayList<Integer>();
		List<Integer> buyNumsList = new ArrayList<Integer>();

		List<Order> childOrderList = orderService.getChildOrdersByParentOrderCode(orderCode, requestId);
		for (int i = 0; i < childOrderList.size(); i++) {
			Order childOrder = childOrderList.get(i);
			List<OrderProduct> orderProductList = orderProductService.getOrderProductByOrderID(childOrder.getID(), requestId);
			for (int j = 0; j < orderProductList.size(); j++) {
				OrderProduct orderProduct = orderProductList.get(j);
				skuIdsList.add(orderProduct.getSkuID());
				buyNumsList.add(orderProduct.getBuyNum());
			}
		}

		Integer[] skuIds = new Integer[skuIdsList.size()];
		Integer[] buyNums = new Integer[buyNumsList.size()];
		skuIdsList.toArray(skuIds);
		buyNumsList.toArray(buyNums);

		List<Map<String, Object>> supplierProducts = new ArrayList<Map<String, Object>>();

		/* 代表多个供应商的 列表 不细化到 仓库 */
		supplierProducts = this.getShopCarProducts(skuIds, buyNums, requestId);

		// 1 收货地址
		model.addAttribute("recName", parentOrder.getRecName());
		model.addAttribute("recMobile", parentOrder.getRecMobile());
		String address = "";
		if (StringUtils.isNotBlank(parentOrder.getProvince())) {
			address += parentOrder.getProvince();
		}
		if (StringUtils.isNotBlank(parentOrder.getCity())) {
			address += parentOrder.getCity();
		}
		if (StringUtils.isNotBlank(parentOrder.getDistrict())) {
			address += parentOrder.getDistrict();
		}
		if (StringUtils.isNotBlank(parentOrder.getAddress())) {
			address += parentOrder.getAddress();
		}
		model.addAttribute("fullAddress", address);

		// 2 商品
		Long totalPrice = 0L;
		Long totalPostFee = 0L; // 单位为分
		for (int i = 0; i < supplierProducts.size(); i++) {

			Map<String, Object> map = supplierProducts.get(i);
			List<Map<String, Object>> sameSupplierOrderProductList = (List<Map<String, Object>>) map.get("shopCarProducts");

			for (Map<String, Object> eachMap : sameSupplierOrderProductList) {
				totalPrice += (Long) eachMap.get("skuPrice") * (Integer) eachMap.get("skuQuality");
			}
		}

		model.addAttribute("totalPrice", totalPrice);
		model.addAttribute("supplierProducts", supplierProducts);

		for (int i = 0; i < childOrderList.size(); i++) {
			Order childOrder = childOrderList.get(i);
			for (int j = 0; j < supplierProducts.size(); j++) {
				logger.info("childOrder.getSupID:" + childOrder.getSupID());
				logger.info("supplierProducts.get(j).get(supId)):" + supplierProducts.get(j).get("supId"));
				logger.info("childOrder.getSupID().equals((Integer)supplierProducts.get(j).get(supId)):"
						+ childOrder.getSupID().equals((Integer) supplierProducts.get(j).get("supId")));
				if (childOrder.getSupID().equals((Integer) supplierProducts.get(j).get("supId"))) {
					logger.info("supId equals：" + childOrder.getSupID());
					if (supplierProducts.get(j).get("postFee") == null) {
						logger.info("1708");
						supplierProducts.get(j).put("postFee", childOrder.getPostFee());
					} else {
						logger.info("1711");
						Long oldPostFee = (Long) supplierProducts.get(j).get("postFee");
						supplierProducts.get(j).put("postFee", oldPostFee + childOrder.getPostFee());
					}
					if (supplierProducts.get(j).get("supplierAmount") == null) {
						logger.info("1716");
						supplierProducts.get(j).put("supplierAmount", childOrder.getOrderSum());
					} else {
						logger.info("1719");
						Long oldSupplierAmount = (Long) supplierProducts.get(j).get("supplierAmount");
						supplierProducts.get(j).put("supplierAmount", oldSupplierAmount + childOrder.getOrderSum());
					}

				}
			}
		}

		/*
		 * for(int i=0;i<supplierProducts.size();i++){
		 * 
		 * //计算一次邮费 List<Map<String,Object>> shopCarProducts = (List<Map<String,
		 * Object>>) supplierProducts.get(i).get("shopCarProducts"); Long
		 * supplierPostFee = 0L; double supplierAmount = 0.0; for(int
		 * j=0;j<shopCarProducts.size();j++){ Integer skuID = (Integer)
		 * shopCarProducts.get(j).get("skuID"); Integer productID =
		 * productSkuService.getSkuByID(skuID, requestId).getProductID(); Long
		 * postFee = postFeeService.calcPostFee(productID, (Integer)
		 * shopCarProducts.get(j).get("skuQuality"), parentOrder.getCountryID(),
		 * parentOrder.getProvinceID(), parentOrder.getCityID(),
		 * parentOrder.getDistrictID(), requestId); supplierPostFee += postFee;
		 * supplierAmount+=(Integer)
		 * shopCarProducts.get(j).get("skuQuality")*(Long)
		 * shopCarProducts.get(j).get("skuPrice");
		 * 
		 * } //如果供应商对应的小计 不含运费 则 去掉以下这一行 supplierAmount+=supplierPostFee;
		 * supplierProducts.get(i).put("postFee", supplierPostFee);
		 * supplierProducts.get(i).put("supplierAmount", supplierAmount);
		 * totalPostFee+=(supplierPostFee); }
		 */

		model.addAttribute("totalPostFee", parentOrder.getpFee());
		model.addAttribute("totalPrice", parentOrder.getpSum());

		Map<String, Object> remainingMap = accountService.getRemainingSum(requestId, mqID);

		if ((Integer) remainingMap.get("errcode") != 0) {
			logger.info(remainingMap);
		} else {
			logger.info("remainingMap-->" + remainingMap);
			Long remainingSum = (Long) remainingMap.get("result");

			if (remainingSum.compareTo(parentOrder.getpSum()) < 0) {
				model.addAttribute("remainNotEnough", "not enough");
			}
		}

		// 3 发票抬头
		Map<String, Object> commonInvoice = userService.selectCommonInvoice(requestId, mqID);
		model.addAttribute("invoice", commonInvoice.get("result"));

		Integer invoiceID = parentOrder.getInvoiceID();
		String invoiceName = parentOrder.getInvoiceName();
		logger.info("orderCode:" + parentOrder.getOrderCode());
		logger.info("invoiceID:" + invoiceID);
		logger.info("invoiceName:" + invoiceName);

		model.addAttribute("invoiceID", invoiceID);
		model.addAttribute("invoiceName", invoiceName);
		model.addAttribute("notes", parentOrder.getNotes());

		return "order/pay-again";
	}

	/**
	 * 返回23位的支付单号 时间+6位随机数
	 * 
	 * @return
	 */
	private String getCommonPayNum() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		return sdf.format(new Date()) + (new Random().nextInt(900000) + 100000);
	}

	/**
	 * 跳转到评价页面
	 * 
	 * @param requestID
	 * @param orderProductID
	 * @param model
	 * @return
	 */
	@RequestMapping("evaluate-index")
	public String evaluateIndex(String requestId, Integer orderProductID, Model model) {

		model.addAttribute("orderProductID", orderProductID);
		return "comment/evaluate";

	}

	@RequestMapping("/more-returnmoney")
	@ResponseBody
	public List<Map<String,Object>> moreReturnMoney(String requestId, Integer pageIndex, Model model){
		
		Principal principal = WebUtil.getCurrentPrincipal();
		String mqID = principal.getMqId();
		
		List<Map<String,Object>> returnMoneyList = orderService.selectReturnMoneyList(mqID, (pageIndex - 1) * DEFAULT_PAGE_SIZE, DEFAULT_PAGE_SIZE, requestId);
		for (int i = 0; i < returnMoneyList.size(); i++) {
			
			List<Map<String, Object>> orderProductList = (List<Map<String, Object>>) returnMoneyList.get(i).get("orderProductList");
			for (int j = 0; j < orderProductList.size(); j++) {
				
				String origSkuImg = (String)orderProductList.get(j).get("skuImg");
				logger.info("origSkuImg:"+origSkuImg);
				logger.info(QiNiuUtil.getDownloadUrl("encode:"+origSkuImg));
				
				orderProductList.get(j).put("skuImg", QiNiuUtil.getDownloadUrl((String) (orderProductList.get(j).get("skuImg"))));
			}
		}
		
		return returnMoneyList;
	}
	
	@RequestMapping("/more-returngoods")
	@ResponseBody
	public List<Map<String, Object>> moreReturnGoods(String requestId, Integer pageIndex, Model model) {

		Principal principal = WebUtil.getCurrentPrincipal();
		// if(principal==null)principal = new Principal("2","");
		String mqID = principal.getMqId();

		List<Map<String, Object>> returnGoodsList = orderService.selectReturnGoodsList(mqID, pageIndex, DEFAULT_PAGE_SIZE, requestId);

		for (int i = 0; i < returnGoodsList.size(); i++) {
			returnGoodsList.get(i).put("skuImg", QiNiuUtil.getDownloadUrl((String) returnGoodsList.get(i).get("skuImg")));
		}

		return returnGoodsList;

	}

	@RequestMapping("/more-comments")
	@ResponseBody
	public List<Map<String, Object>> moreComments(String requestId, Integer pageIndex, Model model) {

		Principal principal = WebUtil.getCurrentPrincipal();
		// if(principal==null)principal = new Principal("2","");
		String mqID = principal.getMqId();

		List<Map<String, Object>> waitForCommentList = orderService.selectWaitForCommentList(mqID, pageIndex, DEFAULT_PAGE_SIZE, requestId);

		for (int i = 0; i < waitForCommentList.size(); i++) {
			waitForCommentList.get(i).put("skuImg", QiNiuUtil.getDownloadUrl((String) waitForCommentList.get(i).get("skuImg")));
		}

		return waitForCommentList;

	}

	@RequestMapping("/more-nosend-order")
	@ResponseBody
	public List<Map<String, Object>> moreNoSend(String requestId, Integer pageIndex, Model model) {

		Principal principal = WebUtil.getCurrentPrincipal();
		String mqID = principal.getMqId();

		List<Map<String, Object>> noSendOrderList = orderService.selectNoSendOrder(mqID, (pageIndex - 1) * DEFAULT_PAGE_SIZE, DEFAULT_PAGE_SIZE, requestId);
		for (int i = 0; i < noSendOrderList.size(); i++) {
			List<Map<String, Object>> orderProductList = (List<Map<String, Object>>) noSendOrderList.get(i).get("orderProductList");
			for (int j = 0; j < orderProductList.size(); j++) {
				orderProductList.get(j).put("skuImg", QiNiuUtil.getDownloadUrl((String) (orderProductList.get(j).get("skuImg"))));
			}
		}

		return noSendOrderList;

	}

	/**
	 * 退货列表 查看订单详情专用 orderID 必须为子订单ID
	 * 
	 * @param requestId
	 * @param orderID
	 * @param page
	 * @return
	 */
	@RequestMapping("/order-detail-withid")
	public String orderDetailWithID(String requestId, Integer orderID, Model model) {

		if (orderID == null) {
			return "404";
		}

		Order order = orderService.getOrderByID(orderID, requestId);

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

		// 查找 父订单
		parentOrder = orderService.getOrderByOrderCode(order.getParentCode(), requestId);
		pFee = order.getPostFee().toString();
		pSum = order.getOrderSum().toString();

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
			orderProductMap.put("buyNum", op.getBuyNum());
			childOrderProductVo.add(orderProductMap);
		}

		childOrderMap.put("supName", orderProductList.get(0).getSupName());
		childOrderMap.put("orderCode", order.getOrderCode());
		childOrderMap.put("childOrdreProductVo", childOrderProductVo);

		childOrderVo.add(childOrderMap);

		model.addAttribute("childOrderVo", childOrderVo);

		model.addAttribute("orderCode", parentOrder.getOrderCode());

		return "order/child-order-detail";
	}

	@RequestMapping("/more-order")
	@ResponseBody
	public List<Map<String, Object>> moreOrder(String requestId, Integer pageIndex, Model model) {

		Principal principal = WebUtil.getCurrentPrincipal();
		// if(principal==null)principal = new Principal("2","");
		String mqID = principal.getMqId();

		// 1 查找出所有的订单商品
		List<Map<String, Object>> childOrderList = orderService.getOrderListByMqID(mqID, pageIndex, DEFAULT_PAGE_SIZE, requestId);
		for (int i = 0; i < childOrderList.size(); i++) {
			List<Map<String, Object>> orderProductList = (List<Map<String, Object>>) childOrderList.get(i).get("orderProductList");
			for (int j = 0; j < orderProductList.size(); j++) {
				orderProductList.get(j).put("skuImg", QiNiuUtil.getDownloadUrl((String) (orderProductList.get(j).get("skuImg"))));
			}
		}

		model.addAttribute("childOrderList", childOrderList);

		return childOrderList;
	}

	@RequestMapping("/more-nopay-order")
	@ResponseBody
	public List<Map<String, Object>> moreNoPayOrder(String requestId, Integer pageIndex) {

		Principal principal = WebUtil.getCurrentPrincipal();
		String mqID = principal.getMqId();

		List<Map<String, Object>> noPayOrderList = orderService.selectNoPayedOrder(mqID, (pageIndex - 1) * DEFAULT_PAGE_SIZE, DEFAULT_PAGE_SIZE, requestId);

		for (int i = 0; i < noPayOrderList.size(); i++) {
			List<Map<String, Object>> orderProductList = (List<Map<String, Object>>) noPayOrderList.get(i).get("orderProductList");
			for (int j = 0; j < orderProductList.size(); j++) {
				orderProductList.get(j).put("skuImg", QiNiuUtil.getDownloadUrl((String) (orderProductList.get(j).get("skuImg"))));
			}
		}

		return noPayOrderList;
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

		Principal principal = WebUtil.getCurrentPrincipal();
		// if(principal==null)principal = new Principal("2","");
		String mqID = principal.getMqId();

		GoodsReturn goodsReturn = orderService.getGoodsReturnByOrderProductID(orderProductID, requestId);

		if (goodsReturn == null) {
			logger.info("查找的退款单不存在");
			return "404";
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
			return "order/returnG-examine";
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

			return "order/returnG-pass";
		} else if ("THZ".equals(status)) { // 用户填写了退货物流信息之后
			return "order/returnG-takeG";
		} else if ("NO#".equals(status)) {
			return "order/returnG-reject";
		} else if ("DTK".equals(status)) {
			return "order/returnG-dtk";
		} else if ("TKZ".equals(status)) {
			return "order/returnG-takeG";
		} else if ("JTK".equals(status)) {
			return "order/returnG-jtk";
		} else if ("FAI".equals(status)) {
			return "order/returnG-reject";
		} else if ("COM".equals(status)) {
			return "order/returnG-success";
		}

		return "404";
	}

	@RequestMapping("/set-return-goods-logistics")
	public String setReturnGoodsLogistics(String requestId, Integer goodsReturnID, Integer logisticsID, String logisticsNum, String logistics, Model model) {

		Map<String, Object> retMap = new HashMap<String, Object>();

		try {

			GoodsReturn goodsReturn = orderService.getGoodsReturnByID(goodsReturnID, requestId);
			goodsReturn.setLogisticsID(logisticsID);
			goodsReturn.setLogistics(logistics);
			goodsReturn.setLogisticsNum(logisticsNum);
			goodsReturn.setStatus("THZ");

			OrderProduct orderProduct = orderProductService.getOrderProductByID(goodsReturn.getOrderProductID(), requestId);
			orderProduct.setReturnGoodsStatus("THZ");
			orderProductService.updateOrderProduct(orderProduct, requestId);

			orderService.updateGoodsReturn(goodsReturn, requestId);
			retMap.put("errcode", 0);

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			retMap.put("errcode", 10000);
			retMap.put("errmsg", "提交失败,请联系后台管理员");
		}

		return "redirect:return-goods-detail?goodsReturnID=" + goodsReturnID;
	}

	private void fetch(String[] imgs) {

		try {
			String accessToken = wxInteractionService.getAccessToken(false);
			for (int i = 0; i < imgs.length; i++) {
				if (imgs[i].indexOf("#old") < 0) {
					String from = "https://api.weixin.qq.com/cgi-bin/media/get?access_token=" + accessToken + "&media_id=" + imgs[i];
					logger.info("from:" + from);
					logger.info("begin fetch");
					QiNiuUtil.fetch(from, qiNiuSpace, "return_img_" + imgs[i]);
					logger.info("end fetch");
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

	}
	
	

	
}
