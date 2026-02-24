package com.scriptuotyper.repository;

import com.scriptuotyper.domain.board.Reply;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReplyRepository extends JpaRepository<Reply, Long> {

    List<Reply> findByBoardIdOrderByCreatedAtAsc(Long boardId);
}
