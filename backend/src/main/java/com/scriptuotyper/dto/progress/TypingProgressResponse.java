package com.scriptuotyper.dto.progress;

import com.scriptuotyper.domain.progress.UserProgress;

public record TypingProgressResponse(
        String bookName,
        int chapter,
        int lastTypedVerse,
        int readCount,
        int totalVerses
) {
    public static TypingProgressResponse from(UserProgress progress, int totalVerses) {
        return new TypingProgressResponse(
                progress.getBookName(),
                progress.getChapter(),
                progress.getLastTypedVerse(),
                progress.getReadCount(),
                totalVerses
        );
    }
}
