package com.mi360.aladdin.mall;

import java.io.Serializable;

/**
 * 登陆用户身份信息
 * 
 * @author JSC
 *
 */
public class WxShare implements Serializable {
	private static final long serialVersionUID = 7534104684362489251L;

	private String appId;
	private String nonceStr;
	private String timestamp;
	private String signature;
	public String getAppId() {
		return appId;
	}
	public void setAppId(String appId) {
		this.appId = appId;
	}
	public String getNonceStr() {
		return nonceStr;
	}
	public void setNonceStr(String nonceStr) {
		this.nonceStr = nonceStr;
	}
	public String getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	public String getSignature() {
		return signature;
	}
	public void setSignature(String signature) {
		this.signature = signature;
	}

}
