package com.dz.nlu;

import lombok.Getter;

/**
 * 32 类业务意图（文档 2.2.1）。
 *
 * 说明：这里实际列了 34 条，比文档写的 32 多 2 条：
 *   ①TRANSFER —— 文档在括号里给了 code 却没写中文名，我按"转账（发起）"补的；
 *   ②"其他"类文档数量列写 2、却列了 4 条（活动咨询 / 投诉建议 / 转人工 / 问候），我全保留了。
 * 想变回 32 就删掉任意 2 条，改这里 + intent_config 表数据即可。
 *
 * 关键词一律用「归一化之后」的词：理财 → 理财产品、利率 → 年化收益率、开户 → 开立账户。
 */
@Getter
public enum IntentCode {

    // 账户
    QUERY_BALANCE("查询余额", BizDomain.ACCOUNT, 0.6, false, RouteChain.API,
            new SlotType[]{SlotType.ACCOUNT},
            new String[]{"余额", "还有多少钱", "查余额", "卡里还有"}),
    QUERY_DETAIL("交易明细", BizDomain.ACCOUNT, 0.6, false, RouteChain.API,
            new SlotType[]{SlotType.ACCOUNT, SlotType.DATE},
            new String[]{"明细", "流水", "交易记录", "账单"}),
    OPEN_ACCOUNT_CONDITION("开户条件", BizDomain.ACCOUNT, 0.7, false, RouteChain.RAG,
            new SlotType[]{},
            new String[]{"开立账户", "开户条件", "办账户"}),
    CLOSE_ACCOUNT_FLOW("销户流程", BizDomain.ACCOUNT, 0.9, true, RouteChain.RAG,
            new SlotType[]{SlotType.ACCOUNT},
            new String[]{"销户", "注销账户", "销掉账户"}),
    REPORT_ACCOUNT_LOSS("账户挂失", BizDomain.ACCOUNT, 0.9, true, RouteChain.DIALOG,
            new SlotType[]{SlotType.ACCOUNT},
            new String[]{"账户挂失", "挂失账户"}),
    FREEZE_ACCOUNT("账户冻结", BizDomain.ACCOUNT, 0.9, true, RouteChain.RAG,
            new SlotType[]{SlotType.ACCOUNT},
            new String[]{"冻结", "账户冻结", "被冻结"}),

    // ===== 支付转账 =====
    TRANSFER("转账", BizDomain.TRANSFER, 0.9, true, RouteChain.DIALOG,
            new SlotType[]{SlotType.ACCOUNT, SlotType.AMOUNT},
            new String[]{"转账", "汇款", "打钱", "转给"}),
    TRANSFER_LIMIT("转账限额", BizDomain.TRANSFER, 0.7, false, RouteChain.RAG,
            new SlotType[]{},
            new String[]{"转账限额", "最多转", "额度上限"}),
    TRANSFER_FEE("转账手续费", BizDomain.TRANSFER, 0.7, false, RouteChain.RAG,
            new SlotType[]{SlotType.AMOUNT},
            new String[]{"手续费", "转账手续费", "转账收费"}),
    TRANSFER_FAILED("转账失败", BizDomain.TRANSFER, 0.7, false, RouteChain.RAG,
            new SlotType[]{},
            new String[]{"转账失败", "转不了", "汇款失败"}),
    TRANSFER_ARRIVAL("到账时间", BizDomain.TRANSFER, 0.7, false, RouteChain.RAG,
            new SlotType[]{},
            new String[]{"到账时间", "多久到账", "什么时候到账"}),
    CANCEL_TRANSFER("代扣解约", BizDomain.TRANSFER, 0.9, true, RouteChain.DIALOG,
            new SlotType[]{},
            new String[]{"代扣", "解约", "取消代扣", "撤销转账"}),

    // ===== 银行卡 =====
    CARD_APPLY_CONDITION("办卡条件", BizDomain.BANK_CARD, 0.7, false, RouteChain.RAG,
            new SlotType[]{},
            new String[]{"办卡", "办卡条件", "申请信用卡"}),
    CARD_ANNUAL_FEE("年费", BizDomain.BANK_CARD, 0.6, false, RouteChain.RAG,
            new SlotType[]{SlotType.PRODUCT},
            new String[]{"年费", "卡费"}),
    REPORT_LOSS("挂失补卡", BizDomain.BANK_CARD, 0.9, true, RouteChain.DIALOG,
            new SlotType[]{SlotType.ACCOUNT},
            new String[]{"挂失", "补卡", "卡丢了"}),
    CARD_PHONE_CHANGE("预留手机号修改", BizDomain.BANK_CARD, 0.9, true, RouteChain.DIALOG,
            new SlotType[]{},
            new String[]{"预留手机号", "改手机号", "换预留号"}),
    ACTIVATE_CARD("卡片激活", BizDomain.BANK_CARD, 0.9, false, RouteChain.RAG,
            new SlotType[]{SlotType.ACCOUNT},
            new String[]{"激活", "开卡", "启用卡"}),
    CANCEL_CARD("卡片注销", BizDomain.BANK_CARD, 0.9, true, RouteChain.RAG,
            new SlotType[]{SlotType.ACCOUNT},
            new String[]{"销卡", "注销卡"}),

    // ===== 理财投资 =====
    QUERY_PRODUCT("理财产品查询", BizDomain.WEALTH, 0.6, false, RouteChain.RAG,
            new SlotType[]{SlotType.PRODUCT},
            new String[]{"理财产品", "有什么理财", "产品查询"}),
    BUY_PRODUCT("购买条件", BizDomain.WEALTH, 0.9, true, RouteChain.RAG,
            new SlotType[]{SlotType.PRODUCT, SlotType.AMOUNT},
            new String[]{"购买", "怎么买", "买入"}),
    QUERY_PROFIT("收益计算", BizDomain.WEALTH, 0.6, false, RouteChain.RAG,
            new SlotType[]{SlotType.AMOUNT, SlotType.PRODUCT},
            new String[]{"收益", "利息", "赚多少", "利息多少"}),
    RISK_RATING("风险评级", BizDomain.WEALTH, 0.6, false, RouteChain.RAG,
            new SlotType[]{SlotType.PRODUCT},
            new String[]{"风险", "风险等级", "评级"}),
    REDEEM_PRODUCT("赎回规则", BizDomain.WEALTH, 0.9, true, RouteChain.RAG,
            new SlotType[]{SlotType.PRODUCT},
            new String[]{"赎回", "取出", "提前赎回"}),

    // ===== 贷款 =====
    APPLY_LOAN("贷款申请条件", BizDomain.LOAN, 0.9, true, RouteChain.RAG,
            new SlotType[]{SlotType.AMOUNT},
            new String[]{"贷款", "借钱", "申请贷款"}),
    QUERY_INTEREST("利率", BizDomain.LOAN, 0.6, false, RouteChain.RAG,
            new SlotType[]{SlotType.PRODUCT},
            new String[]{"年化收益率", "贷款利率", "利息率"}),
    REPAY_LOAN("还款方式", BizDomain.LOAN, 0.9, true, RouteChain.RAG,
            new SlotType[]{SlotType.AMOUNT},
            new String[]{"还款", "怎么还", "还贷"}),
    LOAN_OVERDUE("逾期处理", BizDomain.LOAN, 0.7, false, RouteChain.RAG,
            new SlotType[]{},
            new String[]{"逾期", "没还上", "逾期处理"}),
    PREPAY_LOAN("提前还款", BizDomain.LOAN, 0.9, true, RouteChain.RAG,
            new SlotType[]{SlotType.AMOUNT},
            new String[]{"提前还款", "提前还贷"}),

    // ===== 网点服务 =====
    BRANCH_QUERY("网点查询", BizDomain.BRANCH, 0.6, false, RouteChain.RAG,
            new SlotType[]{},
            new String[]{"网点", "营业厅", "在哪"}),
    BRANCH_HOURS("营业时间", BizDomain.BRANCH, 0.6, false, RouteChain.RAG,
            new SlotType[]{},
            new String[]{"营业时间", "几点开门", "上班时间"}),
    BRANCH_APPOINTMENT("预约办理", BizDomain.BRANCH, 0.7, false, RouteChain.DIALOG,
            new SlotType[]{SlotType.DATE},
            new String[]{"预约", "预约办理"}),

    // ===== 其他 =====
    COMPLAINT("投诉建议", BizDomain.OTHER, 0.7, false, RouteChain.HUMAN,
            new SlotType[]{},
            new String[]{"投诉", "举报", "不满意"}),
    HUMAN_TRANSFER("转人工", BizDomain.OTHER, 0.6, false, RouteChain.HUMAN,
            new SlotType[]{},
            new String[]{"人工", "转人工", "人工客服"}),
    GREETING("问候", BizDomain.OTHER, 0.6, false, RouteChain.RAG,
            new SlotType[]{},
            new String[]{"你好", "您好", "在吗"}),
    PROMOTION_QUERY("活动咨询", BizDomain.OTHER, 0.6, false, RouteChain.RAG,
            new SlotType[]{},
            new String[]{"活动", "优惠", "权益"});

    private final String label;
    private final BizDomain domain;
    /** 分流置信度阈值：普通咨询 0.6，涉及资金/账户安全的高风险业务 0.9（文档 2.2.2） */
    private final double threshold;
    private final boolean highRisk;
    private final RouteChain defaultChain;
    private final SlotType[] requiredSlots;
    /** 关键词：第 0 个是强特征词（权重高），其余是辅助词 */
    private final String[] keywords;

    IntentCode(String label, BizDomain domain, double threshold, boolean highRisk,
               RouteChain defaultChain, SlotType[] requiredSlots, String[] keywords) {
        this.label = label;
        this.domain = domain;
        this.threshold = threshold;
        this.highRisk = highRisk;
        this.defaultChain = defaultChain;
        this.requiredSlots = requiredSlots;
        this.keywords = keywords;
    }
}
