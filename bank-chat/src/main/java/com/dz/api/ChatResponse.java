package com.dz.api;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ChatResponse {

    private String sessionId;
    private String answer;
    /** M2 起填真实意图，M0 固定 UNKNOWN */
    private String intent;
    /** M2 起填真实链路（RAG/API/DIALOG/HUMAN），M0 固定 STUB */
    private String routeChain;
    /** M3 起填可溯源的引用文档，满足金融审计要求 */
    private List<String> references;
    private long costMs;

}
