package com.dz.service;

import com.dz.api.ChatRequest;
import com.dz.api.ChatResponse;

public interface ChatService {

    ChatResponse chat(ChatRequest request);

}
