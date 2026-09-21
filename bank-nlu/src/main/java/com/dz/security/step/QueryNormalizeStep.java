package com.dz.security.step;

import com.dz.api.ErrorCode;
import com.dz.security.SecurityContext;
import com.dz.security.SecurityStep;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Map;

@Component
public class QueryNormalizeStep implements SecurityStep {


    /** 常见错别字修正（基于银行业务词库，按需扩充） */
    private static final Map<String, String> TYPO_MAP = Map.of(
            "登陆", "登录",
            "帐号", "账号",
            "密吗", "密码"
    );


    @Override
    public int order() {
        return 7;
    }

    @Override
    public String name() {
        return "Query 规范化输出";
    }

    @Override
    public ErrorCode errorCode() {
        return ErrorCode.QUERY_NORMALIZE_FAILED;
    }

    @Override
    public void check(SecurityContext context) {
        String query = toHalfWidth(context.getQuery());
        query = query.toLowerCase();
        query = query.replaceAll("\\s+", " ").trim();
        for(Map.Entry<String, String> typo : TYPO_MAP.entrySet()){
            query = query.replace(typo.getKey(), typo.getValue());
        }
        context.setQuery(query);
    }

    /** 全角转半角 */
    private String toHalfWidth(String input){
        char[] chars = input.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] >= 0xFF01 && chars[i] <= 0xFF5E){
                chars[i] = (char) (chars[i] - 0xFEE0);
            } else if(chars[i] == 0x3000){
                chars[i] = ' ';
            }
        }
        return new String(chars);
    }

}
