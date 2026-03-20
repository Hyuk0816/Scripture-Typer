package com.scriptuotyper.dto.bible;

import com.scriptuotyper.domain.bible.Bible;

public record DailyVerseResponse(
        String bookName,
        int chapter,
        int verse,
        String content
) {
    public static DailyVerseResponse from(Bible bible) {
        return new DailyVerseResponse(
                bible.getBookName(),
                bible.getChapter(),
                bible.getVerse(),
                bible.getContent()
        );
    }
}
