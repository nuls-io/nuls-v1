/**
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
 */
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
        init();
    }

    private void init(){
        this.cacheService = NulsContext.getServiceBean(CacheService.class);
        this.cacheService.createCache(CACHE_OF_SENDED,100, 0, TIME_OF_IDLE_SECONDS);
        this.cacheService.createCache(CACHE_OF_RECIEVED,100, 0, TIME_OF_IDLE_SECONDS);
    }

    public static EventCacheService getInstance() {
        return INSTANCE;
    }

    public void cacheSendedEvent(BaseEvent event) {
         this.cacheService.putElement(CACHE_OF_SENDED, event.getHash().getDigestHex()+"="+event.getSign().getSignHex(), event);
    }

    public void cacheRecievedEventHash(String hashHex,String signHex) {
        this.cacheService.putElement(CACHE_OF_SENDED, hashHex+"="+signHex, 1);
    }

    public boolean isKnown(String hashHex,String signHex) {
        return false;
//todo        return this.cacheService.containsKey(CACHE_OF_RECIEVED, hashHex+"="+signHex) ||
//                this.cacheService.containsKey(CACHE_OF_SENDED, hashHex+"="+signHex);
    }

    public BaseEvent getEvent(String hashHex,String signHex) {
        return (BaseEvent) this.cacheService.getElement(CACHE_OF_SENDED, hashHex+"="+signHex);
    }

    public void destroy() {
        this.cacheService.removeCache(CACHE_OF_SENDED);
        this.cacheService.removeCache(CACHE_OF_RECIEVED);
    }
}
