package ru.ifmo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.ifmo.dto.TextTask;
import ru.ifmo.dto.TextProcessingResult;
import ru.ifmo.service.TextProcessingService;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {
    
    private final TextProcessingService textProcessingService;
    
    @PostMapping("/process")
    public TextProcessingResult processText(@RequestBody TextTask task) {
        return textProcessingService.processTask(task);
    }
    
    @GetMapping("/demo")
    public TextProcessingResult demoProcessing() {
        TextTask task = new TextTask();
        task.setTaskId("demo-001");
        task.setTaskType("ALL_TASKS");
        task.setTopN(5);
        task.setNameReplacement("[PERSON]");
        task.setText("Hello, my name is John Smith. This is a wonderful day! " +
                    "I love programming and John enjoys reading books. " +
                    "Mary said she feels great today. " +
                    "The weather is terrible but we are happy.");
        
        return textProcessingService.processTask(task);
    }
}