package com.scriptuotyper.dto.board;

import com.scriptuotyper.domain.board.Board;

import java.time.LocalDateTime;

public record BoardListResponse(
        Long id,
        String postType,
        String title,
        String authorName,
        String authorRole,
        int replyCount,
        LocalDateTime createdAt
) {
    public static BoardListResponse from(Board board) {
        return new BoardListResponse(
                board.getId(),
                board.getPostType().name(),
                board.getTitle(),
                board.getUser().getName(),
                board.getUser().getRole().name(),
                board.getReplies().size(),
                board.getCreatedAt()
        );
    }
}
