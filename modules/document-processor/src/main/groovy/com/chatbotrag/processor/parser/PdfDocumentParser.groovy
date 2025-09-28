package com.chatbotrag.processor.parser

import com.chatbotrag.core.domain.DocumentMetadata
import com.chatbotrag.processor.mapper.MetadataMapper
import dev.langchain4j.data.document.Document
import dev.langchain4j.data.document.DocumentParser
import groovy.util.logging.Slf4j
import jakarta.inject.Inject
import jakarta.inject.Named
import jakarta.inject.Singleton
import reactor.core.publisher.Mono

import java.time.LocalDateTime

@Slf4j
@Singleton
class PdfDocumentParser implements com.chatbotrag.processor.service.DocumentParser {

    @Inject
    @Named("pdf")
    DocumentParser documentParser

    @Inject
    @Named("pdf")
    MetadataMapper mapper

    @Override
    boolean supports(String contentType) {
        return "application/pdf" == contentType
    }

    @Override
    Mono<Document> parse(InputStream inputStream, File file, String contentType) {
        Mono.fromCallable(() -> {
            try {
                Document document = documentParser.parse(inputStream)
                log.debug("Successfully parsed PDF document '{}' - Content length: {} chars, Metadata: {}", 
                    file.name, document.text().length(), document.metadata())
                log.trace("PDF document content:\n {}", document.text())

                document.metadata().put(Document.FILE_NAME, file.name)
                document.metadata().put(Document.ABSOLUTE_DIRECTORY_PATH, file.absolutePath)
                document.metadata().put(DocumentMetadata.FILE_SIZE, file.size())
                document.metadata().put(DocumentMetadata.CONTENT_TYPE, contentType)
                document.metadata().put(DocumentMetadata.CHARACTER_COUNT, document.text().size())
                document.metadata().put(DocumentMetadata.PROCESSED_AT, LocalDateTime.now().toString())

                document = mapper.mapMetadata(document)

                return document
            } catch (Exception e) {
                throw new RuntimeException("Failed to process PDF document: " + file.name, e)
            }
        })
    }
}
