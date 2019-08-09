package com.example.rabbitmqconsumer.consumer;

import static com.example.rabbitmqconsumer.consumer.TopicListener.CONTAINER_ID;

import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.RetryOneTime;
import org.apache.zookeeper.KeeperException;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnProperty(value = "consumer.enabled", havingValue = "true")
public class ZooKeeperLockingConsumer {

    private static final String ZOOKEEPER_HOST = "localhost:2181";
    private static final String LOCK_PATH = "/lock/path";
    private static final int LOCK_FOR_SECONDS = 60;

    private RabbitListenerEndpointRegistry rabbitListenerEndpointRegistry;
    private CuratorFramework client;
    private InterProcessMutex lock;

    @Autowired
    public ZooKeeperLockingConsumer(RabbitListenerEndpointRegistry rabbitListenerEndpointRegistry) {
        this.rabbitListenerEndpointRegistry = rabbitListenerEndpointRegistry;
    }

    @PostConstruct
    public void init() throws Exception {
        client = CuratorFrameworkFactory.newClient(ZOOKEEPER_HOST, 60000, 15000, new RetryOneTime(10_000));
        client.start();
        createNode(client, LOCK_PATH);
    }

    /*
        Scheduled job will run every minute at :00 seconds.
     */
    @Scheduled(cron = "0 * * * * *")
    public void lockAndSubscribe() throws Exception {
        if (lock == null) {
            lock = new InterProcessMutex(client, LOCK_PATH);
        }

        if (lock.isOwnedByCurrentThread()) {
            rabbitListenerEndpointRegistry.getListenerContainer(CONTAINER_ID).stop();
            lock.release();
            log.info("Lock released for node {}", LOCK_PATH);
        }

        if (lock.acquire(LOCK_FOR_SECONDS, TimeUnit.SECONDS)) {
            log.info("Lock acquired for node {}", LOCK_PATH);
            rabbitListenerEndpointRegistry.getListenerContainer(CONTAINER_ID).start();
        }
    }

    private void createNode(CuratorFramework client, String nodePath) throws Exception {
        try {
            client.create().creatingParentsIfNeeded().forPath(nodePath);
        } catch (KeeperException.NodeExistsException e) {
            log.info("Node for path {} already exists.", nodePath);
        }
    }
}
