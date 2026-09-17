package com.dz.exception;

import com.dz.api.ErrorCode;
import lombok.Getter;

@Getter
public class BizException extends RuntimeException{

    private static final long serialVersionUID = 1L;

    private final ErrorCode errorCode;

    public BizException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public BizException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public BizException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public static BizException of(ErrorCode errorCode) {
        return new BizException(errorCode);
    }

    /**
     * 业务异常属于正常流程分支，不需要堆栈。
     * 高 QPS 下限流拦截频繁发生，关闭栈采集可省下可观开销。
     */
    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }

}
