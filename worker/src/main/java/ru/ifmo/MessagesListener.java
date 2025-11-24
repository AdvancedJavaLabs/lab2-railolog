package ru.ifmo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.ifmo.dto.TextTask;
import ru.ifmo.dto.TextProcessingResult;
import ru.ifmo.service.TextProcessingService;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessagesListener {

    private final TextProcessingService textProcessingService;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${rabbitmq.results.exchange.name}")
    private String resultsExchange;

    @Value("${rabbitmq.results.routing.key}")
    private String resultsRoutingKey;

    @RabbitListener(queues = "${rabbitmq.queue.name}")
    public void receiveMessage(String message) {
        log.info("Received message: {}", message);
        
        try {
            // Parse the incoming message as TextTask
            TextTask task = objectMapper.readValue(message, TextTask.class);
            log.info("Processing task: {} of type: {}", task.getTaskId(), task.getTaskType());
            
            // Process the task
            TextProcessingResult result = textProcessingService.processTask(task);
            
            // Send result back to results queue
            sendResult(result);
            
            log.info("Successfully processed task: {}", task.getTaskId());
            
        } catch (JsonProcessingException e) {
            log.error("Failed to parse message as TextTask: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Error processing task: {}", e.getMessage(), e);
        }
    }

    private void sendResult(TextProcessingResult result) {
        try {
            rabbitTemplate.convertAndSend(resultsExchange, resultsRoutingKey, result);
            log.info("Result sent for task: {}", result.getTaskId());
        } catch (Exception e) {
            log.error("Failed to send result for task {}: {}", result.getTaskId(), e.getMessage(), e);
        }
    }
}
