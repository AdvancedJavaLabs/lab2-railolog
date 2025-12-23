package ru.ifmo.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

@Service
public class SentenceSortingService {

    private static final Pattern SENTENCE_PATTERN = Pattern.compile(
            "(?<=[.!?])\\s+(?=[A-ZА-Я])"
    );

    public List<String> sortSentencesByLength(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new ArrayList<>();
        }

        List<String> sentences = splitIntoSentences(text);

        return sentences.stream()
                .filter(sentence -> !sentence.trim().isEmpty())
                .sorted(Comparator.comparing(String::length))
                .collect(Collectors.toList());
    }

    public List<String> sortSentencesByLengthDescending(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new ArrayList<>();
        }

        List<String> sentences = splitIntoSentences(text);

        return sentences.stream()
                .filter(sentence -> !sentence.trim().isEmpty())
                .sorted(Comparator.comparing(String::length).reversed())
                .collect(Collectors.toList());
    }

    private List<String> splitIntoSentences(String text) {
        String cleanText = text.trim().replaceAll("\\s+", " ");

        String[] sentences = SENTENCE_PATTERN.split(cleanText);

        List<String> result = new ArrayList<>();
        for (String sentence : sentences) {
            String trimmed = sentence.trim();
            if (!trimmed.isEmpty()) {
                if (!trimmed.matches(".*[.!?]$")) {
                    if (!trimmed.endsWith(".") && !trimmed.endsWith("!") && !trimmed.endsWith("?")) {
                        trimmed += ".";
                    }
                }
                result.add(trimmed);
            }
        }

        if (result.isEmpty() && !cleanText.isEmpty()) {
            String[] fallbackSentences = cleanText.split("(?<=[.!?])\\s+");
            for (String sentence : fallbackSentences) {
                String trimmed = sentence.trim();
                if (!trimmed.isEmpty()) {
                    result.add(trimmed);
                }
            }
        }

        if (result.isEmpty() && !cleanText.isEmpty()) {
            result.add(cleanText);
        }

        return result;
    }
}