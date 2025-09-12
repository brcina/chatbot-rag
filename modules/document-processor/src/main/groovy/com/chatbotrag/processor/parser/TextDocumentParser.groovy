package com.chatbotrag.processor.parser


import dev.langchain4j.data.document.Document
import dev.langchain4j.data.document.DocumentParser
import groovy.util.logging.Slf4j
import jakarta.inject.Inject
import jakarta.inject.Named
import jakarta.inject.Singleton
import reactor.core.publisher.Mono

@Slf4j
@Singleton
class TextDocumentParser implements com.chatbotrag.processor.service.DocumentParser {

    @Inject
    @Named("text")
    DocumentParser documentParser

    @Override
    boolean supports(String contentType) {
        return contentType != null && (
                contentType.startsWith("text/") ||
                        contentType == "application/json" ||
                        contentType == "application/xml"
        )
    }

    @Override
    Mono<Document> parse(InputStream inputStream, String fileName, String contentType) {
        Mono.fromCallable(() -> {
            try {
                Document document = documentParser.parse(inputStream)
                document.metadata().put(Document.FILE_NAME, fileName)
                document.metadata().put("contentType", contentType)

                log.debug("Successfully parsed Text document '{}' - Content length: {} chars, Metadata: {}",
                        fileName, document.text().length(), document.metadata())
                log.trace("Text document content:\n {}", document.text())

                return document
            } catch (Exception e) {
                throw new RuntimeException("Failed to process Text document: " + fileName, e)
            }
        })
    }
}