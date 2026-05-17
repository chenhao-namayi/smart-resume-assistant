package com.aicopilot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OptimizeRequest {
    @NotNull(message = "简历ID不能为空")
    private Long resumeId;

    @NotBlank(message = "区块类型不能为空")
    private String sectionType;

    @NotBlank(message = "原始文本不能为空")
    private String originalText;

    private String instruction;
}
