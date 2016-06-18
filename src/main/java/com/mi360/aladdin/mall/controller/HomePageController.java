package com.mi360.aladdin.mall.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.mi360.aladdin.groupbuy.service.IGroupBuyService;
import com.mi360.aladdin.mall.Principal;
import com.mi360.aladdin.mall.util.WebUtil;
import com.mi360.aladdin.product.service.IProductService;
import com.mi360.aladdin.shopcar.service.IShopCarService;

/**
 * 首页控制器
 * 
 * @author JSC
 *
 */
@Controller
public class HomePageController  extends BaseWxController{
	
	@Value("${home_product_page_size}")
	private Integer home_product_page_size;
	
	@Autowired
	private IProductService productService;
	@Autowired
	private IGroupBuyService groupBuyService;
	
	@Autowired
	private IShopCarService shopCarService;
		
	/**
	 * 首页
	 * 
	 * @return
	 */
	@RequestMapping(value = "", method = RequestMethod.GET)
	public String homePage(String requestId, ModelMap modelMap, HttpServletRequest req) {
		
		String homePath = "/page/home.html";
		String pathString = req.getSession().getServletContext().getRealPath(homePath);
		
		System.out.println("pathStr:"+pathString);
		
		File file = new File(pathString);
		if(file.exists()){
			System.out.println("exists");
			return "home";
		}else{
			System.out.println("not exists");
		}
		
		Principal principal = WebUtil.getCurrentPrincipal();
		String mqID = principal.getMqId();
		
		List<Map<String, String>> order = new ArrayList<>();
		Map<String, String> map = new HashMap<>();
		map.put("column", "sellCount");
		order.add(map);
		
		List<Map<String, Object>> queryCondition = new ArrayList<>();
		Map<String, Object> queryMap = new HashMap<>();
		queryMap.put("column", "t_product.status");
		queryMap.put("operator", "=");
		queryMap.put("value", "UP#");
		
		Map<String, Object> queryMap2 = new HashMap<>();
		queryMap2.put("column", "t_product_img.imgPos");
		queryMap2.put("operator", "=");
		queryMap2.put("value", "MAJ");

		Map<String, Object> queryMap3 = new HashMap<>();
		queryMap3.put("column", "t_product_img.status");
		queryMap3.put("operator", "=");
		queryMap3.put("value", "OK#");
		
		queryCondition.add(queryMap);
		queryCondition.add(queryMap2);
		queryCondition.add(queryMap3);
		
		List<Map> hotSellProducts = productService.selectHotSellProducts(queryCondition, 1, 5, order, requestId);
		
		List<Map<String, String>> order2 = new ArrayList<>();
		Map<String, String> map2 = new HashMap<>();
		map2.put("column", "t_product.createTime");
		order2.add(map2);
		List<Map> newestProducts = productService.selectNewestProducts(queryCondition, 1, home_product_page_size, order2, requestId);

		//查询购物车 商品数量
		Integer productCount = shopCarService.getShopCarProductsCount(mqID, requestId);
		modelMap.addAttribute("productCount", productCount);
		
		List<Map<String, Object>> groupQueryCondition = new ArrayList<>();
		Map<String,Object> groupMap = new HashMap<String,Object>();
		groupMap.put("column", "status");
		groupMap.put("operator", "=");
		groupMap.put("value","UP#");
		groupQueryCondition.add(groupMap);
		
		List<Map<String, String>> order3 = new ArrayList<>();
		Map<String, String> map3 = new HashMap<>();
		map3.put("column", "createTime");
		order3.add(map3);
		//List<PinInfo> newestGroupBuys = groupBuyService.selectByConditionWithPagination(groupQueryCondition, 1, 3, order3, requestId);

		modelMap.addAttribute("hotSellProducts", hotSellProducts);
		modelMap.addAttribute("newestProducts", newestProducts);
		//modelMap.addAttribute("newestGroupBuys", newestGroupBuys);
		
		return "index";
	}
}
