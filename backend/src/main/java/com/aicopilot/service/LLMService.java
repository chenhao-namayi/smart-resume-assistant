package com.aicopilot.service;

import com.aicopilot.dto.MatchResponse;
import com.aicopilot.dto.OptimizeResponse;
import com.aicopilot.exception.BusinessException;
import com.aicopilot.util.PromptBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class LLMService {

    private final WebClient webClient;
    private final PromptBuilder promptBuilder;
    private final ObjectMapper objectMapper;

    @Value("${llm.api-key}")
    private String apiKey;

    @Value("${llm.base-url}")
    private String baseUrl;

    @Value("${llm.model}")
    private String model;

    @Value("${llm.timeout}")
    private long timeout;

    @Value("${llm.max-retries}")
    private int maxRetries;

    public OptimizeResponse optimizeSection(String originalText, String instruction) {
        String systemPrompt = promptBuilder.buildOptimizeSystemPrompt();
        String userPrompt = promptBuilder.buildOptimizeUserPrompt(originalText, instruction);

        String response = callLLM(systemPrompt, userPrompt);

        return new OptimizeResponse(response, estimateTokens(systemPrompt + userPrompt + response));
    }

    public OptimizeResponse optimizeFullResume(String resumeJson, String instruction) {
        String systemPrompt = promptBuilder.buildOptimizeSystemPrompt();
        String userPrompt = """
                请对以下完整简历进行全文优化，使其更加专业、有吸引力：

                简历内容：
                %s

                额外要求：%s

                请保持JSON结构不变，仅优化各字段的文本内容。
                """.formatted(resumeJson, instruction != null ? instruction : "无");

        String response = callLLM(systemPrompt, userPrompt);
        return new OptimizeResponse(response, estimateTokens(systemPrompt + userPrompt + response));
    }

    public MatchResponse analyzeMatch(String resumeJson, String jobDescription) {
        String systemPrompt = promptBuilder.buildMatchSystemPrompt();
        String userPrompt = promptBuilder.buildMatchUserPrompt(resumeJson, jobDescription);

        String response = callLLM(systemPrompt, userPrompt);

        try {
            return objectMapper.readValue(extractJson(response), MatchResponse.class);
        } catch (Exception e) {
            log.error("Failed to parse match response: {}", e.getMessage());
            return new MatchResponse(0, List.of(), List.of(), List.of("匹配分析服务暂时不可用"));
        }
    }

    @SuppressWarnings("unchecked")
    public String callLLM(String systemPrompt, String userPrompt) {
        String apiUrl = baseUrl.endsWith("/") ? baseUrl + "v1/messages" : baseUrl + "/v1/messages";

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "system", systemPrompt,
                "messages", List.of(
                        Map.of("role", "user", "content", userPrompt)
                ),
                "max_tokens", 4096,
                "temperature", 0.7
        );

        Exception lastException = null;
        for (int i = 0; i <= maxRetries; i++) {
            try {
                Map response = webClient.post()
                        .uri(apiUrl)
                        .header("x-api-key", apiKey)
                        .header("Content-Type", "application/json")
                        .header("anthropic-version", "2023-06-01")
                        .bodyValue(requestBody)
                        .retrieve()
                        .onStatus(HttpStatusCode::is4xxClientError, resp ->
                                resp.bodyToMono(String.class).flatMap(error -> {
                                    log.error("LLM API 4xx error: {}", error);
                                    return Mono.error(new BusinessException(500, "LLM服务调用失败: " + error));
                                }))
                        .bodyToMono(Map.class)
                        .timeout(Duration.ofMillis(timeout))
                        .block();

                if (response != null && response.containsKey("content")) {
                    Object contentObj = response.get("content");
                    if (contentObj instanceof List) {
                        List<Map<String, Object>> content = (List<Map<String, Object>>) contentObj;
                        if (!content.isEmpty() && content.get(0).containsKey("text")) {
                            return (String) content.get(0).get("text");
                        }
                    }
                }
                throw new BusinessException(500, "LLM返回格式异常");
            } catch (BusinessException e) {
                throw e;
            } catch (Exception e) {
                lastException = e;
                log.warn("LLM call attempt {} failed: {}", i + 1, e.getMessage());
                if (i < maxRetries) {
                    try {
                        Thread.sleep(1000L * (i + 1));
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        throw new BusinessException(503, "优化服务繁忙，请稍后再试");
    }

    private String extractJson(String text) {
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start != -1 && end > start) {
            return text.substring(start, end + 1);
        }
        return text;
    }

    private int estimateTokens(String text) {
        return (int) (text.length() / 1.3);
    }
}
