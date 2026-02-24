package com.scriptuotyper.dto.chat;

import java.util.List;

public record ChatStreamRequest(
        List<MessageItem> messages,
        ChatContext context
) {
    public record MessageItem(String role, String content) {}

    public record ChatContext(String bookName, Integer chapter, Integer verse) {}
}
