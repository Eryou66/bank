package com.dz.service.impl;

import com.dz.api.ChatRequest;
import com.dz.api.ChatResponse;
import com.dz.nlu.RouteChain;
import com.dz.route.ChainHandler;
import com.dz.route.IntentRouter;
import com.dz.route.RouteDecision;
import com.dz.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * M2：安检 → 意图识别 → 会话状态机 → 四链路分流 → 链路话术。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NluChatServiceImpl implements ChatService {

    private final IntentRouter router;
    private final List<ChainHandler> handlers;

    @Override
    public ChatResponse chat(ChatRequest request) {
        long start = System.currentTimeMillis();
        // request.getQuestion() 已经是七步安检后的结果（术语归一化 + PII 脱敏）
        RouteDecision decision = router.route(request.getSessionId(), request.getQuestion());
        String answer = handler(decision.chain()).handle(decision);

        log.info("[M2] session={} intent={} confidence={} chain={} costMs={}",
                request.getSessionId(), decision.intentCode(), decision.confidence(),
                decision.chain(), System.currentTimeMillis() - start);

        return ChatResponse.builder()
                .sessionId(request.getSessionId())
                .answer(answer)
                .intent(decision.intentCode())
                .confidence(decision.confidence())
                .routeChain(decision.chain().name())
                .guide(decision.guide())
                .references(Collections.emptyList())
                .costMs(System.currentTimeMillis() - start)
                .build();
    }

    private ChainHandler handler(RouteChain chain) {
        Map<RouteChain, ChainHandler> byChain = handlers.stream()
                .collect(Collectors.toMap(ChainHandler::chain, Function.identity()));
        ChainHandler handler = byChain.get(chain);
        if (handler == null) {
            // 兜底：所有链路都匹配失败时引导重新描述并提供人工入口
            return byChain.get(RouteChain.HUMAN);
        }
        return handler;
    }
}
