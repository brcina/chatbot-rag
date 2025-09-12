package com.chatbotrag.processor.service

import dev.langchain4j.data.document.Document
import jakarta.inject.Inject
import jakarta.inject.Singleton
import reactor.core.publisher.Mono

@Singleton
class DocumentParserService {

    private final List<DocumentParser> parsers

    @Inject
    DocumentParserService(List<DocumentParser> parsers) {
        this.parsers = parsers
    }

    Mono<Document> processDocument(File file, String contentType) {
        return Mono.using(
            { -> file.newInputStream() },
            { InputStream is -> processDocument(is, file.name, contentType) },
            { InputStream is -> is.close() }
        )
    }

    Mono<Document> processDocument(InputStream inputStream, String fileName, String contentType) {
        return parsers.stream()
                .filter(processor -> processor.supports(contentType))
                .findFirst()
                .map(processor -> processor.parse(inputStream, fileName, contentType))
                .orElse(Mono.error(new UnsupportedOperationException("No processor found for content type: " + contentType)))
    }

    boolean isSupported(String contentType) {
        return parsers.stream().anyMatch(processor -> processor.supports(contentType))
    }
}