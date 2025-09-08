package com.chatbotrag.api.controller

import com.chatbotrag.core.domain.Document
import com.chatbotrag.core.service.DocumentService
import io.micronaut.http.annotation.*
import jakarta.inject.Inject
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Controller("/api/documents")
class DocumentController {

    @Inject
    private DocumentService documentService

    @Get
    Flux<Document> getAllDocuments() {
        documentService.findAll()
    }

    @Get("/{id}")
    Mono<Document> getDocument(@PathVariable String id) {
        documentService.findById(id)
    }

    @Delete("/{id}")
    Mono<Void> deleteDocument(@PathVariable String id) {
        documentService.deleteById(id)
    }
}