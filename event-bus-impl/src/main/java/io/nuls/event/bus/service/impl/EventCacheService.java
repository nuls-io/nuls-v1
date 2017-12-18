package io.nuls.event.bus.service.impl;

import io.nuls.cache.service.intf.CacheService;
import io.nuls.core.context.NulsContext;
import io.nuls.core.event.BaseNulsEvent;

/**
 * @author Niels
 * @date 2017/12/10
 */
public class EventCacheService {
    private static EventCacheService INSTANCE = new EventCacheService();
    private static final String CACHE_OF_SENDED = "event-cache-sended";
    private static final String CACHE_OF_RECIEVED = "event-cache-recieved";
    private static final int TIME_OF_IDLE_SECONDS = 60;
    private final CacheService cacheService;

    private EventCacheService() {
        this.cacheService = NulsContext.getInstance().getService(CacheService.class);
        this.cacheService.createCache(CACHE_OF_SENDED, 0, TIME_OF_IDLE_SECONDS);
        this.cacheService.createCache(CACHE_OF_RECIEVED, 0, TIME_OF_IDLE_SECONDS);
    }

    public static EventCacheService getInstance() {
        return INSTANCE;
    }

    public void cacheSendedEvent(BaseNulsEvent event) {
        this.cacheService.putElement(CACHE_OF_SENDED, event.getHash().getDigestHex(), event);
    }

    public void cacheRecievedEventHash(String hashHex) {
        this.cacheService.putElementWithoutClone(CACHE_OF_SENDED, hashHex, 1);
    }

    public boolean isKnown(String hashHex) {
        return this.cacheService.containsKey(CACHE_OF_RECIEVED, hashHex) ||
                this.cacheService.containsKey(CACHE_OF_SENDED, hashHex);
    }

    public BaseNulsEvent getEvent(String hashHex) {
        return (BaseNulsEvent) this.cacheService.getElementValue(CACHE_OF_SENDED, hashHex);
    }
}
