package io.nuls.message.bus.service.impl;

import io.nuls.protocol.context.NulsContext;
import io.nuls.protocol.message.base.BaseMessage;

/**
 * @author: Charlie
 * @date: 2018/5/6
 */
public class MessageCacheService {
    private static MessageCacheService INSTANCE = new MessageCacheService();
    private static final String CACHE_OF_SENDED = "event-cache-sended";
    private static final String CACHE_OF_RECIEVED = "event-cache-recieved";
    private static final int TIME_OF_IDLE_SECONDS = 600;
    private CacheService cacheService;

    private MessageCacheService() {
        init();
    }

    private void init() {
        this.cacheService = NulsContext.getServiceBean(CacheService.class);
        this.cacheService.createCache(CACHE_OF_SENDED, 32, 0, TIME_OF_IDLE_SECONDS);
        this.cacheService.createCache(CACHE_OF_RECIEVED, 8, 0, TIME_OF_IDLE_SECONDS);
    }

    public static MessageCacheService getInstance() {
        return INSTANCE;
    }

    public void cacheSendedEvent(BaseMessage event) {
        this.cacheService.putElement(CACHE_OF_SENDED, event.getHash().getDigestHex(), event);
    }

    public void removeSendedEvent(BaseMessage event) {
        this.cacheService.removeElement(CACHE_OF_SENDED, event.getHash().getDigestHex());
    }

    public void cacheRecievedEventHash(String hashHex) {
        this.cacheService.putElement(CACHE_OF_RECIEVED, hashHex, 1);
    }

    public boolean kownTheEvent(String hashHex) {
        boolean b = this.cacheService.containsKey(CACHE_OF_RECIEVED, hashHex) ||
                this.cacheService.containsKey(CACHE_OF_SENDED, hashHex);
        return b;
    }

    public BaseMessage getSendEvent(String hashHex) {
        return (BaseMessage) this.cacheService.getElement(CACHE_OF_SENDED, hashHex);
    }

    public void destroy() {
        this.cacheService.removeCache(CACHE_OF_SENDED);
        this.cacheService.removeCache(CACHE_OF_RECIEVED);
    }
}
