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
    @Client("http://localhost:8080")
    HttpClient httpClient
    
    @Get("/{+path}")
    Mono<HttpResponse<String>> proxyGet(String path) {
        return Mono.from(httpClient.exchange(
            HttpRequest.GET("/api/${path}"), 
            String
        ))
    }
    
    @Post("/{+path}")
    Mono<HttpResponse<String>> proxyPost(String path, @Body String body) {
        return Mono.from(httpClient.exchange(
            HttpRequest.POST("/api/${path}", body), 
            String
        ))
    }
}