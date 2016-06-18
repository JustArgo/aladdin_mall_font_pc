package com.mi360.aladdin.mall.util;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;

public class EncodeUtils {
	public static String CHARSET = "utf-8";

	public static String encodeBase64URLSafeString(String p) {
	    return encodeBase64URLSafeString(toByte(p));
	}

	public static String encodeBase64URLSafeString(byte[] binaryData) {
	    byte[] b = encodeBase64URLSafe(binaryData);
	    return toString(b);
	}

	/** 保留尾部的“=” */
	public static byte[] encodeBase64URLSafe(byte[] binaryData) {
	    byte[] b = Base64.encodeBase64URLSafe(binaryData);
	    int mod = b.length % 4;
	    if(mod == 0){
	        return b;
	    }else{
	        int pad = 4 - mod;
	        byte[] b2 = new byte[b.length + pad];
	        System.arraycopy(b, 0, b2, 0, b.length);
	        b2[b.length] = '=';
	        if (pad > 1) {
	            b2[b.length + 1] = '=';
	        }
	        return b2;
	    }
	}

	public static byte[] toByte(String s){
	    try {
	        return s.getBytes(CHARSET);
	    } catch (UnsupportedEncodingException e) {
	        throw new RuntimeException(e);
	    }
	}

	public static String toString(byte[] bs){
	    try {
	        return new String(bs, CHARSET);
	    } catch (UnsupportedEncodingException e) {
	        throw new RuntimeException(e);
	    }
	}

	private static HttpEntity getEntity(HttpRequestBase request) {
	    try {
	        HttpPost post = (HttpPost) request;
	        if (post != null) {
	            return post.getEntity();
	        }
	    } catch (Exception e) {
	    }

	    return null;
	}
}