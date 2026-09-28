package com.dz.route;

import com.dz.dialog.DialogDecision;
import com.dz.dialog.DialogStateMachine;
import com.dz.nlu.IntentCode;
import com.dz.nlu.IntentRecognizer;
import com.dz.nlu.IntentResult;
import com.dz.nlu.RouteChain;
import com.dz.nlu.Slot;
import com.dz.nlu.SlotExtractor;
import com.dz.nlu.SlotType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 意图路由引擎（文档 2.3.1）。
 *
 * 分流优先级：API > RAG > DIALOG > HUMAN，命中高优先级不再判断低优先级。
 * 但有一条更优先的规则：只要必填槽位还缺，一律先走 DIALOG 补槽。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IntentRouter {

    /** 文档 2.3.1：HUMAN 的置信度门槛 */
    private static final double HUMAN_THRESHOLD = 0.7;
    /** API 链路要求更高（实时数据，错了影响大） */
    private static final double API_THRESHOLD = 0.95;
    private static final double RAG_THRESHOLD = 0.9;

    private final IntentRecognizer recognizer;
    private final SlotExtractor slotExtractor;
    private final DialogStateMachine stateMachine;

    public RouteDecision route(String sessionId, String normalizedQuery) {
        IntentResult intentResult = recognizer.recognize(normalizedQuery);
        Map<SlotType, Slot> extracted = slotExtractor.extract(normalizedQuery);
        DialogDecision dialog = stateMachine.handle(sessionId, intentResult, extracted);

        IntentCode intent = intentResult.intent();
        double confidence = intentResult.confidence();

        // 1. 状态机判定重置（多轮兜底 / 无意图）：引导用户重新描述，并提供人工入口
        if (dialog.reset()) {
            return new RouteDecision(RouteChain.HUMAN, intent, confidence,
                    dialog.slots(), dialog.missing(), dialog.guide());
        }

        // 2. 槽位缺失优先：一次只问一个，走 DIALOG
        if (!dialog.complete()) {
            return new RouteDecision(RouteChain.DIALOG, intent, confidence,
                    dialog.slots(), dialog.missing(), dialog.guide());
        }

        RouteChain chain = decideChain(intent, confidence);
        log.info("[路由] session={} intent={} confidence={} chain={}",
                sessionId, intent, confidence, chain);
        return new RouteDecision(chain, intent, confidence, dialog.slots(), List.of(), null);
    }

    private RouteChain decideChain(IntentCode intent, double confidence) {
        if (intent == null) {
            return RouteChain.HUMAN;
        }
        // 投诉 / 主动转人工 / 置信度过低 → 人工兜底
        if (intent == IntentCode.COMPLAINT
                || intent == IntentCode.HUMAN_TRANSFER
                || confidence < HUMAN_THRESHOLD) {
            return RouteChain.HUMAN;
        }
        // 用户专属实时数据，且置信度极高 → API（文档要求 ≥0.95 且已认证；认证能力后续补）
        if (intent.getDefaultChain() == RouteChain.API && confidence >= API_THRESHOLD) {
            return RouteChain.API;
        }
        // 知识类意图且置信度够 → RAG
        if (confidence >= RAG_THRESHOLD) {
            return RouteChain.RAG;
        }
        // 0.7~0.9 之间，走多轮确认
        return RouteChain.DIALOG;
    }
}

