package com.scriptuotyper.domain.group;

import com.scriptuotyper.domain.affiliation.Affiliation;
import com.scriptuotyper.domain.progress.ProgressMode;
import com.scriptuotyper.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "group_reading_plans")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroupReadingPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "affiliation_id", nullable = false)
    private Affiliation affiliation;

    @Column(nullable = false)
    private String bookName;

    @Column(nullable = false)
    private int startChapter;

    @Column(nullable = false)
    private int endChapter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProgressMode mode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GroupPlanStatus status = GroupPlanStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public GroupReadingPlan(Affiliation affiliation, String bookName, int startChapter, int endChapter, ProgressMode mode, User createdBy) {
        this.affiliation = affiliation;
        this.bookName = bookName;
        this.startChapter = startChapter;
        this.endChapter = endChapter;
        this.mode = mode;
        this.createdBy = createdBy;
    }

    public void complete() {
        this.status = GroupPlanStatus.COMPLETED;
    }
}
