package com.dz.nlu;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BizDomain {

    ACCOUNT("账户"),
    TRANSFER("支付转账"),
    BANK_CARD("银行卡"),
    WEALTH("理财投资"),
    LOAN("贷款"),
    BRANCH("网点服务"),
    OTHER("其他");

    private final String label;
}
