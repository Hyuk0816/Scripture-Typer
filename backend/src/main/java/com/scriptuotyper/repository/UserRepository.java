package com.scriptuotyper.repository;

import com.scriptuotyper.domain.user.User;
import com.scriptuotyper.domain.user.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByStatus(UserStatus status);

    @Query("SELECT u FROM User u WHERE u.affiliation.id = :affiliationId AND u.status = 'ACTIVE'")
    List<User> findActiveByAffiliationId(@Param("affiliationId") Long affiliationId);

    List<User> findByAffiliationId(Long affiliationId);

    long countByAffiliationId(Long affiliationId);
}