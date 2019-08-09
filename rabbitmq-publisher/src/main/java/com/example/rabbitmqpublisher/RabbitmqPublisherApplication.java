package com.example.rabbitmqpublisher;

import static com.example.rabbitmqpublisher.config.RabbitMqConfig.EXCHANGE;

import java.time.Duration;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Flux;

@SpringBootApplication
public class RabbitmqPublisherApplication implements CommandLineRunner {

    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public RabbitmqPublisherApplication(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public static void main(String[] args) {
        SpringApplication.run(RabbitmqPublisherApplication.class, args);
    }

    @Override
    public void run(String... args) {
        Flux.interval(Duration.ofMillis(1000L))
            .doOnNext(x -> {
                rabbitTemplate.convertAndSend(EXCHANGE, "routingKey", x);
            }).subscribe();
    }
}
