package com.dz.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChatRequest {

    @NotBlank(message = "sessionId 不能为空")
    private String sessionId;

    private String question;

}
