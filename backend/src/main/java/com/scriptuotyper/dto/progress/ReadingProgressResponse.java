package com.scriptuotyper.dto.progress;

import com.scriptuotyper.domain.progress.UserProgress;

public record ReadingProgressResponse(
        String bookName,
        int chapter,
        int lastReadVerse,
        int readCount
) {
    public static ReadingProgressResponse from(UserProgress progress) {
        return new ReadingProgressResponse(
                progress.getBookName(),
                progress.getChapter(),
                progress.getLastTypedVerse(),
                progress.getReadCount()
        );
    }
}