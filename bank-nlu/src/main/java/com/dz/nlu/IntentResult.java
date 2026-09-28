package com.dz.nlu;


import java.util.List;

/** 意图识别结果。intent 为 null 表示完全没命中，由路由层按 HUMAN 兜底 */
public record IntentResult(IntentCode intent, double confidence, List<String> hitKeywords) {

    public boolean isUnknown() {
        return intent == null;
    }

    public boolean isConfident() {
        return intent != null && confidence >= intent.getThreshold();
    }
}
