package com.scriptuotyper.repository;

import com.scriptuotyper.domain.board.Board;
import com.scriptuotyper.domain.board.PostType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository<Board, Long> {

    Page<Board> findByPostTypeOrderByCreatedAtDesc(PostType postType, Pageable pageable);

    Page<Board> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
