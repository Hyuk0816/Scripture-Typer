package com.scriptuotyper.service;

import com.scriptuotyper.common.exception.board.BoardNotFoundException;
import com.scriptuotyper.common.exception.board.ReplyNotFoundException;
import com.scriptuotyper.common.exception.board.UnauthorizedBoardAccessException;
import com.scriptuotyper.common.exception.user.UserNotFoundException;
import com.scriptuotyper.domain.board.Board;
import com.scriptuotyper.domain.board.PostType;
import com.scriptuotyper.domain.board.Reply;
import com.scriptuotyper.domain.user.Role;
import com.scriptuotyper.domain.user.User;
import com.scriptuotyper.dto.board.BoardDetailResponse;
import com.scriptuotyper.dto.board.BoardListResponse;
import com.scriptuotyper.dto.board.BoardRequest;
import com.scriptuotyper.dto.board.ReplyRequest;
import com.scriptuotyper.repository.BoardRepository;
import com.scriptuotyper.repository.ReplyRepository;
import com.scriptuotyper.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final ReplyRepository replyRepository;
    private final UserRepository userRepository;

    private static final Set<Role> PRIVILEGED_ROLES = Set.of(Role.PASTOR, Role.MOKJANG, Role.ADMIN);

    @Transactional
    @CacheEvict(value = "board:list", allEntries = true)
    public Long createBoard(Long userId, BoardRequest request) {
        User user = findUser(userId);

        if (request.postType() == PostType.NOTICE && user.getRole() != Role.ADMIN) {
            throw new UnauthorizedBoardAccessException();
        }

        Board board = Board.builder()
                .user(user)
                .postType(request.postType())
                .title(request.title())
                .content(request.content())
                .build();
        return boardRepository.save(board).getId();
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "board:list", key = "T(String).valueOf(#postType) + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    public Page<BoardListResponse> getBoards(PostType postType, Pageable pageable) {
        Page<Board> boards;
        if (postType == PostType.NOTICE) {
            boards = boardRepository.findByPostTypeOrderByCreatedAtAsc(postType, pageable);
        } else if (postType != null) {
            boards = boardRepository.findByPostTypeOrderByCreatedAtDesc(postType, pageable);
        } else {
            boards = boardRepository.findAllWithNoticesPinned(pageable);
        }
        return boards.map(BoardListResponse::from);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "board:detail", key = "#boardId", unless = "#result.postType() == 'BIBLE_QUESTION'")
    public BoardDetailResponse getBoard(Long boardId, Long userId) {
        Board board = findBoard(boardId);
        User user = findUser(userId);

        if (board.getPostType() == PostType.BIBLE_QUESTION) {
            boolean isAuthor = board.getUser().getId().equals(userId);
            boolean isPrivileged = PRIVILEGED_ROLES.contains(user.getRole());
            if (!isAuthor && !isPrivileged) {
                throw new UnauthorizedBoardAccessException();
            }
        }

        return BoardDetailResponse.from(board);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "board:list", allEntries = true),
            @CacheEvict(value = "board:detail", key = "#boardId")
    })
    public void updateBoard(Long boardId, Long userId, BoardRequest request) {
        Board board = findBoard(boardId);
        if (!board.getUser().getId().equals(userId)) {
            throw new UnauthorizedBoardAccessException();
        }
        board.update(request.title(), request.content());
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "board:list", allEntries = true),
            @CacheEvict(value = "board:detail", key = "#boardId")
    })
    public void deleteBoard(Long boardId, Long userId) {
        Board board = findBoard(boardId);
        User user = findUser(userId);

        boolean isAuthor = board.getUser().getId().equals(userId);
        boolean isAdmin = user.getRole() == Role.ADMIN;
        if (!isAuthor && !isAdmin) {
            throw new UnauthorizedBoardAccessException();
        }

        boardRepository.delete(board);
    }

    @Transactional
    @CacheEvict(value = "board:detail", key = "#boardId")
    public Long createReply(Long boardId, Long userId, ReplyRequest request) {
        Board board = findBoard(boardId);
        User user = findUser(userId);

        if (board.getPostType() == PostType.NOTICE) {
            throw new UnauthorizedBoardAccessException();
        }

        if (board.getPostType() == PostType.BIBLE_QUESTION) {
            if (!PRIVILEGED_ROLES.contains(user.getRole())) {
                throw new UnauthorizedBoardAccessException();
            }
        }

        Reply reply = Reply.builder()
                .board(board)
                .user(user)
                .content(request.content())
                .build();
        return replyRepository.save(reply).getId();
    }

    @Transactional
    @CacheEvict(value = "board:detail", key = "#boardId")
    public void updateReply(Long boardId, Long replyId, Long userId, ReplyRequest request) {
        Reply reply = findReply(replyId);
        if (!reply.getUser().getId().equals(userId)) {
            throw new UnauthorizedBoardAccessException();
        }
        reply.update(request.content());
    }

    @Transactional
    @CacheEvict(value = "board:detail", key = "#boardId")
    public void deleteReply(Long boardId, Long replyId, Long userId) {
        Reply reply = findReply(replyId);
        User user = findUser(userId);

        boolean isAuthor = reply.getUser().getId().equals(userId);
        boolean isAdmin = user.getRole() == Role.ADMIN;
        if (!isAuthor && !isAdmin) {
            throw new UnauthorizedBoardAccessException();
        }

        replyRepository.delete(reply);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
    }

    private Board findBoard(Long boardId) {
        return boardRepository.findById(boardId)
                .orElseThrow(BoardNotFoundException::new);
    }

    private Reply findReply(Long replyId) {
        return replyRepository.findById(replyId)
                .orElseThrow(ReplyNotFoundException::new);
    }
}
