package com.scriptuotyper.repository;

import com.scriptuotyper.domain.progress.ProgressMode;
import com.scriptuotyper.domain.progress.UserProgress;
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
            SELECT CAST(p.updatedAt AS LocalDate) AS progressDate,
                   p.user.id AS userId, p.user.name AS userName,
                   p.mode AS mode, SUM(p.readCount) AS totalCount
            FROM UserProgress p
            WHERE p.updatedAt >= :start AND p.updatedAt < :end
            GROUP BY CAST(p.updatedAt AS LocalDate), p.user.id, p.user.name, p.mode
            ORDER BY CAST(p.updatedAt AS LocalDate), p.user.name
            """)
    List<Object[]> countDailyProgressByUser(@Param("start") LocalDateTime start,
                                            @Param("end") LocalDateTime end);

    @Query("""
            SELECT p.mode AS mode, SUM(p.readCount) AS totalCount
            FROM UserProgress p
            WHERE p.updatedAt >= :start AND p.updatedAt < :end
            GROUP BY p.mode
            """)
    List<Object[]> countMonthlyProgress(@Param("start") LocalDateTime start,
                                        @Param("end") LocalDateTime end);
}