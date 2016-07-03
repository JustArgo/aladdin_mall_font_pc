package com.mi360.aladdin.mall.util;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.mi360.aladdin.mall.Principal;
import com.radiadesign.catalina.session.SessionUserAuthInfo;

/**
 * Web工具
 * 
 * @author JSC
 *
 */
public class WebUtil extends HttpServlet {
	private static final long serialVersionUID = 283892857645407691L;
	/** 在session中登陆前请求地址的key */
	public static final String SAVE_REQUEST_KEY = "saveRequestKey";

	/**
	 * 获取当前用户身份信息，若未登录返回null
	 * 
	 * @return 身份信息，未登录为null
	 */
	public static Principal getCurrentPrincipal() {
		
//		Principal principal=new Principal(317,"ee9de73cf5a24e1597d916e61bd89365", "");// liqing
//		Principal principal=new Principal(279,"d140dd2c30dc4005aa2758ecb1ca981b", ""); //涓藉嫟
//		Principal principal=new Principal(332,"1621f314a9574a4e8918a3e38a33f85f", ""); //john
//		Principal principal=new Principal(320,"e5a3ccc5fd814c06b0bb8adcf9000923", ""); //yongzhong
//		Principal principal = new Principal(342, "d9afefcc54ec4a2ca6ca099e8cbd2413", "oiUxPwfWK32w9VlqU0sm1F0SIUuk", 100000342);
		Principal principal = new Principal(206, "6313b50f20754261846c10fb23c6d33b", "ojXXot253-tB5TY0Tk_mjipUKDBI", 100000206);
		return principal;
		//return (Principal) getSession().getAttribute(Principal.ATTRIBUTE_KEY);
	}

	/**
	 * 登陆
	 * 
	 * @return
	 */
	public static void login(SessionUserAuthInfo sessionUserAuthInfo) {
		RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
		if (requestAttributes != null) {
			HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
			request.getSession().setAttribute("loginUser", sessionUserAuthInfo);
		}
	}

	/**
	 * 获取当前session
	 * 
	 * @return
	 */
	public static HttpSession getSession() {
		RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
		HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
		return request.getSession();
	}
	
	/**
	 * 获取当前request
	 * 
	 * @return
	 */
	public static HttpServletRequest getRequest() {
		RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
		HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
		return request;
	}
	
}
