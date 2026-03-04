package com.scriptuotyper.repository;

import com.scriptuotyper.domain.log.UserApiLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserApiLogRepository extends JpaRepository<UserApiLog, Long> {
}
