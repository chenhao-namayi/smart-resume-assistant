package com.aicopilot.controller;

import com.aicopilot.dto.*;
import com.aicopilot.service.JobAnalysisService;
import com.aicopilot.service.LLMService;
import com.aicopilot.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AIController {

    private final LLMService llmService;
    private final JobAnalysisService jobAnalysisService;
    private final JwtUtil jwtUtil;

    @PostMapping("/optimize")
    public ApiResponse<OptimizeResponse> optimize(@Valid @RequestBody OptimizeRequest request,
                                                  @RequestHeader("Authorization") String authHeader) {
        extractUserId(authHeader);
        return ApiResponse.success(llmService.optimizeSection(request.getOriginalText(), request.getInstruction()));
    }

    @PostMapping("/optimize-full")
    public ApiResponse<OptimizeResponse> optimizeFull(@RequestBody OptimizeRequest request,
                                                      @RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserId(authHeader);
        return ApiResponse.success(llmService.optimizeFullResume(request.getOriginalText(), request.getInstruction()));
    }

    @PostMapping("/match")
    public ApiResponse<MatchResponse> match(@Valid @RequestBody MatchRequest request,
                                            @RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserId(authHeader);
        return ApiResponse.success(jobAnalysisService.analyzeMatch(request.getResumeId(), request.getJobDescription(), userId));
    }

    private Long extractUserId(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        return jwtUtil.getUserIdFromToken(token);
    }
}
