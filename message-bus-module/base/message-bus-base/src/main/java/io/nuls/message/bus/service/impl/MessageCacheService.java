package io.nuls.message.bus.service.impl;

import io.nuls.cache.CacheMap;
import io.nuls.protocol.message.base.BaseMessage;

/**
 * @author: Charlie
 * @date: 2018/5/6
 */
public class MessageCacheService {

    private static final MessageCacheService INSTANCE = new MessageCacheService();

    private static final int TIME_OF_IDLE_SECONDS = 600;
    private CacheMap<String, BaseMessage> cacheMapSended = new CacheMap<>("event-cache-sended",32, String.class, BaseMessage.class,0, TIME_OF_IDLE_SECONDS);
    private CacheMap<String, Integer> cacheMapRecieved = new CacheMap<>("event-cache-recieved",8, String.class, Integer.class,0, TIME_OF_IDLE_SECONDS);

    public static MessageCacheService getInstance() {
        return INSTANCE;
    }

    public void cacheSendedMessage(BaseMessage messgae) {
        this.cacheMapSended.put(messgae.getHash().getDigestHex(), messgae);
    }

    public void removeSendedMessage(BaseMessage message) {
        this.cacheMapSended.remove(message.getHash().getDigestHex());
    }

    public void cacheRecievedMessageHash(String hashHex) {
        this.cacheMapRecieved.put(hashHex, 1);
    }

    public boolean kownTheMessage(String hashHex) {
        boolean b = this.cacheMapRecieved.containsKey(hashHex) ||
                this.cacheMapSended.containsKey(hashHex);
        return b;
    }

    public BaseMessage getSendMessage(String hashHex) {
        return (BaseMessage) this.cacheMapSended.get(hashHex);
    }

    public void destroy() {
        this.cacheMapSended.destroy();
        this.cacheMapRecieved.destroy();
    }
}
