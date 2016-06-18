

/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2014 All Rights Reserved.
 */
package com.mi360.aladdin.mall.util;

/**
 * 支付宝服务窗环境常量（demo中常量只是参考，需要修改成自己的常量值）
 * 
 * @author taixu.zqq
 * @version $Id: AlipayServiceConstants.java, v 0.1 2014年7月24日 下午4:33:49 taixu.zqq Exp $
 */
public class AlipayServiceEnvConstants {

    /**支付宝公钥-从支付宝服务窗获取*/
    public static final String ALIPAY_PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDDI6d306Q8fIfCOaTXyiUeJHkrIvYISRcc73s3vF1ZT7XN8RNPwJxo8pWaJMmvyTn9N4HQ632qJBVHf8sxHi/fEsraprwCtzvzQETrNRwVxLO5jVmRGi60j8Ue1efIlzPXV9je9mkjzOmdssymZkh2QhUrCmZYI/FCEa3/cNMW0QIDAQAB";

    /**签名编码-视支付宝服务窗要求*/
    public static final String SIGN_CHARSET      = "GBK";

    /**字符编码-传递给支付宝的数据编码*/
    public static final String CHARSET           = "GBK";

    /**签名类型-视支付宝服务窗要求*/
    public static final String SIGN_TYPE         = "RSA";
    
    
    public static final String PARTNER           = "2088712177049725";

    /** 服务窗appId  */
    //TODO !!!! 注：该appId必须设为开发者自己的服务窗id  这里只是个测试id
    public static final String APP_ID            = "2016020201135609";

    //开发者请使用openssl生成的密钥替换此处  请看文档：https://fuwu.alipay.com/platform/doc.htm#2-1接入指南
    //TODO !!!! 注：该私钥为测试账号私钥  开发者必须设置自己的私钥 , 否则会存在安全隐患 
    public static final String PRIVATE_KEY       = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAPMY7z7E0nc85kLBRmoGz/FQqxqNSUJN30dgAtxjtJvowg8tJbN2tNMMRB6vTRiNoTVaT7gRmz2Rm9JkMQYTVttWpBOk1QPk2L6c4yR4zY9MCgUYeeByI2/PV9ERKOPQB5rTs++S88sSmT6RfHwtTPSNAyycex8jy+n9hNQhM+MFAgMBAAECgYAd9Niml4KIBw0o9MBEmIpPZm1vXDHG5em51e8EUZUUEV64mAupTduClmRek6OgfvN6q6JfQGws7iCRkJ8p7IArUcmS7s4p/kLn59kjskshcyJ8YAa3g1VAc8iNTxSblp1UEh7xWTMqw06BzUOkFoDjr5JNEzU7aodH+5iRv0urkQJBAPvAzI3dep6r55s5HdAGjtHUwiyo0h47dzaf5Wry/AY4sRI1EZAera+Ozp1L5U1RoiV8AZ+RAq5Ux8g01QR8eUsCQQD3MsFvb3WbeuCOP0dXytBZn6iIDrFy0epc1tLvkHrE1YxTNv20lVZ+QvP0hkKdYhVmtEle+yU3H5Cuw9hRKjLvAkBWwvk8IwoxSNlG+4Y0vS3XTtWkgoH3fVL2dmCgXSFSG5OazMNCwe/lwved6hwaa3nQJU6B6X3dCsbXTq3jH+rFAkEAkvyaKqHTA62QAuU7jcdqFjCqbKSX/Rc2zRdr+8kkRxBXzU9gklVOReuX1elTiPO+mABRtGmPmnRpepG3eaOm/wJBAIlGKvAXjOOrezHqX4YnLbILsRMoEQBJUQRKsiSajwFC3Ut/Ge6OfMpXZNAyclCsBqzPqRbZeq+ViEwgt/FVlwQ=";

    //TODO !!!! 注：该公钥为测试账号公钥  开发者必须设置自己的公钥 ,否则会存在安全隐患
    public static final String PUBLIC_KEY        = "wg68752h7rotassrnnz7pay2i1rpren3";

    /**支付宝网关*/
    public static final String ALIPAY_GATEWAY    = "https://openapi.alipay.com/gateway.do";

    /**授权访问令牌的授权类型*/
    public static final String GRANT_TYPE        = "authorization_code";
}