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
class PdfDocumentParser implements com.chatbotrag.processor.service.DocumentParser {

    @Inject
    @Named("pdf")
    DocumentParser documentParser

    @Override
    boolean supports(String contentType) {
        return "application/pdf" == contentType
    }

    @Override
    Mono<Document> parse(InputStream inputStream, String fileName, String contentType) {
        Mono.fromCallable(() -> {
            try {
                Document document = documentParser.parse(inputStream)
                document.metadata().put(Document.FILE_NAME, fileName)
                document.metadata().put("contentType", contentType)
                
                log.debug("Successfully parsed PDF document '{}' - Content length: {} chars, Metadata: {}", 
                    fileName, document.text().length(), document.metadata())
                log.trace("PDF document content:\n {}", document.text())

                return document
            } catch (Exception e) {
                throw new RuntimeException("Failed to process PDF document: " + fileName, e)
            }
        })
    }
}