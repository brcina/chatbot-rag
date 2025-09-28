package com.chatbotrag.processor.mapper

import dev.langchain4j.data.document.Document

interface MetadataMapper {

    Document mapMetadata(Document document)

}