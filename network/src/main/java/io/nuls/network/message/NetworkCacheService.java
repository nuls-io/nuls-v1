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
        this.cacheService = NulsContext .getServiceBean(CacheService.class);
        this.cacheService.createCache(PING_EVENT_CACHE,5, 6, 0);
        this.cacheService.createCache(NETWORK_EVENT_CACHE,10, 60, 0);
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
