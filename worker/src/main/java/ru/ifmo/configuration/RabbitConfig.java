package ru.ifmo.configuration;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }

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

    @Bean
    public DirectExchange resultsExchange(
            @Value("${rabbitmq.results.exchange.name}") String exchangeName
    ) {
        return new DirectExchange(exchangeName);
    }

    @Bean
    public Queue resultsQueue(
            @Value("${rabbitmq.results.queue.name}") String queueName
    ) {
        return new Queue(queueName);
    }

    @Bean
    public Binding resultsBinding(
            Queue resultsQueue,
            DirectExchange resultsExchange,
            @Value("${rabbitmq.results.routing.key}") String routingKey
    ) {
        return BindingBuilder.bind(resultsQueue).to(resultsExchange).with(routingKey);
    }
}
