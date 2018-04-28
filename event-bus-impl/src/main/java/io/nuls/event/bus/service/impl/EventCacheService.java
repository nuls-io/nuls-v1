/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.event.bus.service.impl;

import io.nuls.cache.service.intf.CacheService;
import io.nuls.protocol.context.NulsContext;
import io.nuls.protocol.event.base.BaseEvent;

/**
 * @author Niels
 * @date 2017/12/10
 */
public class EventCacheService {
    private static EventCacheService INSTANCE = new EventCacheService();
    private static final String CACHE_OF_SENDED = "event-cache-sended";
    private static final String CACHE_OF_RECIEVED = "event-cache-recieved";
    private static final int TIME_OF_IDLE_SECONDS = 600;
    private CacheService cacheService;

    private EventCacheService() {
        init();
    }

    private void init() {
        this.cacheService = NulsContext.getServiceBean(CacheService.class);
        this.cacheService.createCache(CACHE_OF_SENDED, 32, 0, TIME_OF_IDLE_SECONDS);
        this.cacheService.createCache(CACHE_OF_RECIEVED, 8, 0, TIME_OF_IDLE_SECONDS);
    }

    public static EventCacheService getInstance() {
        return INSTANCE;
    }

    public void cacheSendedEvent(BaseEvent event) {
        this.cacheService.putElement(CACHE_OF_SENDED, event.getHash().getDigestHex(), event);
    }

    public void removeSendedEvent(BaseEvent event) {
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

    public BaseEvent getSendEvent(String hashHex) {
        return (BaseEvent) this.cacheService.getElement(CACHE_OF_SENDED, hashHex);
    }

    public void destroy() {
        this.cacheService.removeCache(CACHE_OF_SENDED);
        this.cacheService.removeCache(CACHE_OF_RECIEVED);
    }
}
