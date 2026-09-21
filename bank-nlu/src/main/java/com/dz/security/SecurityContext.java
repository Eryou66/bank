package com.dz.security;

import lombok.Data;


/** 一次安检的上下文，在七个步骤之间流转，每步可以改写 query 或补充标记 */
@Data
public class SecurityContext {

    /** 用户原始提问 */
    private String rawQuery;

    /** 当前处理中的 query，第 5/6/7 步会改写它 */
    private String query;

    private String userId;
    private String deviceId;
    private String clientIp;

    /** PII 命中最高级别，默认 LOW */
    private String piiLevel = "LOW";
    /** L2 统计异常命中的高风险标记，供 L3 二次确认使用 */
    private boolean highRisk;
}
