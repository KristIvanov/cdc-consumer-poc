# cdc-consumer-poc

## How to run

- Execute 'docker-compose -f docker-compose-zookeeper.yml' to run Zookeeper.
- Execute 'docker-compose -f docker-compose-rabbitmq.yml' to run RabbitMQ.
- Run the rabbitmq-publisher service.
  This will create all necessarry queues, bindings, exchanges. It will also start producing messages to a queue.
- Run multiple instances of rabbitmq-consumer service.
