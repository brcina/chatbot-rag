package com.chatbotrag.processor.service

import com.chatbotrag.core.domain.DocumentText
import com.chatbotrag.core.domain.DocumentTextChunk
import com.chatbotrag.core.domain.DocumentTextChunkRepository
import com.chatbotrag.core.domain.DocumentTextRepository
import dev.langchain4j.data.document.Document
import dev.langchain4j.data.segment.TextSegment
import groovy.io.FileType
import groovy.util.logging.Slf4j
import io.micronaut.transaction.annotation.Transactional
import jakarta.inject.Inject
import jakarta.inject.Singleton
import reactor.core.publisher.Mono
import reactor.core.publisher.Flux

import java.nio.file.Files

@Slf4j
@Singleton
class DocumentProcessorService {

    @Inject
    DocumentTextRepository documentTextRepository

    @Inject
    DocumentTextChunkRepository documentTextChunkRepository

    @Inject
    EmbeddingService embeddingService

    @Inject
    DocumentChunkerService documentChunkerService

    @Inject
    DocumentParserService documentParserService

    Flux<File> getSupportedFilesFromDirectory(File directory) {
        return Flux.create(sink -> {
            try {
                directory.eachFile(FileType.FILES, file -> {
                    try {
                        String contentType = Files.probeContentType(file.toPath())
                        if (documentParserService.isSupported(contentType)) {
                            sink.next(file)
                        } else {
                            log.info("Skipping {} since contentType: {} is not supported", file.name, contentType)
                        }
                    } catch (Exception e) {
                        log.error("Error processing file: {}", file.name, e)
                    }
                })
                sink.complete()
            } catch (Exception e) {
                sink.error(e)
            }
        })
    }


    @Transactional
    Mono<DocumentText> saveDocument(Document document) {
        return documentTextRepository.save(new DocumentText(document))
                .doOnSuccess(saved -> log.debug("Saved DocumentText with ID: {}", saved.id))
    }

    @Transactional
    Mono<Document> processFileWithForceHandling(File file, String contentType, boolean force) {
        String fileName = file.name
        log.debug("Processing file: {} with force: {}", fileName, force)

        if (!force) {
            return documentParserService.processDocument(file, contentType)
                    .filterWhen(document -> documentTextRepository.existsByFileName(fileName)
                            .map(exists -> !exists)
                            .doOnNext(shouldProcess -> {
                                if (!shouldProcess) {
                                    log.info("Skipping {} - already processed (use --force to override)", fileName)
                                }
                            }))
                    .doOnSuccess(document -> log.info("Successfully processed file: {}", fileName))
                    .doOnError(error -> log.error("Failed to process file: {}", fileName, error))
                    .onErrorResume(error -> Mono.empty())
        } else {
            // Force mode: delete existing first
            log.debug("Force mode: attempting to delete existing DocumentText for: {}", fileName)
            return documentTextRepository.deleteByFileName(fileName)
                    .doOnSuccess(v -> log.debug("Successfully deleted existing DocumentText for: {}", fileName))
                    .doOnError(error -> log.warn("No existing DocumentText found to delete for: {}", fileName))
                    .onErrorResume(error -> Mono.empty())
                    .then(documentParserService.processDocument(file, contentType))
                    .doOnSuccess(document -> log.info("Successfully processed file: {} (forced)", fileName))
                    .doOnError(error -> log.error("Failed to process file: {}", fileName, error))
                    .onErrorResume(error -> Mono.empty())
        }
    }

    @Transactional
    Mono<List<DocumentTextChunk>> processDocumentChunks(Document document, String chunkingStrategy) {
        String fileName = document.metadata().getString(Document.FILE_NAME) ?: "unknown"

        return documentChunkerService.processChunks(document, chunkingStrategy)
                .doOnSuccess(chunks -> log.info("Successfully processed file: {} with chunks: {}", fileName, chunks.size()))
                .doOnError(error -> log.error("Failed to process file: {} chunks: {}", fileName, error))
                .flatMap(segments -> {
                    return documentTextRepository.findByFileName(fileName)
                            .flatMap(documentText -> {
                                return embeddingService.embedBatch(segments)
                                        .flatMap(embeddings -> {
                                            log.info("Successfully processed file: {} with embeddings: {}", fileName, embeddings.size())

                                            List<DocumentTextChunk> chunks = []
                                            for (int i = 0; i < segments.size(); i++) {
                                                TextSegment segment = segments[i]
                                                DocumentTextChunk chunk = new DocumentTextChunk(
                                                        documentText,
                                                        fileName,
                                                        i,
                                                        segment.text()
                                                )
                                                if (i < embeddings.size()) {
                                                    chunk.setEmbeddingFromLangChain(embeddings[i])
                                                }
                                                documentText.metadata.each { key, value ->
                                                    chunk.addMetadata(key, value)
                                                }
                                                chunks.add(chunk)
                                            }

                                            return documentTextChunkRepository.saveAll(chunks).collectList()
                                                    .flatMap(savedChunks -> {
                                                        // Mark DocumentText as processed
                                                        documentText.markAsProcessed()
                                                        return documentTextRepository.update(documentText)
                                                                .map(saved -> savedChunks)
                                                    })
                                        })
                            })
                })
                .doOnError(error -> {
                    log.error("Failed to process chunks for file: {}", fileName, error)
                    // Mark DocumentText as failed
                    documentTextRepository.findByFileName(fileName)
                            .doOnNext(documentText -> {
                                documentText.markAsFailed()
                                documentTextRepository.update(documentText).subscribe()
                            })
                            .subscribe()
                })
    }
}