package com.scriptuotyper.dto.board;

import com.scriptuotyper.domain.board.Board;

import java.time.LocalDateTime;
import java.util.List;

public record BoardDetailResponse(
        Long id,
        String postType,
        String title,
        String content,
        Long authorId,
        String authorName,
        String authorRole,
        List<ReplyResponse> replies,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static BoardDetailResponse from(Board board) {
        List<ReplyResponse> replyResponses = board.getReplies().stream()
                .map(ReplyResponse::from)
                .toList();

        return new BoardDetailResponse(
                board.getId(),
                board.getPostType().name(),
                board.getTitle(),
                board.getContent(),
                board.getUser().getId(),
                board.getUser().getName(),
                board.getUser().getRole().name(),
                replyResponses,
                board.getCreatedAt(),
                board.getUpdatedAt()
        );
    }
}
