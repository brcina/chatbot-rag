package com.chatbotrag.core.domain


import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.r2dbc.annotation.R2dbcRepository
import io.micronaut.data.repository.reactive.ReactorCrudRepository
import reactor.core.publisher.Mono

@R2dbcRepository(dialect = Dialect.POSTGRES)
interface DocumentTextChunkRepository extends ReactorCrudRepository<DocumentTextChunk, Long> {

    Mono<Void> deleteByFilename(String filename)
}
