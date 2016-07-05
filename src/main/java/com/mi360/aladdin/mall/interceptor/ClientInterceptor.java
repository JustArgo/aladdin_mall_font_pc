package com.mi360.aladdin.mall.interceptor;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import me.chanjar.weixin.common.api.WxConsts;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.mi360.aladdin.interaction.wx.service.WxInteractionService;
import com.mi360.aladdin.mall.Principal;
import com.mi360.aladdin.mall.util.WebUtil;

/**
 * 客户端过滤器
 * 
 * @author JSC
 *
 */
public class ClientInterceptor extends HandlerInterceptorAdapter {
	private Logger logger = Logger.getLogger(this.getClass());
	@Autowired
	private WxInteractionService wxInteractionService;

	@Value("${host_name}")
	private String hostName;
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

		System.out.println("requestUrl--->" + request.getRequestURL().toString() + (request.getQueryString() == null ? "" : ("?" + request.getQueryString())));
		
		Map<String,Object> userInfo = WebUtil.getCurrentUserInfo();
		
		System.out.println("userInfo:"+userInfo);
		
		if(userInfo==null){
			response.sendRedirect(hostName+"/login");
			return false;
		}else{
			return true;
		}

	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
	}

}
