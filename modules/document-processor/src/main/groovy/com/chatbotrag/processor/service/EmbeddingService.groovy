package com.chatbotrag.processor.service


import dev.langchain4j.data.embedding.Embedding
import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.model.embedding.EmbeddingModel
import dev.langchain4j.model.output.Response
import groovy.util.logging.Slf4j
import jakarta.inject.Inject
import jakarta.inject.Singleton
import reactor.core.publisher.Mono

@Slf4j
@Singleton
class EmbeddingService {

    private final EmbeddingModel embeddingModel

    @Inject
    EmbeddingService(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel
    }

    Mono<Embedding> embed(TextSegment segment) {
        Mono.fromCallable(() -> {
            try {
                Response<Embedding> response = embeddingModel.embed(segment)
                log.debug("Embedding response - metadata: {}, finishReason: {}, tokenUsage: {}", 
                    response.metadata(), response.finishReason(), response.tokenUsage())
                return response.content()
            } catch (Exception e) {
                throw new RuntimeException("Failed to embed text segment with metadata ${segment.metadata()}:", e)
            }
        })
    }

    Mono<List<Embedding>> embedBatch(List<TextSegment> segments) {
        Mono.fromCallable(() -> {
            try {
                Response<List<Embedding>> response = embeddingModel.embedAll(segments)
                log.debug("Embedding response - metadata: {}, finishReason: {}, tokenUsage: {}",
                        response.metadata(), response.finishReason(), response.tokenUsage())
                return response.content()
            } catch (Exception e) {
                throw new RuntimeException("Failed to embed text segments:", e)
            }
        })
    }
}
