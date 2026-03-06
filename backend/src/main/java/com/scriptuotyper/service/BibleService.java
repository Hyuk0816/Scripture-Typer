package com.scriptuotyper.service;

import com.scriptuotyper.dto.bible.*;
import com.scriptuotyper.repository.BibleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BibleService {

    private final BibleRepository bibleRepository;

    @Cacheable("bible:books")
    public BooksResponse getBooks() {
        Map<String, List<BookSummaryResponse>> grouped = bibleRepository.findBookSummaries().stream()
                .collect(Collectors.groupingBy(
                        BibleRepository.BookProjection::getTestament,
                        Collectors.mapping(BookSummaryResponse::from, Collectors.toList())
                ));

        return new BooksResponse(
                grouped.getOrDefault("OLD", List.of()),
                grouped.getOrDefault("NEW", List.of())
        );
    }

    @Cacheable(value = "bible:chapter", key = "#bookName + ':' + #chapter")
    public ChapterResponse getChapter(String bookName, int chapter) {
        var verses = bibleRepository.findByBookNameAndChapterOrderByVerseAsc(bookName, chapter)
                .stream()
                .map(VerseResponse::from)
                .toList();

        return new ChapterResponse(bookName, chapter, verses);
    }
}