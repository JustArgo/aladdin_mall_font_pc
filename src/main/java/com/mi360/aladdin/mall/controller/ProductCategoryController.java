package com.mi360.aladdin.mall.controller;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.mi360.aladdin.mall.util.QiNiuUtil;
import com.mi360.aladdin.product.category.service.ProductCategoryService;
import com.mi360.aladdin.util.MapUtil;
import com.mi360.aladdin.util.MapUtil.MapData;

@Controller
@RequestMapping("/productCategory")
public class ProductCategoryController  extends BaseWxController{
	private Logger logger = Logger.getLogger(this.getClass());
	@Autowired
	private ProductCategoryService productCategoryService;

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/index", method = RequestMethod.GET)
	public String index(String requestId, ModelMap modelMap) {
		MapData data = MapUtil.newInstance(productCategoryService.findList(requestId));
		logger.info(data.dataString());
		List<Map<String, Object>> result = (List<Map<String, Object>>) data.getObject("result");
		for (Map<String, Object> map : result) {
			Map<String, Object> imgMap=(Map<String, Object>)map.get("img");
			try {
				imgMap.put("attrValueImg", QiNiuUtil.getDownloadUrl((String) imgMap.get("attrValueImg")));
			} catch (Exception e) {
			}
		}
		modelMap.addAttribute("productCategory", data.getObject("result"));
		return "productCategory/index";
	}
}
