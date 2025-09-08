package com.chatbotrag.tools.service;

import jakarta.inject.Singleton;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

@Singleton
public class FileSearchTool {
    
    public Flux<String> searchFiles(String directory, String pattern) {
        return Flux.fromStream(() -> {
            try {
                Path dir = Paths.get(directory);
                if (!Files.exists(dir) || !Files.isDirectory(dir)) {
                    return Stream.empty();
                }
                
                return Files.walk(dir)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().contains(pattern))
                    .map(Path::toString);
            } catch (IOException e) {
                return Stream.empty();
            }
        });
    }
    
    public Flux<String> searchInFiles(String directory, String pattern, String fileExtension) {
        return Flux.fromStream(() -> {
            try {
                Path dir = Paths.get(directory);
                if (!Files.exists(dir) || !Files.isDirectory(dir)) {
                    return Stream.empty();
                }
                
                return Files.walk(dir)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(fileExtension))
                    .filter(path -> {
                        try {
                            List<String> lines = Files.readAllLines(path);
                            return lines.stream().anyMatch(line -> line.contains(pattern));
                        } catch (IOException e) {
                            return false;
                        }
                    })
                    .map(Path::toString);
            } catch (IOException e) {
                return Stream.empty();
            }
        });
    }
    
    public boolean fileExists(String filePath) {
        return Files.exists(Paths.get(filePath));
    }
    
    public long getFileSize(String filePath) {
        try {
            return Files.size(Paths.get(filePath));
        } catch (IOException e) {
            return -1;
        }
    }
}