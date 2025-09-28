package com.chatbotrag.core.domain

import com.chatbotrag.core.utils.DocumentUtils
import dev.langchain4j.data.document.Document
import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import io.micronaut.core.annotation.Nullable
import io.micronaut.data.annotation.DateCreated
import io.micronaut.data.annotation.DateUpdated
import io.micronaut.data.annotation.GeneratedValue
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.data.annotation.Transient
import io.micronaut.data.annotation.TypeDef
import io.micronaut.data.model.DataType
import io.micronaut.serde.annotation.Serdeable
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

import java.nio.file.Path
import java.time.LocalDateTime

@Entity
@Table(name = "document_text", schema = "chatbot_rag")
@MappedEntity
@Serdeable
@CompileStatic
@EqualsAndHashCode(includes = ['id'])
@ToString(includeNames = true, includePackage = false, excludes = ['content'])
class DocumentText {

    @Id
    @GeneratedValue(value =  GeneratedValue.Type.SEQUENCE)
    @Column(name = "id")
    Long id

    @NotBlank
    @Size(max = 500)
    @Column(name = "filename", unique = true, nullable = false, length = 500)
    String fileName

    @Nullable
    @Column(name = "file_path", columnDefinition = "TEXT")
    String filePath

    @Nullable
    @Column(name = "file_size")
    Long fileSize

    @Nullable
    @Size(max = 100)
    @Column(name = "mime_type", length = 100)
    String mimeType

    @NotBlank
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    String content

    @Nullable
    @Size(max = 64)
    @Column(name = "content_hash", length = 64)
    String contentHash

    @Nullable
    @Size(max = 64)
    @Column(name = "file_hash", length = 64)
    String fileHash

    @Nullable
    @Column(name = "metadata", columnDefinition = "JSONB")
    @TypeDef(type = DataType.JSON)
    Map<String, Object> metadata = [:]

    @Nullable
    @Size(max = 50)
    @Column(name = "processing_status", length = 50)
    String processingStatus = "pending"

    @Nullable
    @Column(name = "last_modified")
    LocalDateTime lastModified

    @DateCreated
    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt

    @DateUpdated
    @Column(name = "updated_at", nullable = false)
    LocalDateTime updatedAt

    // Relationship zu Document Chunks
    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    Set<DocumentTextChunk> chunks = [] as Set

    DocumentText() {

    }

    DocumentText(Document document) {
        fileName = document.metadata().getString(Document.FILE_NAME)
        filePath = document.metadata().getString(Document.ABSOLUTE_DIRECTORY_PATH)
        fileSize = document.metadata().getLong(DocumentMetadata.FILE_SIZE)
        mimeType = document.metadata().getString(DocumentMetadata.CONTENT_TYPE)
        document.metadata().toMap().each {key, value ->
            addMetadata(key, value)
        }
        contentHash = DocumentUtils.calculateContentHash(document.text())
        content = document.text()
        fileHash = DocumentUtils.calculateFileHash(Path.of(filePath))
    }

    @Transient
    boolean isPending() {
        return processingStatus == "pending"
    }

    @Transient
    boolean isProcessed() {
        return processingStatus == "processed"
    }

    @Transient
    boolean isFailed() {
        return processingStatus == "failed"
    }

    @Transient
    void markAsProcessed() {
        this.processingStatus = "processed"
    }

    @Transient
    void markAsFailed() {
        this.processingStatus = "failed"
    }

    @Transient
    int getChunkCount() {
        return chunks?.size() ?: 0
    }

    @Transient
    boolean hasContentHash(String hash) {
        return contentHash == hash
    }

    @Transient
    boolean hasFileHash(String hash) {
        return fileHash == hash
    }

    @Transient
    void addMetadata(String key, Object value) {
        if (metadata == null) {
            metadata = [:]
        }
        metadata[key] = value
    }

    Object getMetadata(String key) {
        return metadata?.get(key)
    }
}

