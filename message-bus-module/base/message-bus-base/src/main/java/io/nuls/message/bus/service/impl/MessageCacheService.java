package io.nuls.message.bus.service.impl;

import io.nuls.cache.CacheMap;
import io.nuls.kernel.lite.annotation.Service;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.protocol.message.base.BaseMessage;

/**
 * 消息缓存服务实现类
 * Message caching service implementation class.
 *
 * @author: Charlie
 * @date: 2018/5/6
 */
//@Service
public class MessageCacheService {

    private static final MessageCacheService INSTANCE = new MessageCacheService();

    private static final int TIME_OF_IDLE_SECONDS = 600;
    private CacheMap<NulsDigestData, BaseMessage> cacheMapSended = new CacheMap<>("message-cache-sended", 32, NulsDigestData.class, BaseMessage.class, 0, TIME_OF_IDLE_SECONDS);
    private CacheMap<NulsDigestData, Integer> cacheMapRecieved = new CacheMap<>("message-cache-recieved", 8, NulsDigestData.class, Integer.class, 0, TIME_OF_IDLE_SECONDS);

    public static MessageCacheService getInstance() {
        return INSTANCE;
    }

    private MessageCacheService() {
    }

    /**
     * 缓存发送的消息
     * Caching messages sent
     *
     * @param messgae The message you want to send.
     */
    public void cacheSendedMessage(BaseMessage messgae) {
        this.cacheMapSended.put(messgae.getHash(), messgae);
    }

    /**
     * 将缓存的消息移除
     * Removes the cached message.
     *
     * @param message The message you want to remove.
     */
    public void removeSendedMessage(BaseMessage message) {
        this.cacheMapSended.remove(message.getHash());
    }

    /**
     * 缓存接收的消息的散列
     * Caching the hex hash of received messages
     *
     * @param hash THe hash of the received message
     */
    public void cacheRecievedMessageHash(NulsDigestData hash) {
        this.cacheMapRecieved.put(hash, 1);
    }

    /**
     * 是否缓存有对应的消息
     * Check if the cache has a corresponding message.
     *
     * @param hash The hash you want to check
     */
    public boolean kownTheMessage(NulsDigestData hash) {
        boolean b = this.cacheMapRecieved.containsKey(hash) ||
                this.cacheMapSended.containsKey(hash);
        return b;
    }

    /**
     * Get the sent message in the cache according to the hex hash.
     */
    public BaseMessage getSendMessage(NulsDigestData hash) {
        return (BaseMessage) this.cacheMapSended.get(hash);
    }

    /**
     * Destroy all caches
     */
    public void destroy() {
        this.cacheMapSended.destroy();
        this.cacheMapRecieved.destroy();
    }
}
