/**
 * 
 * SessionUserAuthInfo.java
 * 版本所有 深圳市蜂鸟娱乐有限公司 2013-2014
 */
package com.radiadesign.catalina.session;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

//import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * @author john huang
 * 2016年2月4日 下午4:16:10
 * 本类主要做为 用户session登录信息,这个可与php的信息共通
 */
//@JsonIgnoreProperties(ignoreUnknown = true)
public class SessionUserAuthInfo implements ISessionUserAuthInfo {
	
	

    public SessionUserAuthInfo() {
		super();
	}
	public SessionUserAuthInfo(ISessionUserAuthInfo isessionUserAuthInfo) {
		super();
		mobileNum = isessionUserAuthInfo.getMobileNum();
		insertTime = isessionUserAuthInfo.getInsertTime();
		qq = isessionUserAuthInfo.getQq();
		email = isessionUserAuthInfo.getEmail();
		nickname = isessionUserAuthInfo.getNickname();
		address = isessionUserAuthInfo.getAddress();
		token = isessionUserAuthInfo.getToken();
		headImageUrl = isessionUserAuthInfo.getHeadImageUrl();
		realName = isessionUserAuthInfo.getRealName();
		cardID = isessionUserAuthInfo.getCardID();
		expireIn = isessionUserAuthInfo.getExpireIn();
		messageNum = isessionUserAuthInfo.getMessageNum();
		userId = isessionUserAuthInfo.getUserId();
	}
	
	public SessionUserAuthInfo(Map<String,Object> isessionUserAuthInfo){
		mobileNum = String.valueOf(isessionUserAuthInfo.get("mobileNum"));
		Object it = isessionUserAuthInfo.get("insertTime");
		if(it!=null){
			if (it instanceof java.util.Date) {
				java.util.Date nmn = (java.util.Date) it;
				insertTime = nmn;
			}
			else{
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				try {
					insertTime = df.parse(it.toString());
				} catch (ParseException e) {
					System.out.println(it.toString()+" can not convert to SessionUserAuthInfo.insertTime");
					e.printStackTrace();
					
				}
			}
		}
		qq = String.valueOf(isessionUserAuthInfo.get("qq"));
		email = String.valueOf(isessionUserAuthInfo.get("email"));
		nickname = String.valueOf(isessionUserAuthInfo.get("nickname"));
		address = String.valueOf(isessionUserAuthInfo.get("address"));
		token = String.valueOf(isessionUserAuthInfo.get("token"));
		headImageUrl = String.valueOf(isessionUserAuthInfo.get("headImageUrl"));
		realName = String.valueOf(isessionUserAuthInfo.get("realName"));
		cardID = String.valueOf(isessionUserAuthInfo.get("cardID"));
		expireIn =0l;
		Object ei = isessionUserAuthInfo.get("expireIn");
		if(ei!=null){
			if (ei instanceof Number) {
				Number nmn = (Number) ei;
				expireIn = nmn.longValue();
			}
			else{
				expireIn = Long.parseLong(ei.toString());
			}
		}
		Object mn = isessionUserAuthInfo.get("messageNum");
		messageNum =0;
		if(mn!=null){
			if (mn instanceof Number) {
				Number nmn = (Number) mn;
				messageNum = nmn.intValue();
			}
			else{
				messageNum = Integer.parseInt(mn.toString());
			}
		}
		userId =(Integer)isessionUserAuthInfo.get("userId");
	}
	
	/**
	 * 转化为map
	 * @return
	 */
	public Map toMap(){
		Map<String,Object> isessionUserAuthInfo = new HashMap<>();
		isessionUserAuthInfo.put("mobileNum", mobileNum);
		isessionUserAuthInfo.put("insertTime", insertTime);
		isessionUserAuthInfo.put("qq", qq);
		isessionUserAuthInfo.put("email", email);
		isessionUserAuthInfo.put("nickname", nickname);
		isessionUserAuthInfo.put("address", address);
		isessionUserAuthInfo.put("token", token);
		isessionUserAuthInfo.put("headImageUrl", headImageUrl);
		isessionUserAuthInfo.put("realName", realName);
		isessionUserAuthInfo.put("expireIn", expireIn);
		isessionUserAuthInfo.put("messageNum", messageNum);
		isessionUserAuthInfo.put("userId", userId);
		
		return isessionUserAuthInfo;
	}
	
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
    private Integer userId;
    
    private String mqId;
    private Integer luckNum;
	/* (non-Javadoc)
	 * @see com.hummingbird.common.ext.ISessionUserAuthInfo#getMobileNum()
	 */
	@Override
	public String getMobileNum() {
		return mobileNum;
	}
	/**
	 * 手机电话 
	 */
	public void setMobileNum(String mobileNum) {
		this.mobileNum = mobileNum;
	}
	/* (non-Javadoc)
	 * @see com.hummingbird.common.ext.ISessionUserAuthInfo#getInsertTime()
	 */
	@Override
	public Date getInsertTime() {
		return insertTime;
	}
	/**
	 * insert_time 
	 */
	public void setInsertTime(Date insertTime) {
		this.insertTime = insertTime;
	}
	/* (non-Javadoc)
	 * @see com.hummingbird.common.ext.ISessionUserAuthInfo#getQq()
	 */
	@Override
	public String getQq() {
		return qq;
	}
	/**
	 * qq号 
	 */
	public void setQq(String qq) {
		this.qq = qq;
	}
	/* (non-Javadoc)
	 * @see com.hummingbird.common.ext.ISessionUserAuthInfo#getEmail()
	 */
	@Override
	public String getEmail() {
		return email;
	}
	/**
	 * email地址 
	 */
	public void setEmail(String email) {
		this.email = email;
	}
	/* (non-Javadoc)
	 * @see com.hummingbird.common.ext.ISessionUserAuthInfo#getNickname()
	 */
	@Override
	public String getNickname() {
		return nickname;
	}
	/**
	 * 昵称 
	 */
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	/* (non-Javadoc)
	 * @see com.hummingbird.common.ext.ISessionUserAuthInfo#getAddress()
	 */
	@Override
	public String getAddress() {
		return address;
	}
	/**
	 * 地址 
	 */
	public void setAddress(String address) {
		this.address = address;
	}
	/* (non-Javadoc)
	 * @see com.hummingbird.common.ext.ISessionUserAuthInfo#getToken()
	 */
	@Override
	public String getToken() {
		return token;
	}
	/**
	 * 用户token 
	 */
	public void setToken(String token) {
		this.token = token;
	}
	/* (non-Javadoc)
	 * @see com.hummingbird.common.ext.ISessionUserAuthInfo#getHeadImageUrl()
	 */
	@Override
	public String getHeadImageUrl() {
		return headImageUrl;
	}
	/**
	 * 头像 
	 */
	public void setHeadImageUrl(String headImageUrl) {
		this.headImageUrl = headImageUrl;
	}
	/* (non-Javadoc)
	 * @see com.hummingbird.common.ext.ISessionUserAuthInfo#getRealName()
	 */
	@Override
	public String getRealName() {
		return realName;
	}
	/**
	 * 实名 
	 */
	public void setRealName(String realName) {
		this.realName = realName;
	}
	/* (non-Javadoc)
	 * @see com.hummingbird.common.ext.ISessionUserAuthInfo#getCardID()
	 */
	@Override
	public String getCardID() {
		return cardID;
	}
	/**
	 * 身份证 
	 */
	public void setCardID(String cardID) {
		this.cardID = cardID;
	}
	/* (non-Javadoc)
	 * @see com.hummingbird.common.ext.ISessionUserAuthInfo#getExpireIn()
	 */
	@Override
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
	/* (non-Javadoc)
	 * @see com.hummingbird.common.ext.ISessionUserAuthInfo#getMessageNum()
	 */
	@Override
	public Integer getMessageNum() {
		return messageNum;
	}
	/**
	 * 消息数 
	 */
	public void setMessageNum(Integer messageNum) {
		this.messageNum = messageNum;
	}
	/* (non-Javadoc)
	 * @see com.hummingbird.common.ext.ISessionUserAuthInfo#getUserId()
	 */
	@Override
	public Integer getUserId() {
		return userId;
	}
	/**
	 * 用户id 
	 */
	public void setUserId(Integer userId) {
		this.userId = userId;
	}
	public String getMqId() {
		return mqId;
	}
	public void setMqId(String mqId) {
		this.mqId = mqId;
	}
	public Integer getLuckNum() {
		return luckNum;
	}
	public void setLuckNum(Integer luckNum) {
		this.luckNum = luckNum;
	}


	
	
}
