# cdc-consumer-poc

## How to run on Mac

- Execute `docker-compose -f docker-compose-zookeeper.yml up` to run Zookeeper.
- Execute `docker-compose -f docker-compose-rabbitmq.yml up` to run RabbitMQ.
- Run the rabbitmq-publisher service.
  This will create all necessary queues, bindings, exchanges. It will also start producing messages to RabbitMq.
- Run multiple instances of rabbitmq-consumer service. After some time, one of the services will start consuming the messages from the queue. 
