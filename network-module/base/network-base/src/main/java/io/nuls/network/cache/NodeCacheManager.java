package io.nuls.network.cache;

import io.nuls.cache.CacheMap;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.protocol.message.P2PNodeBody;

public class NodeCacheManager {

    private static NodeCacheManager instance = new NodeCacheManager();

    private NodeCacheManager() {

    }

    public static NodeCacheManager getInstance() {
        return instance;
    }

    //将收到的广播节点的信息缓存20分钟，避免重复广播
    private CacheMap<String, P2PNodeBody> cacheNodeMap = new CacheMap<>(NetworkConstant.CACHE_P2P_NODE, 8, String.class, P2PNodeBody.class, 60 * 20, 0, null);

    public void cacheNode(P2PNodeBody body) {
        cacheNodeMap.put(body.getId(), body);
    }

    public P2PNodeBody getNode(String id) {
        return cacheNodeMap.get(id);
    }
}
