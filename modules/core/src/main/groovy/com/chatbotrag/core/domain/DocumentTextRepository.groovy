package com.chatbotrag.core.domain

import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.r2dbc.annotation.R2dbcRepository
import io.micronaut.data.repository.reactive.ReactorCrudRepository
import reactor.core.publisher.Mono

@R2dbcRepository(dialect = Dialect.POSTGRES)
interface DocumentTextRepository extends ReactorCrudRepository<DocumentText, Long> {

    Mono<Boolean> existsByFileName(String fileName)

    Mono<DocumentText> findByFileName(String fileName)

    Mono<Void> deleteByFileName(String fileName)
}
