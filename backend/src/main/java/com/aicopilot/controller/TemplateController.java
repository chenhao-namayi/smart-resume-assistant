package com.aicopilot.controller;

import com.aicopilot.dto.ApiResponse;
import com.aicopilot.entity.Template;
import com.aicopilot.repository.TemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/templates")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateRepository templateRepository;

    @GetMapping
    public ApiResponse<List<Template>> listTemplates(@RequestParam(required = false) String category) {
        List<Template> templates;
        if (category != null && !category.isEmpty()) {
            templates = templateRepository.findByCategoryOrderByNameAsc(category);
        } else {
            templates = templateRepository.findAllByOrderByCategoryAscNameAsc();
        }
        return ApiResponse.success(templates);
    }

    @GetMapping("/{id}")
    public ApiResponse<Template> getTemplate(@PathVariable Long id) {
        return templateRepository.findById(id)
                .map(ApiResponse::success)
                .orElseThrow(() -> new com.aicopilot.exception.BusinessException(404, "模板不存在"));
    }
}
