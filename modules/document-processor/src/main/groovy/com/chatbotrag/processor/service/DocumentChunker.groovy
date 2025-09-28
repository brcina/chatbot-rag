package com.chatbotrag.processor.service

import dev.langchain4j.data.document.Document
import dev.langchain4j.data.segment.TextSegment
import reactor.core.publisher.Mono

interface DocumentChunker {

    Mono<List<TextSegment>> chunk(Document document)

    boolean supports(String strategy);

}