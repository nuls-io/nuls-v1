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
package io.nuls.cache.util;

import io.nuls.cache.listener.intf.NulsCacheListener;
import io.nuls.cache.service.CacheService;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.lite.core.SpringLiteContext;

import java.util.List;
import java.util.Set;

/**
 * @author Niels
 * @date 2017/12/20
 */
public class CacheMap<K, V> {

    private final String cacheName;

    private CacheService cacheService = getServiceBean(CacheService.class, 0L);

    public CacheMap(String cacheName, int heapMb) {
        this(cacheName, heapMb, 0, 0);
    }

    public CacheMap(String cacheName, int heapMb, int timeToLiveSeconds, int timeToIdleSeconds) {
        this(cacheName, heapMb, timeToLiveSeconds, timeToIdleSeconds, null);
    }

    public CacheMap(String cacheName, int heapMb, int timeToLiveSeconds, int timeToIdleSeconds, NulsCacheListener listener) {
        this.cacheService.createCache(cacheName, heapMb, timeToLiveSeconds, timeToIdleSeconds, listener);
        this.cacheName = cacheName;
    }

    public int size() {
        return this.keySet().size();
    }

    public boolean isEmpty() {
        return this.keySet().isEmpty();
    }

    public boolean containsKey(K key) {
        return this.cacheService.containsKey(cacheName, key);
    }

    public boolean containsValue(V value) {
        List<V> vlist = this.cacheService.getElementList(cacheName);
        return vlist.contains(value);
    }


    public V get(K key) {
        return (V) this.cacheService.getElement(cacheName, key);
    }


    public void put(K key, V value) {
        this.cacheService.putElement(cacheName, key, value);
    }

    public void remove(K key) {
        this.cacheService.removeElement(cacheName, key);
    }

    public void clear() {
        this.cacheService.clearCache(cacheName);
    }

    public Set<K> keySet() {
        return this.cacheService.keySet(cacheName);
    }

    public List<V> values() {
        return this.cacheService.getElementList(cacheName);
    }

    public void destroy() {
        this.cacheService.removeCache(cacheName);
    }

    private static <T> T getServiceBean(Class<T> tClass, long l) {
        try {
            Thread.sleep(300L);
        } catch (InterruptedException e1) {
            Log.error(e1);
        }
        try {
            return SpringLiteContext.getBean(tClass);
        } catch (Exception e) {
            if (l > 20000) {
                Log.error(e);
                return null;
            }
            return getServiceBean(tClass, l + 10L);
        }
    }
}
