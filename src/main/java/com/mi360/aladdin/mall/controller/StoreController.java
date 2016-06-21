package com.mi360.aladdin.mall.controller;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mi360.aladdin.mall.Principal;
import com.mi360.aladdin.mall.util.WebUtil;
import com.mi360.aladdin.store.service.IStoreService;
import com.mi360.aladdin.user.service.UserService;
import com.mi360.aladdin.util.MapUtil;
import com.mi360.aladdin.util.MapUtil.MapData;


@Controller
@RequestMapping("/store")
public class StoreController {

	private Logger logger = Logger.getLogger(StoreController.class);
	
	public static final int DEFAULT_PAGE_SIZE = 8;
	
	@Autowired
	private IStoreService storeService;
	
	@Autowired
	private UserService userService;
	
	@RequestMapping("/")
	public String index(String requestId, Model model){
		
		Principal principal = WebUtil.getCurrentPrincipal();
		String mqId = principal.getMqId();
		
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
		
		Principal principal = WebUtil.getCurrentPrincipal();
		String mqId = principal.getMqId();
		
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
		
		Principal principal = WebUtil.getCurrentPrincipal();
		String mqId = principal.getMqId();
		
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
		
		return "store/index";
		
	}
	
	/**
	 * 更新店铺信息
	 */
	@RequestMapping("/update")
	public String updateStore(String requestId, String title, String logoPath, String abstraction, Model model){
		
		Principal principal = WebUtil.getCurrentPrincipal();
		String mqId = principal.getMqId();
		
		Map<String,Object> map = storeService.updateStoreInfo(requestId, mqId, title, logoPath, abstraction);
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
	public String products(String requestId,  Model model, Integer startIndex, Integer pageSize){
		
		Principal principal = WebUtil.getCurrentPrincipal();
		String mqId = principal.getMqId();
		
		Map<String,Object> map = storeService.getProductInStore(requestId, mqId, 0, DEFAULT_PAGE_SIZE);
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
		
		return "store/products";
		
	}
	
	/**
	 * 销售统计
	 */
	@RequestMapping("/sale-calc")
	public String saleCalc(String requestId, Model model){
		
		Principal principal = WebUtil.getCurrentPrincipal();
		String mqId = principal.getMqId();
		
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
		
		Principal principal = WebUtil.getCurrentPrincipal();
		String mqId = principal.getMqId();
		
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
		
		
		model.addAttribute("allOrder"+allOrderMap.get("result"));
		
		model.addAttribute("noPayOrder"+noPayOrderMap.get("result"));
		model.addAttribute("noSendOrder"+noSendOrderMap.get("result"));
		model.addAttribute("waitForComment"+waitForCommentMap.get("result"));
		model.addAttribute("returnMoney"+returnMoneyMap.get("result"));
		model.addAttribute("returnGoods"+returnGoodsMap.get("result"));
		
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
		
		
		Principal principal = WebUtil.getCurrentPrincipal();
		String mqId = principal.getMqId();
		
		return storeService.topProductInStore(requestId, mqId, productIds);
		
	}
	
	/**
	 * 店长推荐
	 */
	@RequestMapping("/recommend")
	@ResponseBody
	public Map<String,Object> recommend(String requestId, Integer[] productIds){
		
		Principal principal = WebUtil.getCurrentPrincipal();
		String mqId = principal.getMqId();
		
		return storeService.recommendProducts(requestId, mqId, productIds);
		
	}
	
	/**
	 * 取消代理
	 */
	@RequestMapping("/cancel-proxy")
	@ResponseBody
	public Map<String,Object> cancelProxy(String requestId, Integer[] productIds){
		
		Principal principal = WebUtil.getCurrentPrincipal();
		String mqId = principal.getMqId();
		
		return storeService.delProductsFromStore(requestId, mqId, productIds);
		
	}
	
}
