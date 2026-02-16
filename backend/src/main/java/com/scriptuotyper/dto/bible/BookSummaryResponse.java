package com.scriptuotyper.dto.bible;

import com.scriptuotyper.repository.BibleRepository.BookProjection;

public record BookSummaryResponse(
        String bookName,
        int bookOrder,
        int totalChapters
) {
    public static BookSummaryResponse from(BookProjection projection) {
        return new BookSummaryResponse(
                projection.getBookName(),
                projection.getBookOrder(),
                projection.getTotalChapters()
        );
    }
}