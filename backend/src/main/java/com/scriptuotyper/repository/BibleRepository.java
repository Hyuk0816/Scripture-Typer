package com.scriptuotyper.repository;

import com.scriptuotyper.domain.bible.Bible;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BibleRepository extends JpaRepository<Bible, Long> {
}