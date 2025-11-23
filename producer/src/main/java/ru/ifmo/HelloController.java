package ru.ifmo;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
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
    String hello() {
        return "Hello World!";
    }

    @RequestMapping("/send")
    public void send(
            @RequestParam("message") String message
    ) {
        rabbitTemplate.convertAndSend(exchangeName, "", message);
    }
}
