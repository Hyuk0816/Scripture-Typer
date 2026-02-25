package com.scriptuotyper.dto.chat;

import com.scriptuotyper.domain.chat.ChatSession;

import java.time.LocalDateTime;

public record ChatSessionResponse(
        Long id,
        String title,
        String bookName,
        Integer chapter,
        int messageCount,
        LocalDateTime updatedAt
) {
    public static ChatSessionResponse from(ChatSession session) {
        return new ChatSessionResponse(
                session.getId(),
                session.getTitle(),
                session.getBookName(),
                session.getChapter(),
                session.getMessages().size(),
                session.getUpdatedAt()
        );
    }
}
