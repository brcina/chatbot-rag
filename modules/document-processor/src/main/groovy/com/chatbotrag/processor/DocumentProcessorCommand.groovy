package com.chatbotrag.processor

import com.chatbotrag.processor.service.DocumentChunkerService
import com.chatbotrag.processor.service.DocumentProcessorService
import dev.langchain4j.data.document.Document
import groovy.util.logging.Slf4j
import io.micronaut.configuration.picocli.PicocliRunner
import io.micronaut.logging.LogLevel
import io.micronaut.logging.LoggingSystem
import jakarta.inject.Inject
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import reactor.core.publisher.Mono

import java.nio.file.Files

@Slf4j
@Command(name = 'document-processor', description = """
            Processes documents and saves them as embeddings in the vector store
        """,
        mixinStandardHelpOptions = true)
class DocumentProcessorCommand implements Runnable {

    @Option(names = ['-v', '--verbose'], description = 'Enable verbose logging')
    boolean verbose

    @Option(names = ['-d', '--directory'], required = true, description = 'The directory to parse the files and save them as embeddings')
    File directory

    @Option(names = ['-f', '--force'], description = 'Whether already saved embeddings should be overwritten, default is skips if filename already embedded')
    boolean force

    @Option(names = "--chunking-strategy", defaultValue = "recursive")
    private String chunkingStrategy;

    @Inject
    LoggingSystem loggingSystem



    @Inject
    DocumentChunkerService documentChunkerService

    @Inject
    DocumentProcessorService documentProcessor

    static void main(String[] args) throws Exception {
        PicocliRunner.run(DocumentProcessorCommand.class, args)
    }

    void run() {
        if (verbose) {
            loggingSystem.setLogLevel(this.getClass().getPackageName(), LogLevel.DEBUG)
        }

        if (!documentChunkerService.isSupported(chunkingStrategy)) {
            log.info("Stopping since chunking strategy: {} is not supported", chunkingStrategy)
            return
        }

        log.info("Processing files in directory: {} with chunking strategy: {}", directory.absolutePath, chunkingStrategy)

        documentProcessor.getSupportedFilesFromDirectory(directory)
                .doOnNext(file -> log.info("Starting to process file: {}", file.name))
                .concatMap(file -> {
                    String contentType = Files.probeContentType(file.toPath())
                    return documentProcessor.processFileWithForceHandling(file, contentType, force)
                })
                .flatMap((Document document) -> {
                    return documentProcessor.saveDocument(document)
                            .map(savedDocumentText -> document)
                })
                .flatMap((Document document) -> {
                    return documentProcessor.processDocumentChunks(document, chunkingStrategy)
                            .onErrorResume(error -> Mono.empty())
                })
                .collectList()
                .doOnSuccess(documents -> log.info("Completed processing {} documents", documents.size()))
                .block()
    }
}
