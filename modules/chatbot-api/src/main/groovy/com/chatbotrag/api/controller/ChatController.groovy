package com.chatbotrag.api.controller

import com.chatbotrag.api.service.ChatService
import com.chatbotrag.api.dto.ChatRequest
import com.chatbotrag.api.dto.ChatResponse
import com.chatbotrag.core.domain.ChatMessage
import io.micronaut.http.annotation.*
import jakarta.inject.Inject
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Controller("/api/chat")
class ChatController {

    @Inject
    private ChatService chatService

    @Post
    Mono<ChatResponse> chat(@Body ChatRequest request) {
        chatService.processMessage(request.sessionId, request.message, request.userId)
            .map { message -> new ChatResponse(message.content, message.sessionId) }
            .onErrorReturn(ChatResponse.error("Failed to process message", request.sessionId))
    }

    @Get("/{sessionId}/history")
    Flux<ChatMessage> getHistory(@PathVariable String sessionId) {
        chatService.getConversationHistory(sessionId)
    }

    @Delete("/{sessionId}")
    Mono<Void> clearConversation(@PathVariable String sessionId) {
        chatService.clearConversation(sessionId)
    }
}