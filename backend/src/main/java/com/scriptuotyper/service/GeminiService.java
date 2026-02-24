package com.scriptuotyper.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scriptuotyper.dto.chat.ChatStreamRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.Disposable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Service
public class GeminiService {

    private final String apiKey;
    private final String model;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public GeminiService(
            @Value("${gemini.api-key}") String apiKey,
            @Value("${gemini.model}") String model) {
        this.apiKey = apiKey;
        this.model = model;
        this.webClient = WebClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com")
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public SseEmitter streamChat(ChatStreamRequest request) {
        SseEmitter emitter = new SseEmitter(120_000L);

        String systemPrompt = buildSystemPrompt(request.context());

        List<Map<String, Object>> contents = new ArrayList<>();
        for (ChatStreamRequest.MessageItem msg : request.messages()) {
            String role = "assistant".equals(msg.role()) ? "model" : "user";
            contents.add(Map.of(
                    "role", role,
                    "parts", List.of(Map.of("text", msg.content()))
            ));
        }

        Map<String, Object> body = new HashMap<>();
        body.put("contents", contents);
        body.put("systemInstruction", Map.of("parts", List.of(Map.of("text", systemPrompt))));

        String url = "/v1beta/models/" + model + ":streamGenerateContent?alt=sse";

        Disposable subscription = webClient.post()
                .uri(url)
                .header("x-goog-api-key", apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(DataBuffer.class)
                .doOnNext(dataBuffer -> {
                    String chunk = dataBuffer.toString(StandardCharsets.UTF_8);
                    DataBufferUtils.release(dataBuffer);
                    processChunk(chunk, emitter);
                })
                .doOnComplete(() -> {
                    try {
                        emitter.send(SseEmitter.event().data("[DONE]"));
                        emitter.complete();
                    } catch (IOException e) {
                        log.debug("Emitter already completed on done");
                    }
                })
                .doOnError(error -> {
                    log.error("Gemini API error: {}", error.getMessage());
                    try {
                        String errorJson = objectMapper.writeValueAsString(
                                Map.of("error", "Gemini API 연결 실패: " + error.getMessage())
                        );
                        emitter.send(SseEmitter.event().data(errorJson));
                        emitter.send(SseEmitter.event().data("[DONE]"));
                        emitter.complete();
                    } catch (IOException e) {
                        emitter.completeWithError(error);
                    }
                })
                .subscribe();

        emitter.onCompletion(subscription::dispose);
        emitter.onTimeout(() -> {
            subscription.dispose();
            emitter.complete();
        });

        return emitter;
    }

    private void processChunk(String chunk, SseEmitter emitter) {
        String[] lines = chunk.split("\n");
        for (String line : lines) {
            if (!line.startsWith("data: ")) continue;
            String data = line.substring(6).trim();
            if (data.isEmpty()) continue;

            try {
                JsonNode parsed = objectMapper.readTree(data);
                JsonNode textNode = parsed.path("candidates")
                        .path(0)
                        .path("content")
                        .path("parts")
                        .path(0)
                        .path("text");

                if (!textNode.isMissingNode() && !textNode.asText().isEmpty()) {
                    String contentJson = objectMapper.writeValueAsString(
                            Map.of("content", textNode.asText())
                    );
                    emitter.send(SseEmitter.event().data(contentJson));
                }
            } catch (Exception e) {
                log.debug("Skipping malformed SSE line: {}", line);
            }
        }
    }

    private String buildSystemPrompt(ChatStreamRequest.ChatContext context) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("당신은 성경 말씀 도우미입니다. 사용자가 성경 구절에 대해 질문하면 ")
                .append("해석, 묵상 포인트, 역사적 배경, 적용 등을 친절하고 간결하게 답변해주세요. ")
                .append("한국어로 답변하세요.");

        if (context != null && context.bookName() != null) {
            prompt.append("\n\n현재 사용자가 타이핑 중인 구절: ").append(context.bookName());
            if (context.chapter() != null) {
                prompt.append(" ").append(context.chapter()).append("장");
            }
            if (context.verse() != null) {
                prompt.append(" ").append(context.verse()).append("절");
            }
            prompt.append(". 이 구절의 맥락을 참고하여 답변해주세요.");
        }

        return prompt.toString();
    }
}
