package com.aicopilot.controller;

import com.aicopilot.dto.*;
import com.aicopilot.entity.*;
import com.aicopilot.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // ===== User Management =====

    @GetMapping("/users")
    public ApiResponse<List<UserManageResponse>> listUsers() {
        return ApiResponse.success(adminService.listUsers());
    }

    @GetMapping("/users/{id}")
    public ApiResponse<UserManageResponse> getUser(@PathVariable Long id) {
        return ApiResponse.success(adminService.getUser(id));
    }

    @PutMapping("/users/{id}")
    public ApiResponse<UserManageResponse> updateUser(@PathVariable Long id,
                                                       @RequestBody UserUpdateRequest request) {
        return ApiResponse.success(adminService.updateUser(id, request));
    }

    @DeleteMapping("/users/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ApiResponse.success(null);
    }

    // ===== Resume Management =====

    @GetMapping("/users/{userId}/resumes")
    public ApiResponse<List<Resume>> getUserResumes(@PathVariable Long userId) {
        return ApiResponse.success(adminService.getUserResumes(userId));
    }

    @DeleteMapping("/users/{userId}/resumes/{resumeId}")
    public ApiResponse<Void> deleteUserResume(@PathVariable Long userId,
                                              @PathVariable Long resumeId) {
        adminService.deleteResume(userId, resumeId);
        return ApiResponse.success(null);
    }

    // ===== Template CRUD =====

    @GetMapping("/templates")
    public ApiResponse<List<Template>> listTemplates(@RequestParam(required = false) String category) {
        return ApiResponse.success(adminService.listTemplates(category));
    }

    @GetMapping("/templates/{id}")
    public ApiResponse<Template> getTemplate(@PathVariable Long id) {
        return ApiResponse.success(adminService.getTemplate(id));
    }

    @PostMapping("/templates")
    public ApiResponse<Template> createTemplate(@Valid @RequestBody TemplateCreateRequest request) {
        return ApiResponse.success(adminService.createTemplate(request));
    }

    @PutMapping("/templates/{id}")
    public ApiResponse<Template> updateTemplate(@PathVariable Long id,
                                                @RequestBody TemplateUpdateRequest request) {
        return ApiResponse.success(adminService.updateTemplate(id, request));
    }

    @DeleteMapping("/templates/{id}")
    public ApiResponse<Void> deleteTemplate(@PathVariable Long id) {
        adminService.deleteTemplate(id);
        return ApiResponse.success(null);
    }

    // ===== Statistics =====

    @GetMapping("/stats")
    public ApiResponse<StatsResponse> getStats() {
        return ApiResponse.success(adminService.getStats());
    }

    // ===== Optimization Logs =====

    @GetMapping("/logs")
    public ApiResponse<Map<String, Object>> listLogs(@RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "20") int size) {
        Page<OptimizationLog> result = adminService.listOptimizationLogs(page, size);
        return ApiResponse.success(Map.of(
                "content", result.getContent(),
                "totalElements", result.getTotalElements(),
                "totalPages", result.getTotalPages(),
                "page", result.getNumber()
        ));
    }

    // ===== Interview Sessions =====

    @GetMapping("/interviews")
    public ApiResponse<Map<String, Object>> listInterviews(@RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "20") int size) {
        Page<InterviewSession> result = adminService.listInterviews(page, size);
        return ApiResponse.success(Map.of(
                "content", result.getContent(),
                "totalElements", result.getTotalElements(),
                "totalPages", result.getTotalPages(),
                "page", result.getNumber()
        ));
    }

    // ===== Job Analyses =====

    @GetMapping("/analyses")
    public ApiResponse<Map<String, Object>> listAnalyses(@RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "20") int size) {
        Page<JobAnalysis> result = adminService.listJobAnalyses(page, size);
        return ApiResponse.success(Map.of(
                "content", result.getContent(),
                "totalElements", result.getTotalElements(),
                "totalPages", result.getTotalPages(),
                "page", result.getNumber()
        ));
    }
}
