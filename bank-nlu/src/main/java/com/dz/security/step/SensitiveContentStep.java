package com.dz.security.step;

import com.dz.algorithm.DfaMatcher;
import com.dz.api.ErrorCode;
import com.dz.exception.BizException;
import com.dz.security.SecurityContext;
import com.dz.security.SecurityStep;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class SensitiveContentStep implements SecurityStep {

    /**
     * 最小种子集：金融违规宣传 + 辱骂占位。
     * 替换成银行合规部门提供的违禁词库即可（文档说约 2000 条）。
     */
    private static final List<String> SENSITIVE_WORDS = List.of(
            "保本保收益", "稳赚不赔", "零风险高收益", "内幕消息", "包过审",
            "傻逼", "滚蛋", "去死"
    );

    private final DfaMatcher dfa = new DfaMatcher(SENSITIVE_WORDS);

    @Override
    public int order() {
        return 4;
    }

    @Override
    public String name() {
        return "违规敏感内容检测";
    }

    @Override
    public ErrorCode errorCode() {
        return ErrorCode.SENSITIVE_CONTENT_DETECTED;
    }

    @Override
    public void check(SecurityContext context) {
        String hit = dfa.findFirst(context.getQuery());
        if (hit != null) {
            log.warn("[安检] 敏感内容命中：{}", hit);
            throw new BizException(errorCode(), "输入包含违规内容， 请重新描述您的问题");
        }
    }
}
