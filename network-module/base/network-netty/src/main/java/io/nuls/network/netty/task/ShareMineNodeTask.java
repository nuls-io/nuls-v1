package io.nuls.network.netty.task;

import io.nuls.core.tools.log.Log;
import io.nuls.network.constant.NetworkParam;
import io.nuls.network.listener.EventListener;
import io.nuls.network.model.Node;
import io.nuls.network.netty.broadcast.BroadcastHandler;
import io.nuls.network.netty.manager.ConnectionManager;
import io.nuls.network.netty.manager.NodeManager;
import io.nuls.network.protocol.message.P2PNodeBody;
import io.nuls.network.protocol.message.P2PNodeMessage;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ShareMineNodeTask implements Runnable {

    private final NetworkParam networkParam = NetworkParam.getInstance();
    private final NodeManager nodeManager = NodeManager.getInstance();
    private final ConnectionManager connectionManager = ConnectionManager.getInstance();
    private final BroadcastHandler broadcastHandler = BroadcastHandler.getInstance();

    private boolean hasShare;

    @Override
    public void run() {
        while(!hasShare) {
            shareMyServer();

            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                Log.error(e);
            }
        }
    }

    private void shareMyServer() {

        String externalIp = getMyExtranetIp();

        if (externalIp == null) {
            return;
        }

        Log.info("my external ip  is {}" , externalIp);

        networkParam.getLocalIps().add(externalIp);

        Node myNode = new Node(externalIp, networkParam.getPort(), Node.OUT);

        myNode.setConnectedListener(() -> {
            myNode.getChannel().close();
            doShare(externalIp);
        });

        myNode.setDisconnectListener(new EventListener() {
            @Override
            public void action() {
                myNode.setChannel(null);
                hasShare = true;
            }
        });

        connectionManager.connection(myNode);
    }

    private String getMyExtranetIp() {
        int nodeCount = 0;
        long timeout = 20000L;
        long lastTime = System.currentTimeMillis();

        while (true) {
            int count = nodeManager.getAvailableNodesCount();
            if (count == nodeCount && count >= 1) {
                if (System.currentTimeMillis() - lastTime > timeout) {
                    break;
                }
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    Log.error(e);
                }
            } else {
                nodeCount = count;
                lastTime = System.currentTimeMillis();
            }
        }

        Collection<Node> nodes = nodeManager.getAvailableNodes();

        return getMostSameIp(nodes);
    }

    private String getMostSameIp(Collection<Node> nodes) {

        Map<String, Integer> ipMaps = new HashMap<>();

        for (Node node : nodes) {
            String ip = node.getExternalIp();
            if (ip == null) {
                continue;
            }
            Integer count = ipMaps.get(ip);
            if (count == null) {
                ipMaps.put(ip, 1);
            } else {
                ipMaps.put(ip, count + 1);
            }
        }

        int maxCount = 0;
        String ip = null;
        for (Map.Entry<String, Integer> entry : ipMaps.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                ip = entry.getKey();
            }
        }

        return ip;
    }

    private void doShare(String externalIp) {
        P2PNodeBody p2PNodeBody = new P2PNodeBody(externalIp, networkParam.getPort());
        P2PNodeMessage message = new P2PNodeMessage(p2PNodeBody);
        broadcastHandler.broadcastToAllNode(message, null, true, 100);
    }
}
