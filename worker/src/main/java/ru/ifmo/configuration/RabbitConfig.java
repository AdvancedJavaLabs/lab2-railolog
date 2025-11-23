package ru.ifmo.configuration;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
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

    @Bean
    public Queue queue(
            @Value("${rabbitmq.queue.name}") String queueName
    ) {
        return new Queue(queueName);
    }

    @Bean
    public Binding binding(
            Queue queue,
            FanoutExchange fanoutExchange
    ) {
        return BindingBuilder.bind(queue).to(fanoutExchange);
    }
}
