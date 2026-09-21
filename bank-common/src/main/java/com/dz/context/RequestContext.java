package com.dz.context;

import lombok.Data;

/**
 * 请求级上下文
 */
@Data
public class RequestContext {

    private static final ThreadLocal<RequestContext> HOLDER = new ThreadLocal<>();

    /** 全链路追踪 ID */
    private String traceId;
    /** 用户标识（M1 用户级限流 Key 用） */
    private String userId;
    /** 会话 ID（M2 多轮对话状态机用） */
    private String sessionId;
    /** 设备标识（M1 设备级限流 Key 用） */
    private String deviceId;
    /** 客户端 IP（M1 IP 级限流 Key 用） */
    private String clientIp;
    /** PII 命中最高级别：CRITICAL / HIGH / MEDIUM / LOW */
    private String piiLevel;
    /** 安检规范化后的 Query（术语归一化 + PII 脱敏结果），下游 NLU / RAG 直接用这个 */
    private String normalizedQuery;

    private RequestContext(){}

    /** 请求入口调用，覆盖式初始化 */
    public static RequestContext init() {
        RequestContext ctx = new RequestContext();
        HOLDER.set(ctx);
        return ctx;
    }

    /** 可能为 null（非 Web 线程调用时） */
    public static RequestContext get() {
        return HOLDER.get();
    }

    public static String getTraceId() {
        RequestContext ctx = HOLDER.get();
        return ctx == null ? null : ctx.traceId;
    }

    public static void clear() {
        HOLDER.remove();
    }


}
