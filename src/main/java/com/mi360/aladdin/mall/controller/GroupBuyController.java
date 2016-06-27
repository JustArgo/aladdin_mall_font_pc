package com.mi360.aladdin.mall.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.mi360.aladdin.groupbuy.domain.PinInfo;
import com.mi360.aladdin.groupbuy.domain.PinOrder;
import com.mi360.aladdin.groupbuy.domain.PinOrderProduct;
import com.mi360.aladdin.groupbuy.service.IGroupBuyService;
import com.mi360.aladdin.interaction.wx.service.WxInteractionService;
import com.mi360.aladdin.mall.Principal;
import com.mi360.aladdin.mall.util.WebUtil;
import com.mi360.aladdin.product.domain.Product;
import com.mi360.aladdin.product.domain.ProductSku;
import com.mi360.aladdin.product.service.IPostFeeService;
import com.mi360.aladdin.product.service.IProductService;
import com.mi360.aladdin.product.service.IProductSkuService;
import com.mi360.aladdin.receadd.domain.ReceiveAddress;
import com.mi360.aladdin.receadd.service.IManageReceAddService;
import com.mi360.aladdin.supplier.domain.Supplier;
import com.mi360.aladdin.supplier.service.ISupplierService;

@Controller
@RequestMapping("/groupbuy")
public class GroupBuyController{

	@Autowired
	private IGroupBuyService groupBuyService;
	
	@Autowired
	private IProductService productService;
	
	@Autowired
	private IPostFeeService postFeeService;
	
	@Autowired
	private ISupplierService supplierService;
	
	@Autowired
	private IProductSkuService productSkuService;
	
	@Autowired
	private IManageReceAddService manageReceAddService;
	
	@Autowired(required=false)
	private WxInteractionService wxInteractionService;
	
	/**
	 * 拼团首页
	 */
	@RequestMapping("")
	public String groupbuy(String requestId, Model model){
		
		List<PinInfo> pinInfoList = groupBuyService.getPinInfoList(requestId);
		model.addAttribute("pinInfoList",pinInfoList);
		
		return "groupbuy/group-index";
	}
	
	/**
	 * 拼团详情
	 */
	@RequestMapping("groupbuy-detail")
	public String groupbuyDetail(String requestId, Integer id,Model model){
		
		PinInfo pinInfo = groupBuyService.getPinInfo(id, requestId);
		Product product = productService.queryProduct(pinInfo.getProductID(), requestId);
		//postFeeService.calcPostFee(product.getID(), 1, countryID, provinceID, cityID, districtID, requestID)
		
		model.addAttribute("pinInfo",pinInfo);
		model.addAttribute("productDesc",product.getSellDesc());
		model.addAttribute("postFee",0);
		model.addAttribute("skuID",4);
		
		return "groupbuy/group-detail";
	}
	
	/**
	 * 立即参团 
	 */
	@RequestMapping("join-group")
	public String joinGroup(String requestId, Integer skuID,Integer pinID, Long pinPrice, Integer limitCount, Model model){
		
		Principal principal = WebUtil.getCurrentPrincipal();
//if(principal==null)principal = new Principal("2","");		
		String mqID = principal.getMqId();
		
		ProductSku sku = productSkuService.getSkuByID(skuID, requestId);
		Product product = productService.queryProduct(sku.getProductID(), requestId);
		Supplier supplier = supplierService.getSupplier(product.getSupplyID(), requestId);
		
		ReceiveAddress receAdd = manageReceAddService.getDefaultAddress(mqID, requestId);
		
		if(receAdd!=null){
			
			Long postFee = postFeeService.calcPostFee(product.getID(), 1, receAdd.getCountryID(), receAdd.getProvinceID(), receAdd.getCityID(), receAdd.getDistrictID(), requestId);
			model.addAttribute("postFee",postFee/100);
			model.addAttribute("recName",receAdd.getRecName());
			model.addAttribute("recMobile",receAdd.getRecMobile());
			model.addAttribute("fullAddress",manageReceAddService.getFullAddress(receAdd, requestId));
		}
		
		List<String> skuStrs = productSkuService.getSkuStr(skuID, requestId);
		
		model.addAttribute("pinID",pinID);
		model.addAttribute("productID",product.getID());
		model.addAttribute("skuID",skuID);
		model.addAttribute("supName",supplier.getName());
		model.addAttribute("imgPath",sku.getSkuImg());
		model.addAttribute("sellDesc",product.getSellDesc());
		model.addAttribute("skuStrs",skuStrs);
		model.addAttribute("pinPrice",pinPrice);
		model.addAttribute("limitCount",limitCount);
		
		return "groupbuy/group-order-pay";
	}
	
	/**
	 * 提交订单 或点击 支付
	 */
	@RequestMapping("/group-order")
	public String groupOrder(String requestId, Integer skuID, Integer pinID, Integer productID, Integer buyNum, Long pinPrice, Long postFee, String invoiceName, String notes, Model model){
		
		Principal principal = WebUtil.getCurrentPrincipal();
//if(principal==null)principal = new Principal("2","");		
		String mqID = principal.getMqId();
		
		Integer pinUserID = 2;
		
		PinInfo pinInfo = groupBuyService.getPinInfo(pinID, requestId.replace("-", ""));
		Integer people = pinInfo.getPeople();
		Integer pinCount = pinInfo.getPinCount();
		
		Integer remainPeople = pinCount-people;
		Long remainTime = pinInfo.getEndTime().getTime()-new Date().getTime();
		
		Long remainHours = remainTime/60/60/1000;
		remainTime = remainTime - remainHours*60*60*1000;
		Long remainMinutes = remainTime/60/1000;
		remainTime = remainTime - remainMinutes*60*1000;
		Long remainSeconds = remainTime/1000;
		Long remainMillSeconds = remainTime - remainSeconds*1000;
		
		model.addAttribute("people",people);
		model.addAttribute("pinCount", pinCount);
		
		model.addAttribute("remainHours",remainHours);
		model.addAttribute("remainMinutes",remainMinutes);
		model.addAttribute("remainSeconds",remainSeconds);
		model.addAttribute("remainMillSeconds",remainMillSeconds);
		
		//判断是否已经参加过该拼团
		
		
		if(pinInfo.getEndTime().getTime()<new Date().getTime()){//拼团已结束
			model.addAttribute("pinStr","拼团失败");
			model.addAttribute("failReason","该拼团已结束");
			return "groupbuy/fail-create-group";
		}else if(people==pinCount){//该团已满员
			model.addAttribute("pinStr","拼团失败");
			model.addAttribute("failReason","该拼团已满员");
			return "groupbuy/fail-create-group";
		}else if(people==0){//开团
			groupBuyService.placeOrder(mqID, pinUserID, pinID, productID, skuID, buyNum, pinPrice, postFee, invoiceName, notes, requestId.replace("-", ""));
			groupBuyService.createGroupBuy(mqID, pinID, productID, requestId.replace("-", ""));
			
			List<Map<String,Object>> pinUserInfoList = groupBuyService.getPinUserList(pinID, requestId.replace("-", ""));
			//团长的信息
			model.addAttribute("colonelNickName",pinUserInfoList.get(0).get("nickName"));
			model.addAttribute("colonelImageHead",pinUserInfoList.get(0).get("imageHead"));
			model.addAttribute("colonelJoinTime",pinUserInfoList.get(0).get("joinTime"));
			
			pinUserInfoList.remove(0);
			model.addAttribute("pinUserInfoList",pinUserInfoList);
			
			model.addAttribute("people",people+1);
			model.addAttribute("pinStr","开团成功");
			model.addAttribute("remainPeople",remainPeople-1);//还需多少人
		}else{//参团
			
			try{
				groupBuyService.placeOrder(mqID, pinUserID, pinID, productID, skuID, buyNum, pinPrice, postFee, invoiceName, notes, requestId.replace("-", ""));
				groupBuyService.joinGroupBuy(mqID, pinID, productID, requestId.replace("-", ""));
				
				List<Map<String,Object>> pinUserInfoList = groupBuyService.getPinUserList(pinID, requestId.replace("-", ""));
				//团长的信息
				model.addAttribute("colonelNickName",pinUserInfoList.get(0).get("nickName"));
				model.addAttribute("colonelImageHead",pinUserInfoList.get(0).get("imageHead"));
				model.addAttribute("colonelJoinTime",pinUserInfoList.get(0).get("joinTime"));
				
				pinUserInfoList.remove(0);
				model.addAttribute("pinUserInfoList",pinUserInfoList);
				
				model.addAttribute("people",people+1);
				model.addAttribute("pinStr","参团成功");
				model.addAttribute("remainPeople",remainPeople-1);
				
			}catch(RuntimeException e){
				
				model.addAttribute("pinStr","拼团失败");
				if(e.getMessage().equals("该用户已参加过该拼团")){
					model.addAttribute("failReason","您已参加过该拼团");
				}
				
				return "groupbuy/fail-create-group";
			}
			
		}
		
		return "groupbuy/success-create-group";
	}
	
	@RequestMapping("view-order")
	public String viewOrder(String requestId, Model model){
		
		Principal principal = WebUtil.getCurrentPrincipal();
//if(principal==null)principal = new Principal("2","");		
		String mqID = principal.getMqId();
		
		List<Map<String,Object>> pinOrders =  new ArrayList<Map<String,Object>>();
		
		List<PinOrderProduct> pinOrderProductList = groupBuyService.getPinOrderProductListByMqID(mqID, requestId);
		List<PinOrder> pinOrderList = groupBuyService.getPinOrderListByMqID(mqID, requestId);
		
		if(pinOrderProductList.size()!=pinOrderList.size()){
			throw new RuntimeException("订单与订单商品数量不匹配");
		}else{
			for(int i=0;i<pinOrderList.size();i++){
				Map<String,Object> map = new HashMap<String,Object>();
				map.put("pinOrder", pinOrderList.get(i));
				map.put("pinOrderProduct", pinOrderProductList.get(i));
				
				Product product = productService.queryProduct(pinOrderProductList.get(i).getProductID(), requestId);
				map.put("sellDesc",product.getSellDesc());
				
				pinOrders.add(map);
			}
		}
		
		model.addAttribute("pinOrders",pinOrders);
		
		return "groupbuy/group-order";
	}
	
}
