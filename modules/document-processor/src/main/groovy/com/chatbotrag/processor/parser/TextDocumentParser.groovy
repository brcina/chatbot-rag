package com.chatbotrag.processor.parser

import com.chatbotrag.core.domain.DocumentMetadata
import dev.langchain4j.data.document.Document
import dev.langchain4j.data.document.DocumentParser
import groovy.util.logging.Slf4j
import jakarta.inject.Inject
import jakarta.inject.Named
import jakarta.inject.Singleton
import reactor.core.publisher.Mono

import java.nio.file.Files
import java.time.Instant

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
    Mono<Document> parse(InputStream inputStream, File file, String contentType) {
        Mono.fromCallable(() -> {
            try {
                Document document = documentParser.parse(inputStream)

                // Basic metadata
                document.metadata().put(Document.FILE_NAME, file.name)
                document.metadata().put(Document.ABSOLUTE_DIRECTORY_PATH, file.absolutePath)
                document.metadata().put(DocumentMetadata.CONTENT_TYPE, contentType)

                // File system metadata
                document.metadata().put(DocumentMetadata.FILE_SIZE, file.length())
                document.metadata().put(DocumentMetadata.FILE_MODIFIED, Files.getLastModifiedTime(file.toPath()).toInstant().toString())
                document.metadata().put(DocumentMetadata.PROCESSED_AT, Instant.now().toString())
                document.metadata().put(DocumentMetadata.PARSER_TYPE, "text")

                // Content metadata
                String text = document.text()
                document.metadata().put(DocumentMetadata.CHARACTER_COUNT, text.length())
                document.metadata().put(DocumentMetadata.WORD_COUNT, text.split("\\s+").length)

                log.debug("Successfully parsed Text document '{}' - Content length: {} chars, Metadata: {}",
                        file.name, document.text().length(), document.metadata())
                log.trace("Text document content:\n {}", document.text())

                return document
            } catch (Exception e) {
                throw new RuntimeException("Failed to process Text document: " + file.name, e)
            }
        })
    }
}