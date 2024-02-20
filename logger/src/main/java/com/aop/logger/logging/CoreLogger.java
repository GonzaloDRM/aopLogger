package com.aop.logger.logging;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.logging.Log;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Aspect
@Component
@Slf4j
public class CoreLogger {

    private HttpServletRequest request;
    private String headers;
    private String paths;

    public CoreLogger(HttpServletRequest request){
        this.request = request;
    }

    @Pointcut("execution(* com.aop.logger..*.*(..))")
    public void mainPointCut(){};

    @Pointcut("execution(* com.aop.logger.*.*.repository..*.*(..))")
    public void repositoyPointcut(){};

    @Pointcut("execution(* com.aop.logger.*.exceptions..*.*(..))")
    public void exceptionPointcut(){};


    @Before("mainPointCut()")
    public void logginStartPointRequest(){
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes != null){
            request = ((ServletRequestAttributes) attributes).getRequest();
            headers = request.getHeader("headerName");
            paths = request.getServletPath();
        }
    }

    @Around("mainPointcut() && !exceptionPointcut() && !repositoryPointcut()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable{
        long initialTime = System.currentTimeMillis();
        String signature = "";
        String arguments = "";
        boolean includeArgs = false;

        if (joinPoint != null && joinPoint.getSignature() != null){
            signature = joinPoint.getSignature().toShortString();
            MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
            String[] parametersName = methodSignature.getParameterNames();
            Object[] args = joinPoint.getArgs();
            Map<String, String> map = IntStream.range(0, parametersName.length).boxed().collect(Collectors.toMap(
                    i -> parametersName[i], i -> (validateArgs(args[i]))));

            map.entrySet().removeIf(ent -> ent.getValue().isEmpty() || ent.getKey().equals(headers));

            arguments = setArguments(map);
            includeArgs = true;
        }
        return printLog(arguments, signature, joinPoint, includeArgs, initialTime);

    }


    public String validateArgs(Object arg){
        return (arg != null ? arg.toString() : "");
    }

    public String setArguments(Map<String, String> map){
        return map.entrySet().stream().map(entry -> entry.getKey() + ": " + entry.getValue()).collect(Collectors.joining("; "));
    }

    private Object printLog(String arguments, String signature, ProceedingJoinPoint joinPoint, boolean includeArgs, long initialTime) throws Throwable {

        if (!includeArgs) log.info(signature, paths, headers);
        else log.info(signature, paths, headers, arguments);

        Object result = joinPoint.proceed();
        Long elapsedTime = (System.currentTimeMillis() - initialTime);

        if (!includeArgs) log.info(signature, paths, headers, elapsedTime);
        else log.info(signature, paths, headers, arguments, elapsedTime);

        return result;
    }

}
