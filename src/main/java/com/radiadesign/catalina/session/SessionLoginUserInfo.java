/**
 * 
 * SessionLoginUserInfo.java
 * 版本所有 深圳市蜂鸟娱乐有限公司 2013-2014
 */
package com.radiadesign.catalina.session;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author john huang
 * 2016年2月4日 下午4:09:32
 * 本类主要做为 用户session登录信息,这个可与php的信息共通
 */
//@JsonIgnoreProperties(ignoreUnknown = true)
public class SessionLoginUserInfo implements Serializable {

	
	/**
	 * 登录用户
	 */
	Map<String,Object> user_auth;
	
	/**
	 * 是否新session
	 */
	protected boolean isNew=false;
	/**
	 * 是否可用
	 */
	protected boolean isValid=true;
	
	/**
	 * session创建时间
	 */
	protected long creationTime;
	
	/**
	 * 本次连接时间
	 */
	protected long thisAccessedTime;
	
	/**
	 * 属性
	 */
	protected Map<String,Object> attrs = new HashMap<>();
	
	
	/**
	 * sessionid
	 */
	protected String id;

	/**
	 * 登录用户
	 */
	public Map<String, Object> getUser_auth() {
		return user_auth;
	}

	/**
	 * 登录用户
	 */
	public void setUser_auth(Map<String, Object> user_auth) {
		this.user_auth = user_auth;
	}
	

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "SessionLoginUserInfo [user_auth=" + user_auth + "]";
	}

	/**
	 * 是否新session 
	 */
	public boolean isNew() {
		return isNew;
	}

	/**
	 * 是否新session 
	 */
	public void setNew(boolean isNew) {
		this.isNew = isNew;
	}

	/**
	 * 是否可用 
	 */
	public boolean isValid() {
		return isValid;
	}

	/**
	 * 是否可用 
	 */
	public void setValid(boolean isValid) {
		this.isValid = isValid;
	}

	/**
	 * session创建时间 
	 */
	public long getCreationTime() {
		return creationTime;
	}

	/**
	 * session创建时间 
	 */
	public void setCreationTime(long creationTime) {
		this.creationTime = creationTime;
	}

	/**
	 * 本次连接时间 
	 */
	public long getThisAccessedTime() {
		return thisAccessedTime;
	}

	/**
	 * 本次连接时间 
	 */
	public void setThisAccessedTime(long thisAccessedTime) {
		this.thisAccessedTime = thisAccessedTime;
	}

	/**
	 * sessionid 
	 */
	public String getId() {
		return id;
	}

	/**
	 * sessionid 
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * 属性 
	 */
	public Map<String, Object> getAttrs() {
		return attrs;
	}

	/**
	 * 属性 
	 */
	public void setAttrs(Map<String, Object> attrs) {
		this.attrs = attrs;
	}
	
	/**
	 * 添加属性
	 * @param key
	 * @param value
	 */
	public void putAttr(String key,Object value){
		if(attrs==null){
			attrs = new HashMap<>();
		}
		attrs.put(key, value);
	}
	
	/**
	 * 获取属性
	 * @param key
	 * @param value
	 * @return 
	 */
	public Object getAttr(String key){
		if(attrs==null){
			attrs = new HashMap<>();
		}
		return attrs.get(key);
	}
	
	
}
