package io.nuls.message.bus.manager;

import io.nuls.kernel.model.NulsDigestData;
import io.nuls.network.model.Node;
import io.nuls.protocol.message.base.BaseMessage;

import java.util.HashMap;
import java.util.Map;

public class MessageCacher {

    private static final Map<NulsDigestData, BaseMessage> map = new HashMap<>();
    private static final Map<NulsDigestData, Node> nodeMap = new HashMap<>();

    public static void put(BaseMessage message, Node node) {
        NulsDigestData hash = message.getHash();
        map.put(hash, message);
        nodeMap.put(hash, node);
    }

    public static void remove(NulsDigestData hash) {
        map.remove(hash);
        nodeMap.remove(hash);
    }

    public static BaseMessage get(NulsDigestData data) {
        return map.get(data);
    }

    public static Node getNode(NulsDigestData data) {
        return nodeMap.get(data);
    }
}
