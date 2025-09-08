package com.chatbotrag.api.dto

import io.micronaut.serde.annotation.Serdeable

@Serdeable
class ChatRequest {
    String message
    String sessionId
    String userId

    ChatRequest() {}

    ChatRequest(String message, String sessionId, String userId) {
        this.message = message
        this.sessionId = sessionId
        this.userId = userId
    }
}