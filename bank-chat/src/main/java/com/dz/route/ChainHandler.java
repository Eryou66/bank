package com.dz.route;

import com.dz.nlu.RouteChain;

/**
 * 链路处理器（文档 2.3.1：基于策略模式，每个链路封装为一个 Strategy 实现类）。
 * M3 会把 RAG / API 两个桩替换成真实现。
 */
public interface ChainHandler {

    RouteChain chain();

    /** 生成该链路的回答话术 */
    String handle(RouteDecision decision);
}

