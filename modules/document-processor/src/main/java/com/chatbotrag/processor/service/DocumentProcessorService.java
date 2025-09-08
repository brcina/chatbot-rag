package com.chatbotrag.processor.service;

import com.chatbotrag.core.domain.Document;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.util.List;

@Singleton
public class DocumentProcessorService {
    
    private final List<DocumentProcessor> processors;
    
    @Inject
    public DocumentProcessorService(List<DocumentProcessor> processors) {
        this.processors = processors;
    }
    
    public Mono<Document> processDocument(InputStream inputStream, String fileName, String contentType) {
        return processors.stream()
            .filter(processor -> processor.supports(contentType))
            .findFirst()
            .map(processor -> processor.process(inputStream, fileName, contentType))
            .orElse(Mono.error(new UnsupportedOperationException(
                "No processor found for content type: " + contentType)));
    }
    
    public boolean isSupported(String contentType) {
        return processors.stream().anyMatch(processor -> processor.supports(contentType));
    }
}