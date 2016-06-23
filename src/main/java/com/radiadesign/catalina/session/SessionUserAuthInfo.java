/**
 * 
 * SessionUserAuthInfo.java
 * 版本所有 深圳市蜂鸟娱乐有限公司 2013-2014
 */
package com.radiadesign.catalina.session;

import java.io.Serializable;
import java.util.Date;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * @author john huang
 * 2016年2月4日 下午4:16:10
 * 本类主要做为 用户session登录信息,这个可与php的信息共通
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SessionUserAuthInfo implements Serializable {

    /**
     * 手机电话
     */
    private String mobileNum;

    /**
     * insert_time
     */
    private Date insertTime;

    /**
     * qq号
     */
    private String qq;

    /**
     * email地址
     */
    private String email;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 地址
     */
    private String address;
    /**
     * 用户token
     */
    private String token;

    /**
     * 头像
     */
    private String headImageUrl;
    /**
     * 实名
     */
    private String realName;
    /**
     * 身份证
     */
    private String cardID;
    /**
     * 过期时间
     */
    private Long expireIn;
    
    /**
     * 消息数
     */
    private Integer messageNum;
    
    /**
     * 用户id
     */
    private String userId;
	/**
	 * 手机电话 
	 */
	public String getMobileNum() {
		return mobileNum;
	}
	/**
	 * 手机电话 
	 */
	public void setMobileNum(String mobileNum) {
		this.mobileNum = mobileNum;
	}
	/**
	 * insert_time 
	 */
	public Date getInsertTime() {
		return insertTime;
	}
	/**
	 * insert_time 
	 */
	public void setInsertTime(Date insertTime) {
		this.insertTime = insertTime;
	}
	/**
	 * qq号 
	 */
	public String getQq() {
		return qq;
	}
	/**
	 * qq号 
	 */
	public void setQq(String qq) {
		this.qq = qq;
	}
	/**
	 * email地址 
	 */
	public String getEmail() {
		return email;
	}
	/**
	 * email地址 
	 */
	public void setEmail(String email) {
		this.email = email;
	}
	/**
	 * 昵称 
	 */
	public String getNickname() {
		return nickname;
	}
	/**
	 * 昵称 
	 */
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	/**
	 * 地址 
	 */
	public String getAddress() {
		return address;
	}
	/**
	 * 地址 
	 */
	public void setAddress(String address) {
		this.address = address;
	}
	/**
	 * 用户token 
	 */
	public String getToken() {
		return token;
	}
	/**
	 * 用户token 
	 */
	public void setToken(String token) {
		this.token = token;
	}
	/**
	 * 头像 
	 */
	public String getHeadImageUrl() {
		return headImageUrl;
	}
	/**
	 * 头像 
	 */
	public void setHeadImageUrl(String headImageUrl) {
		this.headImageUrl = headImageUrl;
	}
	/**
	 * 实名 
	 */
	public String getRealName() {
		return realName;
	}
	/**
	 * 实名 
	 */
	public void setRealName(String realName) {
		this.realName = realName;
	}
	/**
	 * 身份证 
	 */
	public String getCardID() {
		return cardID;
	}
	/**
	 * 身份证 
	 */
	public void setCardID(String cardID) {
		this.cardID = cardID;
	}
	/**
	 * 过期时间 
	 */
	public Long getExpireIn() {
		return expireIn;
	}
	/**
	 * 过期时间 
	 */
	public void setExpireIn(Long expireIn) {
		this.expireIn = expireIn;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "SessionUserAuthInfo [mobileNum=" + mobileNum + ", insertTime=" + insertTime + ", qq=" + qq + ", email="
				+ email + ", nickname=" + nickname + ", address=" + address + ", token=" + token + ", headImageUrl="
				+ headImageUrl + ", realName=" + realName + ", cardID=" + cardID + ", expireIn=" + expireIn + "]";
	}
	/**
	 * 消息数 
	 */
	public Integer getMessageNum() {
		return messageNum;
	}
	/**
	 * 消息数 
	 */
	public void setMessageNum(Integer messageNum) {
		this.messageNum = messageNum;
	}
	/**
	 * 用户id 
	 */
	public String getUserId() {
		return userId;
	}
	/**
	 * 用户id 
	 */
	public void setUserId(String userId) {
		this.userId = userId;
	}


	
	
}
