package com.aicopilot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ResumeCreateRequest {
    @NotBlank(message = "简历标题不能为空")
    private String title;

    private String contentJson;
}
