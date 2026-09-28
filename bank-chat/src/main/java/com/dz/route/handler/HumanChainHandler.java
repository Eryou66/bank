package com.dz.route.handler;

import com.dz.nlu.RouteChain;
import com.dz.route.ChainHandler;
import com.dz.route.RouteDecision;
import org.springframework.stereotype.Component;

/** 人工客服链路：兜底疑难问题，上下文一并传给坐席（文档 2.3.1） */
@Component
public class HumanChainHandler implements ChainHandler {

    @Override
    public RouteChain chain() {
        return RouteChain.HUMAN;
    }

    @Override
    public String handle(RouteDecision decision) {
        String intent = decision.intent() == null ? "未识别" : decision.intent().getLabel();
        return "已为您转接人工客服，请稍候。已识别诉求：" + intent + "。";
    }
}

