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
        result.setTaskType(task.getTaskType());
        
        switch (task.getTaskType().toUpperCase()) {
            case "WORD_COUNT":
                result.setWordCount(wordCountService.countWords(task.getText()));
                break;
                
            case "TOP_WORDS":
                int topN = task.getTopN() > 0 ? task.getTopN() : 10; // Default to 10
                result.setTopWords(topWordsService.findTopWords(task.getText(), topN));
                break;
                
            case "SENTIMENT_ANALYSIS":
                result.setSentiment(sentimentAnalysisService.analyzeSentiment(task.getText()));
                result.setSentimentScore(sentimentAnalysisService.calculateSentimentScore(task.getText()));
                break;
                
            case "NAME_REPLACEMENT":
                String replacement = task.getNameReplacement() != null ? task.getNameReplacement() : "[NAME]";
                result.setModifiedText(nameReplacementService.replaceNames(task.getText(), replacement));
                break;
                
            case "SENTENCE_SORTING":
                result.setSortedSentences(sentenceSortingService.sortSentencesByLength(task.getText()));
                break;
                
            case "ALL_TASKS":
                result.setWordCount(wordCountService.countWords(task.getText()));
                result.setTopWords(topWordsService.findTopWords(task.getText(), task.getTopN() > 0 ? task.getTopN() : 5));
                result.setSentiment(sentimentAnalysisService.analyzeSentiment(task.getText()));
                result.setSentimentScore(sentimentAnalysisService.calculateSentimentScore(task.getText()));
                result.setModifiedText(nameReplacementService.replaceNames(task.getText(), 
                    task.getNameReplacement() != null ? task.getNameReplacement() : "[NAME]"));
                result.setSortedSentences(sentenceSortingService.sortSentencesByLength(task.getText()));
                break;
                
            default:
                throw new IllegalArgumentException("Unknown task type: " + task.getTaskType());
        }
        
        return result;
    }
}