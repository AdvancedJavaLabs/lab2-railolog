package ru.ifmo.dto;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class TextProcessingResult {
    private String taskId;
    private String taskType;

    private Integer wordCount;

    private Map<String, Integer> topWords;

    private String sentiment;
    private Double sentimentScore;

    private String modifiedText;

    private List<String> sortedSentences;
}