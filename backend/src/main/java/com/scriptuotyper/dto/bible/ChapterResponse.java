package com.scriptuotyper.dto.bible;

import java.util.List;

public record ChapterResponse(
        String bookName,
        int chapter,
        List<VerseResponse> verses
) {
}