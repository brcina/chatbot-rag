package com.chatbotrag.processor.mapper

import com.chatbotrag.core.domain.DocumentMetadata
import com.chatbotrag.core.utils.PDFUtils
import dev.langchain4j.data.document.Document
import jakarta.inject.Named

@Named("pdf")
class PdfMetadataMapper implements MetadataMapper {
    private final Map<String, String> KEY_MAPPING = new HashMap<>();

    PdfMetadataMapper() {
        KEY_MAPPING.put("Title", DocumentMetadata.TITLE);
        KEY_MAPPING.put("Author", DocumentMetadata.AUTHOR);
        KEY_MAPPING.put("Subject", DocumentMetadata.SUBJECT);
        KEY_MAPPING.put("Keywords", DocumentMetadata.KEYWORDS);
        KEY_MAPPING.put("Creator", DocumentMetadata.FILE_CREATED_BY);
        KEY_MAPPING.put("Producer", DocumentMetadata.PRODUCED_BY);
        KEY_MAPPING.put("CreationDate", DocumentMetadata.FILE_CREATED);
        KEY_MAPPING.put("ModDate", DocumentMetadata.FILE_MODIFIED);
    }

    Document mapMetadata(Document document)  {
        KEY_MAPPING.each {pdfKey, metaKey -> {
            if(!document.metadata().containsKey(pdfKey)) {
                return
            }
            def value = document.metadata().getString(pdfKey)
            if(!value) {
                return
            }
            document.metadata().remove(pdfKey)
            if(metaKey == DocumentMetadata.FILE_MODIFIED) {
                document.metadata().put(metaKey, PDFUtils.convertPDFDateToInstant(value).toString())
                return
            }
            if(metaKey == DocumentMetadata.FILE_CREATED) {
                document.metadata().put(metaKey, PDFUtils.convertPDFDateToInstant(value).toString())
                return
            }
            document.metadata().put(metaKey, value)
        }}
        document
    }


}
