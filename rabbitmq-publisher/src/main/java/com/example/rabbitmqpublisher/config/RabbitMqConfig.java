package com.example.rabbitmqpublisher.config;

import java.util.UUID;
import javax.annotation.PostConstruct;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String EXCHANGE = "topic-exchange";
    public static final String QUEUE_1 = "queue-1";
    public static final String QUEUE_2 = "queue-2";

    private final AmqpAdmin amqpAdmin;

    @Autowired
    public RabbitMqConfig(AmqpAdmin amqpAdmin) {
        this.amqpAdmin = amqpAdmin;
    }

    @PostConstruct
    public void afterInit() {
        amqpAdmin.declareExchange(new TopicExchange(EXCHANGE));

        amqpAdmin.declareQueue(new Queue(QUEUE_1));
        amqpAdmin.declareBinding(new Binding(QUEUE_1, Binding.DestinationType.QUEUE, EXCHANGE, "#", null));

        amqpAdmin.declareQueue(new Queue(QUEUE_2));
        amqpAdmin.declareBinding(new Binding(QUEUE_2, Binding.DestinationType.QUEUE, EXCHANGE, "#", null));
    }
}
