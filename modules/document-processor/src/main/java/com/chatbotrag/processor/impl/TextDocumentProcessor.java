package com.chatbotrag.processor.impl;

import com.chatbotrag.core.domain.Document;
import com.chatbotrag.processor.service.DocumentProcessor;
import jakarta.inject.Singleton;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Singleton
public class TextDocumentProcessor implements DocumentProcessor {

    @Override
    public boolean supports(String contentType) {
        return contentType != null && (
            contentType.startsWith("text/") ||
            contentType.equals("application/json") ||
            contentType.equals("application/xml")
        );
    }

    @Override
    public Mono<Document> process(InputStream inputStream, String fileName, String contentType) {
        return Mono.fromCallable(() -> {
            try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                
                String content = reader.lines().collect(Collectors.joining("\n"));
                
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("fileName", fileName);
                metadata.put("encoding", "UTF-8");
                metadata.put("lineCount", content.split("\n").length);
                metadata.put("characterCount", content.length());
                
                Document document = new Document(
                    UUID.randomUUID().toString(),
                    extractTitle(content),
                    content,
                    contentType,
                    fileName
                );
                document.setMetadata(metadata);
                
                return document;
            }
        });
    }
}