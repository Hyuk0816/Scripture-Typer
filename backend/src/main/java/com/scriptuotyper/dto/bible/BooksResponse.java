package com.scriptuotyper.dto.bible;

import java.util.List;

public record BooksResponse(
        List<BookSummaryResponse> oldTestament,
        List<BookSummaryResponse> newTestament
) {
}