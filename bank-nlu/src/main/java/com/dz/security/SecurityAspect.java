package com.dz.security;

import com.dz.context.RequestContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * 七步安检的触发点：在 Controller 方法执行前拦截（文档 2.1.1 的设计）。
 * 拦截失败由 GlobalExceptionHandler 统一转成响应体，这里只负责抛异常。
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class SecurityAspect {

    private final SecurityPipeline pipeline;

    @Around("@annotation(com.dz.security.SecurityCheck)")
    public Object SecurityCheck(ProceedingJoinPoint joinPoint) throws Throwable{
        QueryCarrier carrier = findCarrier(joinPoint.getArgs());
        if (carrier == null) {
            return joinPoint.proceed();
        }
        RequestContext rc = RequestContext.get();
        SecurityContext context = new SecurityContext();
        context.setRawQuery(carrier.getQuestion());
        context.setQuery(carrier.getQuestion());
        if(rc != null){
            context.setUserId(rc.getUserId());
            context.setDeviceId(rc.getDeviceId());
            context.setClientIp(rc.getClientIp());
        }

        pipeline.run(context);

        // 安检通过后把规范化结果回写，后续 NLU 与 RAG 拿到的就是归一化 + 脱敏后的 Query
        carrier.setQuestion(context.getQuery());
        if (rc != null) {
            rc.setPiiLevel(context.getPiiLevel());
            rc.setNormalizedQuery(context.getQuery());
        }
        return joinPoint.proceed();
    }

    private QueryCarrier findCarrier(Object[] args) {
        if (args == null) {
            return null;
        }
        for (Object arg : args) {
            if (arg instanceof QueryCarrier carrier) {
                return carrier;
            }
        }
        return null;
    }
}
