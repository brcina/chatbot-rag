package com.chatbotrag.frontend.controller

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Body
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import jakarta.inject.Inject
import reactor.core.publisher.Mono

@Controller("/api")
class ProxyController {
    
    @Inject
    @Client("${api.backend.url:http://localhost:8080}")
    HttpClient httpClient
    
    @Get("/{+path}")
    Mono<HttpResponse<String>> proxyGet(String path, HttpRequest<?> request) {
        return Mono.from(httpClient.exchange(
            HttpRequest.GET("/api/${path}")
                .headers(request.headers), 
            String
        ))
    }
    
    @Post("/{+path}")
    Mono<HttpResponse<String>> proxyPost(String path, @Body String body, HttpRequest<?> request) {
        return Mono.from(httpClient.exchange(
            HttpRequest.POST("/api/${path}", body)
                .headers(request.headers), 
            String
        ))
    }
}