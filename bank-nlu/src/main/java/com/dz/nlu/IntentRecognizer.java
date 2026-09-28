package com.dz.nlu;

/**
 * 意图识别器
 */
public interface IntentRecognizer {

    /**
     * @param normalizedQuery 已经过七步安检（术语归一化 + PII 脱敏）的 Query
     * @return
     */
    IntentResult recognize(String normalizedQuery);

}
