package io.nuls.event.bus.service.impl;

import io.nuls.cache.service.intf.CacheService;
import io.nuls.core.context.NulsContext;
import io.nuls.core.event.BaseEvent;

/**
 * @author Niels
 * @date 2017/12/10
 */
public class EventCacheService {
    private static EventCacheService INSTANCE = new EventCacheService();
    private static final String CACHE_OF_SENDED = "event-cache-sended";
    private static final String CACHE_OF_RECIEVED = "event-cache-recieved";
    private static final int TIME_OF_IDLE_SECONDS = 60;
    private CacheService cacheService;

    private EventCacheService() {
    }

    public void init(){
        this.cacheService = NulsContext.getInstance().getService(CacheService.class);
        this.cacheService.createCache(CACHE_OF_SENDED, 0, TIME_OF_IDLE_SECONDS);
        this.cacheService.createCache(CACHE_OF_RECIEVED, 0, TIME_OF_IDLE_SECONDS);
    }

    public static EventCacheService getInstance() {
        return INSTANCE;
    }

    public void cacheSendedEvent(BaseEvent event) {
        this.cacheService.putElement(CACHE_OF_SENDED, event.getHash().getDigestHex(), event);
    }

    public void cacheRecievedEventHash(String hashHex) {
        this.cacheService.putElement(CACHE_OF_SENDED, hashHex, 1);
    }

    public boolean isKnown(String hashHex) {
        return this.cacheService.containsKey(CACHE_OF_RECIEVED, hashHex) ||
                this.cacheService.containsKey(CACHE_OF_SENDED, hashHex);
    }

    public BaseEvent getEvent(String hashHex) {
        return (BaseEvent) this.cacheService.getElement(CACHE_OF_SENDED, hashHex);
    }

    public void destroy() {
        this.cacheService.removeCache(CACHE_OF_SENDED);
        this.cacheService.removeCache(CACHE_OF_RECIEVED);
    }
}
