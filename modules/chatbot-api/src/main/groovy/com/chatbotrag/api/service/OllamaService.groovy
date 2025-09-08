package com.chatbotrag.api.service

import dev.langchain4j.model.ollama.OllamaChatModel
import dev.langchain4j.data.message.UserMessage
import io.micronaut.context.annotation.Value
import jakarta.inject.Singleton
import jakarta.annotation.PostConstruct
import reactor.core.publisher.Mono

@Singleton
class OllamaService {
    
    @Value('${ai.ollama.host}')
    String ollamaHost
    
    @Value('${ai.ollama.port}')
    Integer ollamaPort
    
    @Value('${ai.ollama.default-model}')
    String defaultModel
    
    @Value('${ai.ollama.timeout}')
    String timeout
    
    private OllamaChatModel chatModel
    
    @PostConstruct
    void init() {
        String baseUrl = "http://${ollamaHost}:${ollamaPort}"
        chatModel = OllamaChatModel.builder()
            .baseUrl(baseUrl)
            .modelName(defaultModel)
            .timeout(java.time.Duration.parse("PT${timeout}"))
            .build()
    }
    
    Mono<String> chat(String prompt, String model = null) {
        return Mono.fromCallable {
            def modelToUse = model ?: defaultModel
            if (model && model != defaultModel) {
                // Create new model instance if different model requested
                def tempModel = OllamaChatModel.builder()
                    .baseUrl("http://${ollamaHost}:${ollamaPort}")
                    .modelName(modelToUse)
                    .timeout(java.time.Duration.parse("PT${timeout}"))
                    .build()
                return tempModel.generate(UserMessage.from(prompt)).content().text()
            }
            return chatModel.generate(UserMessage.from(prompt)).content().text()
        }
    }
    
    Mono<List<String>> getAvailableModels() {
        return Mono.fromCallable {
            // TODO: Implement actual model listing via Ollama API
            // For now return common models
            return [defaultModel, "llama3.1:8b", "llama3.1:70b", "codellama"]
        }
    }
}