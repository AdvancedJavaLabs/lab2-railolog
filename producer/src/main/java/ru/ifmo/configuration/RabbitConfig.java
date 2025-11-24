package ru.ifmo.configuration;

import org.springframework.amqp.core.FanoutExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Bean
    public FanoutExchange fanoutExchange(
            @Value("${rabbitmq.exchange.name}") String exchangeName
    ) {
        return new FanoutExchange(exchangeName);
    }
}
