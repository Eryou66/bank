package com.dz.controller;

import com.dz.api.ChatRequest;
import com.dz.api.ChatResponse;
import com.dz.api.R;
import com.dz.context.RequestContext;
import com.dz.security.SecurityCheck;
import com.dz.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "智能客服对话", description = "M0 骨架")
public class ChatController {

    @Resource
    private ChatService chatService;

    /**
     * 探针接口，不依赖数据库，用于验证服务与 traceId 链路
     * @return
     */
    @GetMapping("/ping")
    @Operation(summary = "健康探针")
    public R<Map<String, Object>> ping(){
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("service", "bank-ai-customer-service");
        data.put("stage", "M0");
        data.put("traceId", RequestContext.getTraceId());
        return R.ok(data);
    }

    @PostMapping("/chat")
    @SecurityCheck
    @Operation(summary = "对话")
    public R<ChatResponse> chat(@Valid @RequestBody ChatRequest request){
        return R.ok(chatService.chat(request));
    }

}
