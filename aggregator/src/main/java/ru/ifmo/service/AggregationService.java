package ru.ifmo.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.ifmo.dto.AggregatedResult;
import ru.ifmo.dto.SectionResult;
import ru.ifmo.dto.TextProcessingResult;

@Service
@Slf4j
public class AggregationService {

    private final Map<String, List<TextProcessingResult>> aggregationSessions = new ConcurrentHashMap<>();

    private final Map<String, Integer> expectedTaskCounts = new ConcurrentHashMap<>();
    
    private final Map<String, LocalDateTime> sessionStartTimes = new ConcurrentHashMap<>();

    public void setExpectedTaskCount(String sessionId, int expectedCount) {
        expectedTaskCounts.put(sessionId, expectedCount);
        log.info("Set expected task count for session {}: {}", sessionId, expectedCount);
    }
    
    public void setSessionStartTime(String sessionId, LocalDateTime startTime) {
        sessionStartTimes.put(sessionId, startTime);
        log.info("Set start time for session {}: {}", sessionId, startTime);
    }

    public void addResult(String sessionId, TextProcessingResult result) {
        aggregationSessions.computeIfAbsent(sessionId, k -> new ArrayList<>()).add(result);
        log.info("Added result for task {} to session {} ({}/{})",
                result.getTaskId(), sessionId,
                getResultCount(sessionId),
                expectedTaskCounts.get(sessionId));
    }

    public boolean isReadyForAggregation(String sessionId) {
        Integer expectedCount = expectedTaskCounts.get(sessionId);
        if (expectedCount == null) {
            log.warn("No expected count set for session {}", sessionId);
            return false;
        }

        int currentCount = getResultCount(sessionId);
        boolean ready = currentCount + 2 >= expectedCount;

        if (ready) {
            log.info("Session {} is ready for aggregation: {}/{} tasks completed",
                    sessionId, currentCount, expectedCount);
        }

        return ready;
    }

    public AggregatedResult aggregateResults(String sessionId) {
        List<TextProcessingResult> results = aggregationSessions.get(sessionId);
        if (results == null || results.isEmpty()) {
            log.warn("No results found for session {}", sessionId);
            return null;
        }

        log.info("Aggregating {} results for session {}", results.size(), sessionId);

        AggregatedResult aggregated = new AggregatedResult();
        aggregated.setAggregationId(sessionId);
        
        LocalDateTime endTime = LocalDateTime.now();
        aggregated.setEndTime(endTime);
        
        LocalDateTime startTime = sessionStartTimes.get(sessionId);
        if (startTime != null) {
            aggregated.setStartTime(startTime);
            Duration processingDuration = Duration.between(startTime, endTime);
            aggregated.setProcessingDurationMs(processingDuration.toMillis());
            log.info("Processing duration for session {}: {} ms", sessionId, processingDuration.toMillis());
        }
        
        aggregated.setTotalSections(results.size());
        aggregated.setProcessedTaskIds(results.stream()
                .map(TextProcessingResult::getTaskId)
                .collect(Collectors.toList()));

        aggregated.setTotalWordCount(aggregateWordCounts(results));

        aggregated.setMergedTopWords(mergeTopWords(results));

        aggregateSentiment(results, aggregated);

        aggregateModifiedText(results, aggregated);

        aggregated.setAllSortedSentences(aggregateSortedSentences(results));

        aggregated.setSectionResults(createSectionResults(results));

        log.info("Aggregation completed for session {}", sessionId);
        return aggregated;
    }

    private Long aggregateWordCounts(List<TextProcessingResult> results) {
        return results.stream()
                .filter(r -> r.getWordCount() != null)
                .mapToLong(r -> r.getWordCount().longValue())
                .sum();
    }

    private Map<String, Integer> mergeTopWords(List<TextProcessingResult> results) {
        Map<String, Integer> mergedWords = new HashMap<>();

        for (TextProcessingResult result : results) {
            if (result.getTopWords() != null) {
                for (Map.Entry<String, Integer> entry : result.getTopWords().entrySet()) {
                    mergedWords.merge(entry.getKey(), entry.getValue(), Integer::sum);
                }
            }
        }

        return mergedWords.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    private void aggregateSentiment(List<TextProcessingResult> results, AggregatedResult aggregated) {
        List<String> sentiments = results.stream()
                .map(TextProcessingResult::getSentiment)
                .filter(Objects::nonNull)
                .toList();

        List<Double> scores = results.stream()
                .map(TextProcessingResult::getSentimentScore)
                .filter(Objects::nonNull)
                .toList();

        Map<String, Integer> distribution = sentiments.stream()
                .collect(Collectors.groupingBy(
                        sentiment -> sentiment,
                        Collectors.summingInt(sentiment -> 1)
                ));
        aggregated.setSentimentDistribution(distribution);

        if (!scores.isEmpty()) {
            double avgScore = scores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            aggregated.setAverageSentimentScore(avgScore);
        }

        String overallSentiment = distribution.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("NEUTRAL");
        aggregated.setOverallSentiment(overallSentiment);
    }

    private void aggregateModifiedText(List<TextProcessingResult> results, AggregatedResult aggregated) {
        List<String> modifiedSections = results.stream()
                .map(TextProcessingResult::getModifiedText)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        aggregated.setModifiedTextSections(modifiedSections);

        String combinedText = String.join("\n\n", modifiedSections);
        aggregated.setCombinedModifiedText(combinedText);
    }

    private List<String> aggregateSortedSentences(List<TextProcessingResult> results) {
        List<String> allSentences = new ArrayList<>();

        for (TextProcessingResult result : results) {
            if (result.getSortedSentences() != null) {
                allSentences.addAll(result.getSortedSentences());
            }
        }

        return allSentences.stream()
                .sorted(Comparator.comparing(String::length))
                .collect(Collectors.toList());
    }

    private List<SectionResult> createSectionResults(List<TextProcessingResult> results) {
        return results.stream()
                .map(this::convertToSectionResult)
                .collect(Collectors.toList());
    }

    private SectionResult convertToSectionResult(TextProcessingResult result) {
        SectionResult section = new SectionResult();
        section.setTaskId(result.getTaskId());
        section.setWordCount(result.getWordCount());
        section.setTopWords(result.getTopWords());
        section.setSentiment(result.getSentiment());
        section.setSentimentScore(result.getSentimentScore());
        section.setModifiedText(result.getModifiedText());
        section.setSortedSentences(result.getSortedSentences());
        return section;
    }

    public void clearSession(String sessionId) {
        aggregationSessions.remove(sessionId);
        expectedTaskCounts.remove(sessionId);
        sessionStartTimes.remove(sessionId);
        log.info("Cleared session {}", sessionId);
    }

    public List<String> getActiveSessions() {
        return new ArrayList<>(aggregationSessions.keySet());
    }

    public int getResultCount(String sessionId) {
        List<TextProcessingResult> results = aggregationSessions.get(sessionId);
        return results != null ? results.size() : 0;
    }
}