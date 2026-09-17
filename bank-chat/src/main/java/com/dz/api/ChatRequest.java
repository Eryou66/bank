package com.dz.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChatRequest {

    @NotBlank(message = "sessionId 不能为空")
    private String sessionId;

    /**
     * 长度规则来自文档 2.1.2 第 1 步（2~200 字符）。
     * M0 暂由 Bean Validation 兜住，M1 迁入安检流水线并改用 4001 错误码。
     */
    @NotBlank(message = "提问内容不能为空")
    @Size(min = 2, max = 200, message = "提问长度需在 2~200 字符之间")
    private String question;

}
