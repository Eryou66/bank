package com.dz.api;

import lombok.Getter;

/**
 * 全局错误码
 */
@Getter
public enum ErrorCode {

    SUCCESS(0, 200, "成功"),

    // 七步安检流水线
    QUERY_FORMAT_INVALID(4001, 400, "输入长度不合法，长度需要2~200字符"),
    RATE_LIMITED(4002, 429, "请求过于频繁，请稍后再试"),
    PROMPT_INJECTION_DETECTED(4003, 400, "输入内容不合规，请重新描述您的问题"),
    SENSITIVE_CONTENT_DETECTED(4004, 400, "输入包含违规内容，请重新描述您的问题"),
    PII_BLOCKED(4005, 400, "涉及敏感信息，请通过官方渠道核实"),
    TERM_NORMALIZE_FAILED(4006, 400, "输入内容无法识别，请换一种说法"),
    QUERY_NORMALIZE_FAILED(4007, 400, "输入内容无法识别，请换一种说法"),

    // ===== 通用（占位，非文档定义）=====
    PARAM_INVALID(4000, 400, "参数不合法"),
    SYSTEM_ERROR(5000, 500, "系统繁忙，请稍后重试"),
    SERVICE_UNAVAILABLE(5003, 503, "服务暂时不可用，请稍后重试");


    private final int code;
    private final int httpStatus;
    private final String message;

    ErrorCode(int code, int httpStatus, String message) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
