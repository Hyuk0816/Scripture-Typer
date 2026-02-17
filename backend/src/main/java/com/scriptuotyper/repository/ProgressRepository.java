package com.scriptuotyper.repository;

import com.scriptuotyper.domain.progress.ProgressMode;
import com.scriptuotyper.domain.progress.UserProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProgressRepository extends JpaRepository<UserProgress, Long> {

    Optional<UserProgress> findByUserIdAndModeAndBookNameAndChapter(
            Long userId, ProgressMode mode, String bookName, Integer chapter);

    List<UserProgress> findByUserIdAndMode(Long userId, ProgressMode mode);
}