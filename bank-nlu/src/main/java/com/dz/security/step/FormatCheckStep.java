package com.dz.security.step;

import com.dz.api.ErrorCode;
import com.dz.exception.BizException;
import com.dz.security.SecurityContext;
import com.dz.security.SecurityStep;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

@Component
public class FormatCheckStep implements SecurityStep {

    private static final int MIN_LENGTH = 2;
    private static final int MAX_LENGTH = 200;

    /** 至少要有一个中文、字母或数字，否则视为纯符号 / 纯表情 */
    private static final Pattern HAS_MEANING = Pattern.compile("[\\u4e00-\\u9fa5a-zA-Z0-9]");

    /** SQL 注入基础特征 */
    private static final Pattern SQL_INJECTION = Pattern.compile(
            "(?i)(\\bor\\b\\s+\\d+\\s*=\\s*\\d+|union\\s+select|\\bdrop\\s+table\\b|/\\*|;\\s*--)");

    @Override
    public int order() {
        return 1;
    }

    @Override
    public String name() {
        return "基础格式和合法性校验";
    }

    @Override
    public ErrorCode errorCode() {
        return ErrorCode.QUERY_FORMAT_INVALID;
    }

    @Override
    public void check(SecurityContext context) {
        String query = context.getQuery();
        if (!StringUtils.hasText(query)){
            throw new BizException(errorCode(), "提问内容不能为空");
        }
        query = query.trim();
        if (query.length() < MIN_LENGTH || query.length() > MAX_LENGTH){
            throw new BizException(errorCode(), "提问长度需在2~200字符之间");
        }
        if (!HAS_MEANING.matcher(query).find()){
            throw new BizException(errorCode(), "提问内容不能为纯符号或表情");
        }
        if (SQL_INJECTION.matcher(query).find()){
            throw new BizException(errorCode(), "提问内容不合法");
        }
        if (SQL_INJECTION.matcher(query).find()) {
            throw new BizException(errorCode(), "提问内容不合法");
        }
        context.setQuery(query);


    }
}
