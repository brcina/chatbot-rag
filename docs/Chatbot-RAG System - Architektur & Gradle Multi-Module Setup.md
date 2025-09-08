## System-Übersicht

**Ziel:** RAG-basierter Chatbot mit PDF-Indexierung, Ollama LLMs und MCP-Tools  
**Deployment:** Vereinfacht auf Vast.ai (Direkte Ollama Verbindung)  
**Framework:** Micronaut + LangChain4j + Ollama

## Architektur-Aufteilung

### Ollama Instance (GPU - Vast.ai)
- **Zweck:** **NUR** LLM Inferenz (Ollama)
- **Services:** Ollama Server + Models
- **Hardware:** RTX 4090, 24GB VRAM
- **Port:** 11434 (Ollama API)

### Chatbot API Instance (CPU - Vast.ai)
- **Zweck:** RAG, MCP Tools, **komplette API + Frontend** + **direkte Ollama Verbindung**
- **Services:** Vector Store, Document Processing, MCP Tools, **Chatbot API mit integriertem Ollama Client**
- **Hardware:** 8+ CPU Cores, 32GB RAM
- **Ports:** 8080 (API), 80/443 (Web UI)

## Gradle Multi-Module Architektur

### Projekt-Struktur
```groovy
// settings.gradle
rootProject.name = 'chatbot-rag'

include 'modules:core'                    // Domain Models & Vector Store
include 'modules:document-processor'      // PDF/Text Processing & Chunking
include 'modules:tools'                   // MCP Tools (File, Web, System)
include 'modules:chatbot-api'            // Hauptanwendung mit integriertem Ollama Client
include 'modules:chatbot-frontend'       // Static Web Assets (optional)
```

### Root Build Configuration
```groovy
// build.gradle
plugins {
    id 'java-library'
    id 'io.micronaut.application' version '4.3.5' apply false
    id 'io.micronaut.library' version '4.3.5' apply false
}

ext {
    micronautVersion = '4.3.5'
    langchain4jVersion = '0.29.1'
    tikaVersion = '2.9.1'
    reactorVersion = '3.6.2'
}

allprojects {
    group = 'com.chatbotrag'
    version = '1.0.0'
    
    repositories {
        mavenCentral()
    }
}

// Common configuration for all modules
subprojects {
    apply plugin: 'java-library'
    
    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    dependencies {
        implementation "org.slf4j:slf4j-api:2.0.12"
        testImplementation "org.junit.jupiter:junit-jupiter:5.10.1"
    }
    
    test {
        useJUnitPlatform()
    }
}

// Chatbot API Application configuration
configure(project(':modules:chatbot-api')) {
    apply plugin: 'io.micronaut.application'
    apply plugin: 'application'
    
    micronaut {
        version = micronautVersion
        runtime 'netty'
        testRuntime 'junit5'
    }
    
    application {
        mainClass = 'com.chatbotrag.api.ChatbotRagApplication'
    }
}

// Library modules configuration
configure(subprojects.findAll { 
    it.name != 'chatbot-api' && 
    it.name != 'chatbot-frontend' 
}) {
    apply plugin: 'io.micronaut.library'
}
```

### Core Module (Domain & Vector Store)
```groovy
// core/build.gradle
dependencies {
    // LangChain4j Core
    api "dev.langchain4j:langchain4j:$langchain4jVersion"
    api "dev.langchain4j:langchain4j-embeddings:$langchain4jVersion"
    api "io.projectreactor:reactor-core:$reactorVersion"
    
    // Micronaut DI
    implementation "io.micronaut:micronaut-inject"
    implementation "jakarta.annotation:jakarta.annotation-api:2.1.1"
    
    // Vector Store (implementation TBD)
    // implementation "vector-store-dependency" // To be decided
    implementation "dev.langchain4j:langchain4j-embeddings-all-minilm-l6-v2:$langchain4jVersion"
}
```

**Domain Classes:**
- `Document.java`, `ChatMessage.java`, `SearchResult.java`
- `DocumentService.java`, `EmbeddingService.java`, `VectorStoreService.java`
- `VectorStoreService.java` (Generic interface for vector store implementations)
- `CoreConfiguration.java`

### Document Processor Module (PDF/Text Processing)
```groovy
// document-processor/build.gradle
dependencies {
    api project(':core')
    
    // Document parsing
    implementation "org.apache.tika:tika-core:$tikaVersion"
    implementation "org.apache.tika:tika-parsers-standard-package:$tikaVersion"
    implementation "org.apache.pdfbox:pdfbox:3.0.1"
    implementation "org.jsoup:jsoup:1.17.2"
    
    // LangChain4j document processing
    implementation "dev.langchain4j:langchain4j-document-parser-apache-tika:$langchain4jVersion"
}
```

**Features:**
- Generische DocumentProcessor Interface
- PDF, Text, Word, HTML Processor Implementierungen
- Smart Chunking (Semantic, Fixed-Size, Sentence-based)
- Metadata Extraction (Author, Title, Creation Date)
- Integration mit Vector Store für automatische Indexierung

### Tools Module (MCP Tools)
```groovy
// tools/build.gradle
dependencies {
    api project(':core')
    
    // Tool implementations
    implementation "org.jsoup:jsoup:1.17.2"           // Web Scraping
    implementation "com.github.oshi:oshi-core:6.4.10" // System Info
}
```

**Tools:**
- `FileSearchTool.java` (Local file search)
- `WebSearchTool.java` (Web scraping mit DuckDuckGo)
- `SystemInfoTool.java` (System monitoring)
- `MCPToolRegistry.java` (Tool coordination)

### Chatbot API Module (Agentic AI - Hauptanwendung)
```groovy
// chatbot-api/build.gradle
dependencies {
    // All modules
    implementation project(':core')
    implementation project(':document-processor')
    implementation project(':tools')
    
    // Micronaut Web Stack
    implementation "io.micronaut:micronaut-http-server-netty"
    implementation "io.micronaut:micronaut-websocket"
    implementation "io.micronaut:micronaut-multipart"
    implementation "io.micronaut.serde:micronaut-serde-jackson"
    implementation "io.micronaut:micronaut-http-client" // Für Generative AI Client
    
    // RAG Components + Ollama Client
    implementation "dev.langchain4j:langchain4j:$langchain4jVersion"
    implementation "dev.langchain4j:langchain4j-ollama:$langchain4jVersion"
    implementation "dev.langchain4j:langchain4j-embeddings-all-minilm-l6-v2:$langchain4jVersion"
    
    // Vector Store (implementation TBD)
    // implementation "vector-store-dependency" // To be decided
    
    // Frontend Integration
    implementation "io.micronaut.views:micronaut-views-thymeleaf"
    
    // Runtime
    runtimeOnly "ch.qos.logback:logback-classic"
    
    // Testing
    testImplementation "io.micronaut:micronaut-http-client"
    testImplementation "io.micronaut.test:micronaut-test-junit5"
}

application {
    mainClass = 'com.chatbotrag.api.ChatbotRagApplication'
}
```

**Services (integriert RAG + API + Ollama Client):**
- `ChatService.java` (RAG Logic + Vector Search)
- `OllamaService.groovy` (LangChain4j Ollama Client zu GPU Instance)
- `MCPToolRegistry.java` (Tool Coordination)

**Controllers:**
- `ChatController.java` (REST Chat API)
- `DocumentController.java` (Document Upload/Management)
- `ChatWebSocketHandler.java` (Real-time Chat)

**DTOs:**
- `ChatRequest.java`, `ChatResponse.java`
- `DocumentUploadRequest.java`, `SearchRequest.java`

### Ollama Configuration (direkt in chatbot-api integriert)
```yaml
# application.yml
ai:
  ollama:
    host: ${OLLAMA_HOST:localhost}
    port: ${OLLAMA_PORT:11434}
    default-model: ${OLLAMA_MODEL:llama3.2}
    timeout: ${OLLAMA_TIMEOUT:60s}
```

**Services:**
- `OllamaService.groovy` (LangChain4j Ollama Client)
- Direkte Integration in ChatService

### Frontend Module (Static Assets)
```groovy
// chatbot-frontend/build.gradle
dependencies {
    // Optional - nur für separate Frontend-Builds
}

task buildFrontend(type: Exec) {
    description = 'Build frontend assets'
    commandLine 'npm', 'run', 'build'
    
    inputs.dir 'src'
    outputs.dir 'build/resources/static'
}

processResources.dependsOn buildFrontend
```

**Static Assets:**
- `index.html` (Chatbot UI)
- `chat.js` (WebSocket client)
- `chatbot.css` (Styling)

## Module Dependencies & Flow

```groovy
// Dependency Graph
chatbot-api {
    dependencies {
        implementation project(':modules:core')           // Vector Store & Domain
        implementation project(':modules:document-processor') // PDF Processing
        implementation project(':modules:tools')          // MCP Tools
        // RAG Logic + Ollama Client direkt integriert
    }
}

document-processor {
    dependencies {
        api project(':core')                     // Vector Store Integration
    }
}

tools {
    dependencies {
        api project(':core')                     // Common Interfaces
    }
}
```

## Konfiguration

### Chatbot API Instance Configuration
```yaml
# application.yml
micronaut:
  application:
    name: chatbot-rag
  server:
    port: 8080

ai:
  ollama:
    host: ${OLLAMA_HOST:localhost} # IP der GPU Instance
    port: ${OLLAMA_PORT:11434}
    default-model: ${OLLAMA_MODEL:llama3.2}
    timeout: ${OLLAMA_TIMEOUT:60s}

vector-store:
  # Configuration will depend on chosen implementation
  # url: TBD
  # collection: documents

mcp:
  tools:
    enabled: true
    file-search:
      enabled: true
      max-depth: 3
    web-search:
      enabled: true
      provider: duckduckgo
    system-info:
      enabled: true
```

### Ollama Instance Configuration (externe Vast.ai Instanz)
```yaml
# Ollama läuft direkt auf der GPU Instanz
# Konfiguration erfolgt über Ollama's eigene Konfiguration
# Keine separate Micronaut Anwendung mehr nötig
```

## Docker Deployment

### Chatbot API Docker Compose
```yaml
# docker-compose.api.yml
version: '3.8'
services:
  chatbot-api:
    image: chatbot-rag-api
    ports:
      - "8080:8080"
    environment:
      - OLLAMA_HOST=${OLLAMA_HOST}  # IP der Vast.ai Ollama Instanz
      - OLLAMA_PORT=${OLLAMA_PORT:11434}
      - OLLAMA_MODEL=${OLLAMA_MODEL:llama3.2}
    volumes:
      - ./uploads:/app/uploads
      - ./documents:/app/documents
    depends_on:
      # - vector-store  # Will be added when vector store is chosen
      
  # vector-store:
  #   image: TBD
  #   ports:
  #     - "TBD:TBD"
  #   volumes:
  #     - vector_data:/var/data

volumes:
  # vector_data:
```

### Ollama Instance (läuft direkt auf Vast.ai GPU)
```bash
# Ollama wird direkt über Vast.ai Template installiert
# Keine separate Docker Compose nötig
# Standard Ollama Installation mit GPU Support
```

## Build & Deployment Tasks

```groovy
// Custom Gradle Tasks
task buildAPI {
    description = 'Build for API Frontend deployment'
    dependsOn 'clean', ':modules:chatbot-api:build'
}

task buildLLM {
    description = 'Build for LLM Backend deployment (now integrated into API)'
    dependsOn 'clean', ':modules:chatbot-api:build'
}

task dockerBuildAll {
    dependsOn 'build'
    description = 'Build Docker images for both deployments'
    
    doLast {
        exec { 
            commandLine 'docker', 'build', '-f', 'docker/llm/Dockerfile.llm', '-t', 'chatbot-rag-llm', '.'
            ignoreExitValue = true
        }
        exec { 
            commandLine 'docker', 'build', '-f', 'docker/api/Dockerfile.api', '-t', 'chatbot-rag-api', '.'
            ignoreExitValue = true
        }
    }
}

task deployVastAI {
    dependsOn 'dockerBuildAll'
    description = 'Deploy to Vast.ai instances'
    
    doLast {
        exec { 
            commandLine './deploy-llm.sh', System.getProperty('llm.instance.id')
            ignoreExitValue = true
        }
        exec { 
            commandLine './deploy-api.sh', System.getProperty('api.instance.id'), System.getProperty('llm.host')
            ignoreExitValue = true
        }
    }
}
```

## Deployment auf Vast.ai

### 1. Ollama Instance (GPU)
- **Hardware:** RTX 4090, 24GB VRAM, 8+ CPU Cores, 32GB RAM
- **Services:** Nur Ollama + LLM Models (Standard Installation)
- **Port:** 11434 (Ollama API)

### 2. Chatbot API Instance (CPU)
- **Hardware:** 8+ CPU Cores, 32GB RAM
- **Services:** Chatbot API + Frontend + integrierter Ollama Client, Vector Store, Document Processing, MCP Tools
- **Ports:** 8080 (API), 80/443 (Nginx)

### Deployment Commands
```bash
# Build alle Module
./gradlew build

# Docker Images erstellen
./gradlew dockerBuildAll

# Deploy auf Vast.ai
./gradlew deployVastAI \
  -Dllm.instance.id=12345 \
  -Dapi.instance.id=67890 \
  -Dllm.host=1.2.3.4
```

## Datenfluss

1. **User Query** → Chatbot API Instance (chatbot-api)
2. **Vector Search** → Vector Store (lokale Vector DB auf API Instance)
3. **MCP Tools** → FileSearch, WebSearch, SystemInfo (auf API Instance)
4. **Prompt Building** → Kontext aus PDFs + Tool Results (auf API Instance)
5. **LLM Call** → LangChain4j Ollama Client → REST zu Ollama Instance (GPU)
6. **Response** → Zurück zu User über WebSocket/REST

## Key Features der korrigierten Architektur

1. **Klare Trennung**: GPU Instance nur für Ollama, CPU Instance für alles andere
2. **Vereinfachte Architektur**: Kein separates generative-ai-server Modul mehr
3. **RAG auf CPU**: Vector Search und Document Processing auf kostengünstiger CPU Instance
4. **MCP Tools Integration**: File, Web und System Tools auf API Instance
5. **Direkte Ollama Integration**: LangChain4j Ollama Client direkt in chatbot-api
6. **Skalierbar**: Vector Store und API können unabhängig von GPU skaliert werden
7. **Frontend Integration**: Chat Bot UI

## Technologie-Stack

- **Framework:** Micronaut 4.3.5
- **AI/LLM:** LangChain4j + Ollama (GPU Instance)
- **Document Processing:** Apache Tika + PDFBox (CPU Instance)
- **MCP Tools:** File Search, Web Search, System Info (CPU Instance)
- **Frontend:** HTML/CSS/JS + WebSocket (CPU Instance)
- **Build:** Gradle Multi-Module
- **Deployment:** Docker + Vast.ai

*Vector Store wird später hinzugefügt*

## Verzeichnisstruktur

```
chatbot-rag/
├── settings.gradle
├── build.gradle
├── gradle.properties
├── core/
│   ├── build.gradle
│   └── src/main/java/com/chatbotrag/core/
├── document-processor/
│   ├── build.gradle
│   └── src/main/java/com/chatbotrag/processor/
├── tools/
│   ├── build.gradle
│   └── src/main/java/com/chatbotrag/tools/
├── modules/
│   ├── core/
│   │   ├── build.gradle
│   │   └── src/main/groovy/com/chatbotrag/core/
│   ├── document-processor/
│   │   ├── build.gradle
│   │   └── src/main/groovy/com/chatbotrag/processor/
│   ├── tools/
│   │   ├── build.gradle
│   │   └── src/main/groovy/com/chatbotrag/tools/
│   ├── chatbot-api/
│   │   ├── build.gradle
│   │   └── src/main/groovy/com/chatbotrag/api/
│   └── chatbot-frontend/
│       ├── build.gradle
│       └── src/main/resources/
├── chatbot-frontend/
│   ├── build.gradle
│   └── src/main/resources/
└── docker/
    ├── llm/
    │   └── Dockerfile.llm
    └── api/
        └── Dockerfile.api
```
