/**
 * 验证是否为电话号码 以数字开头 以数字结尾 中间可以有多个 - 号 
 * @param num
 * @returns
 */
function isPhoneNum(num){
	var pattern = /^\d{1,}[0-9\-]*\d{1,}$/;
	return pattern.test(num);
}

/**
 * 对两个数进行精确计算
 * @param arg1 
 * @param arg2
 * @returns {Number}
 */
function accMul(arg1,arg2)
{  
	var m=0,s1=arg1.toString(),s2=arg2.toString();  
	try{m+=s1.split(".")[1].length}catch(e){}  
	try{m+=s2.split(".")[1].length}catch(e){}  
	return Number(s1.replace(".",""))*Number(s2.replace(".",""))/Math.pow(10,m)  
}