package ru.ifmo.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class AggregatedResult {
    private String aggregationId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long processingDurationMs;
    private int totalSections;
    private List<String> processedTaskIds;

    private Long totalWordCount;

    private Map<String, Integer> mergedTopWords;

    private String overallSentiment; // POSITIVE, NEGATIVE, NEUTRAL
    private Double averageSentimentScore;
    private Map<String, Integer> sentimentDistribution;

    private List<String> modifiedTextSections;
    private String combinedModifiedText;

    private List<String> allSortedSentences;

    private List<SectionResult> sectionResults;
}