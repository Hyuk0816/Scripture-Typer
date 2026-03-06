package com.scriptuotyper.dto.board;

import org.springframework.data.domain.Page;

import java.util.List;

public record BoardPage(
        List<BoardListResponse> content,
        int number,
        int totalPages,
        long totalElements
) {
    public static BoardPage from(Page<BoardListResponse> page) {
        return new BoardPage(
                page.getContent(),
                page.getNumber(),
                page.getTotalPages(),
                page.getTotalElements()
        );
    }
}
