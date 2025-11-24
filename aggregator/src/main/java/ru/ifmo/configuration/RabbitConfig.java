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

    @Bean
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

    @Bean
    public DirectExchange sessionInfoExchange(
            @Value("${rabbitmq.session.exchange.name}") String exchangeName
    ) {
        return new DirectExchange(exchangeName);
    }

    @Bean
    public Queue sessionInfoQueue(
            @Value("${rabbitmq.session.queue.name}") String queueName
    ) {
        return new Queue(queueName);
    }

    @Bean
    public Binding sessionInfoBinding(
            Queue sessionInfoQueue,
            DirectExchange sessionInfoExchange,
            @Value("${rabbitmq.session.routing.key}") String routingKey
    ) {
        return BindingBuilder.bind(sessionInfoQueue).to(sessionInfoExchange).with(routingKey);
    }

    @Bean
    public DirectExchange finalResultsExchange(
            @Value("${rabbitmq.final.exchange.name}") String exchangeName
    ) {
        return new DirectExchange(exchangeName);
    }

    @Bean
    public Queue finalResultsQueue(
            @Value("${rabbitmq.final.queue.name}") String queueName
    ) {
        return new Queue(queueName);
    }

    @Bean
    public Binding finalResultsBinding(
            Queue finalResultsQueue,
            DirectExchange finalResultsExchange,
            @Value("${rabbitmq.final.routing.key}") String routingKey
    ) {
        return BindingBuilder.bind(finalResultsQueue).to(finalResultsExchange).with(routingKey);
    }
}
