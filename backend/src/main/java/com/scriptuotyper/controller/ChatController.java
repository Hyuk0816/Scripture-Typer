package com.scriptuotyper.controller;

import com.scriptuotyper.dto.chat.*;
import com.scriptuotyper.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(
            @AuthenticationPrincipal Long userId,
            @RequestBody ChatStreamRequest request) {
        return chatService.streamChat(userId, request);
    }

    @GetMapping("/usage")
    public ChatUsageResponse getUsage(@AuthenticationPrincipal Long userId) {
        return chatService.getUsage(userId);
    }

    @GetMapping("/sessions")
    public List<ChatSessionResponse> getSessions(@AuthenticationPrincipal Long userId) {
        return chatService.getSessions(userId);
    }

    @GetMapping("/sessions/{id}/messages")
    public List<ChatMessageResponse> getMessages(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId) {
        return chatService.getMessages(id, userId);
    }

    @PostMapping("/sessions")
    @ResponseStatus(HttpStatus.CREATED)
    public ChatSessionResponse createSession(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody ChatSessionCreateRequest request) {
        return chatService.createSession(userId, request);
    }

    @PostMapping("/sessions/{id}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public ChatMessageResponse addMessage(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody ChatMessageRequest request) {
        return chatService.addMessage(id, userId, request);
    }

    @DeleteMapping("/sessions/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSession(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId) {
        chatService.deleteSession(id, userId);
    }
}
