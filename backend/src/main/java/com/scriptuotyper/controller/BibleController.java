package com.scriptuotyper.controller;

import com.scriptuotyper.dto.bible.BooksResponse;
import com.scriptuotyper.dto.bible.ChapterResponse;
import com.scriptuotyper.service.BibleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bible")
@RequiredArgsConstructor
public class BibleController {

    private final BibleService bibleService;

    @GetMapping("/books")
    public BooksResponse getBooks() {
        return bibleService.getBooks();
    }

    @GetMapping("/{bookName}/{chapter}")
    public ChapterResponse getChapter(
            @PathVariable String bookName,
            @PathVariable int chapter
    ) {
        return bibleService.getChapter(bookName, chapter);
    }
}