function getItemStr(childOrder){
	var tmpStr = "";
	var orderProduct = {};
	if(childOrder.status=='COM'){
		tmpStr += '<div class="order-info-all"><div class="info-content"><div class="info-bottom"><div class="titleb">供应商：'+childOrder.supName+'<span>已完成<span></div>';
		for(var i=0;i<childOrder.orderProductList.length;i++){
			orderProduct = childOrder.orderProductList[i];
			tmpStr += '<a href="/order/order-detail?orderCode='+childOrder.orderCode+'"><div class="info-bottom-img"><img src="'+orderProduct.skuImg+'" /></div><p class="bottom-1" style="font-size:16px;">'+orderProduct.productName+'</p><p class="bottom-3"><span style="color:#F389AA;font-size:16px;">￥'+(orderProduct.skuPrice/100).toFixed(2)+'</span></p><div class="bottom-4"><div class="bottom-number">数量x'+orderProduct.buyNum+'</div></div></a><br/>';
		}
		tmpStr += '</div></div></div>';
	}else if(childOrder.status=='CAN'){
		tmpStr += '<div class="order-info-all"><div class="info-content"><div class="info-bottom"><div class="titleb">供应商：'+childOrder.supName+'<span>已取消<span></div>';
		for(var i=0;i<childOrder.orderProductList.length;i++){
			orderProduct = childOrder.orderProductList[i];
			tmpStr += '<a href="/order/order-detail?orderCode='+childOrder.orderCode+'"><div class="info-bottom-img"><img src="'+orderProduct.skuImg+'" /></div><p class="bottom-1" style="font-size:16px;">'+orderProduct.productName+'</p><p class="bottom-3"><span style="color:#F389AA;font-size:16px;">￥'+(orderProduct.skuPrice/100).toFixed(2)+'</span></p><div class="bottom-4"><div class="bottom-number">数量x'+orderProduct.buyNum+'</div></div></a><br/>';
		}
		tmpStr += '</div></div></div>';
	}else if(childOrder.status=='DFK'){
		tmpStr += '<div class="order-info"><div class="info-content"><div class="info-bottom"><div class="titleb">供应商：'+childOrder.supName+'<span>待付款<span></div>';
		for(var i=0;i<childOrder.orderProductList.length;i++){
			orderProduct = childOrder.orderProductList[i];
			tmpStr += '<a href="/order/order-nopay-detail?orderCode='+childOrder.parentCode+'"><div class="info-bottom-img"><img src="'+orderProduct.skuImg+'" /></div><p class="bottom-1" style="font-size:16px;">'+orderProduct.productName+'</p><p class="bottom-3"><span style="color:#F389AA;font-size:16px;">￥'+(orderProduct.skuPrice/100).toFixed(2)+'</span></p><div class="bottom-4"><div class="bottom-number">数量x'+orderProduct.buyNum+'</div></div></a><br/>';
		}
		tmpStr += '</div></div></div>';
	}else if(childOrder.status=='DFH'){
		tmpStr += '<div class="order-info2"><div class="info-content"><div class="info-bottom"><div class="titleb">供应商：'+childOrder.supName+'<span>待发货<span></div>';
		for(var i=0;i<childOrder.orderProductList.length;i++){
			orderProduct = childOrder.orderProductList[i];
			tmpStr += '<a href="/order/order-detail?orderCode='+childOrder.orderCode+'"><div class="info-bottom-img"><img src="'+orderProduct.skuImg+'" /></div><p class="bottom-1" style="font-size:16px;">'+orderProduct.productName+'</p><p class="bottom-3"><span style="color:#F389AA;font-size:16px;">￥'+(orderProduct.skuPrice/100).toFixed(2)+'</span></p><div class="bottom-4"><div class="bottom-number">数量x'+orderProduct.buyNum+'</div></div></a><br/>';
		}
		tmpStr += '<div style="height:40px;"><a href="/order/return-money?orderCode='+childOrder.orderCode+'" class="btn-select"><div style="float:right;padding-left:5px;padding-right:10px;font-size:18px;line-height:30px;margin-right:19px;" class="bg1">退款</div></a></div></div></div></div>';
	}else if(childOrder.status=='YFH'){
		tmpStr += '<div class="order-info2"><div class="info-content"><div class="info-bottom"><div class="titleb">供应商：'+childOrder.supName+'<span>已发货<span></div>';
		for(var i=0;i<childOrder.orderProductList.length;i++){
			orderProduct = childOrder.orderProductList[i];
			tmpStr += '<a href="/order/order-detail?orderCode='+childOrder.orderCode+'"><div class="info-bottom-img"><img src="'+orderProduct.skuImg+'" /></div><p class="bottom-1" style="font-size:16px;">'+orderProduct.productName+'</p><p class="bottom-3"><span style="color:#F389AA;font-size:16px;">￥'+(orderProduct.skuPrice/100).toFixed(2)+'</span></p><div class="bottom-4"><div class="bottom-number">数量x'+orderProduct.buyNum+'</div><a href="/order/rg?o='+childOrder.orderCode+'&p='+orderProduct.ID+'" class="btn-select"><div class="bottom-number-right bg1" style="font-size:18px;">退货</div></a> </div></a><br/>';
		}
		tmpStr += '</div></div></div>';
	}else if(childOrder.status=='STK'){
		tmpStr += '<div class="order-info4"><div class="info-content"><div class="info-bottom"><div class="titleb">供应商：'+childOrder.supName+'<span>退款审核中<span></div>';
		for(var i=0;i<childOrder.orderProductList.length;i++){
			orderProduct = childOrder.orderProductList[i];
			tmpStr += '<a href="/order/order-detail?orderCode='+childOrder.orderCode+'"><div class="info-bottom-img"><img src="'+orderProduct.skuImg+'" /></div><p class="bottom-1" style="font-size:16px;">'+orderProduct.productName+'</p><p class="bottom-3"><span style="color:#F389AA;font-size:16px;">￥'+(orderProduct.skuPrice/100).toFixed(2)+'</span></p><div class="bottom-4"><div class="bottom-number">数量x'+orderProduct.buyNum+'</div></div></a><br/>';
		}
		tmpStr += '</div></div></div>';
	}else if(childOrder.status=='TKZ'){
		tmpStr += '<div class="order-info4"><div class="info-content"><div class="info-bottom"><div class="titleb">供应商：'+childOrder.supName+'<span>退款中<span></div>';
		for(var i=0;i<childOrder.orderProductList.length;i++){
			orderProduct = childOrder.orderProductList[i];
			tmpStr += '<a href="/order/order-detail?orderCode='+childOrder.orderCode+'"><div class="info-bottom-img"><img src="'+orderProduct.skuImg+'" /></div><p class="bottom-1" style="font-size:16px;">'+orderProduct.productName+'</p><p class="bottom-3"><span style="color:#F389AA;font-size:16px;">￥'+(orderProduct.skuPrice/100).toFixed(2)+'</span></p><div class="bottom-4"><div class="bottom-number">数量x'+orderProduct.buyNum+'</div></div></a><br/>';
		}
		tmpStr += '</div></div></div>';
	}else if(childOrder.status=='TKC'){
		tmpStr += '<div class="order-info4"><div class="info-content"><div class="info-bottom"><div class="titleb">供应商：'+childOrder.supName+'<span>退款成功<span></div>';
		for(var i=0;i<childOrder.orderProductList.length;i++){
			orderProduct = childOrder.orderProductList[i];
			tmpStr += '<a href="/order/order-detail?orderCode='+childOrder.orderCode+'"><div class="info-bottom-img"><img src="'+orderProduct.skuImg+'" /></div><p class="bottom-1" style="font-size:16px;">'+orderProduct.productName+'</p><p class="bottom-3"><span style="color:#F389AA;font-size:16px;">￥'+(orderProduct.skuPrice/100).toFixed(2)+'</span></p><div class="bottom-4"><div class="bottom-number">数量x'+orderProduct.buyNum+'</div></div></a><br/>';
		}
		tmpStr += '</div></div></div>';
	}else if(childOrder.status=='TKB'){
		tmpStr += '<div class="order-info4"><div class="info-content"><div class="info-bottom"><div class="titleb">供应商：'+childOrder.supName+'<span>退款审核不通过<span></div>';
		for(var i=0;i<childOrder.orderProductList.length;i++){
			orderProduct = childOrder.orderProductList[i];
			tmpStr += '<a href="/order/order-detail?orderCode='+childOrder.orderCode+'"><div class="info-bottom-img"><img src="'+orderProduct.skuImg+'" /></div><p class="bottom-1" style="font-size:16px;">'+orderProduct.productName+'</p><p class="bottom-3"><span style="color:#F389AA;font-size:16px;">￥'+(orderProduct.skuPrice/100).toFixed(2)+'</span></p><div class="bottom-4"><div class="bottom-number">数量x'+orderProduct.buyNum+'</div></div></a><br/>';
		}
		tmpStr += '<div style="height:40px;"><a href="/order/return-money?orderCode='+childOrder.orderCode+'" class="btn-select"><div style="float:right;padding-left:5px;padding-right:10px;font-size:15px;line-height:30px;margin-right:19px;" class="bg1">重新申请</div></a></div></div></div></div>';
	}else if(childOrder.status=='TKS'){
		tmpStr += '<div class="order-info4"><div class="info-content"><div class="info-bottom"><div class="titleb">供应商：'+childOrder.supName+'<span>退款失败<span></div>';
		for(var i=0;i<childOrder.orderProductList.length;i++){
			orderProduct = childOrder.orderProductList[i];
			tmpStr += '<a href="/order/order-detail?orderCode='+childOrder.orderCode+'"><div class="info-bottom-img"><img src="'+orderProduct.skuImg+'" /></div><p class="bottom-1" style="font-size:16px;">'+orderProduct.productName+'</p><p class="bottom-3"><span style="color:#F389AA;font-size:16px;">￥'+(orderProduct.skuPrice/100).toFixed(2)+'</span></p><div class="bottom-4"><div class="bottom-number">数量x'+orderProduct.buyNum+'</div></div></a><br/>';
		}
		tmpStr += '</div></div></div>';
	}else if(childOrder.status=='TH'){
		tmpStr += '<div class="order-info-all"><div class="info-content"><div class="info-bottom"><div class="titleb">供应商：'+childOrder.supName+'<span><span></div>';
		for(var i=0;i<childOrder.orderProductList.length;i++){
			orderProduct = childOrder.orderProductList[i];
			tmpStr += '<a href="/order/order-detail?orderCode='+childOrder.orderCode+'"><div class="info-bottom-img"><img src="'+orderProduct.skuImg+'" /></div><p class="bottom-1" style="font-size:16px;">'+orderProduct.productName+'</p><p class="bottom-3" style="display:inline-block;"><span style="color:#F389AA;font-size:16px;">￥'+(orderProduct.skuPrice/100).toFixed(2)+'</span></p><p style="float:right;color:red;font-size:12px;">'+orderProduct.statuStr+'</p><div class="bottom-4"><div class="bottom-number">数量x'+orderProduct.buyNum+'</div></div></a><br/>';
		}
		tmpStr += '</div></div></div>';
	}
	
	return tmpStr;
}