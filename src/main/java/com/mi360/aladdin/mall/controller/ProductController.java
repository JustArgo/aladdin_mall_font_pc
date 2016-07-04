package com.mi360.aladdin.mall.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mi360.aladdin.comment.service.ICommentService;
import com.mi360.aladdin.comment.service.ICommentVoService;
import com.mi360.aladdin.comment.vo.CommentVo;
import com.mi360.aladdin.mall.util.QiNiuUtil;
import com.mi360.aladdin.mall.util.WebUtil;
import com.mi360.aladdin.order.service.IOrderService;
import com.mi360.aladdin.product.category.service.ProductCategoryService;
import com.mi360.aladdin.product.domain.Product;
import com.mi360.aladdin.product.domain.ProductAttr;
import com.mi360.aladdin.product.domain.ProductAttrValue;
import com.mi360.aladdin.product.domain.ProductSearchRecord;
import com.mi360.aladdin.product.domain.ProductSku;
import com.mi360.aladdin.product.domain.ProductSkuAttr;
import com.mi360.aladdin.product.service.IPostFeeService;
import com.mi360.aladdin.product.service.IProductCollectService;
import com.mi360.aladdin.product.service.IProductService;
import com.mi360.aladdin.product.service.IProductSkuService;
import com.mi360.aladdin.product.service.IProductVoService;
import com.mi360.aladdin.product.vo.ProductVo;
import com.mi360.aladdin.shopcar.service.IShopCarService;
import com.mi360.aladdin.user.service.UserService;
import com.mi360.aladdin.util.MapUtil;
import com.mi360.aladdin.util.MapUtil.MapData;

@Controller
@RequestMapping("/product")
public class ProductController {
	private Logger logger = Logger.getLogger(this.getClass());

	private static final String DEFAULT_PLATFORM = "PC#";
	
	private static final int DEFAULT_PAGE_SIZE = 12;
	
	@Autowired
	private IProductService productService;

	@Autowired
	private IProductVoService productVoService;

	@Autowired
	private IProductSkuService productSkuService;

	@Autowired
	private IPostFeeService postFeeService;

	@Autowired
	private ProductCategoryService productCategoryService;

	@Autowired
	private UserService userService;

	@Autowired
	private IProductCollectService productCollectService;

	@Autowired
	private IShopCarService shopCarService;
	
	@Autowired
	private IOrderService orderService;

	@Autowired
	private ICommentService commentService;
	
	@Autowired
	private ICommentVoService commentVoService;
	
	/**
	 * 查看商品详情
	 * 
	 * @param productID
	 * @param model
	 * @return
	 */
	@RequestMapping("/product_detail")
	public String productDetail(String requestId, Integer productID, Integer storeId, Model model) {

		Map<String,Object> principal = WebUtil.getCurrentUserInfo();
		String mqID = "d9afefcc54ec4a2ca6ca099e8cbd2413";//(String)principal.get("mqId");

		// 参数判断
		if (productID == null) {
			return "404";
		}

		List<Map<String, Object>> attrItems = new ArrayList<Map<String, Object>>();
		ProductVo productVo = productVoService.getProductVo(productID, requestId);

		if (productVo == null) {
			return "404";
		}

		// 获得该商品 共有多少个sku
		Integer skuStock = 0;
		for (ProductSku sku : productVo.getSkus()) {
			skuStock += sku.getSkuStock();
		}

		Product p = productService.queryProduct(productID, requestId);
		if ("DEL".equals(p.getStatus())) {
			logger.info("商品ID " + p.getID() + " 已删除");
			return "404";
		}

		if(storeId!=null){
			model.addAttribute("storeId",storeId);
		}
		model.addAttribute("status", p.getStatus());
		model.addAttribute("limitCount", p.getLimitCount()==null?0:p.getLimitCount());

		model.addAttribute("productImgList", productVo.getProductImgList());
		model.addAttribute("productVo", productVo);

		// 查看用户是否已关注
		Map<String, Object> userInfo = userService.findWxUserByMqId(requestId, mqID);
		MapData data = MapUtil.newInstance(userInfo);
		Map<String, Object> user = (Map<String, Object>) data.getObject("result");
		if(user!=null){
			model.addAttribute("subscribe", user.get("subscribe"));
		}
		
		// 查看用户是否已收藏该商品
		int isCollect = productCollectService.isCollect(mqID, productID, requestId);
		if (isCollect == 1) {
			model.addAttribute("isCollect", 1);
		}

		// 封装该产品的属性
		List<ProductAttr> productAttrs = productService.getProductAttrByProductID(productID, requestId);
		for (int i = 0; i < productAttrs.size(); i++) {

			Map<String, Object> attrMap = new HashMap<String, Object>();
			attrMap.put("attrName", productAttrs.get(i).getAttrName());
			attrMap.put("attrID", productAttrs.get(i).getID());

			List<ProductAttrValue> productAttrValues = productService.getAttrValuesByAttrID(productAttrs.get(i).getID(),
					requestId);
			List<String[]> attrValues = new ArrayList<String[]>();
			for (int j = 0; j < productAttrValues.size(); j++) {

				String[] valueTwin = new String[2];
				valueTwin[0] = productAttrValues.get(j).getID() + "";
				valueTwin[1] = productAttrValues.get(j).getAttrValue();
				attrValues.add(valueTwin);
			}

			attrMap.put("attrValues", attrValues);

			attrItems.add(attrMap);
		}
		
		/*
		 * 
		 * 
		 * for()  红绿蓝
		 * 
		 * 	
		 
		
		for(int i=0;i<attrItems.size();i++){
			for(int j=0;j<attrItems.size();j++){
				if(i!=j){
					(List<String[]>)attrItems.get(i).get("attrValues")[1];  //得到attrMap 
				}
			}
		}*/
		

		//查询购物车 商品数量
		Integer productCount = shopCarService.getShopCarProductsCount(mqID, requestId);
		model.addAttribute("productCount", productCount);
		
		model.addAttribute("attrItems", attrItems);
		model.addAttribute("productID", productID);
		model.addAttribute("productStock", skuStock);
		
		
		//评论vo列表 全部
		List<CommentVo> commentVoListAll = commentVoService.getCommentVoList(requestId, productID, 2, 0, 4);
		
		if(commentVoListAll!=null && commentVoListAll.size()>0){
			model.addAttribute("commentStatistics",commentVoListAll.get(0).getCommentStatistics());
			
			if(commentVoListAll.get(0).getCommentStatistics()!=null){
				double descScore = commentVoListAll.get(0).getCommentStatistics().getDescConform().intValue()/100.0;
				double speedScore = commentVoListAll.get(0).getCommentStatistics().getSpeed().intValue()/100.0;
				double serviceScore = commentVoListAll.get(0).getCommentStatistics().getService().intValue()/100.0;
				
				model.addAttribute("averageScore",String.format("%.1f", Double.sum(serviceScore, Double.sum(descScore, speedScore))/3));
			}else{
				model.addAttribute("averageScore","0.0");
			}
			
		}else{
			model.addAttribute("averageScore","0.0");
		}
		
		//评论vo列表  有图片
		List<CommentVo> commentVoListHasImage = commentVoService.getCommentVoList(requestId, productID, 1, 0, 4);
		

		int hasImageCount = commentService.getCommentCount(requestId, productID, 1);
		model.addAttribute("hasImageCount",hasImageCount);
		
		model.addAttribute("commentVoListAll",commentVoListAll);
		model.addAttribute("commentVoListHasImage",commentVoListHasImage);
		
		//查询最新的商品
		List<Map> recommendList = productService.selectByKeyWordWithPaginationAddSupplier("", 0, 3, "createTime", DEFAULT_PLATFORM, requestId);
		model.addAttribute("recommendList",recommendList);
		
		//添加商品浏览历史记录
		productService.addBrowseHistory(requestId, mqID, productID, "NOR");
		
		return "product/product_detail";
	}

	@RequestMapping("querySku")
	@ResponseBody
	public Map<String, Object> querySku(String requestId, Integer productID, Integer[] attrs, Integer[] values) {

		Map<String, Object> retMap = new HashMap<String, Object>();
		Map<Integer, Integer> skuMap = new HashMap<Integer, Integer>();

		// 先对参数做基础验证
		if (productID == null || attrs == null || values == null || attrs.length != values.length) {
			return retMap;
		}

		for (int i = 0; i < attrs.length; i++) {
			skuMap.put(attrs[i], values[i]);
		}

		List<ProductSku> skus = productSkuService.getSkuByProductID(productID, requestId);

		for (int i = 0; i < skus.size(); i++) {

			int matchAttr = 0;

			ProductSku sku = skus.get(i);
			List<ProductSkuAttr> skuAttrs = productSkuService.getSkuAttrBySkuID(sku.getID(), requestId);
			if (skuMap.size() == skuAttrs.size()) {
				for (int j = 0; j < skuAttrs.size(); j++) {
					ProductSkuAttr skuAttr = skuAttrs.get(j);
					if (skuMap.containsKey(skuAttr.getAttrID()) && skuMap.get(skuAttr.getAttrID()).equals(skuAttr.getAttrValueID())) {
						matchAttr++;
					} else {
						break;
					}
				}
				// 说明这个sku就是我们要找的sku
				if (matchAttr == skuMap.size()) {
					retMap.put("errcode", 0);
					retMap.put("skuID", sku.getID());
					retMap.put("skuImg", QiNiuUtil.getDownloadUrl(sku.getSkuImg()));
					retMap.put("skuStock", sku.getSkuStock());
					retMap.put("skuPrice", sku.getSkuPrice());
				}
			}

		}

		return retMap;
	}

	@RequestMapping("/collect")
	@ResponseBody
	public Map<String, Object> collect(String requestId, Integer productID, Integer collect) {

		Map<String,Object> principal = WebUtil.getCurrentUserInfo();
		// if(principal==null)principal = new Principal("2","");
		String mqID = (String)principal.get("mqId");

		logger.info("productID: " + productID + " collect: " + collect);

		Map<String, Object> ret = new HashMap<String, Object>();

		if (mqID == null || productID == null || collect == null) {
			ret.put("errcode", 10042);
			ret.put("errmsg", "invalid arguments");
			return ret;
		}

		if (collect == 0) {// 0代表取消收藏
			productService.uncollectProduct(mqID, productID, requestId);
			ret.put("errcode", 0);
			ret.put("errmsg", "uncollect success");
		} else {// 1代表收藏
			productService.collectProduct(mqID, productID, requestId);
			ret.put("errcode", 0);
			ret.put("errmsg", "collect success");
		}

		return ret;

	}

	@RequestMapping("/calcPostFee")
	@ResponseBody
	public Long calcPostFee(String requestId, Integer productID, Integer buyNum, Integer countryID, Integer provinceID, Integer cityID, Integer districtID) {
		return postFeeService.calcPostFee(productID, buyNum, countryID, provinceID, cityID, districtID, requestId);
	}

	/**
	 * 商品列表
	 * 
	 * @param categoryId
	 *            商品分类id
	 * @param modelMap
	 * @return
	 */
	@RequestMapping(value = "/list/category/{categoryId}", method = RequestMethod.GET)
	public String listOfCategory(String requestId, @PathVariable Integer categoryId, ModelMap modelMap) {
		modelMap.addAttribute("categoryId", categoryId);
		MapData data = MapUtil.newInstance(productCategoryService.findSimpleInfoById(requestId, categoryId));
		logger.info(data.dataString());
		Map<String, Object> result = (Map<String, Object>) data.getObject("result");
		if (result != null) {
			result.put("classNameImg", QiNiuUtil.getDownloadUrl((String) result.get("classNameImg")));
		}
		modelMap.addAttribute("categorys", result);
		return "product/list_category";
	}

	@RequestMapping(value = "/query/category/{categoryId}")
	@ResponseBody
	public Object queryOfCategory(String requestId, HttpServletRequest request, ModelMap modelMap, @PathVariable Integer categoryId, String orderProperty,
			String orderDirection, int page, int pageSize) {
		List<Map<String, String>> orderCondition = new ArrayList<>();
		Map<String, String> order = new HashMap<>();
		order.put("column", orderProperty);
		order.put("direction", orderDirection);
		orderCondition.add(order);
		List<Map<String, Object>> data = productService.getProductListByCategoryID(categoryId, orderCondition, page, pageSize, requestId);
		for (Map<String, Object> map : data) {
			map.put("imgPath", QiNiuUtil.getDownloadUrl((String) map.get("imgPath")));
		}
		return data;
	}

	@RequestMapping("/search-index")
	public String searchIndex(String requestId, Model model) {

		Map<String,Object> principal = WebUtil.getCurrentUserInfo();
		// if(principal==null)principal = new Principal("2","");
		String mqID = (String)principal.get("mqId");

		List<ProductSearchRecord> searchRecordList = productService.selectPopularSearchRecord(mqID, requestId);
		model.addAttribute("searchRecordList", searchRecordList);

		return "product/search-index";
	}

	/**
	 * 页面点击搜索按钮 进行搜索
	 * 
	 * @param requestId
	 * @param keyWord
	 * @param startIndex
	 * @param pageSize
	 * @param model
	 * @return
	 */
	@RequestMapping("/search")
	public String search(String requestId, String keyWord, Integer searchID, Model model) {

		Map<String,Object> principal = WebUtil.getCurrentUserInfo();
		String mqID = (String)principal.get("mqId");

		Integer pageSize = 8;

		logger.info("keyWord: " + keyWord);
		List<Map> productList = new ArrayList<Map>();

		if (searchID != null) {
			productService.updateSearchKey(searchID, requestId);
		} else {
			productService.insertSearchRecord(keyWord, mqID, requestId);
		}

		if (keyWord == null) {
			productList = productService.selectByKeyWordWithPagination("", 0, pageSize, "asc", requestId);// 默认采用价格上升排序
		} else {
			productList = productService.selectByKeyWordWithPagination(keyWord, 0, pageSize, "asc", requestId);// 默认采用价格上升排序
		}
		model.addAttribute("productList", productList);
		model.addAttribute("keyWord", keyWord);

		List<Map> promoteList = productService.selectByKeyWordWithPagination("", 0, 4, "sellCount", requestId);
		model.addAttribute("promoteList",promoteList);	
		
		// 查找搜索历史
		List<ProductSearchRecord> searchRecordList = productService.selectPopularSearchRecord(mqID, requestId);
		model.addAttribute("searchRecordList", searchRecordList);
		
		int count = productService.getProductCountByKeyWordAndPlatform(requestId, keyWord==null?"":keyWord, DEFAULT_PLATFORM);
		model.addAttribute("pageCount",(count+DEFAULT_PAGE_SIZE-1)/DEFAULT_PAGE_SIZE);
		
		if (productList.size() == 0) {
			productList = productService.selectByKeyWordWithPagination("", 0, 10, "createTime", requestId);
			model.addAttribute("productList", productList);
			return "product/search-fail";
		}

		return "product/search-success";

	}

	@RequestMapping("/ajaxSearch")
	@ResponseBody
	public List<Map> ajaxSearch(String requestID, String keyWord, Integer startIndex, Integer pageSize, String orderBy, Model model) {

		List<Map> productList = productService.selectByKeyWordWithPagination(keyWord, startIndex, pageSize, orderBy, requestID);
		for (int i = 0; i < productList.size(); i++) {
			productList.get(i).put("imgPath", QiNiuUtil.getDownloadUrl((String) productList.get(i).get("imgPath")));
		}
		return productList;

	}

	/**
	 * 检查商品是否超出限购数量
	 * 
	 * @return
	 */
	@RequestMapping("/check-limit-count")
	@ResponseBody
	public Map<String, Object> checkLimitCount(String requestId, Integer[] skuIds, Integer buyNums[], Model model) {

		Map<String, Object> retMap = new HashMap<String, Object>();

		/**
		 * 商品 及其 购买数量 对应关系
		 */
		Map<Integer, Integer> productBuyNumsMap = new HashMap<Integer, Integer>();

		for (int i = 0; i < skuIds.length; i++) {
			ProductSku sku = productSkuService.getSkuByID(skuIds[i], requestId);
			if (productBuyNumsMap.get(sku.getProductID()) == null) {
				productBuyNumsMap.put(sku.getProductID(), buyNums[i]);
			} else {
				productBuyNumsMap.put(sku.getProductID(), productBuyNumsMap.get(sku.getProductID()) + buyNums[i]);
			}
		}

		for (Entry<Integer, Integer> entry : productBuyNumsMap.entrySet()) {
			Product p = productService.queryProduct(entry.getKey(), requestId);
			if (p.getLimitCount()!=null && p.getLimitCount()!=0 && p.getLimitCount() < entry.getValue()) {
				retMap.put("limit", true);
				break;
			}
		}

		return retMap;
	}
	
	@RequestMapping("/except-add")
	public String exceptAdd(String requestId, Integer productID, Model model){
		
		model.addAttribute("exceptAddList",productService.getProductFreightTplExcept(requestId, productID));
		
		return "product/except-add";
	}
	
	/**
	 * 检查某个商品的购买数量是否超出限购数量
	 * 1 如果本次检查是由 购物车页面请求的则不再重复计算购物车中已添加的商品数量
	 * 2 如果本次检查是由 商品页面请求的则要加入购物车中的数量进行计算
	 * @return
	 */
	@RequestMapping("/check-limit")
	@ResponseBody
	public Map<String,Object> checkLimit(String requestId, Integer[] skuIds, Integer[] buyNums, String fromShopCar){
		
		Map<String,Object> principal = WebUtil.getCurrentUserInfo();
		String mqID = (String)principal.get("mqId");
		
		Map<String,Object> retMap = new HashMap<String,Object>();
		retMap.put("errcode", 0);
		
		if(skuIds==null || buyNums==null || skuIds.length!=buyNums.length){
			retMap.put("errcode", 10000);
			retMap.put("errmsg", "参数错误");
			return retMap;
		}
		//map的key 为商品的id  值 为 Object数组   第0个为限购数量  第1个为之前购买的数量(从t_order_product查询) 第2个为当前购物车中的数量  第3个为 现在要购买的数量 第4个为对应的商品
		Map<Integer,Object[]> productLimitCountBuyNumMap = new HashMap<Integer,Object[]>();
		
		for(int i=0;i<skuIds.length;i++){
			ProductSku sku = productSkuService.getSkuByID(skuIds[i], requestId);
			if(productLimitCountBuyNumMap.get(sku.getProductID())!=null){
				Object[] limitCountBuyNum = productLimitCountBuyNumMap.get(sku.getProductID());
				limitCountBuyNum[3] = (Integer)limitCountBuyNum[3]+buyNums[i];
				productLimitCountBuyNumMap.put(sku.getProductID(),limitCountBuyNum);
			}else{
				Product product = productService.queryProduct(sku.getProductID(), requestId);
				Object[] limitCountBuyNum = new Object[5];
				limitCountBuyNum[0] = product.getLimitCount()==null?0:product.getLimitCount();
				limitCountBuyNum[1] = orderService.getBuyCountByProductID(requestId, product.getID(), mqID);
				limitCountBuyNum[2] = shopCarService.getProductCountInShopCarByProductID(requestId, product.getID(), mqID);
				limitCountBuyNum[3] = buyNums[i];
				limitCountBuyNum[4] = product;
				productLimitCountBuyNumMap.put(sku.getProductID(),limitCountBuyNum);
				logger.info("limitCountBuyNum:"+Arrays.toString(limitCountBuyNum));
			}
		}
		
		
		if(StringUtils.isNotBlank(fromShopCar)){
			for(Entry<Integer,Object[]> entry:productLimitCountBuyNumMap.entrySet()){
				Object[] obj = entry.getValue();
				Integer limitCount = (Integer) obj[0];
				Integer passBuyCount = (Integer) obj[1];
				Integer buyNum = (Integer)obj[3];
				Product product = (Product) obj[4];
				logger.info("productID:"+product.getID()+" limitCount:"+limitCount+" passBuyCount:"+passBuyCount+" inShopCarNum:"+obj[2]+" buyNum:"+buyNum);
				if(limitCount!=0 && (passBuyCount+buyNum)>limitCount.intValue()){
					retMap.put("errcode", 10000);
					if(retMap.get("limitProductNames")==null){
						List<String> limitProductNames = new ArrayList<String>();
						limitProductNames.add(product.getProductName());
						retMap.put("limitProductNames", limitProductNames);
					}else{
						((List<String>)retMap.get("limitProductNames")).add(product.getProductName());
					}
				}
			}
		}else{
			for(Entry<Integer,Object[]> entry:productLimitCountBuyNumMap.entrySet()){
				Object[] obj = entry.getValue();
				Integer limitCount = (Integer) obj[0];
				Integer passBuyCount = (Integer) obj[1];
				Integer inShopCarNum = (Integer)obj[2];
				Integer buyNum = (Integer)obj[3];
				Product product = (Product) obj[4];
				logger.info("productID:"+product.getID()+" limitCount:"+limitCount+" passBuyCount:"+passBuyCount+" inShopCarNum:"+inShopCarNum+" buyNum:"+buyNum);
				if(limitCount!=0 && passBuyCount+buyNum+inShopCarNum>limitCount.intValue()){
					retMap.put("errcode", 10000);
					if(retMap.get("limitProductNames")==null){
						List<String> limitProductNames = new ArrayList<String>();
						limitProductNames.add(product.getProductName());
						retMap.put("limitProductNames", limitProductNames);
					}else{
						((List<String>)retMap.get("limitProductNames")).add(product.getProductName());
					}
				}
			}
		}
		
		return retMap;
	}
	
	/**
	 * 如果购买规格的数量没有超出 库存 返回 errcode:0  否则返回errcode:10000 errmsg:[商品名的列表]
	 * @param requestId
	 * @param skuIds
	 * @param buyNums
	 * @return
	 */
	@RequestMapping("/check-stock")
	@ResponseBody
	public Map<String, Object> checkStock(String requestId, Integer[] skuIds, Integer[] buyNums){
		
		Map<String, Object> retMap = new HashMap<String,Object>();
		
		retMap.put("errcode", 0);
		
		if(skuIds==null || buyNums==null || skuIds.length!=buyNums.length){
			retMap.put("errcode", 10000);
			retMap.put("errmsg", "参数错误");
			return retMap;
		}
		
		Set<Integer> productIDSet = new HashSet<Integer>(); 
		
		for(int i=0;i<skuIds.length;i++){
			ProductSku sku = productSkuService.getSkuByID(skuIds[i], requestId);
			if(sku.getSkuStock().compareTo(buyNums[i])<0){
				productIDSet.add(sku.getProductID());	
			}
		}
		
		List<String> productNameList = new ArrayList<String>();
		
		for(Integer productID:productIDSet){
			Product product = productService.queryProduct(productID, requestId);
			productNameList.add(product.getProductName());
		}
		
		if(productNameList.size()>0){
			retMap.put("errcode", 10000);
			retMap.put("errmsg", productNameList);
		}
		
		return retMap;
		
	}
	
	@RequestMapping("/recommend")
	@ResponseBody
	public List<Map> getRecommend(String requestId, Integer pageSize){
		
		List<Map> resultMap = productService.selectByKeyWordWithPaginationAddSupplier("", 0, pageSize, "sellCount", DEFAULT_PLATFORM, requestId);
		
		for(int i=0;i<resultMap.size();i++){
			resultMap.get(i).put("imgPath",QiNiuUtil.getDownloadUrl((String)resultMap.get(i).get("imgPath")));
		}
		
		return resultMap;
		
	}
}
