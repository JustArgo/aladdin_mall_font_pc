package com.mi360.aladdin.mall.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mi360.aladdin.mall.util.WebUtil;
import com.mi360.aladdin.order.service.IOrderService;
import com.mi360.aladdin.product.domain.Product;
import com.mi360.aladdin.product.service.IProductService;
import com.mi360.aladdin.shopcar.service.IShopCarService;


@Controller
@RequestMapping("/shop_car")
public class ShopCarController {

	private Logger logger = Logger.getLogger(this.getClass());
	
	@Autowired
	private IShopCarService shopCarService;
	
	@Autowired
	private IProductService productService;
	
	@Autowired
	private IOrderService orderService;
	
	/**
	 * 查看购物车
	 * @return
	 */
	@RequestMapping("")
	public String shopping_cart(String requestId,Model model){
		
		Map<String,Object> principal = WebUtil.getCurrentUserInfo();
		String mqID = (String)principal.get("mqId");
		
		List<Map<String,Object>> supplierProducts = shopCarService.viewShopCar(mqID, requestId);		
		
		Long totalPrice = 0L;
		
		for(int i=0;i<supplierProducts.size();i++){
			
			Map<String,Object> map = supplierProducts.get(i);
			List<Map<String,Object>> sameSupplierOrderProductList = (List<Map<String, Object>>) map.get("shopCarProducts");
			
			for(Map<String,Object> eachMap:sameSupplierOrderProductList){
				totalPrice += (Long)eachMap.get("skuPrice")*(Integer)eachMap.get("skuQuality");
			}
		}
		
		model.addAttribute("totalPrice",totalPrice);
		model.addAttribute("supplierProducts",supplierProducts);
		
		return "shop-car";
	}
	
	@RequestMapping("/remove_shopcar_product")
	@ResponseBody
	public String remove_shopcar_product(String requestId,Integer[] skuIDs){
		
		Map<String,Object> principal = WebUtil.getCurrentUserInfo();
		String mqID = (String)principal.get("mqId");
		
		if(mqID==null || skuIDs==null){
			return "{\"errcode\":10042,\"errmsg\":\"invalid arguments\"}";
		}
		shopCarService.removeShopCarProduct(mqID, skuIDs, requestId);
		return "{\"errcode\":0,\"errmsg\":\"success\"}";
	}
	
	@RequestMapping("/add_to_shopcar")
	@ResponseBody
	public String add_to_shopcar(String requestId,Integer productID, Integer skuID, Integer buyNum){
		
		Map<String,Object> principal = WebUtil.getCurrentUserInfo();
		String mqID = (String)principal.get("mqId");
		
		//查询 该商品的限购数量
		Product product = productService.queryProduct(productID,requestId);
		if(product==null || "DW#".equals(product.getStatus())){
			return "{\"errcode\":10000,\"errmsg\":\"该商品不存在或已下架,无法进行购买\"}";
		}
		Integer limitCount = product.getLimitCount();
		//查询该用户是否购买过该商品  购买了几件
		Integer buyCount = orderService.getBuyCountByProductID(requestId, productID, mqID);
		//查询购物车里有多少件该商品
		Integer inShopCarCount = shopCarService.getProductCountInShopCarByProductID(requestId, productID, mqID);
		logger.info((limitCount+" "+buyNum+" "+buyCount+" "+inShopCarCount));
		if(limitCount!=null && limitCount!=0 && buyNum+buyCount+inShopCarCount>limitCount.intValue()){
			return "{\"errcode\":10000,\"errmsg\":\"购买数量超过限购数量,无法加入到购物车\"}";
		}
		
		int count = shopCarService.addToShopCar(mqID, productID, skuID, buyNum, requestId);
		
		return "{\"errcode\":\"0\",\"count\":"+count+"}";
	}
	
	@RequestMapping("/batch_add_to_shopcar")
	@ResponseBody
	public String batch_add_to_shopcar(String requestId,Integer[] productIds){
		
		Map<String,Object> principal = WebUtil.getCurrentUserInfo();
		String mqID = (String)principal.get("mqId");
		int totalCount=0;
		for (int i = 0; i < productIds.length; i++) {
			Integer productID=productIds[i];
			//查询 该商品的限购数量
			Product product = productService.queryProduct(productID,requestId);
			if(product==null || "DW#".equals(product.getStatus())){
				return "{\"errcode\":10000,\"errmsg\":\"该商品不存在或已下架,无法进行购买\"}";
			}
			Integer limitCount = product.getLimitCount();
			//查询该用户是否购买过该商品  购买了几件
			Integer buyCount = orderService.getBuyCountByProductID(requestId, productID, mqID);
			//查询购物车里有多少件该商品
			Integer inShopCarCount = shopCarService.getProductCountInShopCarByProductID(requestId, productID, mqID);
			if(limitCount!=null && limitCount!=0 && 1+buyCount+inShopCarCount>limitCount.intValue()){
				return "{\"errcode\":10000,\"errmsg\":\"购买数量超过限购数量,无法加入到购物车\"}";
			}
			
			totalCount+= shopCarService.addToShopCar(mqID, productID, null, 1, requestId);
		}
		
		return "{\"errcode\":\"0\",\"count\":"+totalCount+"}";
	}
	
	@RequestMapping("/shopcar_count")
	@ResponseBody
	public Map<String, Object> shopcarCount(String requestId){
		
		Map<String,Object> principal = WebUtil.getCurrentUserInfo();
		String mqID = (String)principal.get("mqId");
		
		Map<String, Object> retMap = new HashMap<String,Object>();
		retMap.put("errcode", 0);
		
		try{
			Integer productCount = shopCarService.getShopCarProductsCount(mqID, requestId);
			retMap.put("count", productCount);
		}catch(Exception e){
			logger.error(e.getMessage(),e);
			retMap.put("count", 0);
		}
		
		return retMap;
		
	}
	
}
