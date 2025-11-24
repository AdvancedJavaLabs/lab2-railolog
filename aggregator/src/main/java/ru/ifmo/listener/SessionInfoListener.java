package ru.ifmo.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import ru.ifmo.dto.SessionInfo;
import ru.ifmo.service.AggregationService;

@Component
@RequiredArgsConstructor
@Slf4j
public class SessionInfoListener {

    private final AggregationService aggregationService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @RabbitListener(queues = "${rabbitmq.session.queue.name}")
    public void receiveSessionInfo(String message) {
        log.info("Received session info message: {}", message);

        try {
            SessionInfo sessionInfo = objectMapper.readValue(message, SessionInfo.class);
            log.info("Processing session info: {} with expected {} tasks",
                    sessionInfo.getSessionId(), sessionInfo.getExpectedTaskCount());

            aggregationService.setExpectedTaskCount(
                    sessionInfo.getSessionId(),
                    sessionInfo.getExpectedTaskCount()
            );

            log.info("Successfully registered session {} with {} expected tasks",
                    sessionInfo.getSessionId(), sessionInfo.getExpectedTaskCount());

        } catch (JsonProcessingException e) {
            log.error("Failed to parse message as SessionInfo: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Error processing session info: {}", e.getMessage(), e);
        }
    }
}