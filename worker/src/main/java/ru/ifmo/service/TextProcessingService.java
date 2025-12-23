package ru.ifmo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.ifmo.dto.TextTask;
import ru.ifmo.dto.TextProcessingResult;

@Service
@RequiredArgsConstructor
public class TextProcessingService {

    private final WordCountService wordCountService;
    private final TopWordsService topWordsService;
    private final SentimentAnalysisService sentimentAnalysisService;
    private final NameReplacementService nameReplacementService;
    private final SentenceSortingService sentenceSortingService;

    public TextProcessingResult processTask(TextTask task) {
        TextProcessingResult result = new TextProcessingResult();
        result.setTaskId(task.getTaskId());

        result.setWordCount(wordCountService.countWords(task.getText()));

        int topN = task.getTopN() > 0 ? task.getTopN() : 5;
        result.setTopWords(topWordsService.findTopWords(task.getText(), topN));

        result.setSentiment(sentimentAnalysisService.analyzeSentiment(task.getText()));
        result.setSentimentScore(sentimentAnalysisService.calculateSentimentScore(task.getText()));

        String replacement = task.getNameReplacement() != null ? task.getNameReplacement() : "[NAME]";
        result.setModifiedText(nameReplacementService.replaceNames(task.getText(), replacement));

        result.setSortedSentences(sentenceSortingService.sortSentencesByLength(task.getText()));

        return result;
    }
}