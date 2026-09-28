package com.dz.route.handler;

import com.dz.nlu.RouteChain;
import com.dz.route.ChainHandler;
import com.dz.route.RouteDecision;
import org.springframework.stereotype.Component;

/** 多轮引导链路：把状态机生成的反问话术直接返回给用户 */
@Component
public class DialogChainHandler implements ChainHandler {

    @Override
    public RouteChain chain() {
        return RouteChain.DIALOG;
    }

    @Override
    public String handle(RouteDecision decision) {
        return decision.guide() != null ? decision.guide() : "请问您能再补充一些信息吗？";
    }
}

