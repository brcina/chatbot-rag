package com.chatbotrag.api.service

import com.chatbotrag.core.domain.ChatMessage
import jakarta.inject.Singleton
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Singleton
interface ChatService {
    
    Mono<ChatMessage> processMessage(String sessionId, String userMessage, String userId)
    
    Flux<ChatMessage> getConversationHistory(String sessionId)
    
    Mono<Void> clearConversation(String sessionId)
}