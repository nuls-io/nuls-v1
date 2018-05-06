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
package io.nuls.cache;

import io.nuls.cache.listener.intf.NulsCacheListener;
import io.nuls.cache.manager.EhCacheManager;
import org.ehcache.Cache;
import org.ehcache.spi.copy.Copier;

import java.io.Serializable;
import java.util.*;

/**
 * @author Niels
 * @date 2017/12/20
 */
public class CacheMap<K, V> {

    private EhCacheManager cacheManager = EhCacheManager.getInstance();

    private final String cacheName;

    public CacheMap(String cacheName, int heapMb, Copier<V> valueCopier) {
        this(cacheName, heapMb, 0, 0, valueCopier);
    }

    public CacheMap(String cacheName, int heapMb, int timeToLiveSeconds, int timeToIdleSeconds, Copier<V> valueCopier) {
        this(cacheName, heapMb, timeToLiveSeconds, timeToIdleSeconds, null,valueCopier);
    }

    public CacheMap(String cacheName, int heapMb, int timeToLiveSeconds, int timeToIdleSeconds, NulsCacheListener listener, Copier<V> valueCopier) {
        this.cacheManager.createCache(cacheName, String.class, Serializable.class, heapMb, timeToLiveSeconds, timeToIdleSeconds, listener, valueCopier);
        this.cacheName = cacheName;
    }

    public int size() {
        return this.keySet().size();
    }

    public boolean isEmpty() {
        return this.keySet().isEmpty();
    }

    public boolean containsKey(K key) {
        Cache cache = this.cacheManager.getCache(cacheName);
        if (cache == null) {
            return false;
        }
        return cache.containsKey(key);
    }

    public boolean containsValue(V value) {
        List<V> vlist = this.values();
        return vlist.contains(value);
    }


    public V get(K key) {
        if (null == cacheManager.getCache(cacheName) || null == key) {
            return null;
        }
        return ((V) cacheManager.getCache(cacheName).get(key));
    }


    public void put(K key, V value) {
        Object valueObj = value;
        if (null == cacheManager.getCache(cacheName)) {
            throw new RuntimeException("Cache not exist!");
        }
        cacheManager.getCache(cacheName).put(key, valueObj);
    }

    public void remove(K key) {
        if (null == cacheManager.getCache(cacheName)) {
            return;
        }
        cacheManager.getCache(cacheName).remove(key);
    }

    public void clear() {
        if (null == cacheManager.getCache(cacheName)) {
            return;
        }
        cacheManager.getCache(cacheName).clear();
    }

    public Set<K> keySet() {
        Cache cache = this.cacheManager.getCache(cacheName);
        if (null == cache) {
            return new HashSet<>();
        }
        Iterator it = cache.iterator();
        Set<K> set = new HashSet<>();
        while (it.hasNext()) {
            Cache.Entry<K, V> entry = (Cache.Entry<K, V>) it.next();
            set.add((K) entry.getKey());
        }
        return set;
    }

    public List<V> values() {
        if (cacheManager == null || null == cacheManager.getCache(cacheName)) {
            return new ArrayList<>();
        }
        Iterator it = cacheManager.getCache(cacheName).iterator();
        List<V> list = new ArrayList<>();
        while (it.hasNext()) {
            Cache.Entry<K, V> entry = (Cache.Entry<K, V>) it.next();
            V t = entry.getValue();
            list.add(t);
        }
        return list;
    }

    public void destroy() {
        this.cacheManager.removeCache(cacheName);
    }


}
