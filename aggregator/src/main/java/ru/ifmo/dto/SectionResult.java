package ru.ifmo.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class SectionResult {
    private String taskId;
    private Integer wordCount;
    private Map<String, Integer> topWords;
    private String sentiment;
    private Double sentimentScore;
    private String modifiedText;
    private List<String> sortedSentences;
}