package io.nuls.message.bus.manager;

import io.nuls.network.model.Node;
import io.nuls.protocol.message.base.BaseMessage;

import java.util.HashMap;
import java.util.Map;

public class MessageCacher {

    private static final Map<String, BaseMessage> map = new HashMap<>();
    private static final Map<String, Node> nodeMap = new HashMap<>();

    public static void put(String key, BaseMessage message, Node node) {
        map.put(key, message);
        nodeMap.put(key, node);
    }

    public static void remove(String key) {
        map.remove(key);
        nodeMap.remove(key);
    }

    public static BaseMessage get(String key) {
        return map.get(key);
    }

    public static Node getNode(String key) {
        return nodeMap.get(key);
    }
}
