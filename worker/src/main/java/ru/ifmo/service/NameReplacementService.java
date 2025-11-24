package ru.ifmo.service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

@Service
public class NameReplacementService {

    private static final Pattern NAME_PATTERN = Pattern.compile(
            "(?<!^)(?<!\\. )(?<!\\! )(?<!\\? )\\b[A-ZА-Я][a-zа-я]{1,}(?:\\s+[A-ZА-Я][a-zа-я]{1,})*\\b"
    );

    private static final Pattern CONTEXT_NAME_PATTERN = Pattern.compile(
            "(?i)(?:мистер|мисс|миссис|господин|госпожа|mr|ms|mrs|dr|prof)\\s+([A-ZА-Я][a-zа-я]+" +
                    "(?:\\s+[A-ZА-Я][a-zа-я]+)*)"
    );

    private static final Pattern QUOTED_NAME_PATTERN = Pattern.compile(
            "\"([A-ZА-Я][a-zа-я]+(?:\\s+[A-ZА-Я][a-zа-я]+)*)\"(?=\\s+" +
                    "(?:сказал|говорит|ответил|спросил|said|replied|asked))"
    );

    public String replaceNames(String text, String replacement) {
        if (text == null || text.trim().isEmpty() || replacement == null) {
            return text;
        }

        String result = text;

        result = CONTEXT_NAME_PATTERN.matcher(result)
                .replaceAll(matchResult -> {
                    String prefix = matchResult.group().substring(0, matchResult.start(1) - matchResult.start());
                    return prefix + replacement;
                });

        result = QUOTED_NAME_PATTERN.matcher(result)
                .replaceAll(matchResult -> "\"" + replacement + "\"" +
                        matchResult.group().substring(matchResult.end(1) + 1));

        Matcher nameMatcher = NAME_PATTERN.matcher(result);
        StringBuffer sb = new StringBuffer();

        while (nameMatcher.find()) {
            String match = nameMatcher.group();
            if (isPotentialName(match)) {
                nameMatcher.appendReplacement(sb, replacement);
            } else {
                nameMatcher.appendReplacement(sb, match);
            }
        }
        nameMatcher.appendTail(sb);

        return sb.toString();
    }

    private boolean isPotentialName(String word) {
        String lowerWord = word.toLowerCase();

        String[] excludeWords = {
                "january", "february", "march", "april", "may", "june",
                "july", "august", "september", "october", "november", "december",
                "monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday",
                "январь", "февраль", "март", "апрель", "май", "июнь",
                "июль", "август", "сентябрь", "октябрь", "ноябрь", "декабрь",
                "понедельник", "вторник", "среда", "четверг", "пятница", "суббота", "воскресенье",
                "the", "this", "that", "these", "those", "when", "where", "why", "how",
                "это", "этот", "эта", "эти", "когда", "где", "почему", "как",
                "god", "lord", "jesus", "christ", "allah", "buddha",
                "бог", "господь", "иисус", "христос", "аллах", "будда",
                "america", "europe", "asia", "africa", "australia",
                "америка", "европа", "азия", "африка", "австралия"
        };

        for (String exclude : excludeWords) {
            if (lowerWord.equals(exclude)) {
                return false;
            }
        }

        return word.length() >= 2 && word.length() <= 20 && word.matches("^[A-ZА-Я][a-zа-я]+$");
    }
}