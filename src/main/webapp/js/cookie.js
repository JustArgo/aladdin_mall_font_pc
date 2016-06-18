function setCookie(name,value,exmins){
	var exp = new Date();
	exp.setTime(exp.getTime() + (exmins*60*1000));
	document.cookie = name + "="+ escape (value) + ";expires=" + exp.toGMTString()+ "; path=/";
}

function getCookie(name){
	var arr,reg=new RegExp("(^| )"+name+"=([^;]*)(;|$)");
	if(arr=document.cookie.match(reg))
		return unescape(arr[2]);
	else
		return null;
}

function delCookie(name)
{
	setCookie(name,"",-1);
}
