package com.rpc.registry;

import io.netty.util.internal.ThreadLocalRandom;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @Auther: 庞洋洋
 * @Date: 2018/9/5 15:31
 * @Description:
 */
public class ServiceDiscovery {

    private static final Logger logger = LoggerFactory.getLogger(ServiceDiscovery.class);

    private CountDownLatch latch = new CountDownLatch(1);

    private volatile List<String> dataList = new ArrayList<String>();

    private String registryAddress;

    /**
     * zk 连接
     * @param registryAddress
     */
    public ServiceDiscovery(String registryAddress){
        this.registryAddress = registryAddress;

        ZooKeeper zk = connectServer();
        if (zk != null){
            watchNode(zk);
        }
    }

    /**
     * 创建zookeeper链接，监听
     * @return
     */
    private ZooKeeper connectServer(){
        ZooKeeper  zk = null;
        try {
            zk = new ZooKeeper(registryAddress, Constant.ZK_TIME_OUT,
                    new Watcher() {  //判断连接有没有完成
                        @Override
                        public void process(WatchedEvent watchedEvent) {
                            if(watchedEvent.getState() == Event.KeeperState.SyncConnected){
                                latch.countDown();
                            }
                        }
                    });
            latch.await();
        } catch (Exception e){
            logger.error("", e);
        }
        return zk;
    }

    public String discover(){
        String data = null;
        int size = dataList.size();
        // 存在节点，使用即可
        if(size > 0){
            if(size == 1){
                data = dataList.get(0);
                logger.debug("using only data: {} ", data);
            }else{
                data = dataList.get(ThreadLocalRandom.current().nextInt());
                logger.debug("using random data: {} ", data);
            }
        }
        return data;
    }

    private void watchNode(final ZooKeeper zk){
        try {
            //获取子节点
            List<String> nodeList = zk.getChildren(Constant.ZK_REGISTRY_PATH,
                    new Watcher() {
                        @Override
                        public void process(WatchedEvent watchedEvent) {
                            //节点改变
                            if(watchedEvent.getType() == Event.EventType.NodeChildrenChanged){
                                watchNode(zk);
                            }
                        }
                    });
            List<String> dataList = new ArrayList<String>();
            //循环子节点
            for (String node: nodeList) {
                //获取子节点中的服务器地址
                byte[] bytes = zk.getData(Constant.ZK_REGISTRY_PATH + "/"
                    + node, false, null);
                //存储到list中
                dataList.add(new String (bytes));
            }
            logger.debug("node data: {} ", dataList);
            //将节点信息记录在成员变量
            this.dataList = dataList;
        } catch (Exception e){
            logger.error("", e);
        }
    }
}
