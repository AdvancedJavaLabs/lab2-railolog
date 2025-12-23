package ru.ifmo.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class StartupTextProcessingService {
    
    private final TextProcessingJobService textProcessingJobService;
    
    @EventListener(ApplicationReadyEvent.class)
    public void processDefaultTextOnStartup() {
        try {
            log.info("Application started, checking for default text processing...");
            
            // Check if sample text file exists and process it
            ClassPathResource resource = new ClassPathResource("big.txt");
            if (resource.exists()) {
                log.info("Found sample text file, processing...");
                
                // Copy to temp file for processing
                Path tempFile = Files.createTempFile("big", ".txt");
                Files.copy(resource.getInputStream(), tempFile, 
                          java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                
                // Process the file
                String sessionId = textProcessingJobService.processTextFile(
                    tempFile.toString(), 
                    TextProcessingJobService.SplitStrategy.BY_PARAGRAPHS, 
                    1000
                );
                
                log.info("Sample text processing started with session ID: {}", sessionId);
                
                // Clean up temp file
                Files.deleteIfExists(tempFile);
                
            } else {
                log.info("No sample text file found, skipping automatic processing");
            }
            
        } catch (IOException e) {
            log.error("Error processing sample text file: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during startup text processing: {}", e.getMessage(), e);
        }
    }
}