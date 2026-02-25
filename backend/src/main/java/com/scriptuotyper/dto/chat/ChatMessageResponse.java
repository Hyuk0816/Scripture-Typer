package com.scriptuotyper.dto.chat;

import com.scriptuotyper.domain.chat.ChatMessage;

import java.time.LocalDateTime;

public record ChatMessageResponse(
        Long id,
        String role,
        String content,
        LocalDateTime createdAt
) {
    public static ChatMessageResponse from(ChatMessage message) {
        return new ChatMessageResponse(
                message.getId(),
                message.getRole(),
                message.getContent(),
                message.getCreatedAt()
        );
    }
}
