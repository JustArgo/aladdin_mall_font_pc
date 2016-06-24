package com.mi360.aladdin.mall.util;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

import com.mi360.aladdin.util.MD5Util;

/**
 * 找回密码安全密钥
 * 
 * @author ek
 *
 */
public class PwdKey implements Serializable {
	/** 密钥 */
	private String value;
	/** 到期时间 */
	private Date expire;
	/** 用户名（手机或邮箱 */
	private String username;

	/**
	 * 创建（到期时间为10分钟后）
	 */
	public PwdKey(String username) {
		this.username = username;
		value = MD5Util.encrypt(username) + UUID.randomUUID().toString().replace("-", "");
		Date date = new Date();
		expire = new Date(date.getTime() + 600000);
	}

	/**
	 * 验证密钥是否正确并未过期
	 * 
	 * @param value
	 *            密钥
	 */
	public boolean vertify(String value) {
		if (this.value.equals(value) && expire.after(new Date())) {
			return true;
		}
		return false;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Date getExpire() {
		return expire;
	}

	public void setExpire(Date expire) {
		this.expire = expire;
	}

}
