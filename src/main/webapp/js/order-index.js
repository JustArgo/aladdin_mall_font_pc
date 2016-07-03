function formatDate(strTime) {
    var date = new Date(strTime);
    return date.getFullYear()+"-"+(date.getMonth()+1)+"-"+date.getDate();
}
function getItemStr(childOrder){
	var tmpStr = "";
	var orderProduct = {};
	if(childOrder.status=='COM'){
		tmpStr = tmpStr+'<tr class="status"><td colspan="6" class="text-left"><span>'+formatDate(childOrder.createTime)+'</span><span>订单编号：'+childOrder.orderCode+'</span><span>供应商：'+childOrder.supName+'</span></td></tr>';
		
		for(var i=0;i<childOrder.orderProductList.length;i++){
			orderProduct = childOrder.orderProductList[i];
			
			tmpStr += '<tr><td class="text-left"><a href="javascript:;" class="fl"><img src="'+orderProduct.skuImg+'" style="width:121px;height:120px;"></a><div class="fr"><p><a href="javascript:;">'+orderProduct.productName+'</a></p><p>';
			for(var j=0;j<orderProduct.skuStrs.length;j++){
				tmpStr += '<span>'+orderProduct.skuStrs[j]+'</span>&nbsp;&nbsp;';
			}
			
			tmpStr += '</p></div></td><td>¥'+(orderProduct.skuPrice/100).toFixed(2)+'</td><td>'+orderProduct.buyNum+'</td>';
			
			if(i==0){
				tmpStr += '<td rowspan="'+childOrder.orderProductList.length+'" class="status"><p>¥'+(childOrder.orderSum/100.0).toFixed(2)+'</p><p>（含运费¥'+(childOrder.postFee/100).toFixed(2)+'）</p></td><td rowspan="'+childOrder.orderProductList.length+'" class="status">已完成<a href="/order/order-detail-com?orderCode='+childOrder.orderCode+'">订单详情</a></td><td rowspan="'+childOrder.orderProductList.length+'" class="status"></td>';
			}
			
			tmpStr += '</tr>';
		}
	}else if(childOrder.status=='CAN'){
		tmpStr = tmpStr+'<tr class="status"><td colspan="6" class="text-left"><span>'+formatDate(childOrder.createTime)+'</span><span>订单编号：'+childOrder.orderCode+'</span><span>供应商：'+childOrder.supName+'</span></td></tr>';
		
		for(var i=0;i<childOrder.orderProductList.length;i++){
			orderProduct = childOrder.orderProductList[i];
			
			tmpStr += '<tr><td class="text-left"><a href="javascript:;" class="fl"><img src="'+orderProduct.skuImg+'" style="width:121px;height:120px;"></a><div class="fr"><p><a href="javascript:;">'+orderProduct.productName+'</a></p><p>';
			for(var j=0;j<orderProduct.skuStrs.length;j++){
				tmpStr += '<span>'+orderProduct.skuStrs[j]+'</span>&nbsp;&nbsp;';
			}
			
			tmpStr += '</p></div></td><td>¥'+(orderProduct.skuPrice/100).toFixed(2)+'</td><td>'+orderProduct.buyNum+'</td>';
			
			if(i==0){
				tmpStr += '<td rowspan="'+childOrder.orderProductList.length+'" class="status"><p>¥'+(childOrder.orderSum/100.0).toFixed(2)+'</p><p>（含运费¥'+(childOrder.postFee/100).toFixed(2)+'）</p></td><td rowspan="'+childOrder.orderProductList.length+'" class="status">已取消<a href="/order/order-detail-can?orderCode='+childOrder.orderCode+'">订单详情</a></td><td rowspan="'+childOrder.orderProductList.length+'" class="status"></td>';
			}
			
			tmpStr += '</tr>';
		}
	}else if(childOrder.status=='DFK'){
		tmpStr += '<tr class="status"><td colspan="6" class="text-left"><span>'+formatDate(childOrder.createTime)+'</span><span>订单编号：'+childOrder.parentCode+'</span><span class="time">'+((childOrder.remainTime=='OUT_OF_DATE')?'订单已过付款时间':('还剩'+childOrder.remainTime+'可以进行付款'))+'</span></td></tr>';
		for(var i=0;i<childOrder.orderProductList.length;i++){
			orderProduct = childOrder.orderProductList[i];
			
			tmpStr += '<tr><td class="text-left"><a href="javascript:;" class="fl"><img src="'+orderProduct.skuImg+'" style="width:121px;height:120px;"></a><div class="fr"><p><a href="javascript:;">'+orderProduct.productName+'</a></p><p>';
			
			for(var j=0;j<orderProduct.skuStrs.length;j++){
				tmpStr += '<span>'+orderProduct.skuStrs[j]+'</span>&nbsp;&nbsp;';
			}
			
			tmpStr += '</p></div></td><td>¥'+(orderProduct.skuPrice/100).toFixed(2)+'</td><td>'+orderProduct.buyNum+'</td>';
			
			if(i==0){
				tmpStr += '<td rowspan="'+childOrder.orderProductList.length+'" class="status"><p>¥'+(childOrder.orderSum/100.0).toFixed(2)+'</p><p>（含运费¥'+(childOrder.postFee/100).toFixed(2)+'）</p></td><td rowspan="'+childOrder.orderProductList.length+'" class="status">待付款<a href="/order/order-detail?orderCode='+childOrder.parentCode+'">订单详情</a><a href="/order/cancel?orderCode='+childOrder.parentCode+'" data-toggle="modal" data-target="#myModal" class="jq-orderCancel">取消订单</a></td><td rowspan="'+childOrder.orderProductList.length+'" class="status">';
				
				if(childOrder.remainTime!='OUT_OF_DATE'){
					tmpStr += '<a href="/order/pay-again?orderCode='+childOrder.parentCode+'" class="pay-money">付款</a>';
				}
				
				tmpStr += '</td>';
				
			}
			
			tmpStr += '</tr>';
		}
	}else if(childOrder.status=='DFH'){
		tmpStr += '<tr class="status"><td colspan="6" class="text-left"><span>'+formatDate(childOrder.createTime)+'</span><span>订单编号：'+childOrder.orderCode+'</span><span>供应商：'+childOrder.supName+'</span></td></tr>';
		/*
		childOrder.orderProductList[1] = new Object();
		childOrder.orderProductList[1].skuImg = "http://www.baidu.com";
		childOrder.orderProductList[1].productName="一个商品";
		childOrder.orderProductList[1].skuStrs=[];
		childOrder.orderProductList[1].skuStrs[0] = "规格:红色";
		childOrder.orderProductList[1].skuPrice=35533;
		childOrder.orderProductList[1].buyNum=3;
		*/
		for(var i=0;i<childOrder.orderProductList.length;i++){
			orderProduct = childOrder.orderProductList[i];
			
			tmpStr += '<tr><td class="text-left"><a href="javascript:;" class="fl"><img src="'+orderProduct.skuImg+'" style="width:121px;height:120px;"></a><div class="fr"><p><a href="javascript:;">'+orderProduct.productName+'</a></p><p>';
			
			for(var j=0;j<orderProduct.skuStrs.length;j++){
				tmpStr += '<span>'+orderProduct.skuStrs[j]+'</span>&nbsp;&nbsp;';
			}
			
			tmpStr += '</p></div></td><td>¥'+(orderProduct.skuPrice/100).toFixed(2)+'</td><td>'+orderProduct.buyNum+'</td>';
			
			if(i==0){
				tmpStr += '<td rowspan="'+childOrder.orderProductList.length+'"><p>¥'+(childOrder.orderSum/100.0).toFixed(2)+'</p><p>（含运费¥'+(childOrder.postFee/100).toFixed(2)+'）</p></td><td rowspan="'+childOrder.orderProductList.length+'">待发货<a style="display:block;" href="/order/order-detail-dfh?orderCode='+childOrder.orderCode+'">订单详情</a></td><td rowspan="'+childOrder.orderProductList.length+'">';
				
				
				tmpStr += '<a href="/order/return-money?orderCode='+childOrder.orderCode+'" class="pay-money">退款</a>';
				
				
				tmpStr += '</td>';
				
			}
			
			tmpStr += '</tr>';
			
		}
		
	}else if(childOrder.status=='YFH'){
		tmpStr += '<tr class="status"><td colspan="6" class="text-left"><span>'+formatDate(childOrder.createTime)+'</span><span>订单编号：'+childOrder.orderCode+'</span><span>供应商：'+childOrder.supName+'</span></td></tr>';
		
		
		
		for(var i=0;i<childOrder.orderProductList.length;i++){
			orderProduct = childOrder.orderProductList[i];
			
			tmpStr += '<tr><td class="text-left"><a href="javascript:;" class="fl"><img src="'+orderProduct.skuImg+'" style="width:121px;height:120px;"></a><div class="fr"><p><a href="javascript:;">'+orderProduct.productName+'</a></p><p>';
			
			for(var j=0;j<orderProduct.skuStrs.length;j++){
				tmpStr += '<span>'+orderProduct.skuStrs[j]+'</span>&nbsp;&nbsp;';
			}
			
			tmpStr += '</p></div></td><td>¥'+(orderProduct.skuPrice/100).toFixed(2)+'</td><td>'+orderProduct.buyNum+'</td>';
			
			if(i==0){
				tmpStr += '<td rowspan="'+childOrder.orderProductList.length+'"><p>¥'+(childOrder.orderSum/100.0).toFixed(2)+'</p><p>（含运费¥'+(childOrder.postFee/100).toFixed(2)+'）</p></td><td rowspan="'+childOrder.orderProductList.length+'">已发货<a style="display:block;" href="/order/order-detail-yfh?orderCode='+childOrder.orderCode+'">订单详情</a></td>';
			}
			
			tmpStr += '<td><a href="/order/rg?o='+childOrder.orderCode+'&p='+orderProduct.ID+'" class="pay-money">退货</a></td></tr>';
			
		}
	}else if(childOrder.status=='STK'){
		tmpStr += '<tr class="status"><td colspan="6" class="text-left"><span>'+formatDate(childOrder.createTime)+'</span><span>订单编号：'+childOrder.orderCode+'</span><span>供应商：'+childOrder.supName+'</span></td></tr>';
		
		for(var i=0;i<childOrder.orderProductList.length;i++){
			orderProduct = childOrder.orderProductList[i];
			tmpStr += '<tr><td class="text-left"><a href="javascript:;" class="fl"><img src="'+orderProduct.skuImg+'" style="width:121px;height:120px;"></a><div class="fr"><p><a href="javascript:;">'+orderProduct.productName+'</a></p><p>';
			
			for(var j=0;j<orderProduct.skuStrs.length;j++){
				tmpStr += '<span>'+orderProduct.skuStrs[j]+'</span>&nbsp;&nbsp;';
			}
			
			tmpStr += '</p></div></td><td>¥'+(orderProduct.skuPrice/100).toFixed(2)+'</td><td>'+orderProduct.buyNum+'</td>';
			if(i==0){
				tmpStr += '<td rowspan="'+childOrder.orderProductList.length+'"><p>¥'+(childOrder.orderSum/100.0).toFixed(2)+'</p><p>（含运费¥'+(childOrder.postFee/100).toFixed(2)+'）</p></td><td rowspan="'+childOrder.orderProductList.length+'">退款审核中<a style="display:block;" href="/order/order-detail?orderCode='+childOrder.orderCode+'">订单详情</a></td><td rowspan="'+childOrder.orderProductList.length+'"></td>';
			}
		}
		
	}else if(childOrder.status=='TKZ'){
		tmpStr += '<tr class="status"><td colspan="6" class="text-left"><span>'+formatDate(childOrder.createTime)+'</span><span>订单编号：'+childOrder.orderCode+'</span><span>供应商：'+childOrder.supName+'</span></td></tr>';
		
		
		
		for(var i=0;i<childOrder.orderProductList.length;i++){
			orderProduct = childOrder.orderProductList[i];
			tmpStr += '<tr><td class="text-left"><a href="javascript:;" class="fl"><img src="'+orderProduct.skuImg+'" style="width:121px;height:120px;"></a><div class="fr"><p><a href="javascript:;">'+orderProduct.productName+'</a></p><p>';
			
			for(var j=0;j<orderProduct.skuStrs.length;j++){
				tmpStr += '<span>'+orderProduct.skuStrs[j]+'</span>&nbsp;&nbsp;';
			}
			
			tmpStr += '</p></div></td><td>¥'+(orderProduct.skuPrice/100).toFixed(2)+'</td><td>'+orderProduct.buyNum+'</td>';
			if(i==0){
				tmpStr += '<td rowspan="'+childOrder.orderProductList.length+'"><p>¥'+(childOrder.orderSum/100.0).toFixed(2)+'</p><p>（含运费¥'+(childOrder.postFee/100).toFixed(2)+'）</p></td><td rowspan="'+childOrder.orderProductList.length+'">退款中<a style="display:block;" href="/order/order-detail?orderCode='+childOrder.orderCode+'">订单详情</a></td><td rowspan="'+childOrder.orderProductList.length+'"></td>';
			}
		}
	}else if(childOrder.status=='TKC'){
		
		
		
		tmpStr += '<tr class="status"><td colspan="6" class="text-left"><span>'+formatDate(childOrder.createTime)+'</span><span>订单编号：'+childOrder.orderCode+'</span><span>供应商：'+childOrder.supName+'</span></td></tr>';
		
		for(var i=0;i<childOrder.orderProductList.length;i++){
			orderProduct = childOrder.orderProductList[i];
			tmpStr += '<tr><td class="text-left"><a href="javascript:;" class="fl"><img src="'+orderProduct.skuImg+'" style="width:121px;height:120px;"></a><div class="fr"><p><a href="javascript:;">'+orderProduct.productName+'</a></p><p>';
			
			for(var j=0;j<orderProduct.skuStrs.length;j++){
				tmpStr += '<span>'+orderProduct.skuStrs[j]+'</span>&nbsp;&nbsp;';
			}
			
			tmpStr += '</p></div></td><td>¥'+(orderProduct.skuPrice/100).toFixed(2)+'</td><td>'+orderProduct.buyNum+'</td>';
			if(i==0){
				tmpStr += '<td rowspan="'+childOrder.orderProductList.length+'"><p>¥'+(childOrder.orderSum/100.0).toFixed(2)+'</p><p>（含运费¥'+(childOrder.postFee/100).toFixed(2)+'）</p></td><td rowspan="'+childOrder.orderProductList.length+'">退款成功<a style="display:block;" href="/order/order-detail?orderCode='+childOrder.orderCode+'">订单详情</a></td><td rowspan="'+childOrder.orderProductList.length+'"></td>';
			}
		}
	}else if(childOrder.status=='TKB'){
		tmpStr += '<tr class="status"><td colspan="6" class="text-left"><span>'+formatDate(childOrder.createTime)+'</span><span>订单编号：'+childOrder.orderCode+'</span><span>供应商：'+childOrder.supName+'</span></td></tr>';
		
		for(var i=0;i<childOrder.orderProductList.length;i++){
			orderProduct = childOrder.orderProductList[i];
			tmpStr += '<tr><td class="text-left"><a href="javascript:;" class="fl"><img src="'+orderProduct.skuImg+'" style="width:121px;height:120px;"></a><div class="fr"><p><a href="javascript:;">'+orderProduct.productName+'</a></p><p>';
			
			for(var j=0;j<orderProduct.skuStrs.length;j++){
				tmpStr += '<span>'+orderProduct.skuStrs[j]+'</span>&nbsp;&nbsp;';
			}
			
			tmpStr += '</p></div></td><td>¥'+(orderProduct.skuPrice/100).toFixed(2)+'</td><td>'+orderProduct.buyNum+'</td>';
			if(i==0){
				tmpStr += '<td rowspan="'+childOrder.orderProductList.length+'"><p>¥'+(childOrder.orderSum/100.0).toFixed(2)+'</p><p>（含运费¥'+(childOrder.postFee/100).toFixed(2)+'）</p></td><td rowspan="'+childOrder.orderProductList.length+'">退款审核不通过<a style="display:block;" href="/order/order-detail?orderCode='+childOrder.orderCode+'">订单详情</a></td><td rowspan="'+childOrder.orderProductList.length+'"><a href="/order/return-money?orderCode='+childOrder.orderCode+'" class="pay-money">重新申请</a></td>';
			}
		}
	}else if(childOrder.status=='TKS'){
		tmpStr += '<tr class="status"><td colspan="6" class="text-left"><span>'+formatDate(childOrder.createTime)+'</span><span>订单编号：'+childOrder.orderCode+'</span><span>供应商：'+childOrder.supName+'</span></td></tr>';
		
		for(var i=0;i<childOrder.orderProductList.length;i++){
			orderProduct = childOrder.orderProductList[i];
			tmpStr += '<tr><td class="text-left"><a href="javascript:;" class="fl"><img src="'+orderProduct.skuImg+'" style="width:121px;height:120px;"></a><div class="fr"><p><a href="javascript:;">'+orderProduct.productName+'</a></p><p>';
			
			for(var j=0;j<orderProduct.skuStrs.length;j++){
				tmpStr += '<span>'+orderProduct.skuStrs[j]+'</span>&nbsp;&nbsp;';
			}
			
			tmpStr += '</p></div></td><td>¥'+(orderProduct.skuPrice/100).toFixed(2)+'</td><td>'+orderProduct.buyNum+'</td>';
			if(i==0){
				tmpStr += '<td rowspan="'+childOrder.orderProductList.length+'"><p>¥'+(childOrder.orderSum/100.0).toFixed(2)+'</p><p>（含运费¥'+(childOrder.postFee/100).toFixed(2)+'）</p></td><td rowspan="'+childOrder.orderProductList.length+'">退款失败<a style="display:block;" href="/order/order-detail?orderCode='+childOrder.orderCode+'">订单详情</a></td><td rowspan="'+childOrder.orderProductList.length+'"></td>';
			}
		}
	}else if(childOrder.status=='TH'){
		tmpStr += '<tr class="status"><td colspan="6" class="text-left"><span>'+formatDate(childOrder.createTime)+'</span><span>订单编号：'+childOrder.orderCode+'</span><span>供应商：'+childOrder.supName+'</span></td></tr>';
		
		for(var i=0;i<childOrder.orderProductList.length;i++){
			orderProduct = childOrder.orderProductList[i];
			tmpStr += '<tr><td class="text-left"><a href="javascript:;" class="fl"><img src="'+orderProduct.skuImg+'" style="width:121px;height:120px;"></a><div class="fr"><p><a href="javascript:;">'+orderProduct.productName+'</a></p><p>';
			
			for(var j=0;j<orderProduct.skuStrs.length;j++){
				tmpStr += '<span>'+orderProduct.skuStrs[j]+'</span>&nbsp;&nbsp;';
			}
			
			tmpStr += '</p></div></td><td>¥'+(orderProduct.skuPrice/100).toFixed(2)+'</td><td>'+orderProduct.buyNum+'</td>';
			if(i==0){
				tmpStr += '<td rowspan="'+childOrder.orderProductList.length+'"><p>¥'+(childOrder.orderSum/100.0).toFixed(2)+'</p><p>（含运费¥'+(childOrder.postFee/100).toFixed(2)+'）</p></td>';
			}
			
			console.log(orderProduct);
			
			if(orderProduct.statuStr){
				tmpStr += '<td>'+orderProduct.statuStr+'<a style="display:block;" href="/order/return-goods-detail-orderproductid?orderProductID='+orderProduct.ID+'">退货详情</a></td><td></td>';
			}else{
				tmpStr += '<td></td><td><a class="pay-money" href="/order/rg?o='+childOrder.orderCode+'&p='+orderProduct.ID+'">退货</a></td>';
			}
			
		}
	}
	
	return tmpStr;
}

/**
 * 组装分页按钮
 * page为 总页数
 */
function pagination(page){
	
	if(page==0){
		return "";
	}
	
	var pageStr = '<a href="javascript:;" class="prev">上一页</a>';
	
	if(page<=4){
		for(var i=1;i<=page;i++){
			if(i==1){
				pageStr += '<a href="javascript:;" class="num active">'+i+'</a>';
			}else{
				pageStr += '<a href="javascript:;" class="num">'+i+'</a>';
			}
		}
	}else{
		for(var i=1;i<=3;i++){
			if(i==1){
				pageStr += '<a href="javascript:;" class="num active">'+i+'</a>';
			}else{
				pageStr += '<a href="javascript:;" class="num">'+i+'</a>';
			}
		}
		pageStr += '<span class="etc">...</span><a href="javascript:;" class="num">'+page+'</a>';
		
	}
	
	pageStr += '<a href="javascript:;" class="next">下一页</a>';
	
	return pageStr;
	
}

/**
 * 自定义分页按钮
 * pages 为数组 [ 1 2 3 0 18]  0代表...
 */
function paginationDefined(pages){
	
	var pageStr = '<a href="javascript:;" class="prev">上一页</a>';
	
	for(var i=0;i<pages.length;i++){
		if(pages[i]==0){
			pageStr += '<span class="etc">...</span>';
		}else{
			pageStr += '<a href="javascript:" class="num">'+pages[i]+'</a>';
		}
	}
	
	pageStr += '<a href="javascript:;" class="next">下一页</a>';
	
	return pageStr;
			
}

