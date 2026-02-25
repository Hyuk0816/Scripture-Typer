package com.scriptuotyper.service;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageType;
import dev.langchain4j.data.message.Content;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.listener.ChatModelErrorContext;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.listener.ChatModelRequestContext;
import dev.langchain4j.model.chat.listener.ChatModelResponseContext;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.output.TokenUsage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * LangChain4j ChatModelListener 구현.
 * onRequest에서 traceId + startTime 기록,
 * onResponse에서 LangFuse로 trace+generation 전송.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LangfuseChatModelListener implements ChatModelListener {

    private static final String ATTR_TRACE_ID = "langfuse.traceId";
    private static final String ATTR_START_TIME = "langfuse.startTime";

    private final LangfuseTraceService langfuseTraceService;

    @Override
    public void onRequest(ChatModelRequestContext requestContext) {
        if (!langfuseTraceService.isEnabled()) return;

        requestContext.attributes().put(ATTR_TRACE_ID, UUID.randomUUID().toString());
        requestContext.attributes().put(ATTR_START_TIME, System.currentTimeMillis());
    }

    @Override
    public void onResponse(ChatModelResponseContext responseContext) {
        if (!langfuseTraceService.isEnabled()) return;

        try {
            String traceId = (String) responseContext.attributes().get(ATTR_TRACE_ID);
            Long startTime = (Long) responseContext.attributes().get(ATTR_START_TIME);
            long durationMs = startTime != null ? System.currentTimeMillis() - startTime : 0;

            ChatRequest chatRequest = responseContext.chatRequest();
            ChatResponse chatResponse = responseContext.chatResponse();

            // 입력 메시지 변환
            List<Map<String, String>> inputMessages = new ArrayList<>();
            for (ChatMessage msg : chatRequest.messages()) {
                String role = mapRole(msg.type());
                inputMessages.add(Map.of("role", role, "content", extractText(msg)));
            }

            // 출력 텍스트
            String output = chatResponse.aiMessage().text();

            // 모델명
            String model = chatRequest.parameters() != null
                    ? chatRequest.parameters().modelName()
                    : "unknown";

            // 토큰 사용량
            Integer inputTokens = null;
            Integer outputTokens = null;
            TokenUsage tokenUsage = chatResponse.metadata() != null
                    ? chatResponse.metadata().tokenUsage()
                    : null;
            if (tokenUsage != null) {
                inputTokens = tokenUsage.inputTokenCount();
                outputTokens = tokenUsage.outputTokenCount();
            }

            langfuseTraceService.sendTrace(
                    traceId != null ? traceId : UUID.randomUUID().toString(),
                    "gemini-chat",
                    model,
                    inputMessages,
                    output,
                    inputTokens,
                    outputTokens,
                    durationMs
            );

        } catch (Exception e) {
            log.warn("LangFuse listener onResponse error: {}", e.getMessage());
        }
    }

    @Override
    public void onError(ChatModelErrorContext errorContext) {
        if (!langfuseTraceService.isEnabled()) return;

        try {
            String traceId = (String) errorContext.attributes().get(ATTR_TRACE_ID);
            Long startTime = (Long) errorContext.attributes().get(ATTR_START_TIME);
            long durationMs = startTime != null ? System.currentTimeMillis() - startTime : 0;

            ChatRequest chatRequest = errorContext.chatRequest();

            List<Map<String, String>> inputMessages = new ArrayList<>();
            if (chatRequest != null) {
                for (ChatMessage msg : chatRequest.messages()) {
                    String role = mapRole(msg.type());
                    inputMessages.add(Map.of("role", role, "content", extractText(msg)));
                }
            }

            String errorOutput = "ERROR: " + errorContext.error().getMessage();

            langfuseTraceService.sendTrace(
                    traceId != null ? traceId : UUID.randomUUID().toString(),
                    "gemini-chat-error",
                    "unknown",
                    inputMessages,
                    errorOutput,
                    null,
                    null,
                    durationMs
            );

        } catch (Exception e) {
            log.warn("LangFuse listener onError error: {}", e.getMessage());
        }
    }

    private String mapRole(ChatMessageType type) {
        return switch (type) {
            case SYSTEM -> "system";
            case USER -> "user";
            case AI -> "assistant";
            default -> type.name().toLowerCase();
        };
    }

    private String extractText(ChatMessage msg) {
        if (msg instanceof SystemMessage sm) {
            return sm.text();
        } else if (msg instanceof AiMessage ai) {
            return ai.text();
        } else if (msg instanceof UserMessage um) {
            StringBuilder sb = new StringBuilder();
            for (Content content : um.contents()) {
                if (content instanceof TextContent tc) {
                    sb.append(tc.text());
                }
            }
            return sb.toString();
        }
        return msg.toString();
    }
}