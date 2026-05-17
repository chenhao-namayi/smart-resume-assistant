package com.aicopilot.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InterviewStartRequest {
    @NotNull(message = "简历ID不能为空")
    private Long resumeId;
    private String position;
}
