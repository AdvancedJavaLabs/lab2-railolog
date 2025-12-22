package ru.ifmo.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.ifmo.dto.AggregatedResult;

@Service
@Slf4j
public class ResultStorageService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ConcurrentMap<String, AggregatedResult> storedResults = new ConcurrentHashMap<>();
    private final String resultsDirectory = "results";

    public ResultStorageService() {
        try {
            Path resultsPath = Paths.get(resultsDirectory);
            if (!Files.exists(resultsPath)) {
                Files.createDirectories(resultsPath);
                log.info("Created results directory: {}", resultsPath.toAbsolutePath());
            }
        } catch (IOException e) {
            log.error("Failed to create results directory: {}", e.getMessage());
        }
    }

    public void storeResult(AggregatedResult result) {
        try {
            storedResults.put(result.getAggregationId(), result);

            saveToFile(result);

            log.info("Stored aggregated result for session: {}", result.getAggregationId());

        } catch (Exception e) {
            log.error("Failed to store result for session {}: {}",
                    result.getAggregationId(), e.getMessage(), e);
        }
    }

    private void saveToFile(AggregatedResult result) throws IOException {
//        String timestamp = result.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        String filename = String.format("%s_%s.json", result.getAggregationId(), System.currentTimeMillis());
        Path filePath = Paths.get(resultsDirectory, filename);

        objectMapper.writerWithDefaultPrettyPrinter().writeValue(filePath.toFile(), result);
        log.info("Saved result to file: {}", filePath.toAbsolutePath());

        saveSummaryReport(result, "timestamp");
    }

    private void saveSummaryReport(AggregatedResult result, String timestamp) throws IOException {
        String filename = String.format("%s_%s_summary.txt", result.getAggregationId(), timestamp);
        Path filePath = Paths.get(resultsDirectory, filename);

        StringBuilder report = new StringBuilder();
        report.append("=== TEXT PROCESSING AGGREGATION REPORT ===\n");
        report.append("Aggregation ID: ").append(result.getAggregationId()).append("\n");
//        report.append("Timestamp: ").append(result.getTimestamp()).append("\n");
        report.append("Total Sections Processed: ").append(result.getTotalSections()).append("\n");
        report.append("Processed Task IDs: ").append(String.join(", ", result.getProcessedTaskIds())).append("\n\n");

        report.append("=== WORD COUNT SUMMARY ===\n");
        report.append("Total Words: ").append(result.getTotalWordCount()).append("\n\n");

        report.append("=== TOP WORDS SUMMARY ===\n");
        if (result.getMergedTopWords() != null) {
            result.getMergedTopWords().entrySet().stream()
                    .limit(10)
                    .forEach(entry -> report.append(String.format("%-20s: %d\n", entry.getKey(), entry.getValue())));
        }
        report.append("\n");

        report.append("=== SENTIMENT ANALYSIS SUMMARY ===\n");
        report.append("Overall Sentiment: ").append(result.getOverallSentiment()).append("\n");
        report.append("Average Sentiment Score: ").append(String.format("%.3f", result.getAverageSentimentScore())).append("\n");
        if (result.getSentimentDistribution() != null) {
            report.append("Sentiment Distribution:\n");
            result.getSentimentDistribution().forEach((sentiment, count) ->
                    report.append(String.format("  %s: %d sections\n", sentiment, count)));
        }
        report.append("\n");

        report.append("=== TEXT MODIFICATION SUMMARY ===\n");
        report.append("Modified Text Sections: ").append(result.getModifiedTextSections() != null ?
                result.getModifiedTextSections().size() : 0).append("\n");
        if (result.getCombinedModifiedText() != null) {
            report.append("Combined Text Length: ").append(result.getCombinedModifiedText().length()).append(" " +
                    "characters\n");
        }
        report.append("\n");

        report.append("=== SENTENCE SORTING SUMMARY ===\n");
        report.append("Total Sorted Sentences: ").append(result.getAllSortedSentences() != null ?
                result.getAllSortedSentences().size() : 0).append("\n");
        if (result.getAllSortedSentences() != null && !result.getAllSortedSentences().isEmpty()) {
            report.append("Shortest Sentence: ").append(result.getAllSortedSentences().getFirst()).append("\n");
            report.append("Longest Sentence: ").append(result.getAllSortedSentences().getLast()).append("\n");
        }
        report.append("\n");

        report.append("=== PER-SECTION SUMMARY ===\n");
        if (result.getSectionResults() != null) {
            result.getSectionResults().forEach(section -> {
                report.append(String.format("Task ID: %s\n", section.getTaskId()));
                report.append(String.format("  Word Count: %d\n", section.getWordCount()));
                report.append(String.format("  Sentiment: %s (%.3f)\n", section.getSentiment(),
                        section.getSentimentScore()));
                report.append(String.format("  Top Words: %d\n", section.getTopWords() != null ?
                        section.getTopWords().size() : 0));
                report.append(String.format("  Sentences: %d\n", section.getSortedSentences() != null ?
                        section.getSortedSentences().size() : 0));
                report.append("\n");
            });
        }

        Files.write(filePath, report.toString().getBytes());
        log.info("Saved summary report to file: {}", filePath.toAbsolutePath());
    }

    public AggregatedResult getResult(String aggregationId) {
        return storedResults.get(aggregationId);
    }

    public List<AggregatedResult> getAllResults() {
        return new ArrayList<>(storedResults.values());
    }

    public List<String> getStoredResultIds() {
        return new ArrayList<>(storedResults.keySet());
    }

    public boolean deleteResult(String aggregationId) {
        AggregatedResult removed = storedResults.remove(aggregationId);
        if (removed != null) {
            log.info("Deleted result for aggregation: {}", aggregationId);
            return true;
        }
        return false;
    }

    public void clearAllResults() {
        storedResults.clear();
        log.info("Cleared all stored results");
    }
}