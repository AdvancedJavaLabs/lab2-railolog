package ru.ifmo.service;

import java.util.Arrays;

import org.springframework.stereotype.Service;

@Service
public class WordCountService {

    public int countWords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }

        String[] words = text.trim().split("\\s+");

        return (int) Arrays.stream(words)
                .filter(word -> !word.trim().isEmpty())
                .filter(this::isWord)
                .count();
    }

    private boolean isWord(String token) {
        return token.matches(".*[a-zA-Zа-яА-Я].*");
    }
}