package com.scriptuotyper.repository;

import com.scriptuotyper.domain.progress.ProgressMode;
import com.scriptuotyper.domain.progress.UserProgress;
import com.scriptuotyper.dto.admin.DailyProgressByUser;
import com.scriptuotyper.dto.admin.MonthlyProgressCount;
import com.scriptuotyper.dto.ranking.UserTypingCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ProgressRepository extends JpaRepository<UserProgress, Long> {

    Optional<UserProgress> findByUserIdAndModeAndBookNameAndChapter(
            Long userId, ProgressMode mode, String bookName, Integer chapter);

    List<UserProgress> findByUserIdAndMode(Long userId, ProgressMode mode);

    Optional<UserProgress> findFirstByUserIdAndModeOrderByUpdatedAtDesc(Long userId, ProgressMode mode);

    List<UserProgress> findByUserIdAndModeOrderByUpdatedAtDesc(Long userId, ProgressMode mode);

    @Query("""
            SELECT p.user.id, SUM(p.readCount)
            FROM UserProgress p
            WHERE p.mode = com.scriptuotyper.domain.progress.ProgressMode.TYPING
              AND p.readCount > 0
              AND p.user.status = com.scriptuotyper.domain.user.UserStatus.ACTIVE
            GROUP BY p.user.id
            """)
    List<UserTypingCount> sumTypingReadCountByActiveUser();

    @Query("""
            SELECT CAST(p.updatedAt AS LocalDate), p.user.id, p.user.name,
                   p.mode, SUM(p.readCount)
            FROM UserProgress p
            WHERE p.updatedAt >= :start AND p.updatedAt < :end
            GROUP BY CAST(p.updatedAt AS LocalDate), p.user.id, p.user.name, p.mode
            ORDER BY CAST(p.updatedAt AS LocalDate), p.user.name
            """)
    List<DailyProgressByUser> countDailyProgressByUser(@Param("start") LocalDateTime start,
                                                       @Param("end") LocalDateTime end);

    @Query("""
            SELECT p.mode, SUM(p.readCount)
            FROM UserProgress p
            WHERE p.updatedAt >= :start AND p.updatedAt < :end
            GROUP BY p.mode
            """)
    List<MonthlyProgressCount> countMonthlyProgress(@Param("start") LocalDateTime start,
                                                    @Param("end") LocalDateTime end);
}