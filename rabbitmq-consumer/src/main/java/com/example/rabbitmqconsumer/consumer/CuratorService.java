package com.example.rabbitmqconsumer.consumer;

import java.util.List;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.retry.RetryOneTime;
import org.apache.zookeeper.CreateMode;
import org.springframework.stereotype.Service;

@Service
public class CuratorService {

    private static final String ZOOKEEPER_HOST = "localhost:2181";

    private final CuratorFramework curatorFramework;

    public CuratorService() {
        this.curatorFramework = CuratorFrameworkFactory.newClient(ZOOKEEPER_HOST, 60000, 15000, new RetryOneTime(10_000));
        this.curatorFramework.start();
    }

    public void watchNode(CuratorWatcher watcher, String path) throws Exception {
        curatorFramework.checkExists().usingWatcher(watcher).forPath(path);
    }

    public List<String> getChildrenPaths(String path) throws Exception {
        return curatorFramework.getChildren().forPath(path);
    }

    public String createNode(String nodePath, CreateMode createMode) throws Exception {

        return curatorFramework.checkExists().forPath(nodePath) != null ? nodePath : curatorFramework.create()
                .withMode(createMode).forPath(nodePath);
    }

    public void delete(String path) throws Exception {
        curatorFramework.delete().forPath(path);
    }
}
