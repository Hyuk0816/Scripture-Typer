package com.scriptuotyper.repository;

import com.scriptuotyper.domain.board.Board;
import com.scriptuotyper.domain.board.PostType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BoardRepository extends JpaRepository<Board, Long> {

    Page<Board> findByPostTypeOrderByCreatedAtDesc(PostType postType, Pageable pageable);

    Page<Board> findByPostTypeOrderByCreatedAtAsc(PostType postType, Pageable pageable);

    Page<Board> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("""
            SELECT b FROM Board b
            LEFT JOIN FETCH b.user
            LEFT JOIN FETCH b.replies r
            LEFT JOIN FETCH r.user
            WHERE b.id = :id
            """)
    Optional<Board> findByIdWithDetails(@Param("id") Long id);

    @Query(value = """
            SELECT * FROM board
            ORDER BY CASE WHEN post_type = 'NOTICE' THEN 0 ELSE 1 END,
                     CASE WHEN post_type = 'NOTICE' THEN created_at END ASC,
                     CASE WHEN post_type != 'NOTICE' THEN created_at END DESC
            """, nativeQuery = true)
    Page<Board> findAllWithNoticesPinned(Pageable pageable);
}
