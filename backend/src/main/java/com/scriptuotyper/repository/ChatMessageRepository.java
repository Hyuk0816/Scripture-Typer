package com.scriptuotyper.repository;

import com.scriptuotyper.domain.chat.ChatMessage;
import com.scriptuotyper.dto.admin.DailyChatByUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findBySessionIdOrderByCreatedAtAsc(Long sessionId);

    @Query("""
            SELECT CAST(m.createdAt AS LocalDate), m.session.user.id, m.session.user.name,
                   COUNT(m)
            FROM ChatMessage m
            WHERE m.role = 'user' AND m.createdAt >= :start AND m.createdAt < :end
            GROUP BY CAST(m.createdAt AS LocalDate), m.session.user.id, m.session.user.name
            ORDER BY CAST(m.createdAt AS LocalDate), m.session.user.name
            """)
    List<DailyChatByUser> countDailyChatByUser(@Param("start") LocalDateTime start,
                                               @Param("end") LocalDateTime end);

    @Query("""
            SELECT COUNT(m)
            FROM ChatMessage m
            WHERE m.role = 'user' AND m.createdAt >= :start AND m.createdAt < :end
            """)
    long countMonthlyChatQuestions(@Param("start") LocalDateTime start,
                                   @Param("end") LocalDateTime end);
}
