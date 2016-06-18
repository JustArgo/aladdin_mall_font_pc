package com.mi360.aladdin.mall.controller;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import me.chanjar.weixin.mp.bean.result.WxMpUser;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mi360.aladdin.interaction.wx.service.WxInteractionService;
import com.mi360.aladdin.mall.Principal;
import com.mi360.aladdin.mall.util.WebUtil;
import com.mi360.aladdin.mq.service.MqService;
import com.mi360.aladdin.user.service.UserService;
import com.mi360.aladdin.util.MapUtil;
import com.mi360.aladdin.util.MapUtil.MapData;

/**
 * 微信验证接口
 * 
 * @author JSC
 *
 */
@Controller
@RequestMapping("/wx")
public class WxController {
	Logger logger = Logger.getLogger(this.getClass());
	@Autowired
	private WxInteractionService wxInteractionService;
	@Autowired
	private UserService userService;
	@Autowired
	private MqService mqService;

	/**
	 * 验证服务器地址的有效性和事件消息回调接口
	 * 
	 * @param signature
	 *            微信加密签名，signature结合了开发者填写的token参数和请求中的timestamp参数、nonce参数。
	 * @param timestamp
	 *            时间戳
	 * @param nonce
	 *            随机数
	 * @param echostr
	 *            随机字符串
	 * @return 有效则返回echostr
	 */
	@RequestMapping(value = "/callback/event")
	@ResponseBody
	public String event(String requestId, String signature, String timestamp, String nonce, String echostr, HttpServletRequest request) {
		/* 验证有效性（配置url的时候使用，或者在配置url的时候直接返回echostr） */
		if (request.getMethod() == "GET") {
			if (wxInteractionService.checkSignature(requestId, timestamp, nonce, signature)) {
				return echostr;
			}
		}
		try {
			InputStream in = request.getInputStream();
			SAXReader reader = new SAXReader();
			Document document = reader.read(in);
			Element element = document.getRootElement();
			String openId = element.elementText("FromUserName");
			String event = element.elementText("Event");
			logger.info("微信事件："+event+"  用户openId："+openId);
			if ("subscribe".equals(event)) {
				logger.info("用户订阅公众号    openId:"+openId);
				logger.info("用户订阅公众号    openId:"+openId);
				MapData data = MapUtil.newInstance(userService.findUserByOpenId(requestId, openId));
				logger.info(data.dataString());
				MapData result = data.getResult();
				if (result != null) {
					try {
						mqService.subscribe(requestId, openId);
						logger.info("发送关注公众号队列成功");
					} catch (Exception e) {
						logger.info("发送关注公众号队列失败");
						logger.error(e.getMessage(), e);
					}
					try {
						mqService.sendWxMessage(requestId, openId, null, "57137e93399c9", null);
						logger.info("发送微信消息成功");
					} catch (Exception e) {
						logger.info("发送微信消息失败");
						logger.error(e.getMessage(), e);
					}
					return "";
				} else {
					logger.info(MapUtil.newInstance(userService.createWx(requestId, null, openId, null)).dataString());
				}
			} else if ("unsubscribe".equals(event)) {
				logger.info("用户取消订阅 公众号时   openId:"+openId);
				try {
					mqService.unsubscribe(requestId, openId);
					logger.info("发送取消关注公众号队列成功");
				} catch (Exception e) {
					logger.info("发送取消关注公众号队列失败");
					logger.error(e.getMessage(), e);
				}
			}
		} catch (Exception e) {
		}
		return "";
	}

	/**
	 * 微信验证回调接口
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/callback/login", method = RequestMethod.GET)
	@ResponseBody
	public void login(String requestId, HttpServletResponse response, String code, String state) throws Exception {

		System.out.println("/callbakc/login");

		WxMpUser wxMpUser = wxInteractionService.getSnsapiBaseUserInfo(requestId, code);
		String openId = wxMpUser.getOpenId();
		String mqId = null;
		Integer userId = null;
		Integer luckNum = null;
		MapData data = MapUtil.newInstance(userService.findUserByOpenId(requestId, openId));
		logger.info(data.dataString());
		if (data.getErrcode() == 0) {
			Map<String, Object> result = (Map<String, Object>) data.getObject("result");
			if (result == null) {
				Integer upDistributionId = null;
				if (!StringUtils.isEmpty(state)) {
					upDistributionId = Integer.valueOf(state);
				}
				logger.info("微信openId：" + openId + "  邀请码：" + upDistributionId);
				MapData data2 = MapUtil.newInstance(userService.createWx(requestId, upDistributionId, openId, null));
				logger.info(data2.dataString());
				if (data2.getErrcode() == 0) {
					result = (Map<String, Object>) data2.getObject("result");
					mqId = (String) result.get("mqId");
					userId = (Integer) result.get("userId");
					luckNum = (Integer) result.get("luckNum");
				} else {
					throw new Exception();
				}
			} else {
				mqId = (String) result.get("mqId");
				userId = (Integer) result.get("userId");
				luckNum = (Integer) result.get("luckNum");
				MapData data2 = MapUtil.newInstance(userService.flushWxUserInfoByMqId(requestId, mqId));
				logger.error(data2.dataString());
			}
		} else {
			throw new Exception();
		}
		Principal principal = new Principal(userId, mqId, openId, luckNum);
		WebUtil.login(principal);
		StringBuilder requestUrlBuilder = new StringBuilder((String) WebUtil.getSession().getAttribute(WebUtil.SAVE_REQUEST_KEY));
		System.out.println("requestUrlBuilder:" + requestUrlBuilder);
		int i = requestUrlBuilder.indexOf("?");
		if (i == -1) {
			requestUrlBuilder.append("?iv=" + userId);
			System.out.println("i==-1 requestUrlBuilder:" + requestUrlBuilder);
		} else {
			requestUrlBuilder.insert(i + 1, "iv=" + userId + "&");
			System.out.println("else requestUrlBuilder:" + requestUrlBuilder);
		}
		response.sendRedirect(requestUrlBuilder.toString());
	}

	/**
	 * 初始化wx js配置 允许支付
	 * 
	 * @return
	 */
	@RequestMapping(value = "/config", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, String> config(String requestId, String url) {

		Map<String, String> config = new HashMap<String, String>();

		config = wxInteractionService.getConfig(requestId, url);

		return config;

	}

}
