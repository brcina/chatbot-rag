package com.chatbotrag.processor.service

import dev.langchain4j.data.document.Document
import dev.langchain4j.data.segment.TextSegment
import jakarta.inject.Inject
import jakarta.inject.Singleton
import reactor.core.publisher.Mono

@Singleton
class DocumentChunkerService {

    private final List<DocumentChunker> chunkers

    @Inject
    DocumentChunkerService(List<DocumentChunker> chunkers) {
        this.chunkers = chunkers
    }

    Mono<List<TextSegment>> processChunks(Document document, String strategy) {
        return chunkers.stream()
                .filter(chunker -> chunker.supports(strategy))
                .findFirst()
                .map(chunker -> chunker.chunk(document))
                .orElse(Mono.error(new UnsupportedOperationException("No chunker found for strategy: " + strategy)))
    }

    boolean isSupported(String strategy) {
        return chunkers.stream().anyMatch(processor -> processor.supports(strategy))
    }
}