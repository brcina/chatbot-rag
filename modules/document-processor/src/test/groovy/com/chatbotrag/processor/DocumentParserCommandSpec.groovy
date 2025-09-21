package com.chatbotrag.processor

import groovy.util.logging.Slf4j
import io.micronaut.configuration.picocli.PicocliRunner
import io.micronaut.context.ApplicationContext
import io.micronaut.context.env.Environment
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

import java.nio.file.Paths

@Slf4j
class DocumentParserCommandSpec extends Specification {

    @Shared
    @AutoCleanup
    ApplicationContext ctx = ApplicationContext.run(Environment.CLI, Environment.TEST)

    void "context loads by utilizing the help command"() {
        given:
            ByteArrayOutputStream baos = new ByteArrayOutputStream()
            System.setOut(new PrintStream(baos))

            String[] args = ['-h'] as String[]
            PicocliRunner.run(DocumentProcessorCommand, ctx, args)

        expect:
            System.println(baos.toString())
            baos.toString().contains('Processes documents and saves them as embeddings in the vector store')
    }

    void "processing documents"() {
        given:
            ByteArrayOutputStream baos = new ByteArrayOutputStream()
            PrintStream originalOut = System.out
            System.setOut(new PrintStream(baos))
            def docs = getResourcePath("docs")
            String[] args = ['-d', docs.toString(), "--chunking-strategy", "sentence"] as String[]
            PicocliRunner.run(DocumentProcessorCommand, ctx, args)
            def output = baos.toString()

            System.setOut(originalOut)
            log.info("Command output:\n{}", output)
            System.out.println(output)
            
        expect:
            output.contains('Starting to process file: TRBA-460.pdf')
            !output.contains('Failed to process file: TRBA-460.pdf')
            output.contains('Completed processing 1 documents')
    }

    private getResourcePath(String resourceName) {
        def resource = getClass().getClassLoader().getResource(resourceName)
        assert resource != null : "Resource not found: $resourceName"
        return Paths.get(resource.toURI())
    }

}
