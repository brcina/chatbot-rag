package com.chatbotrag.api.service

import com.chatbotrag.core.domain.SearchResult
import jakarta.inject.Singleton
import reactor.core.publisher.Mono

@Singleton
interface RAGService {
    
    Mono<String> generateRAGResponse(String query, String sessionId)
    
    Mono<List<SearchResult>> retrieveRelevantDocuments(String query, int limit)
    
    Mono<String> augmentPrompt(String userQuery, List<SearchResult> context)
}