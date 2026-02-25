package com.scriptuotyper.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * LangFuse Cloud REST API 클라이언트.
 * POST /api/public/ingestion 으로 trace + generation 이벤트 전송.
 */
@Slf4j
@Service
public class LangfuseTraceService {

    @Value("${langfuse.secret-key:}")
    private String secretKey;

    @Value("${langfuse.public-key:}")
    private String publicKey;

    @Value("${langfuse.base-url:https://cloud.langfuse.com}")
    private String baseUrl;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private String authHeader;
    private boolean enabled;

    public LangfuseTraceService() {
        this.httpClient = HttpClient.newBuilder().build();
        this.objectMapper = new ObjectMapper();
    }

    @PostConstruct
    void init() {
        this.enabled = !secretKey.isBlank() && !publicKey.isBlank();
        if (enabled) {
            String credentials = publicKey + ":" + secretKey;
            this.authHeader = "Basic " + Base64.getEncoder()
                    .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
            log.info("LangFuse tracing enabled - host: {}", baseUrl);
        } else {
            log.warn("LangFuse tracing disabled - missing keys");
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    /**
     * trace + generation 이벤트를 LangFuse에 전송.
     *
     * @param traceId    고유 trace ID
     * @param traceName  trace 이름 (e.g. "gemini-chat")
     * @param model      모델명
     * @param input      입력 메시지 리스트
     * @param output     출력 텍스트
     * @param inputTokens  입력 토큰 수 (null 가능)
     * @param outputTokens 출력 토큰 수 (null 가능)
     * @param durationMs   소요 시간 (밀리초)
     */
    public void sendTrace(String traceId, String traceName, String model,
                          List<Map<String, String>> input, String output,
                          Integer inputTokens, Integer outputTokens,
                          long durationMs) {
        if (!enabled) return;

        try {
            String now = Instant.now().toString();
            String generationId = UUID.randomUUID().toString();

            // trace-create 이벤트
            Map<String, Object> traceEvent = Map.of(
                    "id", UUID.randomUUID().toString(),
                    "type", "trace-create",
                    "timestamp", now,
                    "body", Map.of(
                            "id", traceId,
                            "name", traceName,
                            "input", input,
                            "output", output != null ? output : "",
                            "metadata", Map.of("model", model)
                    )
            );

            // generation observation 이벤트
            Map<String, Object> generationBody = new java.util.HashMap<>();
            generationBody.put("id", generationId);
            generationBody.put("traceId", traceId);
            generationBody.put("name", "gemini-generation");
            generationBody.put("type", "GENERATION");
            generationBody.put("startTime", now);
            generationBody.put("model", model);
            generationBody.put("input", input);
            generationBody.put("output", output != null ? output : "");

            if (inputTokens != null || outputTokens != null) {
                Map<String, Object> usage = new java.util.HashMap<>();
                if (inputTokens != null) usage.put("input", inputTokens);
                if (outputTokens != null) usage.put("output", outputTokens);
                if (inputTokens != null && outputTokens != null) {
                    usage.put("total", inputTokens + outputTokens);
                }
                generationBody.put("usage", usage);
            }

            Map<String, Object> generationEvent = Map.of(
                    "id", UUID.randomUUID().toString(),
                    "type", "observation-create",
                    "timestamp", now,
                    "body", generationBody
            );

            // batch 요청
            Map<String, Object> batch = Map.of(
                    "batch", List.of(traceEvent, generationEvent)
            );

            String json = objectMapper.writeValueAsString(batch);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/public/ingestion"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", authHeader)
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            // 비동기 전송 (응답 대기 않음)
            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        if (response.statusCode() >= 300) {
                            log.warn("LangFuse ingestion failed: {} - {}", response.statusCode(), response.body());
                        } else {
                            log.debug("LangFuse trace sent: {}", traceId);
                        }
                    })
                    .exceptionally(ex -> {
                        log.warn("LangFuse send error: {}", ex.getMessage());
                        return null;
                    });

        } catch (Exception e) {
            log.warn("LangFuse trace build error: {}", e.getMessage());
        }
    }
}