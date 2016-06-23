package com.radiadesign.catalina.session;

import org.apache.catalina.util.CustomObjectInputStream;
import org.apache.tomcat.util.codec.binary.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;

import javax.servlet.http.HttpSession;
import java.io.*;
import java.util.Enumeration;


/**
 * @author john huang
 * 2016年2月1日 下午1:26:48
 * 本类主要做为 从redis中获取json的数据并进行处理
 */
public class JavaJSONSerializer implements Serializer {
  private ClassLoader loader;

  @Override
  public void setClassLoader(ClassLoader loader) {
    this.loader = loader;
  }

  @Override
  public byte[] serializeFrom(HttpSession session) throws IOException {

    RedisSession redisSession = (RedisSession) session;
//    Enumeration<String> attributeNames = redisSession.getAttributeNames();
//    System.out.println("从session中读取属性并放在redis中");
    SessionLoginUserInfo sessionLoginUserInfo = redisSession.doWriteObject();
    ObjectMapper mapper = new ObjectMapper();
    byte[] writeValueAsBytes = mapper.writeValueAsBytes(sessionLoginUserInfo);
//    while (attributeNames.hasMoreElements()) {
//		String attr = (String) attributeNames.nextElement();
//		Object value = redisSession.getAttribute(attr);
//		System.out.println(String.format("%s=%s\n", attr,value));
//		if(attr.equals("loginUserInfo")){
//			System.out.println("返回登录用户信息详情");
//			return (value==null?"{}":value.toString()).getBytes();
//		}
//	}
//    System.out.println("没有找到登录用户信息详情");
    return writeValueAsBytes;
//    ByteArrayOutputStream bos = new ByteArrayOutputStream();
//    ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(bos));
//    oos.writeLong(redisSession.getCreationTime());
//    redisSession.writeObjectData(oos);

//    oos.close();

//    return bos.toByteArray();
  }

  @Override
  public HttpSession deserializeInto(byte[] data, HttpSession session) throws IOException, ClassNotFoundException {
//	  System.out.println("从redis中读取属性并放在session中111");
    RedisSession redisSession = (RedisSession) session;
    String jsonstr = new String(data);
//    System.out.println(jsonstr);
    ObjectMapper mapper = new ObjectMapper();
    try {
		SessionLoginUserInfo userinfo = mapper.readValue(jsonstr, SessionLoginUserInfo.class);
		if(userinfo!=null)
		{
//			System.out.println(userinfo);
			redisSession.readObjectFromSessionLoginUserInfo(userinfo);
		}
		else{
//			System.out.println("用户信息为空");
		}
	} catch (Exception e) {
		e.printStackTrace();
		throw new IOException("读取session中的登录用户信息出错",e);
	}
    
    return redisSession;
    
  }
}
