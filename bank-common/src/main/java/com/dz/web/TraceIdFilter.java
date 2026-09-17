package com.dz.web;

import com.dz.context.RequestContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdFilter extends OncePerRequestFilter {

    public static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String MDC_TRACE_ID = "traceId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String traceId = request.getHeader(TRACE_ID_HEADER);
        if (!StringUtils.hasText(traceId)) {
            traceId = UUID.randomUUID().toString().replace("-", "");
        }

        RequestContext ctx = RequestContext.init();
        ctx.setTraceId(traceId);
        ctx.setUserId(request.getHeader("X-User-Id"));
        ctx.setSessionId(request.getHeader("X-Session-Id"));
        ctx.setDeviceId(request.getHeader("X-Device-Id"));
        ctx.setClientIp(resolveClientIp(request));

        MDC.put(MDC_TRACE_ID, traceId);
        response.setHeader(TRACE_ID_HEADER, traceId);

        try{
            filterChain.doFilter(request, response);
        }finally {
            MDC.remove(MDC_TRACE_ID);
            RequestContext.clear();
        }
    }

    /**
     * 优先取代理头，Nginx转发后remoteAddr会是网关IP
     */
    private String resolveClientIp(HttpServletRequest request){
        String ip = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {
            int idx = ip.indexOf(',');
            return idx > 0 ? ip.substring(0, idx).trim() : ip.trim();
        }
        ip = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(ip) && !"unknwon".equalsIgnoreCase(ip)){
            return ip.trim();
        }
        return request.getRemoteAddr();
    }
}
