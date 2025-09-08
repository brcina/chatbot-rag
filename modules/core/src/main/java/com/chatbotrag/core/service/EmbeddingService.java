package com.chatbotrag.core.service;

import jakarta.inject.Singleton;
import reactor.core.publisher.Mono;

import java.util.List;

@Singleton
public interface EmbeddingService {
    
    Mono<List<Float>> embed(String text);
    
    Mono<List<List<Float>>> embedBatch(List<String> texts);
    
    Mono<Double> similarity(List<Float> embedding1, List<Float> embedding2);
}