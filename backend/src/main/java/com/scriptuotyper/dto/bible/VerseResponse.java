package com.scriptuotyper.dto.bible;

import com.scriptuotyper.domain.bible.Bible;

public record VerseResponse(
        long id,
        int verse,
        String content
) {
    public static VerseResponse from(Bible bible) {
        return new VerseResponse(bible.getId(), bible.getVerse(), bible.getContent());
    }
}