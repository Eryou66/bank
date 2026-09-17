package com.dz.service.impl;

import com.dz.api.ChatRequest;
import com.dz.api.ChatResponse;
import com.dz.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Slf4j
@Service
public class StubChatServiceImpl implements ChatService {

    @Override
    public ChatResponse chat(ChatRequest request) {
        long start = System.currentTimeMillis();
        log.info("[M0-STUB]收到提问sessionId={} questionId={}",
                request.getSessionId(), request.getQuestion());
        return ChatResponse.builder()
                .sessionId(request.getSessionId())
                .answer("[M0 骨架占位]")
                .intent("UNKNOWN")
                .routeChain("STUB")
                .references(Collections.emptyList())
                .costMs(System.currentTimeMillis() - start)
                .build();
    }

}
