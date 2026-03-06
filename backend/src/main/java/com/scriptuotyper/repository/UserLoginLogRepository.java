package com.scriptuotyper.repository;

import com.scriptuotyper.domain.user.UserLoginLog;
import com.scriptuotyper.dto.admin.DailyLoginByUser;
import com.scriptuotyper.dto.admin.DailyLoginCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface UserLoginLogRepository extends JpaRepository<UserLoginLog, Long> {

    @Query("""
            SELECT CAST(l.loginAt AS LocalDate), COUNT(l)
            FROM UserLoginLog l
            WHERE l.loginAt >= :start AND l.loginAt < :end
            GROUP BY CAST(l.loginAt AS LocalDate)
            ORDER BY CAST(l.loginAt AS LocalDate)
            """)
    List<DailyLoginCount> countDailyLogins(@Param("start") LocalDateTime start,
                                           @Param("end") LocalDateTime end);

    @Query("""
            SELECT CAST(l.loginAt AS LocalDate), l.user.id, l.user.name, COUNT(l)
            FROM UserLoginLog l
            WHERE l.loginAt >= :start AND l.loginAt < :end
            GROUP BY CAST(l.loginAt AS LocalDate), l.user.id, l.user.name
            ORDER BY CAST(l.loginAt AS LocalDate), l.user.name
            """)
    List<DailyLoginByUser> countDailyLoginsByUser(@Param("start") LocalDateTime start,
                                                  @Param("end") LocalDateTime end);
}
