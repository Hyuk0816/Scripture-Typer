package com.scriptuotyper.domain.bible;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "bible",
        uniqueConstraints = @UniqueConstraint(columnNames = {"book_name", "chapter", "verse"}),
        indexes = @Index(columnList = "book_name, chapter"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Bible {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Testament testament;

    @Column(name = "book_name", nullable = false)
    private String bookName;

    @Column(name = "book_order", nullable = false)
    private Integer bookOrder;

    @Column(nullable = false)
    private Integer chapter;

    @Column(nullable = false)
    private Integer verse;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Builder
    public Bible(Testament testament, String bookName, Integer bookOrder, Integer chapter, Integer verse, String content) {
        this.testament = testament;
        this.bookName = bookName;
        this.bookOrder = bookOrder;
        this.chapter = chapter;
        this.verse = verse;
        this.content = content;
    }
}
