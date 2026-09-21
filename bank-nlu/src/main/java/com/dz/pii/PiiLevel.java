package com.dz.pii;

public enum PiiLevel {

    /** 核心敏感信息，泄露造成直接资金风险：直接阻断请求 */
    CRITICAL,
    /** 个人身份核心信息：掩码脱敏替换 */
    HIGH,
    /** 一般个人信息：标记身份，日志中脱敏存储 */
    MEDIUM,
    /** 公开通用信息：不处理，仅记录审计日志 */
    LOW

}
