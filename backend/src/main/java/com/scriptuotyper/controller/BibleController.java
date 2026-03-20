package com.scriptuotyper.controller;

import com.scriptuotyper.dto.bible.BooksResponse;
import com.scriptuotyper.dto.bible.ChapterResponse;
import com.scriptuotyper.dto.bible.DailyVerseResponse;
import com.scriptuotyper.service.BibleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/bible")
@RequiredArgsConstructor
public class BibleController {

    private static final CacheControl BIBLE_CACHE = CacheControl.maxAge(24, TimeUnit.HOURS).cachePublic();

    private final BibleService bibleService;

    @GetMapping("/daily-verse")
    public ResponseEntity<DailyVerseResponse> getDailyVerse() {
        return ResponseEntity.ok()
                .cacheControl(BIBLE_CACHE)
                .body(bibleService.getDailyVerse(LocalDate.now()));
    }

    @GetMapping("/books")
    public ResponseEntity<BooksResponse> getBooks() {
        return ResponseEntity.ok()
                .cacheControl(BIBLE_CACHE)
                .body(bibleService.getBooks());
    }

    @GetMapping("/{bookName}/{chapter}")
    public ResponseEntity<ChapterResponse> getChapter(
            @PathVariable String bookName,
            @PathVariable int chapter
    ) {
        return ResponseEntity.ok()
                .cacheControl(BIBLE_CACHE)
                .body(bibleService.getChapter(bookName, chapter));
    }
}
