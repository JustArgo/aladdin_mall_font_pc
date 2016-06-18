package com.mi360.aladdin.mall.aspect;

import java.util.UUID;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;

/**
 * 日志切面
 * 
 * @author JSC
 *
 */
public class LogAspect {

	public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
		Logger logger = LogManager
				.getLogger(joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName());
		String requestId=UUID.randomUUID().toString().replace("-", "");
		Object[] params=joinPoint.getArgs();
		params[0]=requestId;
		try {
			logger.info(">>>>>>>>>> requestId:" + requestId);
			Object o = joinPoint.proceed(params);
			logger.info("<<<<<<<<<< requestId:" + requestId);
			return o;
		} catch (Throwable e) {
			logger.info(e.getMessage(),e);
			logger.info("<<<<<<<<<< requestId:" + requestId);
			throw e;
		}
	}

}
