package com.mi360.aladdin.mall.util;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import com.qiniu.util.Auth;

public class QiNiuUtil {

	private static String accessKey;
	private static String secretKey;

	static {
		InputStream in = QiNiuUtil.class.getClassLoader().getResourceAsStream("aladdin.properties");
		Properties p = new Properties();
		try {
			p.load(in);
			accessKey = p.getProperty("qiniu.accessKey");
			secretKey = p.getProperty("qiniu.secretKey");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public QiNiuUtil(){};

	public static String getDownloadUrl(String baseUrl) {
		Auth auth = Auth.create(accessKey, secretKey);
		if (baseUrl == null) {
			baseUrl = "";
		}
		return auth.privateDownloadUrl(baseUrl);
	}
	
	public static void fetch(String from, String bucket, String key) throws InvalidKeyException, NoSuchAlgorithmException, IOException{
		String to = bucket + ":" + key;
		//from = "http://gglkhssy.bj.bcebos.com/lancomestyle/wmlm/index_04.jpg";
	    String encodeFrom = EncodeUtils.encodeBase64URLSafeString(from);
	    String encodeTo = EncodeUtils.encodeBase64URLSafeString(to);
	    String url = "http://iovip.qbox.me/fetch/" + encodeFrom + "/to/" + encodeTo;
	    Auth auth = Auth.create(accessKey, secretKey);
	    
	    String accessToken = auth.signRequest(url, null, null);
	   
	    HttpClient client = new DefaultHttpClient();
	    HttpPost post = new HttpPost(url);

	   
	    //post.setHeader("User-Agent", Config.USER_AGENT);
	    post.setHeader("Authorization", "QBox " + accessToken);
	    HttpResponse res = client.execute(post);
	    System.out.println(res.getHeaders("Content-Type")[0].getName()+res.getHeaders("Content-Type")[0].getValue());
	    System.out.println("code: "+res.getStatusLine().getStatusCode());
	    
	}

}
