package com.chatbotrag.api.dto

import io.micronaut.serde.annotation.Serdeable

import java.time.LocalDateTime

@Serdeable
class ChatResponse {
    String message
    String sessionId
    LocalDateTime timestamp
    boolean success
    String error

    ChatResponse() {}

    ChatResponse(String message, String sessionId) {
        this.message = message
        this.sessionId = sessionId
        this.timestamp = LocalDateTime.now()
        this.success = true
    }

    static ChatResponse error(String error, String sessionId) {
        new ChatResponse().with {
            it.error = error
            it.sessionId = sessionId
            it.timestamp = LocalDateTime.now()
            it.success = false
            return it
        }
    }
}