package com.dz.route.handler;

import com.dz.nlu.RouteChain;
import com.dz.route.ChainHandler;
import com.dz.route.RouteDecision;
import org.springframework.stereotype.Component;

/** RAG 知识库检索链路：覆盖 80% 高频问题。M3 接入知识库检索 + 大模型生成后替换 */
@Component
public class RagChainHandler implements ChainHandler {

    @Override
    public RouteChain chain() {
        return RouteChain.RAG;
    }

    @Override
    public String handle(RouteDecision decision) {
        return "【RAG 链路占位】已识别为" + decision.intent().getLabel()
                + "，知识库检索与答案生成将在 M3 接入。";
    }
}

