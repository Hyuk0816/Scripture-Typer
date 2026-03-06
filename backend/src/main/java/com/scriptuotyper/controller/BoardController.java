package com.scriptuotyper.controller;

import com.scriptuotyper.domain.board.PostType;
import com.scriptuotyper.dto.board.BoardDetailResponse;
import com.scriptuotyper.dto.board.BoardListResponse;
import com.scriptuotyper.dto.board.BoardRequest;
import com.scriptuotyper.dto.board.ReplyRequest;
import com.scriptuotyper.service.BoardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    @PostMapping
    public ResponseEntity<Void> createBoard(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody BoardRequest request
    ) {
        Long boardId = boardService.createBoard(userId, request);
        return ResponseEntity.created(URI.create("/api/boards/" + boardId)).build();
    }

    @GetMapping
    public Page<BoardListResponse> getBoards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) PostType postType
    ) {
        return boardService.getBoards(postType, PageRequest.of(page, size));
    }

    @GetMapping("/{id}")
    public BoardDetailResponse getBoard(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId
    ) {
        return boardService.getBoard(id, userId);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateBoard(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody BoardRequest request
    ) {
        boardService.updateBoard(id, userId, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBoard(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId
    ) {
        boardService.deleteBoard(id, userId);
    }

    @PostMapping("/{id}/replies")
    public ResponseEntity<Void> createReply(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody ReplyRequest request
    ) {
        Long replyId = boardService.createReply(id, userId, request);
        return ResponseEntity.created(URI.create("/api/boards/" + id + "/replies/" + replyId)).build();
    }

    @PutMapping("/{id}/replies/{replyId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateReply(
            @PathVariable Long id,
            @PathVariable Long replyId,
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody ReplyRequest request
    ) {
        boardService.updateReply(id, replyId, userId, request);
    }

    @DeleteMapping("/{id}/replies/{replyId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReply(
            @PathVariable Long id,
            @PathVariable Long replyId,
            @AuthenticationPrincipal Long userId
    ) {
        boardService.deleteReply(id, replyId, userId);
    }
}
