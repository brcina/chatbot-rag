package com.chatbotrag.processor.impl;

import com.chatbotrag.core.domain.Document;
import com.chatbotrag.processor.service.DocumentProcessor;
import jakarta.inject.Singleton;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.pdf.PDFParser;
import org.apache.tika.sax.BodyContentHandler;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Singleton
public class PdfDocumentProcessor implements DocumentProcessor {

    @Override
    public boolean supports(String contentType) {
        return "application/pdf".equals(contentType);
    }

    @Override
    public Mono<Document> process(InputStream inputStream, String fileName, String contentType) {
        return Mono.fromCallable(() -> {
            try {
                PDFParser parser = new PDFParser();
                BodyContentHandler handler = new BodyContentHandler(-1);
                Metadata metadata = new Metadata();
                
                parser.parse(inputStream, handler, metadata);
                String content = handler.toString();
                
                Map<String, Object> metadataMap = new HashMap<>();
                metadataMap.put("fileName", fileName);
                
                // Extract metadata from Tika
                for (String name : metadata.names()) {
                    metadataMap.put(name, metadata.get(name));
                }
                
                Document document = new Document(
                    UUID.randomUUID().toString(),
                    extractTitle(content),
                    content,
                    contentType,
                    fileName
                );
                document.setMetadata(metadataMap);
                
                return document;
            } catch (Exception e) {
                throw new RuntimeException("Failed to process PDF document: " + fileName, e);
            }
        });
    }
}