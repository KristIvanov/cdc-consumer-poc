package com.example.rabbitmqconsumer.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TopicListener {

    public static final String CONTAINER_ID = "container-id";

    @RabbitListener(id = CONTAINER_ID, queues = "${consumer.queue}", autoStartup = "false")
    public void onMessage(String message) {
        log.info(message);
    }
}
