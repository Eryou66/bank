package com.dz.api;

import com.dz.context.RequestContext;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;

/**
 * 统一响应体。traceId 自动从当前请求上下文取，便于全链路溯源。
 * M1 会在此基础上补 retryAfter 字段（限流 429 响应要求，见文档 2.1.2 第 2 步）。
 */
@Getter
@ToString
public class R<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int code;
    private final String message;
    private final T data;
    private final String traceId;
    private final long timestamp;

    private R(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.traceId = RequestContext.getTraceId();
        this.timestamp = System.currentTimeMillis();
    }

    public static <T> R<T> ok() {
        return new R<>(ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMessage(), null);
    }

    public static <T> R<T> ok(T data) {
        return new R<>(ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMessage(), data);
    }

    public static <T> R<T> fail(ErrorCode errorCode) {
        return new R<>(errorCode.getCode(), errorCode.getMessage(), null);
    }

    public static <T> R<T> fail(ErrorCode errorCode, String message) {
        return new R<>(errorCode.getCode(), message, null);
    }
}
