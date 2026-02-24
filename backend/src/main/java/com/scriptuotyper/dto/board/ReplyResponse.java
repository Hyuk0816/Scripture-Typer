package com.scriptuotyper.dto.board;

import com.scriptuotyper.domain.board.Reply;

import java.time.LocalDateTime;

public record ReplyResponse(
        Long id,
        String content,
        Long authorId,
        String authorName,
        String authorRole,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ReplyResponse from(Reply reply) {
        return new ReplyResponse(
                reply.getId(),
                reply.getContent(),
                reply.getUser().getId(),
                reply.getUser().getName(),
                reply.getUser().getRole().name(),
                reply.getCreatedAt(),
                reply.getUpdatedAt()
        );
    }
}
