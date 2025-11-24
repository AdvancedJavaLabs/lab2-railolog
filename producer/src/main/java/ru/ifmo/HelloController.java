package ru.ifmo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Producer Controller", description = "API for message production and basic operations")
public class HelloController {

    private final RabbitTemplate rabbitTemplate;
    private final String exchangeName;

    public HelloController(
            @Value("${rabbitmq.exchange.name}") String exchangeName,
            RabbitTemplate rabbitTemplate
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchangeName = exchangeName;
    }

    @RequestMapping("/")
    @Operation(summary = "Get greeting message", description = "Returns a simple greeting message")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved greeting message")
    })
    String hello() {
        return "Hello World!";
    }

    @RequestMapping("/send")
    @Operation(summary = "Send message to RabbitMQ", description = "Sends a message to the configured RabbitMQ exchange")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Message sent successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid message parameter")
    })
    public void send(
            @Parameter(description = "Message to be sent to RabbitMQ queue", required = true)
            @RequestParam("message") String message
    ) {
        rabbitTemplate.convertAndSend(exchangeName, "", message);
    }
}
