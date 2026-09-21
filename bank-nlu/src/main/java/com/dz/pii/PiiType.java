package com.dz.pii;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PiiType {

    /** 18 位身份证，必须放在银行卡前面匹配，否则会被 16~19 位规则吃掉 */
    ID_CARD(PiiLevel.CRITICAL, "\\d{17}[\\dXx]"),
    BANK_CARD(PiiLevel.CRITICAL, "\\d{16,19}"),
    PHONE(PiiLevel.CRITICAL, "1[3-9]\\d{9}"),
    EMAIL(PiiLevel.HIGH, "[\\w.+-]+@[\\w-]+\\.[\\w.]+");

    private final PiiLevel level;
    private final String regex;

    /** 数字后面紧跟这些单位时，说明是业务数字而非证件号（文档 2.1.2 第 5 步的防误判要求） */
    public static final String BUSINESS_UNIT_HINTS = "元万年期个月天次笔块块钱万块";

}
