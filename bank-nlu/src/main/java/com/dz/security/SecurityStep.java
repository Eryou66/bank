package com.dz.security;

import com.dz.api.ErrorCode;

public interface SecurityStep {

    /** 执行顺序 1~7 */
    int order();

    /** 步骤名,日志与监控用 */
    String name();

    /** 该步拦截时的错误码（4001~4007） */
    ErrorCode errorCode();

    /** 执行检查；不通过直接抛 BizException，由全局异常处理器转成统一响应 */
    void check(SecurityContext context);

}
