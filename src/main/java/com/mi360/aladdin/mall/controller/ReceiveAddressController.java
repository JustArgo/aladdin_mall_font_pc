package com.mi360.aladdin.mall.controller;

import java.util.ArrayList;
import java.util.HashMap;
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
import com.mi360.aladdin.receadd.domain.Address;
import com.mi360.aladdin.receadd.domain.ReceiveAddress;
import com.mi360.aladdin.receadd.service.IAddressService;
import com.mi360.aladdin.receadd.service.IManageReceAddService;


/**
 * 管理收货地址
 * @author 黄永宗
 * @date 2016年2月18日 下午3:07:51
 */
@Controller
@RequestMapping("/receadd")
public class ReceiveAddressController {
	
	private Logger logger = Logger.getLogger(this.getClass());
	
	@Autowired
	private IManageReceAddService manageReceAddService;
	
	@Autowired
	private IAddressService addressService;
	
	/**
	 * 新增用户收货地址
	 * @param receiveAddress
	 * @param model
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/add_rece_address")
	@ResponseBody
	public Map<String,Object> add(String requestId, Integer countryID, Integer provinceID, Integer cityID, Integer districtID, String address, String recName, String recMobile, String isDefault, Model model){
		
		Map<String,Object> retMap = new HashMap<String,Object>();
		
		Principal principal = WebUtil.getCurrentPrincipal();
		String mqID = principal.getMqId();
		
		retMap.put("errcode", 0);
		
		if(provinceID==null || cityID==null){
			retMap.put("errcode",10000);
			retMap.put("errmsg","省份和城市不能为空");
			return retMap;
		}
		if(address==null){
			retMap.put("errcode",10000);
			retMap.put("errmsg","地址不能为空");
			return retMap;
		}
		if(recName==null){
			retMap.put("errcode",10000);
			retMap.put("errmsg","收货人姓名不能为空");
			return retMap;
		}
		if(recMobile==null){
			retMap.put("errcode",10000);
			retMap.put("errmsg","收货人电话号码不能为空");
			return retMap;
		}
		address = address.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
		recName = recName.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
		ReceiveAddress receiveAddress = new ReceiveAddress();
		receiveAddress.setMqID(mqID);
		receiveAddress.setCountryID(100);
		receiveAddress.setProvinceID(provinceID);
		receiveAddress.setCityID(cityID);
		receiveAddress.setDistrictID(districtID);
		receiveAddress.setAddress(address);
		receiveAddress.setIsDefault(isDefault);
		receiveAddress.setRecName(recName);
		receiveAddress.setRecMobile(recMobile);
		
		logger.info("receiveAddress:"+receiveAddress);
		
		manageReceAddService.addAddress(receiveAddress,requestId);
		
		return retMap;
	}
	
	/**
	 * 删除用户收货地址
	 * @param ID
	 * @param model
	 * @return
	 */
	@RequestMapping("/del_rece_add")
	public String del(String requestId, int ID,Model model){
		
		manageReceAddService.deleteAddress(ID,requestId);

		return "redirect:manage_rece_add";
	}
	
	/**
	 * 更新用户收货地址
	 * @param receiveAddress
	 * @param model
	 * @return
	 */
	@RequestMapping("/update_rece_add")
	@ResponseBody
	public Map<String,Object> update(String requestId, Integer ID, Integer countryID, Integer provinceID, Integer cityID, Integer districtID, String address, String recName, String recMobile, String isDefault, Model model){
		
		Map<String,Object> retMap = new HashMap<String,Object>();
		retMap.put("errcode", 0);
		if(provinceID==null || cityID==null){
			retMap.put("errcode",10000);
			retMap.put("errmsg","省份和城市不能为空");
			return retMap;
		}
		if(address==null){
			retMap.put("errcode",10000);
			retMap.put("errmsg","地址不能为空");
			return retMap;
		}
		if(recName==null){
			retMap.put("errcode",10000);
			retMap.put("errmsg","收货人姓名不能为空");
			return retMap;
		}
		if(recMobile==null){
			retMap.put("errcode",10000);
			retMap.put("errmsg","收货人电话号码不能为空");
			return retMap;
		}
		address = address.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
		recName = recName.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
		ReceiveAddress receiveAddress = new ReceiveAddress();
		receiveAddress.setID(ID);
		receiveAddress.setCountryID(countryID);
		receiveAddress.setProvinceID(provinceID);
		receiveAddress.setCityID(cityID);
		receiveAddress.setDistrictID(districtID);
		receiveAddress.setAddress(address);
		receiveAddress.setRecName(recName);
		receiveAddress.setRecMobile(recMobile);
		receiveAddress.setIsDefault(isDefault);
		
		logger.info("receiveAddress:"+receiveAddress);
		
		manageReceAddService.updateAddress(receiveAddress,requestId);
		
		return retMap;
	}
	
	/**
	 * 管理用户收货地址
	 * @param model
	 * @return
	 */
	@RequestMapping("/manage_rece_add")
	public String manage(String requestId, Model model){
		
		Principal principal = WebUtil.getCurrentPrincipal();
		String mqID = principal.getMqId();

		List<ReceiveAddress> adds = manageReceAddService.listUsableAddress(mqID,requestId);
		List<Map<String,Object>> addressList = new ArrayList<Map<String,Object>>();
		for(int i=0;i<adds.size();i++){
			ReceiveAddress address = adds.get(i);
			
			Map<String,Object> map = new HashMap<String,Object>();
			
			Address provinceAdd = addressService.getAddress(address.getProvinceID(),requestId);
			Address cityAdd =     addressService.getAddress(address.getCityID(),requestId);
			Address districtAdd = addressService.getAddress(address.getDistrictID(), requestId);
			String province = provinceAdd==null?"":provinceAdd.getName();
			String city     = cityAdd==null?"":cityAdd.getName();
			String district = districtAdd==null?"":districtAdd.getName();
			//adds.get(i).setAddress((province==null?"":province)+(city==null?"":city)+(district==null?"":district)+(address.getAddress()==null?"":address.getAddress()));
			
			map.put("id",address.getID());
			map.put("recName", address.getRecName());
			map.put("mobile", address.getRecMobile());
			map.put("isDefault", address.getIsDefault());
			map.put("provinceID", address.getProvinceID());
			map.put("cityID", address.getCityID());
			map.put("districtID", address.getDistrictID());
			map.put("recMobile",address.getRecMobile().substring(0,3)+"****"+address.getRecMobile().substring(7));
			map.put("addressPrefix", province+city+district);
			map.put("addressSuffix",address.getAddress());
			
			addressList.add(map);
			
		}
		model.addAttribute("adds", addressList);
		
		List<Address> provinces = addressService.getSubAddress(100,requestId);
		model.addAttribute("provinces",provinces);
		List<Address> cities = new ArrayList<Address>();
		List<Address> districts = new ArrayList<Address>();
		
		cities = addressService.getSubAddress(10,requestId);
		districts = addressService.getSubAddress(1010,requestId);
		model.addAttribute("cities",cities);
		model.addAttribute("districts",districts);
		
		return "receadd/manage";
	}
	
	/**
	 * 编辑用户收货地址 可能是更新也可能是新增
	 * @param ID
	 * @param model
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/edit_rece_add")
	public String edit(String requestId, Integer ID,Model model) {
		
		List<Address> provinces = addressService.getSubAddress(100,requestId);
		model.addAttribute("provinces",provinces);
		List<Address> cities = new ArrayList<Address>();
		List<Address> districts = new ArrayList<Address>();
		
		if(ID!=null){
			
			ReceiveAddress address = manageReceAddService.getAddress(ID,requestId);
			model.addAttribute("add",address);
			cities = addressService.getSubAddress(address.getProvinceID(),requestId);
			districts = addressService.getSubAddress(address.getCityID(),requestId);
			
		}else{
			cities = addressService.getSubAddress(10,requestId);
			districts = addressService.getSubAddress(1010,requestId);
		}
		model.addAttribute("cities",cities);
		model.addAttribute("districts",districts);
		return "receadd/edit";
		
	}
	
	@RequestMapping("/setUserDefaultAddress")
	@ResponseBody
	public int setUserDefaultAddress(String requestId, Integer id,String isDefault){
		
		Principal principal = WebUtil.getCurrentPrincipal();
		String mqID = principal.getMqId();
		
		return manageReceAddService.setUserDefaultAddress(mqID, id, isDefault,requestId);
		
	}
	
	@RequestMapping("/getAddressByPid")
	@ResponseBody
	public List<Address> getByPid(String requestId, Integer pid){
		
		List<Address> addresses = new ArrayList<Address>();
		addresses = addressService.getSubAddress(pid,requestId);
		return addresses;
		
	}
	
}
