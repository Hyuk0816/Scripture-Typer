package com.scriptuotyper.service;

import com.scriptuotyper.common.exception.chat.ChatSessionNotFoundException;
import com.scriptuotyper.common.exception.user.UserNotFoundException;
import com.scriptuotyper.domain.chat.ChatMessage;
import com.scriptuotyper.domain.chat.ChatSession;
import com.scriptuotyper.domain.user.User;
import com.scriptuotyper.dto.chat.*;
import com.scriptuotyper.repository.ChatMessageRepository;
import com.scriptuotyper.repository.ChatSessionRepository;
import com.scriptuotyper.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final GeminiService geminiService;
    private final GeminiUsageService geminiUsageService;

    @Transactional(readOnly = true)
    public List<ChatSessionResponse> getSessions(Long userId) {
        return chatSessionRepository.findByUserIdOrderByUpdatedAtDesc(userId)
                .stream()
                .map(ChatSessionResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getMessages(Long sessionId, Long userId) {
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(ChatSessionNotFoundException::new);

        if (!session.getUser().getId().equals(userId)) {
            throw new ChatSessionNotFoundException();
        }

        return chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId)
                .stream()
                .map(ChatMessageResponse::from)
                .toList();
    }

    @Transactional
    public ChatSessionResponse createSession(Long userId, ChatSessionCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        String title = request.title().length() > 30
                ? request.title().substring(0, 30)
                : request.title();

        ChatSession session = ChatSession.builder()
                .user(user)
                .title(title)
                .bookName(request.bookName())
                .chapter(request.chapter())
                .build();

        chatSessionRepository.save(session);
        return ChatSessionResponse.from(session);
    }

    @Transactional
    public ChatMessageResponse addMessage(Long sessionId, Long userId, ChatMessageRequest request) {
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(ChatSessionNotFoundException::new);

        if (!session.getUser().getId().equals(userId)) {
            throw new ChatSessionNotFoundException();
        }

        ChatMessage message = ChatMessage.builder()
                .session(session)
                .role(request.role())
                .content(request.content())
                .build();

        chatMessageRepository.save(message);
        return ChatMessageResponse.from(message);
    }

    @Transactional
    public void deleteSession(Long sessionId, Long userId) {
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(ChatSessionNotFoundException::new);

        if (!session.getUser().getId().equals(userId)) {
            throw new ChatSessionNotFoundException();
        }

        chatSessionRepository.delete(session);
    }

    public ChatUsageResponse getUsage(Long userId) {
        return geminiUsageService.getUsageToday(userId);
    }

    public SseEmitter streamChat(Long userId, ChatStreamRequest request) {
        geminiUsageService.checkAndIncrement(userId);
        return geminiService.streamChat(request);
    }
}
