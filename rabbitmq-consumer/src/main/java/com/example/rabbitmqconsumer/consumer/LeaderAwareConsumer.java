package com.example.rabbitmqconsumer.consumer;

import static com.example.rabbitmqconsumer.consumer.TopicListener.CONTAINER_ID;

import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.retry.RetryOneTime;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnProperty(value = "consumer.enabled", havingValue = "true")
public class LeaderAwareConsumer implements CuratorWatcher, SmartLifecycle {

    private static final String ZOOKEEPER_HOST = "localhost:2181";
    private static final String ELECTION_ROOT_PATH = "/election";
    private static final String PROCESS_NODE_PREFIX = ELECTION_ROOT_PATH + "/leader-";

    private RabbitListenerEndpointRegistry rabbitListenerEndpointRegistry;
    private CuratorFramework client;

    boolean runStatus = false;

    private String processNodePath;
    private String watchedNodePath;

    @Autowired
    public LeaderAwareConsumer(RabbitListenerEndpointRegistry rabbitListenerEndpointRegistry) {
        this.rabbitListenerEndpointRegistry = rabbitListenerEndpointRegistry;
    }

    private void watchNodeWithOneLessSequence() throws Exception {

        List<String> childNodePaths = client.getChildren().forPath(ELECTION_ROOT_PATH);
        Collections.sort(childNodePaths);

        int index = childNodePaths.indexOf(processNodePath.substring(processNodePath.lastIndexOf('/') + 1));
        if (index == 0) {
            log.info("Current node is the leader");
            rabbitListenerEndpointRegistry.getListenerContainer(CONTAINER_ID).start();
        } else {
            String watchedNodeShortPath = childNodePaths.get(index - 1);

            watchedNodePath = ELECTION_ROOT_PATH + "/" + watchedNodeShortPath;

            log.info("Setting watch on node {}", watchedNodePath);
            client.checkExists().usingWatcher(this).forPath(watchedNodePath);
        }
    }

    private String createNode(CuratorFramework client, String nodePath, CreateMode createMode) throws Exception {

        return client.checkExists().forPath(nodePath) != null ? nodePath : client.create()
                .withMode(createMode).forPath(nodePath);
    }

    @Override
    public void process(WatchedEvent event) {

        EventType eventType = event.getType();
        if (EventType.NodeDeleted.equals(eventType) && event.getPath().equalsIgnoreCase(watchedNodePath)) {
            rabbitListenerEndpointRegistry.getListenerContainer(CONTAINER_ID).start();
        }
    }

    @Override
    public void start() {
        client = CuratorFrameworkFactory.newClient(ZOOKEEPER_HOST, 60000, 15000, new RetryOneTime(10_000));
        client.start();

        try {
            createNode(client, ELECTION_ROOT_PATH, CreateMode.PERSISTENT);
            processNodePath = createNode(client, PROCESS_NODE_PREFIX, CreateMode.EPHEMERAL_SEQUENTIAL);

            if (processNodePath != null) {
                watchNodeWithOneLessSequence();
            }

            runStatus = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {

        rabbitListenerEndpointRegistry.stop();
        try {
            client.delete().forPath(processNodePath);
            log.info("Deleting node {}", processNodePath);
            runStatus = false;
        } catch (Exception e) {
            log.error("Exception while deleting node {}", processNodePath);
        }
    }

    @Override
    public boolean isRunning() {
        return runStatus;
    }
}
