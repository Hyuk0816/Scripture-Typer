package com.scriptuotyper.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scriptuotyper.domain.chat.GeminiPromptTemplate;
import com.scriptuotyper.dto.chat.ChatStreamRequest;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.googleai.GoogleAiGeminiStreamingChatModel;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class GeminiService {

    private final StreamingChatModel streamingModel;
    private final ObjectMapper objectMapper;

    public GeminiService(
            @Value("${gemini.api-key}") String apiKey,
            @Value("${gemini.model}") String model,
            LangfuseChatModelListener langfuseListener) {
        this.streamingModel = GoogleAiGeminiStreamingChatModel.builder()
                .apiKey(apiKey)
                .modelName(model)
                .maxOutputTokens(1024)
                .temperature(0.7)
                .listeners(List.of(langfuseListener))
                .build();
        this.objectMapper = new ObjectMapper();
        log.info("GeminiService initialized with LangChain4j + LangFuse - model: {}", model);
    }

    public SseEmitter streamChat(ChatStreamRequest request) {
        SseEmitter emitter = new SseEmitter(120_000L);

        List<ChatMessage> messages = buildMessages(request);

        streamingModel.chat(messages, new StreamingChatResponseHandler() {
            @Override
            public void onPartialResponse(String partialResponse) {
                try {
                    String json = objectMapper.writeValueAsString(Map.of("content", partialResponse));
                    emitter.send(SseEmitter.event().data(json));
                } catch (IOException e) {
                    log.debug("Emitter send failed: {}", e.getMessage());
                }
            }

            @Override
            public void onCompleteResponse(ChatResponse completeResponse) {
                try {
                    emitter.send(SseEmitter.event().data("[DONE]"));
                    emitter.complete();
                } catch (IOException e) {
                    log.debug("Emitter already completed");
                }
            }

            @Override
            public void onError(Throwable error) {
                log.error("Gemini streaming error: {}", error.getMessage());
                try {
                    String errorJson = objectMapper.writeValueAsString(
                            Map.of("error", "Gemini API 오류: " + error.getMessage())
                    );
                    emitter.send(SseEmitter.event().data(errorJson));
                    emitter.send(SseEmitter.event().data("[DONE]"));
                    emitter.complete();
                } catch (IOException e) {
                    emitter.completeWithError(error);
                }
            }
        });

        return emitter;
    }

    private List<ChatMessage> buildMessages(ChatStreamRequest request) {
        List<ChatMessage> messages = new ArrayList<>();

        // System prompt
        String systemPrompt = buildSystemPrompt(request.context());
        messages.add(SystemMessage.from(systemPrompt));

        // Conversation history
        for (ChatStreamRequest.MessageItem msg : request.messages()) {
            if ("user".equals(msg.role())) {
                messages.add(UserMessage.from(msg.content()));
            } else if ("assistant".equals(msg.role())) {
                messages.add(AiMessage.from(msg.content()));
            }
        }

        return messages;
    }

    private String buildSystemPrompt(ChatStreamRequest.ChatContext context) {
        StringBuilder prompt = new StringBuilder();
        prompt.append(GeminiPromptTemplate.SYSTEM_PROMPT.getSystemPrompt());

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