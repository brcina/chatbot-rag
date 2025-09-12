package com.chatbotrag.processor.service

import dev.langchain4j.data.document.Document
import reactor.core.publisher.Mono

interface DocumentParser {
    
    boolean supports(String contentType);
    
    Mono<Document> parse(InputStream inputStream, String fileName, String contentType);
}