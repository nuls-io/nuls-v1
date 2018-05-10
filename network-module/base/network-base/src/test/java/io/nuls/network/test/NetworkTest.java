package io.nuls.network.test;

import io.nuls.core.tools.network.IpUtil;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class NetworkTest {

    public static Map<String, Node> nodes = new ConcurrentHashMap<>();

    private List<String> ipList = new ArrayList<>();

    private Set<String> localIp = IpUtil.getIps();

    private int port = 8050;

    @Before
    public void init() {
        ipList.add("192.168.1.131");
        ipList.add("192.168.1.204");

        NettyServer nettyServer = new NettyServer(port);
        nettyServer.init();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println("----------- server start ------------");
                    nettyServer.start();
                } catch (InterruptedException e) {
                    System.out.println("----------- server start fail ------------");
                }
            }
        });
        thread.start();
    }

    @Test
    public void testConnection() {
        while (true) {
            for (String ip : ipList) {
                if (!localIp.contains(ip) && !notContainNode(ip)) {
                    Node node = new Node();
                    node.setIp(ip);
                    node.setPort(port);
                    addConnect(node);
                }
            }
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean notContainNode(String ip) {
        for (Node node : nodes.values()) {
            if (node.getIp().equals(ip))
                return true;
        }
        return false;
    }

    private void addConnect(Node node) {
        nodes.put(node.getId(), node);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                NettyClient client = new NettyClient(node);
                client.start();
            }
        });
        thread.start();
    }

    public static void removeNode(String id) {
        nodes.remove(id);
    }
}
