package com.scriptuotyper.repository;

import com.scriptuotyper.domain.log.UserMenuAccessLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserMenuAccessLogRepository extends JpaRepository<UserMenuAccessLog, Long> {
}
