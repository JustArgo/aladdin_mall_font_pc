package com.mi360.aladdin.mall.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import net.sinofool.alipay.AlipayConfig;
import net.sinofool.alipay.AlipayRequestData;
import net.sinofool.alipay.PCDirectSDK;
import net.sinofool.alipay.base.StringPair;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipaySystemOauthTokenRequest;
import com.alipay.api.request.AlipayUserUserinfoShareRequest;
import com.alipay.api.response.AlipaySystemOauthTokenResponse;
import com.alipay.api.response.AlipayUserUserinfoShareResponse;
import com.mi360.aladdin.mall.util.AlipayAPIClientFactory;
import com.mi360.aladdin.mall.util.AlipayServiceEnvConstants;

@Controller
@RequestMapping("/alipay")
public class AliPayController {
	
	private Logger logger = Logger.getLogger(AliPayController.class);
	
	@RequestMapping("/auth")
	public void auth(String requestId, String app_id, String source, String scope, String auth_code){
		
		System.out.println("app_id:"+app_id+" source:"+source+" scope:"+scope+" auth_code:"+auth_code);
		
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		AlipayRequestData request = new AlipayRequestData();
		request.setReqString("app_id", app_id);
		request.setReqString("method", "alipay.open.auth.token.app");
		request.setReqString("charset", "UTF-8");
		//request.setReqString("sign_type", "RSA");
		request.setReqString("timestamp", format.format(new Date()));
		request.setReqString("version", "1.0");
		request.setReqString("biz_content", "{\"grant_type\":\"authorization_code\",\"code\":\""+auth_code+"\"}");
		
		AlipayConfig config = new AlipayConfig() {
			
			@Override
			public String getSellerAccount() {
				// TODO Auto-generated method stub
				return "a565148812";
			}
			
			@Override
			public String getPartnerId() {
				// TODO Auto-generated method stub
				return "2088712177049725";
			}
			
			@Override
			public String getMyPrivateKey() {
				// TODO Auto-generated method stub
				return "mi0ytKimTNK1QkEQAG6JAQ==";
			}
			
			@Override
			public String getMD5KEY() {
				// TODO Auto-generated method stub
				return "wg68752h7rotassrnnz7pay2i1rpren3";
			}
			
			@Override
			public String getAlipayPublicKey() {
				// TODO Auto-generated method stub
				//return "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDDI6d306Q8fIfCOaTXyiUeJHkrIvYISRcc73s3vF1ZT7XN8RNPwJxo8pWaJMmvyTn9N4HQ632qJBVHf8sxHi/fEsraprwCtzvzQETrNRwVxLO5jVmRGi60j8Ue1efIlzPXV9je9mkjzOmdssymZkh2QhUrCmZYI/FCEa3/cNMW0QIDAQAB";
				return "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCnxj/9qwVfgoUh/y2W89L6BkRAFljhNhgPdyPuBV64bfQNN1PjbCzkIM6qRdKBoLPXmKKMiFYnkd6rAoprih3/PrQEB/VsW8OoM8fxn67UDYuyBTqA23MML9q1+ilIZwBC2AQ2UBVOrFXfFl75p6/B5KsiNG9zpgmLCUYuLkxpLQIDAQAB";
			}
		};
		
		PCDirectSDK sdk = new PCDirectSDK(config);
		List<StringPair> pairList = sdk.create(request);
		for(int i=0;i<pairList.size();i++){
			System.out.println(pairList);
		}
		
		/*app_id=2014070100171525
			    method=alipay.open.auth.token.app
			    charset=GBK
			    sign_type=RSA
	    timestamp=2014-01-01 08:08:08
	    sign=rXaTEfJ7WTDsP1DWRPHARW3uOr19+fzlngMCJBvbhP1XPEa9qZwGGng9oMDloABpJMT2SGeOj46+BUkqCGRO9fH90Vci3hOH01BfYnbhJz3ADK2h7gpjlponx4/sxELN6f2GXi51XKiHKnxMA9XpLLo68q+roY0M/ZFQ1UdnqeM=
	    version=1.0
	    biz_content={\"grant_type\":\"authorization_code\",\"code\":\"bf67d8d5ed754af297f72cc482287X62\"}
*/		
	}
	
	public static void main(String[] args) throws Exception{
		
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		String app_id = "2016020201135609";
		String auth_code = "b08c143959d9488aa517045a3314PX72";
		String private_key = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAPMY7z7E0nc85kLBRmoGz/FQqxqNSUJN30dgAtxjtJvowg8tJbN2tNMMRB6vTRiNoTVaT7gRmz2Rm9JkMQYTVttWpBOk1QPk2L6c4yR4zY9MCgUYeeByI2/PV9ERKOPQB5rTs++S88sSmT6RfHwtTPSNAyycex8jy+n9hNQhM+MFAgMBAAECgYAd9Niml4KIBw0o9MBEmIpPZm1vXDHG5em51e8EUZUUEV64mAupTduClmRek6OgfvN6q6JfQGws7iCRkJ8p7IArUcmS7s4p/kLn59kjskshcyJ8YAa3g1VAc8iNTxSblp1UEh7xWTMqw06BzUOkFoDjr5JNEzU7aodH+5iRv0urkQJBAPvAzI3dep6r55s5HdAGjtHUwiyo0h47dzaf5Wry/AY4sRI1EZAera+Ozp1L5U1RoiV8AZ+RAq5Ux8g01QR8eUsCQQD3MsFvb3WbeuCOP0dXytBZn6iIDrFy0epc1tLvkHrE1YxTNv20lVZ+QvP0hkKdYhVmtEle+yU3H5Cuw9hRKjLvAkBWwvk8IwoxSNlG+4Y0vS3XTtWkgoH3fVL2dmCgXSFSG5OazMNCwe/lwved6hwaa3nQJU6B6X3dCsbXTq3jH+rFAkEAkvyaKqHTA62QAuU7jcdqFjCqbKSX/Rc2zRdr+8kkRxBXzU9gklVOReuX1elTiPO+mABRtGmPmnRpepG3eaOm/wJBAIlGKvAXjOOrezHqX4YnLbILsRMoEQBJUQRKsiSajwFC3Ut/Ge6OfMpXZNAyclCsBqzPqRbZeq+ViEwgt/FVlwQ=";
		String alipay_public_key = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCnxj/9qwVfgoUh/y2W89L6BkRAFljhNhgPdyPuBV64bfQNN1PjbCzkIM6qRdKBoLPXmKKMiFYnkd6rAoprih3/PrQEB/VsW8OoM8fxn67UDYuyBTqA23MML9q1+ilIZwBC2AQ2UBVOrFXfFl75p6/B5KsiNG9zpgmLCUYuLkxpLQIDAQAB";
		
		AlipayRequestData request = new AlipayRequestData();
		request.setString("app_id", app_id);
		request.setString("method", "alipay.open.auth.token.app");
		request.setString("charset", "GBK");
		request.setString("sign_type", "RSA");
		request.setString("timestamp", format.format(new Date()));
		request.setString("version", "1.0");
		request.setString("biz_content", "{\"grant_type\":\"authorization_code\",\"code\":\""+auth_code+"\"}");
		
		AlipayConfig config = new AlipayConfig() {
			
			@Override
			public String getSellerAccount() {
				// TODO Auto-generated method stub
				return "a565148812";
			}
			
			@Override
			public String getPartnerId() {
				// TODO Auto-generated method stub
				return "2088712177049725";
			}
			
			@Override
			public String getMyPrivateKey() {
				// TODO Auto-generated method stub
				//return "MIICXQIBAAKBgQDzGO8+xNJ3POZCwUZqBs/xUKsajUlCTd9HYALcY7Sb6MIPLSWzdrTTDEQer00YjaE1Wk+4EZs9kZvSZDEGE1bbVqQTpNUD5Ni+nOMkeM2PTAoFGHngciNvz1fRESjj0Aea07PvkvPLEpk+kXx8LUz0jQMsnHsfI8vp/YTUITPjBQIDAQABAoGAHfTYppeCiAcNKPTARJiKT2Ztb1wxxuXpudXvBFGVFBFeuJgLqU3bgpZkXpOjoH7zequiX0BsLO4gkZCfKeyAK1HJku7OKf5C5+fZI7JLIXMifGAGt4NVQHPIjU8Um5adVBIe8VkzKsNOgc1DpBaA46+STRM1O2qHR/uYkb9Lq5ECQQD7wMyN3Xqeq+ebOR3QBo7R1MIsqNIeO3c2n+Vq8vwGOLESNRGQHq2vjs6dS+VNUaIlfAGfkQKuVMfINNUEfHlLAkEA9zLBb291m3rgjj9HV8rQWZ+oiA6xctHqXNbS75B6xNWMUzb9tJVWfkLz9IZCnWIVZrRJXvslNx+QrsPYUSoy7wJAVsL5PCMKMUjZRvuGNL0t107VpIKB931S9nZgoF0hUhuTmszDQsHv5cL3neocGmt50CVOgel93QrG106t4x/qxQJBAJL8miqh0wOtkALlO43HahYwqmykl/0XNs0Xa/vJJEcQV81PYJJVTkXrl9XpU4jzvpgAUbRpj5p0aXqRt3mjpv8CQQCJRirwF4zjq3sx6l+GJy2yC7ETKBEASVEESrIkmo8BQt1LfxnujnzKV2TQMnJQrAasz6kW2XqvlYhMILfxVZcE";				
				return "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAPMY7z7E0nc85kLBRmoGz/FQqxqNSUJN30dgAtxjtJvowg8tJbN2tNMMRB6vTRiNoTVaT7gRmz2Rm9JkMQYTVttWpBOk1QPk2L6c4yR4zY9MCgUYeeByI2/PV9ERKOPQB5rTs++S88sSmT6RfHwtTPSNAyycex8jy+n9hNQhM+MFAgMBAAECgYAd9Niml4KIBw0o9MBEmIpPZm1vXDHG5em51e8EUZUUEV64mAupTduClmRek6OgfvN6q6JfQGws7iCRkJ8p7IArUcmS7s4p/kLn59kjskshcyJ8YAa3g1VAc8iNTxSblp1UEh7xWTMqw06BzUOkFoDjr5JNEzU7aodH+5iRv0urkQJBAPvAzI3dep6r55s5HdAGjtHUwiyo0h47dzaf5Wry/AY4sRI1EZAera+Ozp1L5U1RoiV8AZ+RAq5Ux8g01QR8eUsCQQD3MsFvb3WbeuCOP0dXytBZn6iIDrFy0epc1tLvkHrE1YxTNv20lVZ+QvP0hkKdYhVmtEle+yU3H5Cuw9hRKjLvAkBWwvk8IwoxSNlG+4Y0vS3XTtWkgoH3fVL2dmCgXSFSG5OazMNCwe/lwved6hwaa3nQJU6B6X3dCsbXTq3jH+rFAkEAkvyaKqHTA62QAuU7jcdqFjCqbKSX/Rc2zRdr+8kkRxBXzU9gklVOReuX1elTiPO+mABRtGmPmnRpepG3eaOm/wJBAIlGKvAXjOOrezHqX4YnLbILsRMoEQBJUQRKsiSajwFC3Ut/Ge6OfMpXZNAyclCsBqzPqRbZeq+ViEwgt/FVlwQ=";
			}
			
			@Override
			public String getMD5KEY() {
				// TODO Auto-generated method stub
				return "wg68752h7rotassrnnz7pay2i1rpren3";
			}
			
			@Override
			public String getAlipayPublicKey() {
				// TODO Auto-generated method stub
				return "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDDI6d306Q8fIfCOaTXyiUeJHkrIvYISRcc73s3vF1ZT7XN8RNPwJxo8pWaJMmvyTn9N4HQ632qJBVHf8sxHi/fEsraprwCtzvzQETrNRwVxLO5jVmRGi60j8Ue1efIlzPXV9je9mkjzOmdssymZkh2QhUrCmZYI/FCEa3/cNMW0QIDAQAB";
				//return "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCnxj/9qwVfgoUh/y2W89L6BkRAFljhNhgPdyPuBV64bfQNN1PjbCzkIM6qRdKBoLPXmKKMiFYnkd6rAoprih3/PrQEB/VsW8OoM8fxn67UDYuyBTqA23MML9q1+ilIZwBC2AQ2UBVOrFXfFl75p6/B5KsiNG9zpgmLCUYuLkxpLQIDAQAB";
			}
		};
		
/*		AlipayClient alipayClient= new DefaultAlipayClient("https://openapi.alipay.com/gateway.do",app_id,private_key,"json","GBK",alipay_public_key);
		AlipaySystemOauthTokenRequest request2 = new AlipaySystemOauthTokenRequest();
		request2.setCode(auth_code);
		request2.setGrantType("authorization_code");
		AlipaySystemOauthTokenResponse response = alipayClient.execute(request2);
		System.out.println(response.toString());
		System.out.println(response.getSubCode());
		System.out.println(response.getSubMsg());*/
		
		
		try {
            //3. 利用authCode获得authToken
            AlipaySystemOauthTokenRequest oauthTokenRequest = new AlipaySystemOauthTokenRequest();
            oauthTokenRequest.setCode(auth_code);
            oauthTokenRequest.setGrantType(AlipayServiceEnvConstants.GRANT_TYPE);
            AlipayClient alipayClient2 = AlipayAPIClientFactory.getAlipayClient();
           AlipaySystemOauthTokenResponse oauthTokenResponse = alipayClient2.execute(oauthTokenRequest);

            //成功获得authToken
            if (null != oauthTokenResponse && oauthTokenResponse.isSuccess()) {

                //4. 利用authToken获取用户信息
                AlipayUserUserinfoShareRequest userinfoShareRequest = new AlipayUserUserinfoShareRequest();
                AlipayUserUserinfoShareResponse userinfoShareResponse = alipayClient2.execute(
                    userinfoShareRequest, "composeB583b39c280cb4012820b407e2d458X72");

                //成功获得用户信息
                if (null != userinfoShareResponse && userinfoShareResponse.isSuccess()) {
                    //这里仅是简单打印， 请开发者按实际情况自行进行处理
                    System.out.println("获取用户信息成功：" + userinfoShareResponse.getBody());
                    

                } else {
                    //这里仅是简单打印， 请开发者按实际情况自行进行处理
                    System.out.println("获取用户信息失败");

                }
            } else {
                //这里仅是简单打印， 请开发者按实际情况自行进行处理
                System.out.println("authCode换取authToken失败");
            }
        } catch (AlipayApiException alipayApiException) {
            //自行处理异常
            alipayApiException.printStackTrace();
        }

		
	}
	
	/**
	 * 支付宝支付 成功后 前台回调地址
	 * @return
	 * 2016年7月2日
	 */
	@RequestMapping("/directpay/redirect")
	public String redirect(String requestId, String is_success, String out_trade_no, String trade_status, String total_fee, Model model){
		
		logger.info("requestId:"+requestId+" is_success:"+is_success+" out_trade_no:"+out_trade_no+" trade_status:"+trade_status+" total_fee:"+total_fee);
		
		model.addAttribute("orderCode",out_trade_no);
		
		if("T".equals(is_success) && "TRADE_SUCCESS".equals(trade_status)){
			
			model.addAttribute("pSum",Double.valueOf(total_fee)*100);
			return "order/pay-success";
			
		}else{
			return "order/pay-fail";
		}
		
		
	}
	
}
