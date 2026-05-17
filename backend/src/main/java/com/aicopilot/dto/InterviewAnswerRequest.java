package com.aicopilot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InterviewAnswerRequest {
    @NotNull(message = "面试会话ID不能为空")
    private Long sessionId;
    @NotBlank(message = "回答不能为空")
    private String answer;
}
