package ru.ifmo.service;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.ifmo.dto.SessionInfo;
import ru.ifmo.dto.TextTask;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessagePublisherService {
    
    private final RabbitTemplate rabbitTemplate;
    
    @Value("${rabbitmq.tasks.exchange.name}")
    private String tasksExchange;
    
    @Value("${rabbitmq.session.exchange.name}")
    private String sessionExchange;
    
    @Value("${rabbitmq.session.routing.key}")
    private String sessionRoutingKey;

    @Value("${rabbitmq.queue.name}")
    private String queueName;
    
    public void publishSessionInfo(SessionInfo sessionInfo) {
        try {
            rabbitTemplate.convertAndSend(sessionExchange, sessionRoutingKey, sessionInfo);
            log.info("Published session info: {} with {} expected tasks", 
                    sessionInfo.getSessionId(), sessionInfo.getExpectedTaskCount());
        } catch (Exception e) {
            log.error("Failed to publish session info for {}: {}", 
                     sessionInfo.getSessionId(), e.getMessage(), e);
            throw new RuntimeException("Failed to publish session info", e);
        }
    }
    
    public void publishTasks(List<TextTask> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            log.warn("No tasks to publish");
            return;
        }
        
        log.info("Publishing {} tasks to exchange {}", tasks.size(), tasksExchange);
        
        int successCount = 0;
        int failureCount = 0;
        
        for (TextTask task : tasks) {
            try {
                rabbitTemplate.convertAndSend(queueName, task);
                successCount++;
                log.debug("Published task: {}", task.getTaskId());
            } catch (Exception e) {
                failureCount++;
                log.error("Failed to publish task {}: {}", task.getTaskId(), e.getMessage(), e);
            }
        }
        
        log.info("Task publishing completed. Success: {}, Failures: {}", successCount, failureCount);
        
        if (failureCount > 0) {
            throw new RuntimeException(String.format("Failed to publish %d out of %d tasks", 
                                                    failureCount, tasks.size()));
        }
    }
}