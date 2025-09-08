package com.chatbotrag.core.domain;

import java.time.LocalDateTime;

public class ChatMessage {
    public enum Role {
        USER, ASSISTANT, SYSTEM
    }

    private String id;
    private String sessionId;
    private Role role;
    private String content;
    private LocalDateTime timestamp;
    private String userId;

    public ChatMessage() {}

    public ChatMessage(String sessionId, Role role, String content, String userId) {
        this.sessionId = sessionId;
        this.role = role;
        this.content = content;
        this.userId = userId;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}