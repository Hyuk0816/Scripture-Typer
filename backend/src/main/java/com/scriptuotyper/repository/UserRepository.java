package com.scriptuotyper.repository;

import com.scriptuotyper.domain.user.User;
import com.scriptuotyper.domain.user.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByStatus(UserStatus status);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.affiliation WHERE u.id = :id")
    Optional<User> findByIdWithAffiliation(@Param("id") Long id);

    @Query("SELECT u FROM User u WHERE u.affiliation.id = :affiliationId AND u.status = 'ACTIVE'")
    List<User> findActiveByAffiliationId(@Param("affiliationId") Long affiliationId);

    List<User> findByAffiliationId(Long affiliationId);

    @Query("SELECT u FROM User u WHERE u.affiliation.id IN :affiliationIds")
    List<User> findByAffiliationIdIn(@Param("affiliationIds") Collection<Long> affiliationIds);

    long countByAffiliationId(Long affiliationId);
}