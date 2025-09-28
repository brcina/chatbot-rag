package com.chatbotrag.core.domain

import dev.langchain4j.data.embedding.Embedding
import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import io.micronaut.core.annotation.Nullable
import io.micronaut.data.annotation.*
import io.micronaut.data.model.DataType
import io.micronaut.serde.annotation.Serdeable
import jakarta.persistence.Column
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

import java.time.LocalDateTime

@MappedEntity
@Table(name = "document_text_chunk", schema = "chatbot_rag")
@Serdeable
@CompileStatic
@EqualsAndHashCode(includes = ['id'])
@ToString(
        includeNames = true,
        includePackage = false,
        excludes = ['content', 'embedding', 'documentText']
)
class DocumentTextChunk {

    @Id
    @GeneratedValue(value =  GeneratedValue.Type.SEQUENCE)
    @Column(name = "id")
    Long id

    // Foreign Key zur DocumentTextEntity
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "document_text_id",
            nullable = false,
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "fk_document_text_chunk_document_text")
    )
    DocumentText documentText

    // Denormalisiert für schnelle Suche ohne JOIN
    @NotBlank
    @Size(max = 500)
    @Column(name = "filename", nullable = false, length = 500)
    String filename

    // Position des Chunks im Original-Dokument (0-basiert)
    @NotNull
    @Min(0)
    @Column(name = "chunk_index", nullable = false)
    Integer chunkIndex

    // Text-Inhalt des Chunks
    @NotBlank
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    String content

    // Vector Embedding (384 Dimensionen für sentence-transformers)
    @Nullable
    @Column(name = "embedding", columnDefinition = "vector(384)")
    float[] embedding

    // Anzahl Tokens in diesem Chunk
    @Nullable
    @Min(0)
    @Column(name = "token_count")
    Integer tokenCount

    // Flexible Metadaten (JSONB)
    @Nullable
    @Column(name = "metadata", columnDefinition = "JSONB")
    @TypeDef(type = DataType.JSON)
    Map<String, Object> metadata = [:]

    // Zeitstempel der Erstellung
    @DateCreated
    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt

    // Default constructor für JPA
    DocumentTextChunk() {}

    // Convenience constructor für neue Chunks
    DocumentTextChunk(DocumentText documentText, String filename,
                            Integer chunkIndex, String content) {
        this.documentText = documentText
        this.filename = filename
        this.chunkIndex = chunkIndex
        this.content = content
    }

    // Full constructor
    DocumentTextChunk(DocumentText documentText, String filename,
                            Integer chunkIndex, String content, Integer tokenCount,
                            Map<String, Object> metadata = [:]) {
        this.documentText = documentText
        this.filename = filename
        this.chunkIndex = chunkIndex
        this.content = content
        this.tokenCount = tokenCount
        this.metadata = metadata ?: [:]
    }

    // =====================================================
    // EMBEDDING HELPER METHODS
    // =====================================================

    /**
     * Prüft ob der Chunk ein Embedding hat
     */
    @Transient
    boolean hasEmbedding() {
        return embedding != null && embedding.length > 0
    }

    /**
     * Gibt die Dimensionen des Embeddings zurück
     */
    @Transient
    int getEmbeddingDimension() {
        return embedding?.length ?: 0
    }

    /**
     * Setzt das Embedding aus einer List<Float>
     */
    @Transient
    void setEmbeddingFromList(List<Float> embeddingList) {
        if (embeddingList != null && !embeddingList.isEmpty()) {
            this.embedding = new float[embeddingList.size()]
            for (int i = 0; i < embeddingList.size(); i++) {
                this.embedding[i] = embeddingList[i]
            }
        }
    }

    /**
     * Gibt das Embedding als List<Float> zurück (für LangChain4j)
     */
    @Transient
    List<Float> getEmbeddingAsList() {
        return embedding ? embedding.toList() : []
    }

    /**
     * Setzt das Embedding aus einem LangChain4j Embedding Object
     */
    @Transient
    void setEmbeddingFromLangChain(Embedding langChainEmbedding) {
        if (langChainEmbedding != null) {
            setEmbeddingFromList(langChainEmbedding.vectorAsList())
        }
    }

    // =====================================================
    // CONTENT HELPER METHODS
    // =====================================================

    /**
     * Gibt eine Vorschau des Contents zurück
     */
    @Transient
    String getContentPreview(int maxLength = 100) {
        if (content == null || content.isEmpty()) return ""
        return content.length() <= maxLength ? content : content.substring(0, maxLength) + "..."
    }

    /**
     * Gibt die Länge des Contents in Zeichen zurück
     */
    @Transient
    int getContentLength() {
        return content?.length() ?: 0
    }

    /**
     * Prüft ob der Content leer ist
     */
    @Transient
    boolean hasContent() {
        return content != null && !content.trim().isEmpty()
    }

    // =====================================================
    // METADATA HELPER METHODS
    // =====================================================

    /**
     * Fügt ein Metadata-Key-Value-Paar hinzu
     */
    void addMetadata(String key, Object value) {
        if (metadata == null) {
            metadata = [:]
        }
        metadata[key] = value
    }

    /**
     * Holt einen Metadata-Wert
     */
    @Transient
    Object getMetadata(String key) {
        return metadata?.get(key)
    }

    /**
     * Holt einen Metadata-Wert mit Default
     */
    @Transient
    Object getMetadata(String key, Object defaultValue) {
        return metadata?.get(key) ?: defaultValue
    }

    /**
     * Prüft ob ein Metadata-Key existiert
     */
    @Transient
    boolean hasMetadata(String key) {
        return metadata?.containsKey(key) ?: false
    }


    /**
     * Erstellt eine Debug-Repräsentation des Chunks
     */
    String toDebugString() {
        return """DocumentTextChunk[
    id: ${id}
    filename: ${filename}
    chunkIndex: ${chunkIndex}
    contentLength: ${contentLength}
    tokenCount: ${tokenCount}
    hasEmbedding: ${hasEmbedding()}
    embeddingDim: ${embeddingDimension}
    preview: "${getContentPreview()}"
    ]"""
    }
}