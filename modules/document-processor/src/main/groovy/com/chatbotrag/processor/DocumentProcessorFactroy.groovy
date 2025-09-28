//file:noinspection unused
//file:noinspection GrMethodMayBeStatic
package com.chatbotrag.processor


import dev.langchain4j.data.document.DocumentParser
import dev.langchain4j.data.document.DocumentSplitter
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser
import dev.langchain4j.data.document.splitter.DocumentByParagraphSplitter
import dev.langchain4j.data.document.splitter.DocumentBySentenceSplitter
import dev.langchain4j.data.document.splitter.DocumentSplitters
import dev.langchain4j.model.embedding.EmbeddingModel
import dev.langchain4j.model.ollama.OllamaEmbeddingModel
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Value
import jakarta.inject.Named
import jakarta.inject.Singleton

@Factory
class DocumentProcessorFactroy {

    @Bean
    @Singleton
    EmbeddingModel embeddingModel(
            @Value('${processor.embedding.model}') String model,
            @Value('${processor.embedding.host}') String host,
            @Value('${processor.embedding.port}') String port) {
        return OllamaEmbeddingModel
                .builder()
                .baseUrl("http://$host:$port")
                .modelName(model)
                .build()
    }

    @Bean
    @Singleton
    @Named("pdf")
    DocumentParser pdfDocumentParser() {
        new ApachePdfBoxDocumentParser(true)
    }

    @Bean
    @Singleton
    @Named("text")
    DocumentParser textDocumentParser() {
        new ApacheTikaDocumentParser()
    }

    @Bean
    @Named("recursive")
    DocumentSplitter recursiveChunker() {
        return DocumentSplitters.recursive(
                500,   // Max Tokens per Chunk
                50     // Overlap zwischen Chunks
        )
    }

    @Bean
    @Named("sentence")
    DocumentSplitter sentenceChunker() {
        // Gut für narrative Texte
        return new DocumentBySentenceSplitter(300, 30)
    }

    @Bean
    @Named("paragraph")
    DocumentSplitter paragraphChunker() {
        // Für strukturierte Dokumente
        return new DocumentByParagraphSplitter(800, 80)
    }


}
