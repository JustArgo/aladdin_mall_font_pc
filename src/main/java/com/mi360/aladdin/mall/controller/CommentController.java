package com.mi360.aladdin.mall.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mi360.aladdin.comment.domain.Comment;
import com.mi360.aladdin.comment.domain.CommentImg;
import com.mi360.aladdin.comment.service.ICommentService;
import com.mi360.aladdin.interaction.wx.service.WxInteractionService;
import com.mi360.aladdin.mall.Principal;
import com.mi360.aladdin.mall.util.QiNiuUtil;
import com.mi360.aladdin.mall.util.WebUtil;
import com.mi360.aladdin.entity.order.OrderProduct;
import com.mi360.aladdin.order.service.IOrderProductService;
import com.mi360.aladdin.product.service.IProductService;
import com.mi360.aladdin.product.service.IProductSkuService;
import com.mi360.aladdin.user.service.UserService;
import com.mi360.aladdin.util.MapUtil;
import com.mi360.aladdin.util.MapUtil.MapData;


/**
 * 管理收货地址
 * @author 黄永宗
 * @date 2016年2月18日 下午3:07:51
 */
@Controller
@RequestMapping("/comments")
public class CommentController{
//	
//	private Logger logger = Logger.getLogger(this.getClass());
//	
//	@Value("${qiniu.domain}")
//	protected String qiNiuDomain;
//	
//	@Value("${qiniu.space}")
//	protected String qiNiuSpace;
//	
//	@Autowired
//	private IProductService productService; 
//	
//	@Autowired
//	private ICommentService commentService;
//	
//	@Autowired
//	private IOrderProductService orderProductService;
//	
//	@Autowired
//	private IProductSkuService productSkuService;
//	
//	@Autowired
//	private WxInteractionService wxInteractionService;
//	
//	@Autowired
//	private UserService userService;
//	
//	private String[] names = new String[]{"jimi","halei","jiu"};
//	
//	@RequestMapping("/commentlist")
//	public String getCommentList(String requestId,Integer productID,Integer pageIndex,Integer pageSize,Model model){
//		
//		Principal principal = WebUtil.getCurrentSessionUserAuthInfo();
//// if(principal==null)principal = new Principal("2","");
//		String mqID = principal.getMqId();
//		
//		if(productID==null){
//			return "404";
//		}
//		if(pageIndex==null){
//			pageIndex = 0;
//		}
//		if(pageSize==null){
//			pageSize = 8;
//		}
//		
//		//获得共有多少条未删除评论
//		int commentCount = commentService.getCountNoDeletedByProductID(productID,requestId);
//		model.addAttribute("commentCount", commentCount);
//		
//		//获得评论列表
//		List<Map<String,Object>> comments = new ArrayList<Map<String,Object>>();
//		
//		if(commentCount>0){
//			
//			List<Comment> commentList = commentService.getCommentNoDeletedList(productID,pageIndex,pageSize,requestId);
//			//遍历每一条评论
//			for(int i=0;i<commentList.size();i++){
//				
//				Map<String,Object> map = new HashMap<String,Object>();
//				
//				Map<String,Object> userInfo = userService.findWxUserByMqId(requestId, commentList.get(i).getMqID());
//				MapData data = MapUtil.newInstance(userInfo);
//				Map<String,Object> user = (Map<String, Object>) data.getObject("result");
//				
//				if(user==null){//如果找不到 该条评论的用户  则跳过 不显示 此评论
//					continue;
//				}
//				map.put("userHeadImg",user.get("headimgurl"));
//				map.put("userName",user.get("nickname")); 
//				Comment comment = commentList.get(i);
//				
//				Integer orderProductID = comment.getOrderProdID();
//				OrderProduct orderProduct = orderProductService.getOrderProductByID(orderProductID, requestId);
//				Integer skuID = orderProduct.getSkuID();
//				List<String> skuStrs = productSkuService.getSkuStr(skuID, requestId);
//				
//				String skuStr = "";
//				for(int j=0;j<skuStrs.size();j++){
//					skuStr += skuStrs.get(j)+"   ";
//				}
//				
//				map.put("skuStr", skuStr);
//				map.put("comment",commentList.get(i));
//				
//				logger.info("commentList.get(i):"+commentList.get(i));
//				//加入规格
//				comments.add(map);
//				
//			}
//			
//		}
//		
//		model.addAttribute("comments", comments);
//		
//		return "comments";
//		
//	}
//	
//	@RequestMapping("/uptoken")
//	@ResponseBody
//	public String getUpToken(String requestId){
//		return "6EZmwsqQYeHvlaA44_LwiBAePez-rjpOv4jwg4t4:UENCitOcA8BGmi7roBWnC86g-W8=:IntcInNjb3BlXCI6XCJhbGFkZGluOnN1bmZsb3dlci5qcGdcIixcImRlYWRsaW5lXCI6MTQ1MTQ5MTIwMCxcInJldHVybkJvZHlcIjpcIntcIm5hbWVcIjokKGZuYW1lKSxcInNpemVcIjokKGZzaXplKSxcIndcIjokKGltYWdlSW5mby53aWR0aCksXCJoXCI6JChpbWFnZUluZm8uaGVpZ2h0KSxcImhhc2hcIjokKGV0YWcpfVwifSI=";
//	}
//	
//	/**
//	 * 根据产品id 及 分页信息 查找商品评论
//	 * @param productID
//	 * @param pageIndex
//	 * @param pageSize
//	 * @return
//	 */
//	@RequestMapping("/page_comments")
//	@ResponseBody
//	public List<Comment> getComments(String requestId,Integer productID,Integer pageIndex,Integer pageSize){
//		List<Comment> comments = commentService.getCommentNoDeletedList(productID,pageIndex,pageSize,requestId);
//		return commentService.getCommentNoDeletedList(productID,pageIndex,pageSize,requestId);
//	}
//	
//	/**
//	 * 
//	 */
//	@RequestMapping("/product_detail")
//	public String productDetail(String requestId){
//		return "productdetail";
//	}
//	
//	/**
//	 * 对某个订单商品进行评论
//	 * @param requestId
//	 * @param orderProductID  订单商品id
//	 * @param sendGoodsScore  发货速度评分
//	 * @param descScore		      描述与商品符合分数
//	 * @param serveScore      服务分数
//	 * @param commentStr      评论内容
//	 * @param imgs
//	 * @return
//	 */
//	@RequestMapping("/comment")
//	public String comment(String requestId, Integer orderProductID, Integer sendGoodsScore, Integer descScore, Integer serveScore, String commentStr, String[] imgs){
//		
//		Principal principal = WebUtil.getCurrentSessionUserAuthInfo();
////if(principal==null)principal = new Principal("2", "");
//		String mqID = principal.getMqId();
//		
//		OrderProduct orderProduct = orderProductService.getOrderProductByID(orderProductID, requestId);
//		
//		logger.info("commentStatus: "+orderProduct.getCommentStatus());
//		
//		if("YES".equals(orderProduct.getCommentStatus())){
//			return "comment/already";
//		}
//		
//		Comment comment = new Comment();
//		comment.setCommentDesc(commentStr);
//		comment.setCreateTime(new Date());
//		comment.setDescConform(descScore);
//		comment.setMqID(mqID);
//		comment.setOrderID(orderProduct.getOrderID());
//		comment.setOrderProdID(orderProduct.getID());
//		comment.setProductID(orderProduct.getProductID());
//		comment.setService(serveScore);
//		comment.setSpeed(sendGoodsScore);
//		comment.setStatus(Comment.STATUS_OK);
//		
//		if(imgs!=null && imgs.length>0){
//			for(int i=0;i<imgs.length;i++){
//				CommentImg commentImg = new CommentImg();
//				commentImg.setCreateTime(new Date());
//				commentImg.setImgPath("http://"+qiNiuDomain+"/comment_img_"+imgs[i]);
//				commentImg.setSortOrder(i+1);
//				commentImg.setStatus(CommentImg.STATUS_OK);
//				comment.getImgs().add(commentImg);
//			}
//			this.fetch(imgs);
//		}
//		
//		orderProduct.setCommentStatus("YES");
//		orderProductService.updateOrderProduct(orderProduct, requestId);
//		
//		commentService.comment(comment, requestId);
//		return "redirect:commentlist?productID="+orderProduct.getProductID()+"&pageIndex=0&pageSize=8";
//	}
//	
//	private void fetch(String[] imgs){
//		
//		try{
//			String accessToken = wxInteractionService.getAccessToken(false);
//			for(int i=0;i<imgs.length;i++){
//				String from = "https://api.weixin.qq.com/cgi-bin/media/get?access_token="+accessToken+"&media_id="+imgs[i];
//				logger.info("from:"+from);
//				logger.info("begin fetch");
//				QiNiuUtil.fetch(from, qiNiuSpace, "comment_img_"+imgs[i]);
//				logger.info("end fetch");
//			}
//		}catch(Exception e){
//			logger.error(e.getMessage(),e);
//		}
//		
//		
//	}
}
