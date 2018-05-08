package io.nuls.network.connection.netty;

import io.netty.channel.socket.SocketChannel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NioChannelMap {

    private static Map<String, SocketChannel> map = new ConcurrentHashMap<>();

    public static void add(String channelId, SocketChannel channel) {
        map.put(channelId, channel);
    }

    public static SocketChannel get(String channelId) {
        return map.get(channelId);
    }

    public static void remove(String channelId) {
        map.remove(channelId);
    }

    public static boolean containsKey(String channelId) {
        return map.containsKey(channelId);
    }

    public static Map<String, SocketChannel> channels() {
        return map;
    }
}
