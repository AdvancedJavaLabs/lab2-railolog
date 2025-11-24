package ru.ifmo.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

@Service
public class TopWordsService {

    public Map<String, Integer> findTopWords(String text, int topN) {
        if (text == null || text.trim().isEmpty() || topN <= 0) {
            return new HashMap<>();
        }

        String[] words = text.toLowerCase()
                .replaceAll("[^a-zA-Zа-яА-Я\\s]", " ") // Remove punctuation
                .trim()
                .split("\\s+");

        Map<String, Integer> wordCount = Arrays.stream(words)
                .filter(word -> !word.trim().isEmpty())
                .filter(this::isValidWord)
                .collect(Collectors.groupingBy(
                        word -> word,
                        Collectors.summingInt(word -> 1)
                ));

        return wordCount.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        Math::max,
                        LinkedHashMap::new
                ));
    }

    private boolean isValidWord(String word) {
        return word.length() > 1 && word.matches(".*[a-zA-Zа-яА-Я].*");
    }
}