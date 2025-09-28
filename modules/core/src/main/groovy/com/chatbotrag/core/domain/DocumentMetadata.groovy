package com.chatbotrag.core.domain

/**
 * Extended metadata constants for document processing.
 * Complements the standard LangChain4j Document metadata constants.
 */
class DocumentMetadata {

    // File system metadata
    static final String FILE_SIZE = "file_size"
    static final String FILE_MODIFIED = "file_modified"
    static final String FILE_CREATED = "file_created"
    static final String FILE_CREATED_BY = "file_created_by"
    static final String CONTENT_TYPE = "content_type"

    // Processing metadata
    static final String PROCESSED_AT = "processed_at"
    static final String PRODUCED_BY = "produced_by"
    static final String PARSER_TYPE = "parser_type"
    static final String PAGE_COUNT = "page_count"
    static final String WORD_COUNT = "word_count"
    static final String CHARACTER_COUNT = "character_count"

    // Document structure
    static final String LANGUAGE = "language"
    static final String ENCODING = "encoding"
    static final String TITLE = "title"
    static final String AUTHOR = "author"
    static final String SUBJECT = "subject"
    static final String KEYWORDS = "keywords"
}