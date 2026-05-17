package com.aicopilot.controller;

import com.aicopilot.dto.*;
import com.aicopilot.entity.Resume;
import com.aicopilot.service.ResumeService;
import com.aicopilot.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/resumes")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;
    private final JwtUtil jwtUtil;

    @GetMapping
    public ApiResponse<List<ResumeSummary>> listResumes(@RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserId(authHeader);
        return ApiResponse.success(resumeService.getUserResumes(userId));
    }

    @GetMapping("/{id}")
    public ApiResponse<Resume> getResume(@PathVariable Long id,
                                         @RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserId(authHeader);
        return ApiResponse.success(resumeService.getResumeById(id, userId));
    }

    @PostMapping
    public ApiResponse<Resume> createResume(@Valid @RequestBody ResumeCreateRequest request,
                                            @RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserId(authHeader);
        return ApiResponse.success(resumeService.createResume(request, userId));
    }

    @PutMapping("/{id}")
    public ApiResponse<Resume> updateResume(@PathVariable Long id,
                                            @RequestBody ResumeUpdateRequest request,
                                            @RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserId(authHeader);
        return ApiResponse.success(resumeService.updateResume(id, request, userId));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteResume(@PathVariable Long id,
                                          @RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserId(authHeader);
        resumeService.deleteResume(id, userId);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/versions")
    public ApiResponse<Resume> createVersion(@PathVariable Long id,
                                             @RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserId(authHeader);
        return ApiResponse.success(resumeService.createVersion(id, userId));
    }

    private Long extractUserId(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        return jwtUtil.getUserIdFromToken(token);
    }
}
