package io.nuls.network.cache;

import io.nuls.cache.CacheMap;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.protocol.message.P2PNodeBody;

import java.util.HashSet;
import java.util.Set;

public class NodeCacheManager {

    private static NodeCacheManager instance = new NodeCacheManager();

    private NodeCacheManager() {

    }

    public static NodeCacheManager getInstance() {
        return instance;
    }

    //将收到的广播节点的信息缓存5分钟，避免重复广播
    private CacheMap<String, P2PNodeBody> cacheNodeMap = new CacheMap<>(NetworkConstant.CACHE_P2P_NODE, 8, String.class, P2PNodeBody.class, 60 * 5, 0, null);

    private CacheMap<String, Set<String>> cacheIpMap = new CacheMap<>(NetworkConstant.CACHE_P2P_IP, 2, String.class, HashSet.class, 60, 0, null);

    public void cacheNode(P2PNodeBody body) {
        cacheNodeMap.put(body.getId(), body);
    }

    public P2PNodeBody getNode(String id) {
        return cacheNodeMap.get(id);
    }

    public void cacheIpSet(Set ipSet) {
        cacheIpMap.put("ipSet", ipSet);
    }

    public Set<String> getIpSet() {
        return cacheIpMap.get("ipSet");
    }
}
