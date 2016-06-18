package com.mi360.aladdin.mall.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.mi360.aladdin.interaction.wx.service.WxInteractionService;
import com.mi360.aladdin.mall.Principal;
import com.mi360.aladdin.mall.WxShare;
import com.mi360.aladdin.mall.util.WebUtil;
import com.mi360.aladdin.user.service.UserService;
import com.mi360.aladdin.util.MapUtil;
import com.mi360.aladdin.util.MapUtil.MapData;


/**
 * 
 * @author chouben
 * @date 2016年4月19日 上午3:57:54
 */
@Controller
public class BaseWxController {
	
	@Autowired  
	private  HttpServletRequest request;
	
	@Value("${shareImg}")
	private String shareImg;
	
	@Value("${twoDimensionCodeImg}")
	private String twoDimensionCodeImg;
	
	@Value("${qiniu.space}")
	protected String qiNiuSpace;
	
	@Value("${qiniu.domain}")
	protected String qiNiuDomain;
	
	@Value("${host_name}")
	protected String hostName;
	
	@Autowired
	private UserService userService;
	
	private WxShare share;
	
	@Autowired
	protected WxInteractionService wxInteractionService;

	public WxShare getShare() {
		return share;
	}

	public void setShare(WxShare share) {
		this.share = share;
	}

	@ModelAttribute("wxMap")//<——①向模型对象中添加一个名为wxMap的属性
    public WxShare populateWxMap(String requestId) {
		
		Map<String, String> wxMap = wxInteractionService.getConfig(requestId, getFullUrl(request));
		
		share = new WxShare();
		share.setAppId(wxMap.get("appId"));
		share.setNonceStr(wxMap.get("nonceStr"));
		share.setTimestamp(wxMap.get("timestamp"));
		share.setSignature(wxMap.get("signature"));
		setShare(share);
		
        return getShare();
    }

	/**
	 * 将分享的图片 设置都公用model中
	 * @return
	 */
	@ModelAttribute("common")
	public Map<String,Object> common(String requestId, HttpServletResponse response){
		
		Map<String,Object> map = new HashMap<String,Object>();
		
		Principal principal = WebUtil.getCurrentPrincipal();
		
		System.out.println("principal==null"+principal==null);
		
		if(principal!=null){
			String mqID = principal.getMqId();
			//查看用户是否已关注
			Map<String,Object> userInfo = userService.findWxUserByMqId(requestId, mqID);
			MapData data = MapUtil.newInstance(userInfo);
			Map<String,Object> user = (Map<String, Object>) data.getObject("result");
			System.out.println("user==null"+user==null);
			if(user==null){
				WebUtil.getSession().setAttribute(Principal.ATTRIBUTE_KEY, null);
				try{
					response.sendRedirect(hostName);
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			else{
				
				map.put("subscribe",user.get("subscribe"));
			}
		}
		
		map.put("qiniuDomain", qiNiuDomain);
		map.put("shareImg", shareImg);
		map.put("two_dimension_code_img", twoDimensionCodeImg);
		
		return map;
	}
	
	private String getFullUrl(HttpServletRequest request){
		StringBuffer url = request.getRequestURL();
		 if (request.getQueryString() != null) {
		  url.append("?");
		  url.append(request.getQueryString());
		 }
		 return url.toString();
	}
}
