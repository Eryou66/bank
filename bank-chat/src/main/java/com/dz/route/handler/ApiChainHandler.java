package com.dz.route.handler;

import com.dz.nlu.RouteChain;
import com.dz.route.ChainHandler;
import com.dz.route.RouteDecision;
import org.springframework.stereotype.Component;

/**
 * API 实时查询链路（文档 2.3.1）：余额 / 明细等用户专属数据，不经过大模型。
 * M3 接入银行核心系统后再返回真实数据，现在先占位。
 */
@Component
public class ApiChainHandler implements ChainHandler {

    @Override
    public RouteChain chain() {
        return RouteChain.API;
    }

    @Override
    public String handle(RouteDecision decision) {
        return "【API 链路占位】已收到您的" + decision.intent().getLabel()
                + "请求，实时查询能力将在 M3 接入银行核心系统后返回真实数据。";
    }
}
