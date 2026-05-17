package com.aicopilot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MatchRequest {
    @NotNull(message = "简历ID不能为空")
    private Long resumeId;

    @NotBlank(message = "职位描述不能为空")
    private String jobDescription;
}
