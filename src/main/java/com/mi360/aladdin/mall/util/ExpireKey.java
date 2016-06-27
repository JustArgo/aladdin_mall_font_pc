package com.mi360.aladdin.mall.util;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

import com.mi360.aladdin.util.MD5Util;

/**
 * 具有有效期的安全密钥 
 * 
 * @author ek
 *
 */
public class ExpireKey implements Serializable {
	private static final long serialVersionUID = 1L;
	/** 到期时间 */
	private Date expire;
	/** 属性 */
	private Object attribution;
	/** 属性2 */
	private Object attribution2;
	/** 属性3 */
	private Object attribution3;

	/**
	 * 创建
	 * 
	 * @param url
	 *            请求链接
	 * @param expire
	 *            有效时间，起始时间为创建时间（单位：毫秒）
	 */
	public ExpireKey(long expire) {
		Date now = new Date();
		this.expire = new Date(now.getTime() + expire);
	}

	/**
	 * 是否过期
	 * 
	 * @param value
	 *            密钥
	 */
	public boolean hasExpired() {
		if (new Date().after(expire)) {
			return true;
		}
		return false;
	}

	public Date getExpire() {
		return expire;
	}

	public void setExpire(Date expire) {
		this.expire = expire;
	}

	public Object getAttribution() {
		return attribution;
	}

	public void setAttribution(Object attribution) {
		this.attribution = attribution;
	}

	public Object getAttribution2() {
		return attribution2;
	}

	public void setAttribution2(Object attribution2) {
		this.attribution2 = attribution2;
	}

	public Object getAttribution3() {
		return attribution3;
	}

	public void setAttribution3(Object attribution3) {
		this.attribution3 = attribution3;
	}

}
