function formatDate(strTime) {
    var date = new Date(strTime);
    return date.getFullYear()+"-"+(date.getMonth()+1)+"-"+date.getDate();
}
function getItemStr(childOrder){
	var tmpStr = "";
	var orderProduct = {};
	if(childOrder.status=='COM'){
		tmpStr += '<tr class="status"><td colspan="6" class="text-left"><span>'+formatDate(childOrder.createTime)+'</span><span>订单编号：'+childOrder.orderCode+'</span><span>供应商：$!{childOrder.supName}</span><span class="time">'+(childOrder.remainTime=='OUT_OF_DATE')?'订单已过付款时间':('还剩'+childOrder.remainTime+'可以进行付款')+'</span></td></tr>';
		for(var i=0;i<childOrder.orderProductList.length;i++){
			orderProduct = childOrder.orderProductList[i];
			
			tmpStr += '<tr><td class="text-left"><a href="javascript:;" class="fl"><img src="'+orderProduct.skuImg+'" style="width:121px;height:120px;"></a><div class="fr"><p><a href="javascript:;">'+orderProduct.productName+'</a></p><p><span>颜色：墨蓝</span><span>尺寸：XL</span></p></div></td><td>¥'+(orderProduct.skuPrice/100).toFixed(2)+'</td><td>'+orderProduct.buyNum+'</td>';
			
			if(i==0){
				tmpStr += '<td rowspan="'+childOrder.orderProductList.length+'"><p>¥'+(orderProduct.skuPrice/100.0*orderProduct.buyNum).toFixed(2)+'</p><p>（含运费¥'+childOrder.orderSum+'）</p></td><td rowspan="'+childOrder.orderProductList.length+'">待付款<a href="/order/order-detail?orderCode=$childOrder.orderCode">订单详情</a><a href="javascript:;" data-toggle="modal" data-target="#myModal" data-href="/Aladdin/orderCancel-popup.html" data-class="modal-orderCancel" class="jq-orderCancel">取消订单</a></td><td rowspan="'+childOrder.orderProductList.length+'" class="status">';
				
				if(childOrder.remainTime!='OUT_OF_DATE'){
					tmpStr += '<a href="javascript:;" class="pay-money">付款</a>';
				}
				
				tmpStr += '</td>';
				
			}
			
			tmpStr += '</tr>';
		}
	}else if(childOrder.status=='CAN'){
		tmpStr += '<div class="info-content"><div class="info-bottom"><div class="titleb">供应商：'+childOrder.supName+'<span>已取消<span></div>';
		for(var i=0;i<childOrder.orderProductList.length;i++){
			orderProduct = childOrder.orderProductList[i];
			tmpStr += '<a href="/order/order-detail?orderCode='+childOrder.orderCode+'"><div class="info-bottom-img"><img src="'+orderProduct.skuImg+'" /></div><p class="bottom-1" style="font-size:16px;">'+orderProduct.productName+'</p><p class="bottom-3"><span style="color:#F389AA;font-size:16px;">￥'+(orderProduct.skuPrice/100).toFixed(2)+'</span></p><div class="bottom-4"><div class="bottom-number">数量x'+orderProduct.buyNum+'</div></div></a><br/>';
		}
		tmpStr += '</div></div>';
	}else if(childOrder.status=='DFK'){
		tmpStr += '<tr class="status"><td colspan="6" class="text-left"><span>'+formatDate(childOrder.createTime)+'</span><span>订单编号：'+childOrder.orderCode+'</span><span>供应商：$!{childOrder.supName}</span><span class="time">'+(childOrder.remainTime=='OUT_OF_DATE')?'订单已过付款时间':('还剩'+childOrder.remainTime+'可以进行付款')+'</span></td></tr>';
		for(var i=0;i<childOrder.orderProductList.length;i++){
			orderProduct = childOrder.orderProductList[i];
			
			tmpStr += '<tr><td class="text-left"><a href="javascript:;" class="fl"><img src="'+orderProduct.skuImg+'" style="width:121px;height:120px;"></a><div class="fr"><p><a href="javascript:;">'+orderProduct.productName+'</a></p><p><span>颜色：墨蓝</span><span>尺寸：XL</span></p></div></td><td>¥'+(orderProduct.skuPrice/100).toFixed(2)+'</td><td>'+orderProduct.buyNum+'</td>';
			
			if(i==0){
				tmpStr += '<td rowspan="'+childOrder.orderProductList.length+'"><p>¥'+(orderProduct.skuPrice/100.0*orderProduct.buyNum).toFixed(2)+'</p><p>（含运费¥'+childOrder.orderSum+'）</p></td><td rowspan="'+childOrder.orderProductList.length+'">待付款<a href="/order/order-detail?orderCode=$childOrder.orderCode">订单详情</a><a href="javascript:;" data-toggle="modal" data-target="#myModal" data-href="/Aladdin/orderCancel-popup.html" data-class="modal-orderCancel" class="jq-orderCancel">取消订单</a></td><td rowspan="'+childOrder.orderProductList.length+'" class="status">';
				
				if(childOrder.remainTime!='OUT_OF_DATE'){
					tmpStr += '<a href="javascript:;" class="pay-money">付款</a>';
				}
				
				tmpStr += '</td>';
				
			}
			
			tmpStr += '</tr>';
		}
	}else if(childOrder.status=='DFH'){
		tmpStr += '<tr class="status"><td colspan="6" class="text-left"><span>'+formatDate(childOrder.createTime)+'</span><span>订单编号：'+childOrder.orderCode+'</span><span>供应商：'+childOrder.supName+'</span></td></tr>';
		for(var i=0;i<childOrder.orderProductList.length;i++){
			orderProduct = childOrder.orderProductList[i];
			
			tmpStr += '<tr><td class="text-left"><a href="javascript:;" class="fl"><img src="'+orderProduct.skuImg+'" style="width:121px;height:120px;"></a><div class="fr"><p><a href="javascript:;">'+orderProduct.productName+'</a></p><p><span>颜色：墨蓝</span><span>尺寸：XL</span></p></div></td><td>¥'+(orderProduct.skuPrice/100).toFixed(2)+'</td><td>'+orderProduct.buyNum+'</td>';
			
			if(i==0){
				tmpStr += '<td rowspan="'+childOrder.orderProductList.length+'"><p>¥'+(orderProduct.skuPrice/100.0*orderProduct.buyNum).toFixed(2)+'</p><p>（含运费¥'+childOrder.orderSum+'）</p></td><td rowspan="'+childOrder.orderProductList.length+'">待付款<a href="/order/order-detail?orderCode=$childOrder.orderCode">订单详情</a><a href="javascript:;" data-toggle="modal" data-target="#myModal" data-href="/Aladdin/orderCancel-popup.html" data-class="modal-orderCancel" class="jq-orderCancel">取消订单</a></td><td rowspan="'+childOrder.orderProductList.length+'" class="status">';
				
				
				tmpStr += '<a href="/order/return-money?orderCode='+childOrder.orderCode+'" class="pay-money">退款</a>';
				
				
				tmpStr += '</td>';
				
			}
			
			tmpStr += '</tr>';
			
		}
		
	}else if(childOrder.status=='YFH'){
		tmpStr += '<tr class="status"><td colspan="6" class="text-left"><span>'+formatDate(childOrder.createTime)+'</span><span>订单编号：'+childOrder.orderCode+'</span><span>供应商：'+childOrder.supName+'</span></td></tr>';
		for(var i=0;i<childOrder.orderProductList.length;i++){
			orderProduct = childOrder.orderProductList[i];
			
			tmpStr += '<tr><td class="text-left"><a href="javascript:;" class="fl"><img src="'+orderProduct.skuImg+'" style="width:121px;height:120px;"></a><div class="fr"><p><a href="javascript:;">'+orderProduct.productName+'</a></p><p><span>颜色：墨蓝</span><span>尺寸：XL</span></p></div></td><td>¥'+(orderProduct.skuPrice/100).toFixed(2)+'</td><td>'+orderProduct.buyNum+'</td>';
			
			tmpStr += '<td><p>¥'+(orderProduct.skuPrice/100.0*orderProduct.buyNum).toFixed(2)+'</p><p>（含运费¥'+childOrder.orderSum+'）</p></td><td>待付款<a href="/order/order-detail?orderCode=$childOrder.orderCode">订单详情</a><a href="javascript:;" data-toggle="modal" data-target="#myModal" data-href="/Aladdin/orderCancel-popup.html" data-class="modal-orderCancel" class="jq-orderCancel">取消订单</a></td><td class="status">';
			
			
			tmpStr += '<a href="/order/rg?o='+childOrder.orderCode+'&p='+orderProduct.ID+'" class="pay-money">退货</a>';
			
			
			tmpStr += '</td>';
				
			
			tmpStr += '</tr>';
			
		}
	}else if(childOrder.status=='STK'){
		tmpStr += '<div class="info-content"><div class="info-bottom"><div class="titleb">供应商：'+childOrder.supName+'<span>退款审核中<span></div>';
		for(var i=0;i<childOrder.orderProductList.length;i++){
			orderProduct = childOrder.orderProductList[i];
			tmpStr += '<a href="/order/order-detail?orderCode='+childOrder.orderCode+'"><div class="info-bottom-img"><img src="'+orderProduct.skuImg+'" /></div><p class="bottom-1" style="font-size:16px;">'+orderProduct.productName+'</p><p class="bottom-3"><span style="color:#F389AA;font-size:16px;">￥'+(orderProduct.skuPrice/100).toFixed(2)+'</span></p><div class="bottom-4"><div class="bottom-number">数量x'+orderProduct.buyNum+'</div></div></a><br/>';
		}
		tmpStr += '</div></div>';
	}else if(childOrder.status=='TKZ'){
		tmpStr += '<div class="info-content"><div class="info-bottom"><div class="titleb">供应商：'+childOrder.supName+'<span>退款中<span></div>';
		for(var i=0;i<childOrder.orderProductList.length;i++){
			orderProduct = childOrder.orderProductList[i];
			tmpStr += '<a href="/order/order-detail?orderCode='+childOrder.orderCode+'"><div class="info-bottom-img"><img src="'+orderProduct.skuImg+'" /></div><p class="bottom-1" style="font-size:16px;">'+orderProduct.productName+'</p><p class="bottom-3"><span style="color:#F389AA;font-size:16px;">￥'+(orderProduct.skuPrice/100).toFixed(2)+'</span></p><div class="bottom-4"><div class="bottom-number">数量x'+orderProduct.buyNum+'</div></div></a><br/>';
		}
		tmpStr += '</div></div>';
	}else if(childOrder.status=='TKC'){
		tmpStr += '<div class="info-content"><div class="info-bottom"><div class="titleb">供应商：'+childOrder.supName+'<span>退款成功<span></div>';
		for(var i=0;i<childOrder.orderProductList.length;i++){
			orderProduct = childOrder.orderProductList[i];
			tmpStr += '<a href="/order/order-detail?orderCode='+childOrder.orderCode+'"><div class="info-bottom-img"><img src="'+orderProduct.skuImg+'" /></div><p class="bottom-1" style="font-size:16px;">'+orderProduct.productName+'</p><p class="bottom-3"><span style="color:#F389AA;font-size:16px;">￥'+(orderProduct.skuPrice/100).toFixed(2)+'</span></p><div class="bottom-4"><div class="bottom-number">数量x'+orderProduct.buyNum+'</div></div></a><br/>';
		}
		tmpStr += '</div></div>';
	}else if(childOrder.status=='TKB'){
		tmpStr += '<div class="info-content"><div class="info-bottom"><div class="titleb">供应商：'+childOrder.supName+'<span>退款审核不通过<span></div>';
		for(var i=0;i<childOrder.orderProductList.length;i++){
			orderProduct = childOrder.orderProductList[i];
			tmpStr += '<a href="/order/order-detail?orderCode='+childOrder.orderCode+'"><div class="info-bottom-img"><img src="'+orderProduct.skuImg+'" /></div><p class="bottom-1" style="font-size:16px;">'+orderProduct.productName+'</p><p class="bottom-3"><span style="color:#F389AA;font-size:16px;">￥'+(orderProduct.skuPrice/100).toFixed(2)+'</span></p><div class="bottom-4"><div class="bottom-number">数量x'+orderProduct.buyNum+'</div></div></a><br/>';
		}
		tmpStr += '<div style="height:40px;"><a href="/order/return-money?orderCode='+childOrder.orderCode+'" class="btn-select"><div style="float:right;padding-left:5px;padding-right:10px;font-size:15px;line-height:30px;margin-right:19px;" class="bg1">重新申请</div></a></div></div></div>';
	}else if(childOrder.status=='TKS'){
		tmpStr += '<div class="info-content"><div class="info-bottom"><div class="titleb">供应商：'+childOrder.supName+'<span>退款失败<span></div>';
		for(var i=0;i<childOrder.orderProductList.length;i++){
			orderProduct = childOrder.orderProductList[i];
			tmpStr += '<a href="/order/order-detail?orderCode='+childOrder.orderCode+'"><div class="info-bottom-img"><img src="'+orderProduct.skuImg+'" /></div><p class="bottom-1" style="font-size:16px;">'+orderProduct.productName+'</p><p class="bottom-3"><span style="color:#F389AA;font-size:16px;">￥'+(orderProduct.skuPrice/100).toFixed(2)+'</span></p><div class="bottom-4"><div class="bottom-number">数量x'+orderProduct.buyNum+'</div></div></a><br/>';
		}
		tmpStr += '</div></div>';
	}else if(childOrder.status=='TH'){
		tmpStr += '<div class="info-content"><div class="info-bottom"><div class="titleb">供应商：'+childOrder.supName+'<span><span></div>';
		for(var i=0;i<childOrder.orderProductList.length;i++){
			orderProduct = childOrder.orderProductList[i];
			if(orderProduct.statuStr=='商家拒绝退款'||orderProduct.statuStr=='退货审核不通过'){
				tmpStr += '<a href="/order/order-detail?orderCode='+childOrder.orderCode+'"><div class="info-bottom-img"><img src="'+orderProduct.skuImg+'" /></div><p class="bottom-1" style="font-size:16px;">'+orderProduct.productName+'</p><p class="bottom-3" style="display:inline-block;"><span style="color:#F389AA;font-size:16px;">￥'+(orderProduct.skuPrice/100).toFixed(2)+'</span></p><p style="float:right;color:red;font-size:12px;">'+orderProduct.statuStr+'</p><div class="bottom-4"><div class="bottom-number">数量x'+orderProduct.buyNum+'</div><a href="/order/r2?p='+orderProduct.ID+'" class="btn-select"><div class="bottom-number-right bg1">重新申请</div></a></div></a><br/>';
			}else{
				tmpStr += '<a href="/order/order-detail?orderCode='+childOrder.orderCode+'"><div class="info-bottom-img"><img src="'+orderProduct.skuImg+'" /></div><p class="bottom-1" style="font-size:16px;">'+orderProduct.productName+'</p><p class="bottom-3" style="display:inline-block;"><span style="color:#F389AA;font-size:16px;">￥'+(orderProduct.skuPrice/100).toFixed(2)+'</span></p><p style="float:right;color:red;font-size:12px;">'+orderProduct.statuStr+'</p><div class="bottom-4"><div class="bottom-number">数量x'+orderProduct.buyNum+'</div><a href="/order/return-goods-detail-orderproductid?orderProductID='+orderProduct.ID+'" class="btn-select"><div class="bottom-number-right bg1">退货详情</div></a></div></a><br/>';
			}
		}
		tmpStr += '</div></div>';
	}
	
	return tmpStr;
}