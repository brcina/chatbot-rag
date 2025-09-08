package com.chatbotrag.core.service;

import com.chatbotrag.core.domain.Document;
import com.chatbotrag.core.domain.SearchResult;
import jakarta.inject.Singleton;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Singleton
public interface VectorStoreService {
    
    Mono<Void> store(Document document, List<Float> embedding);
    
    Flux<SearchResult> search(String query, int limit);
    
    Flux<SearchResult> searchByEmbedding(List<Float> embedding, int limit);
    
    Mono<Void> delete(String documentId);
    
    Mono<Boolean> exists(String documentId);
}