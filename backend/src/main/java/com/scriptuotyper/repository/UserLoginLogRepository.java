package com.scriptuotyper.repository;

import com.scriptuotyper.domain.user.UserLoginLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface UserLoginLogRepository extends JpaRepository<UserLoginLog, Long> {

    @Query("""
            SELECT CAST(l.loginAt AS LocalDate) AS loginDate, COUNT(l) AS cnt
            FROM UserLoginLog l
            WHERE l.loginAt >= :start AND l.loginAt < :end
            GROUP BY CAST(l.loginAt AS LocalDate)
            ORDER BY CAST(l.loginAt AS LocalDate)
            """)
    List<Object[]> countDailyLogins(@Param("start") LocalDateTime start,
                                    @Param("end") LocalDateTime end);

    @Query("""
            SELECT CAST(l.loginAt AS LocalDate) AS loginDate,
                   l.user.id AS userId, l.user.name AS userName, COUNT(l) AS cnt
            FROM UserLoginLog l
            WHERE l.loginAt >= :start AND l.loginAt < :end
            GROUP BY CAST(l.loginAt AS LocalDate), l.user.id, l.user.name
            ORDER BY CAST(l.loginAt AS LocalDate), l.user.name
            """)
    List<Object[]> countDailyLoginsByUser(@Param("start") LocalDateTime start,
                                          @Param("end") LocalDateTime end);
}
