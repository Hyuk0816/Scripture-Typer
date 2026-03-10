package com.scriptuotyper.domain.group;

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
@Table(name = "group_plan_members", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"plan_id", "user_id"})
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroupPlanMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private GroupReadingPlan plan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GroupInviteStatus status = GroupInviteStatus.PENDING;

    @CreatedDate
    @Column(name = "invited_at", nullable = false, updatable = false)
    private LocalDateTime invitedAt;

    @Column(name = "assigned_start_chapter")
    private Integer assignedStartChapter;

    @Column(name = "assigned_end_chapter")
    private Integer assignedEndChapter;

    @Builder
    public GroupPlanMember(GroupReadingPlan plan, User user, GroupInviteStatus status) {
        this.plan = plan;
        this.user = user;
        this.status = status;
    }

    public void accept() {
        this.status = GroupInviteStatus.ACCEPTED;
    }

    public void decline() {
        this.status = GroupInviteStatus.DECLINED;
    }

    public int getEffectiveStartChapter() {
        return assignedStartChapter != null ? assignedStartChapter : plan.getStartChapter();
    }

    public int getEffectiveEndChapter() {
        return assignedEndChapter != null ? assignedEndChapter : plan.getEndChapter();
    }

    public int getAssignedTotalChapters() {
        return getEffectiveEndChapter() - getEffectiveStartChapter() + 1;
    }

    public void assignChapterRange(Integer start, Integer end) {
        this.assignedStartChapter = start;
        this.assignedEndChapter = end;
    }
}
