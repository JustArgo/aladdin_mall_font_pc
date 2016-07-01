/**
 * 
 * ISessionUserAuthInfo.java
 * 版本所有 深圳市蜂鸟娱乐有限公司 2013-2014
 */
package com.radiadesign.catalina.session;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * @author john huang
 * 2016年6月30日 上午8:18:03
 * 本类主要做为
 */
public interface ISessionUserAuthInfo extends Serializable {

	/**
	 * 手机电话 
	 */
	String getMobileNum();

	/**
	 * insert_time 
	 */
	Date getInsertTime();

	/**
	 * qq号 
	 */
	String getQq();

	/**
	 * email地址 
	 */
	String getEmail();

	/**
	 * 昵称 
	 */
	String getNickname();

	/**
	 * 地址 
	 */
	String getAddress();

	/**
	 * 用户token 
	 */
	String getToken();

	/**
	 * 头像 
	 */
	String getHeadImageUrl();

	/**
	 * 实名 
	 */
	String getRealName();

	/**
	 * 身份证 
	 */
	String getCardID();

	/**
	 * 过期时间 
	 */
	Long getExpireIn();

	/**
	 * 消息数 
	 */
	Integer getMessageNum();

	/**
	 * 用户id 
	 */
	Integer getUserId();
	
	/**
	 * 转成map
	 * @return
	 */
	public Map toMap();

}