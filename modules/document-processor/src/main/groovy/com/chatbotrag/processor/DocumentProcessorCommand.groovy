package com.chatbotrag.processor

import com.chatbotrag.processor.service.DocumentChunkerService
import com.chatbotrag.processor.service.DocumentParserService
import dev.langchain4j.data.document.Document
import groovy.io.FileType
import groovy.util.logging.Slf4j
import io.micronaut.configuration.picocli.PicocliRunner
import io.micronaut.logging.LogLevel
import io.micronaut.logging.LoggingSystem
import jakarta.inject.Inject
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

import java.nio.file.Files

@Slf4j
@Command(name = 'document-processor', description ="""
            Processes documents and saves them as embeddings in the vector store
        """,
        mixinStandardHelpOptions = true)
class DocumentProcessorCommand implements Runnable {

    @Option(names = ['-v', '--verbose'], description = 'Enable verbose logging')
    boolean verbose

    @Option(names = ['-d', '--directory'],  required = true, description = 'The directory to parse the files and save them as embeddings')
    File directory

    @Option(names = ['-f', '--force'], description = 'Whether already saved embeddings should be overwritten, default is skips if filename already embedded')
    boolean force

    @Option(names = "--chunking-strategy", defaultValue = "recursive")
    private String chunkingStrategy;

    @Inject
    LoggingSystem loggingSystem

    @Inject
    DocumentParserService documentProcessorService

    @Inject
    DocumentChunkerService documentChunkerService;

    static void main(String[] args) throws Exception {
        PicocliRunner.run(DocumentProcessorCommand.class, args)
    }

    void run() {
        if (verbose) {
            loggingSystem.setLogLevel(this.getClass().getPackageName(), LogLevel.DEBUG)
        }

        if(!documentChunkerService.isSupported(chunkingStrategy)) {
            log.info("Stopping since chunking strategy: {} is not supported", chunkingStrategy)
            return
        }

        List<File> filesToProcess = []
        
        directory.eachFile(FileType.FILES, file -> {
            String contentType = Files.probeContentType(file.toPath())
            if (documentProcessorService.isSupported(contentType)) {
                filesToProcess.add(file)
            } else {
                log.info("Skipping {} since contentType: {} is not supported", file.name, contentType)
            }
        })

        if (filesToProcess.isEmpty()) {
            log.info("No supported files found in directory: {}", directory.absolutePath)
            return
        }

        log.info("Processing {} files with chunking strategy {} in parallel", filesToProcess.size(), chunkingStrategy)

        Flux.fromIterable(filesToProcess)
            .doOnNext(file -> log.debug("Starting to process file: {}", file.name))
            .flatMap(file -> {
                String contentType = Files.probeContentType(file.toPath())
                return documentProcessorService.processDocument(file, contentType)
                    .doOnSuccess(document -> log.info("Successfully processed file: {}", file.name))
                    .doOnError(error -> log.error("Failed to process file: {}", file.name, error))
                    .onErrorResume(error -> Mono.empty())
            })
            .flatMap( document -> {
                String fileName = document.metadata().getString(Document.FILE_NAME) ?: "unknown"
                return documentChunkerService.processChunks(document, chunkingStrategy)
                        .doOnSuccess(chunks -> log.info("Successfully processed file: {} with chunks: {}", fileName, chunks.size()))
                        .doOnError(error -> log.error("Failed to process file: {} chunks: {}", fileName,  error))
                        .onErrorResume(error -> Mono.empty())
            })
            .collectList()
            .doOnSuccess(documents -> log.info("Completed processing {} documents", documents.size()))
            .block()
    }
}
