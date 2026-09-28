package com.dz.nlu;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 规则 / 词典版意图识别：关键词加权打分。
 *
 * 打分规则（简单可解释，面试好讲）：
 *   命中第 0 个强特征词 +0.75，命中其余辅助词每个 +0.15，上限 1.0。
 * 取得分最高的意图；全都没命中则返回 intent=null，交给路由层走 HUMAN 兜底。
 */
@Slf4j
@Service("ruleIntentRecognizer")
public class RuleIntentRecognizer implements IntentRecognizer{

    private static final double STRONG_WEIGHT = 0.75;
    private static final double WEAK_WEIGHT = 0.15;
    private static final double MAX_SCORE = 1.0;

    @Override
    public IntentResult recognize(String normalizedQuery) {
        if (!StringUtils.hasText(normalizedQuery)) {
            return new IntentResult(null, 0.0, List.of());
        }
        String query = normalizedQuery.toLowerCase();

        IntentCode best = null;
        double bestScore = 0.0;
        List<String> bestHits = List.of();

        for(IntentCode code : IntentCode.values()){
            String[] keyWords = code.getKeywords();
            List<String> hits = new ArrayList<>();
            double score = 0.0;
            for (int i = 0; i < keyWords.length; i++) {
                if(query.contains(keyWords[i].toLowerCase())){
                    hits.add(keyWords[i]);
                    score += (i == 0) ? STRONG_WEIGHT : WEAK_WEIGHT;
                }
            }
            if(score > 0){
                score = Math.min(score, MAX_SCORE);
                // 同分时优先取领域更靠前的，保证结果稳定可以复现
                if (score > bestScore){
                    best = code;
                    bestScore = score;
                    bestHits = hits;
                }
            }
        }

        if (best == null){
            log.info("[NLU] 未命中任何意图：{}", normalizedQuery);
            return new IntentResult(null, 0.0, List.of());
        }
        log.debug("[NLU] 识别结果 intent = {} confidence = {} hits = {}", best, bestScore, bestHits);
        return new IntentResult(best, bestScore, bestHits);
    }

}
