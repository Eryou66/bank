package com.dz.security.step;

import com.dz.api.ErrorCode;
import com.dz.exception.BizException;
import com.dz.pii.PiiLevel;
import com.dz.pii.PiiType;
import com.dz.security.SecurityContext;
import com.dz.security.SecurityStep;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class PiiCheckStep implements SecurityStep {

    /** 阻断时机在 RAG 检索之前，确保敏感信息绝不进入大模型生成阶段 */
    @Override public int order() { return 5; }
    @Override public String name() { return "PII 分级检测与脱敏"; }
    @Override public ErrorCode errorCode() { return ErrorCode.PII_BLOCKED; }

    @Override
    public void check(SecurityContext context) {
        String query = context.getQuery();
        PiiLevel highest = PiiLevel.LOW;

        for(PiiType type :PiiType.values()){
            Matcher matcher = Pattern.compile(type.getRegex()).matcher(query);
            StringBuffer result = new StringBuffer();
            boolean matched = false;

            while (matcher.find()) {
                if (isBusinessNumber(query, matcher)){
                    continue;
                }
                matched = true;
                if (type.getLevel() == PiiLevel.CRITICAL){
                    log.warn("[安检] PII CRITICAL 级命中，阻断请求：type={}", type);
                    throw new BizException(errorCode(), "涉及敏感信息，请通过官方渠道核实");
                }
                matcher.appendReplacement(result, Matcher.quoteReplacement(mask(type, matcher.group())));
            }
            if (matched){
                matcher.appendTail(result);
                query = result.toString();
                if (type.getLevel().ordinal() < highest.ordinal()) {
                    highest = type.getLevel();
                }
            }
        }

        context.setQuery(query);
        context.setPiiLevel(highest.name());
    }

    /** 紧跟金额 / 期限单位的是业务数字， 不算证件号 */
    private boolean isBusinessNumber(String query, Matcher matcher){
        int end = matcher.end();
        return end < query.length() && PiiType.BUSINESS_UNIT_HINTS.indexOf(query.charAt(end)) > 0;
    }

    private String mask(PiiType type, String value) {
        return switch (type) {
            case PHONE -> value.length() == 11 ? value.substring(0, 3) + "****" + value.substring(7) : "****";
            case BANK_CARD ->
                    value.length() >= 8 ? value.substring(0, 4) + "*".repeat(value.length() - 8) + value.substring(value.length() - 4) : "****";
            case EMAIL -> {
                int at = value.indexOf('@');
                yield at <= 1 ? "*" + value.substring(at) : value.charAt(0) + "***" + value.substring(at);
            }
            default -> "****";
        };
    }
}
