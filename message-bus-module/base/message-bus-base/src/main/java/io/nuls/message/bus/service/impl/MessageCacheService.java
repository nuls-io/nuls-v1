/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package io.nuls.message.bus.service.impl;

import io.nuls.cache.CacheMap;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.protocol.message.base.BaseMessage;

/**
 * 消息缓存服务实现类
 * Message caching service implementation class.
 *
 * @author: Charlie
 * @date: 2018/5/6
 */
public class MessageCacheService {

    private static final MessageCacheService INSTANCE = new MessageCacheService();

    private static final int TIME_OF_IDLE_SECONDS = 600;
    private CacheMap<NulsDigestData, BaseMessage> cacheMapSended = new CacheMap<>("message-cache-sended", 128, NulsDigestData.class, BaseMessage.class, 0, TIME_OF_IDLE_SECONDS);
    private CacheMap<NulsDigestData, Integer> cacheMapRecieved = new CacheMap<>("message-cache-recieved", 64, NulsDigestData.class, Integer.class, 0, TIME_OF_IDLE_SECONDS);

//    private Map<NulsDigestData, BaseMessage> cacheMapSended = new HashMap<>();
//    private Map<NulsDigestData, Integer> cacheMapRecieved = new HashMap<>();

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
        long start = System.currentTimeMillis();
        boolean b = this.cacheMapRecieved.containsKey(hash) ||
                this.cacheMapSended.containsKey(hash);
        long use = System.currentTimeMillis() - start;
        if(use>1){
            Log.info("判斷交易是否重复话费时间：" + use + "ms");
        }
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

//        this.cacheMapSended.clear();
//        this.cacheMapRecieved.clear();
    }
}
