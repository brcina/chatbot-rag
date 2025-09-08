# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Structure

This is a clean, organized Micronaut-based multi-module Gradle project implementing a RAG (Retrieval-Augmented Generation) chatbot system. All source modules are organized in the `modules/` directory:

```
chatbot-rag/
├── gradle/                    # Gradle wrapper
├── gradlew                    # Build scripts
├── build.gradle               # Root configuration
├── settings.gradle            # Project settings
├── modules/                   # Source code modules
│   ├── core/                  # Domain models & vector store services
│   ├── document-processor/    # PDF/text processing with Apache Tika
│   ├── chatbot-api/          # Main Micronaut REST/WebSocket application with integrated Ollama client
│   ├── chatbot-frontend/     # Static web assets (HTML/CSS/JS)
│   └── tools/                # MCP-compatible tools
└── docker/                   # Deployment configurations
    ├── llm/                  # GPU instance deployment
    └── api/                  # CPU instance deployment
```

## Development Commands

### Build and Test
- `./gradlew build` - Build all modules and run tests
- `./gradlew test` - Run all tests across modules
- `./gradlew clean` - Clean build artifacts
- `./gradlew :modules:MODULE_NAME:build` - Build specific module (e.g., `:modules:chatbot-api:build`)

### Running Applications
- `./gradlew :modules:chatbot-api:run` - Start main Micronaut application (port 8080)
- `./gradlew buildLLM` - Build for GPU-intensive deployment (Ollama + RAG)
- `./gradlew buildAPI` - Build for CPU-based deployment (API + Frontend)

### Deployment Commands
- `./gradlew dockerBuildAll` - Build Docker images for both deployment profiles
- `./gradlew deployVastAI -Dllm.instance.id=12345 -Dapi.instance.id=67890` - Deploy to Vast.ai

### Module Development
- All source modules are in `modules/` directory for clean organization
- Module dependencies use `:modules:module-name` format in build.gradle files

## Architecture Notes

### Core Module
- Domain models: `Document`, `ChatMessage`, `SearchResult`
- Service interfaces: `DocumentService`, `EmbeddingService`, `VectorStoreService`
- All-MiniLM-L6-v2 embeddings (384 dimensions)

### Document Processor
- Multi-format support: PDF (PDFBox), Text, HTML, Word documents
- Apache Tika for metadata extraction and content parsing
- Smart chunking strategies (semantic, fixed-size, sentence-based)
- Reactive processing with Reactor

### Chatbot API (Main Application)
- Micronaut 4.3.5 with Netty HTTP server
- REST endpoints: `/api/chat`, `/api/documents`  
- WebSocket support for real-time chat
- File upload with multipart support
- Reactive controllers using Reactor
- Integrated LangChain4j Ollama client for direct GPU instance communication

### Tools Module
- `FileSearchTool`: Local file system search and content analysis
- `WebSearchTool`: Web scraping with Jsoup
- `SystemInfoTool`: System monitoring with OSHI
- MCP-compatible for agentic AI workflows

### Frontend
- Static HTML/CSS/JS application
- WebSocket client for real-time chat
- Document upload interface
- System information sidebar

## Configuration

### Application Profiles
- `llm`: GPU instance profile (now just Ollama server)
- `api`: CPU instance profile (API + Frontend + Tools + Ollama Client)

### Key Configuration Properties
```yaml
core:
  vector-store:
    host: localhost
    port: 8000
    collection: documents
    dimension: 384
  embedding:
    model: sentence-transformers/all-MiniLM-L6-v2

ai:
  ollama:
    host: ${OLLAMA_HOST:localhost}
    port: ${OLLAMA_PORT:11434}
    default-model: ${OLLAMA_MODEL:llama3.2}
    timeout: ${OLLAMA_TIMEOUT:60s}
  rag:
    max-context-results: 5
    relevance-threshold: 0.7
```

## Deployment Architecture

### Vast.ai Deployment
- **LLM Instance**: RTX 4090 (24GB VRAM) for Ollama server only
- **API Instance**: CPU-only for API server + Frontend + MCP Tools + ChromaDB + RAG processing + Ollama Client
- Separate scaling for compute-intensive AI vs. user-facing services
- Direct REST communication between API instance and Ollama instance

## Technology Stack
- **Framework**: Micronaut 4.3.5 (Netty runtime)
- **Language**: Groovy on Java 21
- **AI/LLM**: LangChain4j + Ollama
- **Vector Store**: ChromaDB with all-MiniLM-L6-v2 embeddings
- **Document Processing**: Apache Tika + PDFBox
- **Build System**: Gradle multi-module
- **Deployment**: Docker + Vast.ai