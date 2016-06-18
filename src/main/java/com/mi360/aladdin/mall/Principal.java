package com.mi360.aladdin.mall;

import java.io.Serializable;

/**
 * 登陆用户身份信息
 * 
 * @author JSC
 *
 */
public class Principal implements Serializable {
	private static final long serialVersionUID = 7534104684362489251L;
	/** 在session中身份信息的key */
	public static final String ATTRIBUTE_KEY = "principal";

	/** 用户表id */
	private Integer userId;
	/** 幸运号 */
	private Integer luckNum;
	/** 麦圈用户Id */
	private String mqId;
	/** 微信openId */
	private String openId;

	public Principal(Integer userId, String mqId, String openId,Integer luckNum) {
		this.userId = userId;
		this.mqId = mqId;
		this.openId = openId;
		this.luckNum=luckNum;
	}

	public Integer getLuckNum() {
		return luckNum;
	}

	public void setLuckNum(Integer luckNum) {
		this.luckNum = luckNum;
	}

	public String getMqId() {
		return mqId;
	}

	public void setMqId(String mqId) {
		this.mqId = mqId;
	}

	public String getOpenId() {
		return openId;
	}

	public void setOpenId(String openId) {
		this.openId = openId;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

}
