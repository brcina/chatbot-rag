package com.chatbotrag.processor.service;

import com.chatbotrag.core.domain.Document;
import reactor.core.publisher.Mono;

import java.io.InputStream;

public interface DocumentProcessor {
    
    boolean supports(String contentType);
    
    Mono<Document> process(InputStream inputStream, String fileName, String contentType);
    
    default String extractTitle(String content) {
        // Extract title from first line or first 100 characters
        if (content == null || content.trim().isEmpty()) {
            return "Untitled Document";
        }
        
        String firstLine = content.split("\n")[0].trim();
        if (firstLine.length() > 100) {
            return firstLine.substring(0, 97) + "...";
        }
        return firstLine.isEmpty() ? "Untitled Document" : firstLine;
    }
}