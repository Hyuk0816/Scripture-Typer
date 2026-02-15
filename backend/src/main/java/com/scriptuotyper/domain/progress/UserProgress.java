package com.scriptuotyper.domain.progress;

import com.scriptuotyper.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_progress",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "mode", "book_name", "chapter"}),
        indexes = @Index(columnList = "user_id, updated_at"))
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProgressMode mode;

    @Column(name = "book_name", nullable = false)
    private String bookName;

    @Column(nullable = false)
    private Integer chapter;

    @Column(name = "last_typed_verse", nullable = false)
    private Integer lastTypedVerse;

    @Column(name = "read_count", nullable = false)
    private Integer readCount = 0;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public UserProgress(User user, ProgressMode mode, String bookName, Integer chapter, Integer lastTypedVerse) {
        this.user = user;
        this.mode = mode;
        this.bookName = bookName;
        this.chapter = chapter;
        this.lastTypedVerse = lastTypedVerse;
    }

    public void updateLastTypedVerse(Integer lastTypedVerse) {
        this.lastTypedVerse = lastTypedVerse;
    }

    public void incrementReadCount() {
        this.readCount++;
    }
}
