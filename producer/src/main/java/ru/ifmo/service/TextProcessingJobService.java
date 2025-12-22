package ru.ifmo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.ifmo.dto.SessionInfo;
import ru.ifmo.dto.TextTask;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TextProcessingJobService {
    
    private final TextSplitterService textSplitterService;
    private final MessagePublisherService messagePublisherService;
    
    public String processTextFile(String filePath) throws IOException {
        return processTextFile(filePath, SplitStrategy.BY_PARAGRAPHS, 1000);
    }
    
    public String processTextFile(String filePath, SplitStrategy strategy, int splitSize) throws IOException {
        log.info("Starting text processing job for file: {}", filePath);
        
        // Generate session ID
        String sessionId = textSplitterService.generateSessionId();
        log.info("Generated session ID: {}", sessionId);
        
        // Read and split the text file
        List<TextTask> tasks = splitTextFile(filePath, sessionId, strategy, splitSize);
        
        if (tasks.isEmpty()) {
            throw new IllegalArgumentException("No tasks generated from file: " + filePath);
        }
        
        // Send session info to aggregator
        SessionInfo sessionInfo = new SessionInfo();
        sessionInfo.setSessionId(sessionId);
        sessionInfo.setExpectedTaskCount(tasks.size());
        sessionInfo.setDescription("Text processing job for file: " + filePath);
        
        messagePublisherService.publishSessionInfo(sessionInfo);
        
        // Send tasks to workers
        messagePublisherService.publishTasks(tasks);
        
        log.info("Text processing job completed. Session: {}, Tasks: {}", sessionId, tasks.size());
        return sessionId;
    }
    
    public String processTextContent(String text, String description) {
        return processTextContent(text, description, SplitStrategy.BY_PARAGRAPHS, 1000);
    }
    
    public String processTextContent(String text, String description, SplitStrategy strategy, int splitSize) {
        log.info("Starting text processing job for content: {}", description);
        
        // Generate session ID
        String sessionId = textSplitterService.generateSessionId();
        log.info("Generated session ID: {}", sessionId);
        
        // Split the text content
        List<TextTask> tasks = splitTextContent(text, sessionId, strategy, splitSize);
        
        if (tasks.isEmpty()) {
            throw new IllegalArgumentException("No tasks generated from text content");
        }
        
        // Send session info to aggregator
        SessionInfo sessionInfo = new SessionInfo();
        sessionInfo.setSessionId(sessionId);
        sessionInfo.setExpectedTaskCount(tasks.size());
        sessionInfo.setDescription(description);
        
        messagePublisherService.publishSessionInfo(sessionInfo);
        
        // Send tasks to workers
        messagePublisherService.publishTasks(tasks);
        
        log.info("Text processing job completed. Session: {}, Tasks: {}", sessionId, tasks.size());
        return sessionId;
    }
    
    private List<TextTask> splitTextFile(String filePath, String sessionId, SplitStrategy strategy, int splitSize) throws IOException {
        switch (strategy) {
            case BY_PARAGRAPHS:
                return textSplitterService.splitTextFile(filePath, sessionId);
            case BY_SENTENCES:
                String content = java.nio.file.Files.readString(java.nio.file.Paths.get(filePath));
                return textSplitterService.splitTextBySentences(content, sessionId, splitSize);
            case BY_WORDS:
                content = java.nio.file.Files.readString(java.nio.file.Paths.get(filePath));
                return textSplitterService.splitTextByWords(content, sessionId, splitSize);
            default:
                throw new IllegalArgumentException("Unknown split strategy: " + strategy);
        }
    }
    
    private List<TextTask> splitTextContent(String text, String sessionId, SplitStrategy strategy, int splitSize) {
        switch (strategy) {
            case BY_PARAGRAPHS:
                return textSplitterService.splitTextIntoTasks(text, sessionId);
            case BY_SENTENCES:
                return textSplitterService.splitTextBySentences(text, sessionId, splitSize);
            case BY_WORDS:
                return textSplitterService.splitTextByWords(text, sessionId, splitSize);
            default:
                throw new IllegalArgumentException("Unknown split strategy: " + strategy);
        }
    }
    
    public enum SplitStrategy {
        BY_PARAGRAPHS,
        BY_SENTENCES,
        BY_WORDS
    }
}