package com.xw.restful.aspect;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.xw.restful.constant.ErrorCodeEnum;
import com.xw.restful.exceptions.BizException;
import com.xw.restful.stdo.APIResult;
import com.xw.restful.utils.HttpRequestUtils;
import com.xw.restful.utils.NetUtils;

@Aspect
@Component
public class ControllerAspect {
	private static Logger logger = Logger.getLogger(ControllerAspect.class);
	public long startTimeStamp;
	public long endTimeStamp;
	Map<String, Object> requestInfosMap = null;

	// 切点
	@Pointcut("execution(public * com.xw.restful.controller..*(..))")
	public void log() {

	}
	
	@Before("log()")
	public void doBefore(JoinPoint joinPoint) {
		startTimeStamp = System.currentTimeMillis();
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		requestInfosMap = getRequestInfos(request);
		String classMethod = getClassMethod(joinPoint);
		requestInfosMap.put("class_method", classMethod);
		requestInfosMap.put("thread_id", Thread.currentThread().getId());
		
//		String token = request.getHeader("accessToken"); 
//		requestInfosMap.put("accessToken:", token);
		logger.info("begin-requestInfosMap:"+requestInfosMap);
	}
	
	@Around("log()")
	public Object around(ProceedingJoinPoint point) throws Throwable {
		logger.info("异常处理");
		// TODO 处理操作
		Object result = null; 
        try {
            result = point.proceed();
        } catch (BizException e) {
        	return new APIResult(e.getCode(), e.getMsg());
        }catch (Throwable e) {
            e.printStackTrace();
            return new APIResult(ErrorCodeEnum.SYSTEM_ERROR.getCode(), ErrorCodeEnum.SYSTEM_ERROR.getMsg());
        }  
		return result;
	}

	@After("log()")
	public void doAfter() {
		endTimeStamp =  System.currentTimeMillis();
		requestInfosMap.put("timeTake(ms)", endTimeStamp-startTimeStamp);
		logger.info("end-requestInfosMap:"+requestInfosMap);
	}
	
	private String getClassMethod(JoinPoint joinPoint) {
		return joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName();
	}
	
	private Map<String, Object> getRequestInfos(HttpServletRequest request) {
		String token = request.getHeader("accessToken"); 
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		map.put("start_time", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()));
		map.put("client_iP", NetUtils.getLocalAddress());
		map.put("request_method", request.getMethod());
		map.put("data", HttpRequestUtils.getRequestParamters(request));
		map.put("accessToken", token);
		return map;
	}
	
}
