package com.mi360.aladdin.mall.interceptor;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import me.chanjar.weixin.common.api.WxConsts;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
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

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

		String ua = request.getHeader("user-agent").toLowerCase();

		logger.info("requestUrl--->" + request.getRequestURL().toString() + (request.getQueryString() == null ? "" : ("?" + request.getQueryString())));

		boolean isWx = ua.indexOf("micromessenger") > 0;// 是否微信浏览器

		Principal principal = WebUtil.getCurrentPrincipal();
		if (principal == null) {
			
			principal = new Principal(342, "d9afefcc54ec4a2ca6ca099e8cbd2413", "oiUxPwfWK32w9VlqU0sm1F0SIUuk", 100000342);
			WebUtil.login(principal);

			
/*
			StringBuffer requestUrl = request.getRequestURL();
			String queryString = request.getQueryString();
			if (queryString != null) {
				 去除邀请码 
				int i = queryString.indexOf("&");
				if (i != -1) {
					queryString = queryString.substring(i + 1, queryString.length());
					requestUrl.append("?" + queryString);
				}
			}
			WebUtil.getSession().setAttribute(WebUtil.SAVE_REQUEST_KEY, requestUrl.toString());
			if (!response.isCommitted()) {
				String requestId = UUID.randomUUID().toString().replace("-", "");
				String iv = request.getParameter("iv");
				//String redirectString = wxInteractionService.oauth2buildAuthorizationUrl(requestId, WxConsts.OAUTH2_SCOPE_BASE, iv);
				//logger.info("跳转微信登录验证链接：" + redirectString);
				//response.sendRedirect(redirectString);
				principal = new Principal(342, "d9afefcc54ec4a2ca6ca099e8cbd2413", "oiUxPwfWK32w9VlqU0sm1F0SIUuk", 100000342);
				WebUtil.login(principal);
				return true;
			}*/
		} else if (request.getMethod().equals("GET") && !"XMLHttpRequest".equals(request.getHeader("X-Requested-With"))
				&& StringUtils.isEmpty(request.getParameter("iv"))) {// 链接附带邀请码
			/*StringBuffer requestUrl = request.getRequestURL().append("?iv=" + principal.getUserId());
			String queryString = request.getQueryString();
			if (queryString != null) {
				requestUrl.append("&" + queryString);
			}

			response.sendRedirect(requestUrl.toString());
			return false;*/
			return true;
		}

		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
	}

}
