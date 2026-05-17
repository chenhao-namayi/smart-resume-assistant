package com.aicopilot.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TemplateCreateRequest {
    @NotBlank(message = "模板名称不能为空")
    private String name;

    private String category;

    private String description;

    @NotBlank(message = "模板内容不能为空")
    private String contentJson;

    private String sourceUrl;
}
