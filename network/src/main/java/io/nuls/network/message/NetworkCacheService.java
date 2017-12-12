package io.nuls.network.message;

import io.nuls.cache.service.intf.CacheService;
import io.nuls.core.context.NulsContext;
import io.nuls.network.entity.BroadcastResult;

/**
 * @author vivi
 * @date 2017/12/10.
 */
public class NetworkCacheService {

    private static final NetworkCacheService INSTANCE = new NetworkCacheService();

    private static final String BROADCAST_CACHE = "broadcast-cache";

    private static final int TIME_OF_IDLE_SECONDS = 60;
    private final CacheService cacheService;

    private NetworkCacheService() {
        this.cacheService = NulsContext.getInstance().getService(CacheService.class);
        this.cacheService.createCache(BROADCAST_CACHE, 0, TIME_OF_IDLE_SECONDS);
    }

    public static NetworkCacheService getInstance() {
        return INSTANCE;
    }

    public void addBroadCastResult(BroadcastResult result) {
//        cacheService.putElement(BROADCAST_CACHE, result.getHash(), result);
    }

    public BroadcastResult getBroadCastResult(String hash) {
        return (BroadcastResult) cacheService.getElementValue(BROADCAST_CACHE, hash);
    }

    public void removeBroadCastResult(String hash) {
        cacheService.removeElement(BROADCAST_CACHE, hash);
    }
}
