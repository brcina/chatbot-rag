package com.chatbotrag.processor.paragraph

import com.chatbotrag.processor.service.DocumentChunker
import dev.langchain4j.data.document.Document
import dev.langchain4j.data.document.DocumentSplitter
import dev.langchain4j.data.segment.TextSegment
import jakarta.inject.Inject
import jakarta.inject.Named
import jakarta.inject.Singleton
import reactor.core.publisher.Mono
import groovy.util.logging.Slf4j

@Singleton
@Slf4j
class DocumentParagraphChunker implements DocumentChunker {

    @Inject
    @Named("paragraph")
    DocumentSplitter splitter

    @Override
    Mono<List<TextSegment>> chunk(Document document) {
        Mono.fromCallable(() -> {
            try {
                String fileName = document.metadata().getString(Document.FILE_NAME) ?: "unknown"
                List<TextSegment> segments = splitter.split(document)
                
                log.debug("Paragraph chunking completed for '{}': {} segments created, avg length {} chars", 
                    fileName, 
                    segments.size(), 
                    segments.isEmpty() ? 0 : Math.round(segments.sum { it.text().length() } as Integer / segments.size()))
                
                return segments
            } catch (Exception e) {
                throw new RuntimeException("Failed to chunk document: " + document.metadata().getString(Document.FILE_NAME), e)
            }
        })
    }

    @Override
    boolean supports(String strategy) {
        return strategy == "paragraph"
    }
}
