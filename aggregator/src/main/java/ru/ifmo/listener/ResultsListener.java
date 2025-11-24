package ru.ifmo.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.ifmo.dto.AggregatedResult;
import ru.ifmo.dto.TextProcessingResult;
import ru.ifmo.service.AggregationService;
import ru.ifmo.service.ResultStorageService;

@Component
@RequiredArgsConstructor
@Slf4j
public class ResultsListener {

    private final AggregationService aggregationService;
    private final ResultStorageService resultStorageService;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${rabbitmq.final.exchange.name}")
    private String finalExchange;

    @Value("${rabbitmq.final.routing.key}")
    private String finalRoutingKey;

    @RabbitListener(queues = "${rabbitmq.results.queue.name}")
    public void receiveResult(String message) {
        log.info("Received result message: {}", message);

        try {
            TextProcessingResult result = objectMapper.readValue(message, TextProcessingResult.class);
            log.info("Processing result for task: {}", result.getTaskId());

            String sessionId = extractSessionId(result.getTaskId());

            aggregationService.addResult(sessionId, result);

            checkAndTriggerAggregation(sessionId);

        } catch (JsonProcessingException e) {
            log.error("Failed to parse message as TextProcessingResult: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Error processing result: {}", e.getMessage(), e);
        }
    }

    private String extractSessionId(String taskId) {
        // taskId format "session-001-task-001"
        if (taskId != null && taskId.contains("-")) {
            String[] parts = taskId.split("-");
            if (parts.length >= 2) {
                return parts[0] + "-" + parts[1];
            }
        }
        return "default-session";
    }

    private void checkAndTriggerAggregation(String sessionId) {
        if (aggregationService.isReadyForAggregation(sessionId)) {
            log.info("Session {} is ready for aggregation, triggering...", sessionId);
            triggerAggregation(sessionId);
        } else {
            int resultCount = aggregationService.getResultCount(sessionId);
            log.info("Session {} not ready yet: {} results received", sessionId, resultCount);
        }
    }

    private void triggerAggregation(String sessionId) {
        try {
            log.info("Triggering aggregation for session: {}", sessionId);

            AggregatedResult aggregatedResult = aggregationService.aggregateResults(sessionId);
            if (aggregatedResult != null) {
                resultStorageService.storeResult(aggregatedResult);

                publishFinalResult(aggregatedResult);

                log.info("Aggregation completed and published for session: {}", sessionId);
            }

        } catch (Exception e) {
            log.error("Error during aggregation for session {}: {}", sessionId, e.getMessage(), e);
        }
    }

    private void publishFinalResult(AggregatedResult result) {
        try {
            rabbitTemplate.convertAndSend(finalExchange, finalRoutingKey, result);
            log.info("Final result published for aggregation: {}", result.getAggregationId());
        } catch (Exception e) {
            log.error("Failed to publish final result for aggregation {}: {}",
                    result.getAggregationId(), e.getMessage(), e);
        }
    }
}