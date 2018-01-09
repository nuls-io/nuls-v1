package io.nuls.network.message;

import io.nuls.cache.service.intf.CacheService;
import io.nuls.core.context.NulsContext;
import io.nuls.core.event.BaseEvent;

/**
 * @author vivi
 * @date 2017/12/10.
 */
public class NetworkCacheService {

    private final CacheService cacheService;

    private static final String NETWORK_EVENT_CACHE = "network-event-cache";

    private static final String PING_EVENT_CACHE = "ping-event-cache";


    private static final NetworkCacheService INSTANCE = new NetworkCacheService();

    private NetworkCacheService() {
        this.cacheService = NulsContext.getInstance().getService(CacheService.class);
        this.cacheService.createCache(PING_EVENT_CACHE, 6, 0);
        this.cacheService.createCache(NETWORK_EVENT_CACHE, 60, 0);
    }

    public static NetworkCacheService getInstance() {
        return INSTANCE;
    }

    public void putEvent(BaseEvent event, boolean isPingPong) {
        if (isPingPong) {
            cacheService.putElement(PING_EVENT_CACHE, event.getHash().getDigestHex(), event);
        } else {
            cacheService.putElement(NETWORK_EVENT_CACHE, event.getHash().getDigestHex(), event);
        }
    }

    public void putEvent(Object key, BaseEvent event, boolean isPingPong) {
        if (isPingPong) {
            cacheService.putElement(PING_EVENT_CACHE, key, event);
        } else {
            cacheService.putElement(NETWORK_EVENT_CACHE, key, event);
        }
    }

    public BaseEvent getEvent(Object key, boolean isPingPong) {
        if (isPingPong) {
            return (BaseEvent) cacheService.getElement(PING_EVENT_CACHE, key);
        } else {
            return (BaseEvent) cacheService.getElement(NETWORK_EVENT_CACHE, key);
        }
    }

    public boolean existEvent(Object key) {
        if (cacheService.containsKey(NETWORK_EVENT_CACHE, key)) {
            return true;
        }
        return cacheService.containsKey(PING_EVENT_CACHE, key);
    }

}
