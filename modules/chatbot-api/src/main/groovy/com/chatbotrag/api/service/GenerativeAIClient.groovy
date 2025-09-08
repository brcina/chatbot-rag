package com.chatbotrag.api.service

import jakarta.inject.Singleton
import reactor.core.publisher.Mono

@Singleton
interface GenerativeAIClient {
    
    Mono<String> generateResponse(String prompt, String model)
    
    Mono<String> generateStreamResponse(String prompt, String model)
    
    Mono<List<String>> getAvailableModels()
    
    Mono<Boolean> isModelAvailable(String model)
}