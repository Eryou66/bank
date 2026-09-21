package com.dz.security;

/**
 * 携带用户提问的载体    请求 DTO 实现它之后，
 * 切面就能把安检规范化后的 Query 回写进去，下游 NLU / RAG 直接拿到处理好的文本。
 */
public interface QueryCarrier {

    String getQuestion();

    void setQuestion(String question);

}
