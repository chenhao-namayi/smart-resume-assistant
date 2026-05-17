package com.aicopilot.controller;

import com.aicopilot.dto.*;
import com.aicopilot.entity.InterviewSession;
import com.aicopilot.service.InterviewService;
import com.aicopilot.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/interview")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;
    private final JwtUtil jwtUtil;

    @PostMapping("/start")
    public ApiResponse<InterviewResponse> startInterview(@Valid @RequestBody InterviewStartRequest request,
                                                          @RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserId(authHeader);
        return ApiResponse.success(interviewService.startInterview(request, userId));
    }

    @PostMapping("/answer")
    public ApiResponse<InterviewResponse> submitAnswer(@Valid @RequestBody InterviewAnswerRequest request,
                                                        @RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserId(authHeader);
        return ApiResponse.success(interviewService.submitAnswer(request, userId));
    }

    @PostMapping("/{id}/end")
    public ApiResponse<InterviewReportResponse> endInterview(@PathVariable Long id,
                                                              @RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserId(authHeader);
        return ApiResponse.success(interviewService.endInterview(id, userId));
    }

    @GetMapping("/{id}")
    public ApiResponse<InterviewSession> getSessionDetail(@PathVariable Long id,
                                                           @RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserId(authHeader);
        return ApiResponse.success(interviewService.getSessionDetail(id, userId));
    }

    @GetMapping("/history")
    public ApiResponse<List<InterviewSessionSummary>> getHistory(@RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserId(authHeader);
        return ApiResponse.success(interviewService.getUserHistory(userId));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteSession(@PathVariable Long id,
                                            @RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserId(authHeader);
        interviewService.deleteSession(id, userId);
        return ApiResponse.success(null);
    }

    private Long extractUserId(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        return jwtUtil.getUserIdFromToken(token);
    }
}
