package com.dz.dialog;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 多轮对话状态。
 *
 * 文档里的状态是意图相关的写法（TRANSFER_ASK_ACCOUNT），这里改成通用态：
 * 「意图 + 通用状态」等价表达，34 个意图不必各自建一套状态。
 * 对应关系举例：TRANSFER_ASK_ACCOUNT ≡ (intent=TRANSFER, state=ASK_ACCOUNT)。
 */
@Getter
@RequiredArgsConstructor
public enum DialogState {

    START("初始状态"),
    ASK_ACCOUNT("待补充账号"),
    ASK_AMOUNT("待补充金额"),
    ASK_DATE("待补充日期/期限"),
    ASK_PRODUCT("待补充产品"),
    CONFIRM("待用户确认"),
    HUMAN("已转人工");

    private final String label;

}
