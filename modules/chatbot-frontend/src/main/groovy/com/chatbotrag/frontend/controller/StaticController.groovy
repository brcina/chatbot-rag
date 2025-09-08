package com.chatbotrag.frontend.controller

import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.server.types.files.SystemFile
import jakarta.inject.Inject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

@Controller
class StaticController {
    
    private static final Logger log = LoggerFactory.getLogger(StaticController)
    private static final String STATIC_ROOT = "src/main/frontend/build"
    
    @Get("/{path:.*}")
    HttpResponse<SystemFile> serveStaticFile(String path) {
        // Default to index.html for SPA routing
        if (!path || path.isEmpty() || path == "/") {
            path = "index.html"
        }
        
        Path filePath = Paths.get(STATIC_ROOT, path)
        
        // Check if file exists
        if (!Files.exists(filePath) || Files.isDirectory(filePath)) {
            // For SPA routing, serve index.html for non-API routes
            if (!path.startsWith("api/")) {
                filePath = Paths.get(STATIC_ROOT, "index.html")
            } else {
                return HttpResponse.notFound()
            }
        }
        
        try {
            String contentType = getContentType(filePath.toString())
            return HttpResponse.ok(new SystemFile(filePath.toFile()))
                    .contentType(MediaType.of(contentType))
        } catch (Exception e) {
            log.error("Error serving static file: ${path}", e)
            return HttpResponse.serverError()
        }
    }
    
    private String getContentType(String fileName) {
        if (fileName.endsWith('.html')) return 'text/html'
        if (fileName.endsWith('.css')) return 'text/css'  
        if (fileName.endsWith('.js')) return 'application/javascript'
        if (fileName.endsWith('.json')) return 'application/json'
        if (fileName.endsWith('.png')) return 'image/png'
        if (fileName.endsWith('.jpg') || fileName.endsWith('.jpeg')) return 'image/jpeg'
        if (fileName.endsWith('.gif')) return 'image/gif'
        if (fileName.endsWith('.svg')) return 'image/svg+xml'
        if (fileName.endsWith('.ico')) return 'image/x-icon'
        return 'application/octet-stream'
    }
}