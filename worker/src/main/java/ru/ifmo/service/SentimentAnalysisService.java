package ru.ifmo.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
public class SentimentAnalysisService {

    private Set<String> positiveWords;
    private Set<String> negativeWords;

    @PostConstruct
    public void init() {
        positiveWords = loadWordsFromFile("sentiment/positive-words.txt");
        negativeWords = loadWordsFromFile("sentiment/negative-words.txt");
    }

    public String analyzeSentiment(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "NEUTRAL";
        }

        String[] words = text.toLowerCase()
                .replaceAll("[^a-zA-Zа-яА-Я\\s]", " ")
                .trim()
                .split("\\s+");

        int positiveCount = 0;
        int negativeCount = 0;

        for (String word : words) {
            if (positiveWords.contains(word)) {
                positiveCount++;
            } else if (negativeWords.contains(word)) {
                negativeCount++;
            }
        }

        if (positiveCount > negativeCount) {
            return "POSITIVE";
        } else if (negativeCount > positiveCount) {
            return "NEGATIVE";
        } else {
            return "NEUTRAL";
        }
    }

    public double calculateSentimentScore(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0.0;
        }

        String[] words = text.toLowerCase()
                .replaceAll("[^a-zA-Zа-яА-Я\\s]", " ")
                .trim()
                .split("\\s+");

        int positiveCount = 0;
        int negativeCount = 0;
        int totalWords = words.length;

        for (String word : words) {
            if (positiveWords.contains(word)) {
                positiveCount++;
            } else if (negativeWords.contains(word)) {
                negativeCount++;
            }
        }

        return totalWords > 0 ? (double) (positiveCount - negativeCount) / totalWords : 0.0;
    }

    private Set<String> loadWordsFromFile(String fileName) {
        Set<String> words = new HashSet<>();
        try {
            ClassPathResource resource = new ClassPathResource(fileName);
            if (resource.exists()) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim().toLowerCase();
                        if (!line.isEmpty() && !line.startsWith("#")) {
                            words.add(line);
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Could not load sentiment words from " + fileName + ": " + e.getMessage());
            return getBasicSentimentWords(fileName);
        }
        return words;
    }

    private Set<String> getBasicSentimentWords(String fileName) {
        if (fileName.contains("positive")) {
            return new HashSet<>(Arrays.asList(
                    "good", "great", "excellent", "amazing", "wonderful", "fantastic", "awesome", "perfect",
                    "love", "like", "enjoy", "happy", "pleased", "satisfied", "delighted", "thrilled",
                    "beautiful", "brilliant", "outstanding", "superb", "magnificent", "terrific",
                    "хорошо", "отлично", "прекрасно", "замечательно", "великолепно", "чудесно"
            ));
        } else {
            return new HashSet<>(Arrays.asList(
                    "bad", "terrible", "awful", "horrible", "disgusting", "hate", "dislike", "angry",
                    "sad", "disappointed", "upset", "annoyed", "frustrated", "furious", "disgusted",
                    "worst", "pathetic", "useless", "stupid", "ridiculous", "absurd",
                    "плохо", "ужасно", "отвратительно", "кошмар", "ненавижу", "расстроен"
            ));
        }
    }
}