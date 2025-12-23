package ru.ifmo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.ifmo.dto.TextTask;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class TextSplitterService {
    
    @Value("${text.processing.chunk.size:1000}")
    private int chunkSize;
    
    @Value("${text.processing.top.words:10}")
    private int topWords;
    
    @Value("${text.processing.name.replacement:[NAME]}")
    private String nameReplacement;
    
    public List<TextTask> splitTextFile(String filePath, String sessionId) throws IOException {
        log.info("Reading text file: {}", filePath);
        
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new IOException("File not found: " + filePath);
        }
        
        String content = Files.readString(path);
        log.info("File read successfully. Content length: {} characters", content.length());
        
        return splitTextIntoTasks(content, sessionId);
    }
    
    public List<TextTask> splitTextIntoTasks(String text, String sessionId) {
        List<TextTask> tasks = new ArrayList<>();
        
        if (text == null || text.trim().isEmpty()) {
            log.warn("Empty text provided for splitting");
            return tasks;
        }
        
        // Split by paragraphs first
        String[] paragraphs = text.split("\\n\\s*\\n");
        
        StringBuilder currentChunk = new StringBuilder();
        int taskCounter = 1;
        
        for (String paragraph : paragraphs) {
            paragraph = paragraph.trim();
            if (paragraph.isEmpty()) {
                continue;
            }
            
            // If adding this paragraph would exceed chunk size, create a task
            if (currentChunk.length() > 0 && 
                currentChunk.length() + paragraph.length() + 2 > chunkSize) {
                
                tasks.add(createTextTask(currentChunk.toString(), sessionId, taskCounter++));
                currentChunk = new StringBuilder();
            }
            
            if (currentChunk.length() > 0) {
                currentChunk.append("\n\n");
            }
            currentChunk.append(paragraph);
        }
        
        // Add the last chunk if it's not empty
        if (currentChunk.length() > 0) {
            tasks.add(createTextTask(currentChunk.toString(), sessionId, taskCounter));
        }
        
        log.info("Split text into {} tasks for session {}", tasks.size(), sessionId);
        return tasks;
    }
    
    public List<TextTask> splitTextBySentences(String text, String sessionId, int sentencesPerTask) {
        List<TextTask> tasks = new ArrayList<>();
        
        if (text == null || text.trim().isEmpty()) {
            log.warn("Empty text provided for splitting");
            return tasks;
        }
        
        // Split by sentences
        String[] sentences = text.split("(?<=[.!?])\\s+");
        
        List<String> currentChunk = new ArrayList<>();
        int taskCounter = 1;
        
        for (String sentence : sentences) {
            sentence = sentence.trim();
            if (sentence.isEmpty()) {
                continue;
            }
            
            currentChunk.add(sentence);
            
            if (currentChunk.size() >= sentencesPerTask) {
                String chunkText = String.join(" ", currentChunk);
                tasks.add(createTextTask(chunkText, sessionId, taskCounter++));
                currentChunk.clear();
            }
        }
        
        // Add the last chunk if it's not empty
        if (!currentChunk.isEmpty()) {
            String chunkText = String.join(" ", currentChunk);
            tasks.add(createTextTask(chunkText, sessionId, taskCounter));
        }
        
        log.info("Split text into {} tasks by sentences for session {}", tasks.size(), sessionId);
        return tasks;
    }
    
    public List<TextTask> splitTextByWords(String text, String sessionId, int wordsPerTask) {
        List<TextTask> tasks = new ArrayList<>();
        
        if (text == null || text.trim().isEmpty()) {
            log.warn("Empty text provided for splitting");
            return tasks;
        }
        
        String[] words = text.split("\\s+");
        
        StringBuilder currentChunk = new StringBuilder();
        int wordCount = 0;
        int taskCounter = 1;
        
        for (String word : words) {
            if (word.trim().isEmpty()) {
                continue;
            }
            
            if (wordCount > 0) {
                currentChunk.append(" ");
            }
            currentChunk.append(word);
            wordCount++;
            
            if (wordCount >= wordsPerTask) {
                tasks.add(createTextTask(currentChunk.toString(), sessionId, taskCounter++));
                currentChunk = new StringBuilder();
                wordCount = 0;
            }
        }
        
        // Add the last chunk if it's not empty
        if (wordCount > 0) {
            tasks.add(createTextTask(currentChunk.toString(), sessionId, taskCounter));
        }
        
        log.info("Split text into {} tasks by words for session {}", tasks.size(), sessionId);
        return tasks;
    }
    
    private TextTask createTextTask(String text, String sessionId, int taskNumber) {
        TextTask task = new TextTask();
        task.setTaskId(String.format("%s-task-%03d", sessionId, taskNumber));
        task.setText(text);
        task.setTopN(topWords);
        task.setNameReplacement(nameReplacement);
        return task;
    }
    
    public String generateSessionId() {
        return "session-" + UUID.randomUUID().toString().substring(0, 8);
    }
}