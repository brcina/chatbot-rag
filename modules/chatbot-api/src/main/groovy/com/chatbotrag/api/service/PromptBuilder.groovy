package com.chatbotrag.api.service

import com.chatbotrag.core.domain.SearchResult
import jakarta.inject.Singleton

@Singleton
class PromptBuilder {
    
    private static final String RAG_SYSTEM_PROMPT = """
        You are a helpful AI assistant that answers questions based on the provided context.
        Use the context information to provide accurate and relevant answers.
        If the context doesn't contain enough information to answer the question, say so clearly.
        Always cite the source documents when possible.
        """
    
    private static final String RAG_USER_TEMPLATE = """
        Context:
        %s
        
        Question: %s
        
        Please provide a helpful answer based on the context above.
        """
    
    String buildRAGPrompt(String userQuery, List<SearchResult> context) {
        def contextBuilder = new StringBuilder()
        
        context.eachWithIndex { result, i ->
            contextBuilder.append("[Document ${i + 1}: ${result.title}]\n")
            contextBuilder.append(result.content)
            contextBuilder.append("\n\n")
        }
        
        String.format(RAG_USER_TEMPLATE, contextBuilder.toString().trim(), userQuery)
    }
    
    String getSystemPrompt() {
        RAG_SYSTEM_PROMPT
    }
    
    String buildSimplePrompt(String userMessage) {
        userMessage
    }
}