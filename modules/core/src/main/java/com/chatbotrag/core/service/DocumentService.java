package com.chatbotrag.core.service;

import com.chatbotrag.core.domain.Document;
import jakarta.inject.Singleton;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Singleton
public interface DocumentService {
    
    Mono<Document> save(Document document);
    
    Mono<Document> findById(String id);
    
    Flux<Document> findAll();
    
    Flux<Document> findByContentType(String contentType);
    
    Mono<Void> deleteById(String id);
    
    Mono<List<Document>> findSimilar(String content, int limit);
}