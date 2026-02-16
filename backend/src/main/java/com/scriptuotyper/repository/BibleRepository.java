package com.scriptuotyper.repository;

import com.scriptuotyper.domain.bible.Bible;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BibleRepository extends JpaRepository<Bible, Long> {

    @Query("SELECT b.bookName AS bookName, b.bookOrder AS bookOrder, b.testament AS testament, MAX(b.chapter) AS totalChapters " +
            "FROM Bible b GROUP BY b.bookName, b.bookOrder, b.testament ORDER BY b.bookOrder")
    List<BookProjection> findBookSummaries();

    List<Bible> findByBookNameAndChapterOrderByVerseAsc(String bookName, int chapter);

    interface BookProjection {
        String getBookName();
        Integer getBookOrder();
        String getTestament();
        Integer getTotalChapters();
    }
}