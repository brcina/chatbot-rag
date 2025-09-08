package com.chatbotrag.core.config;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Factory;
import jakarta.inject.Singleton;

@Factory
@ConfigurationProperties("core")
public class CoreConfiguration {

    @ConfigurationProperties("vector-store")
    public static class VectorStoreConfig {
        private String host = "localhost";
        private int port = 8000;
        private String collection = "documents";
        private int dimension = 384;
        
        // Getters and setters
        public String getHost() { return host; }
        public void setHost(String host) { this.host = host; }
        
        public int getPort() { return port; }
        public void setPort(int port) { this.port = port; }
        
        public String getCollection() { return collection; }
        public void setCollection(String collection) { this.collection = collection; }
        
        public int getDimension() { return dimension; }
        public void setDimension(int dimension) { this.dimension = dimension; }
    }

    @ConfigurationProperties("embedding")
    public static class EmbeddingConfig {
        private String model = "sentence-transformers/all-MiniLM-L6-v2";
        private int batchSize = 32;
        
        // Getters and setters
        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
        
        public int getBatchSize() { return batchSize; }
        public void setBatchSize(int batchSize) { this.batchSize = batchSize; }
    }

    @Singleton
    public VectorStoreConfig vectorStoreConfig() {
        return new VectorStoreConfig();
    }

    @Singleton
    public EmbeddingConfig embeddingConfig() {
        return new EmbeddingConfig();
    }
}