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
package io.nuls.cache.service.impl;

import io.nuls.cache.constant.EhCacheConstant;
import io.nuls.cache.entity.CacheElement;
import io.nuls.cache.listener.intf.NulsCacheListener;
import io.nuls.cache.manager.EhCacheManager;
import io.nuls.cache.service.intf.CacheService;
import io.nuls.core.chain.intf.NulsCloneable;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.exception.NulsRuntimeException;
import org.ehcache.Cache;

import java.io.Serializable;
import java.util.*;

/**
 * @author Niels
 * @date 2017/10/27
 */
public class EhCacheServiceImpl<K, T> implements CacheService<K, T> {
    private final EhCacheManager cacheManager = EhCacheManager.getInstance();

    @Override
    public void createCache(String cacheName, int heapMb, int timeToLiveSeconds, int timeToIdleSeconds, NulsCacheListener listener) {
        cacheManager.createCache(cacheName, String.class, Serializable.class, heapMb, timeToLiveSeconds, timeToIdleSeconds, listener);
    }

    @Override
    public void createCache(String title, Map<String, Object> initParams) {

        Class keyType = String.class;
        if (initParams.get(EhCacheConstant.KEY_TYPE_FIELD) != null) {
            keyType = (Class) initParams.get(EhCacheConstant.KEY_TYPE_FIELD);
        }
        Class valueType = Object.class;
        if (initParams.get(EhCacheConstant.VALUE_TYPE_FIELD) != null) {
            valueType = (Class) initParams.get(EhCacheConstant.VALUE_TYPE_FIELD);
        }
        int heap = EhCacheConstant.DEFAULT_MAX_SIZE;
        if (initParams.get(EhCacheConstant.POOL_HEAP_FIELD) != null) {
            heap = (Integer) initParams.get(EhCacheConstant.POOL_HEAP_FIELD);
        }
        int timeToLiveSeconds = 0;
        if (initParams.get(EhCacheConstant.POOL_TIME_OF_LIVE_SECONDS) != null) {
            timeToLiveSeconds = (Integer) initParams.get(EhCacheConstant.POOL_TIME_OF_LIVE_SECONDS);
        }
        int timeToIdleSeconds = 0;
        if (initParams.get(EhCacheConstant.POOL_TIME_OF_IDLE_SECONDS) != null) {
            timeToIdleSeconds = (Integer) initParams.get(EhCacheConstant.POOL_TIME_OF_IDLE_SECONDS);
        }
        cacheManager.createCache(title, keyType, valueType, heap, timeToLiveSeconds, timeToIdleSeconds);
    }

    @Override
    public void createCache(String title, int heapMb) {
        cacheManager.createCache(title, String.class, Serializable.class, heapMb, 0, 0);
    }

    @Override
    public void createCache(String title, int heapMb, int timeToLiveSeconds, int timeToIdleSeconds) {
        cacheManager.createCache(title, String.class, Serializable.class, heapMb, timeToLiveSeconds, timeToIdleSeconds);
    }


    @Override
    public void removeCache(String title) {
        cacheManager.removeCache(title);
    }

    @Override
    public void putElement(String cacheTitle, K key, Object value) {
        Object valueObj = value;
        if (value instanceof NulsCloneable) {
            valueObj = ((NulsCloneable) value).copy();
        }
        if (null == cacheManager.getCache(cacheTitle)) {
            throw new NulsRuntimeException(ErrorCode.FAILED, "Cache not exist!");
        }
        cacheManager.getCache(cacheTitle).put(key, valueObj);
    }

    @Override
    public void putElement(CacheElement element) {
        if (null == cacheManager.getCache(element.getCacheTitle())) {
            throw new NulsRuntimeException(ErrorCode.FAILED, "Cache not exist!");
        }
        cacheManager.getCache(element.getCacheTitle()).put(element.getKey(), element.getValue().copy());
    }

    @Override
    public T getElement(String cacheTitle, K key) {
        if (null == cacheManager.getCache(cacheTitle) || null == key) {
            return null;
        }
        T t = ((T) cacheManager.getCache(cacheTitle).get(key));
        if (null == t) {
            return t;
        }
        if (t instanceof NulsCloneable) {
            return (T) ((NulsCloneable) t).copy();
        }
        return t;
    }

    @Override
    public List<T> getElementList(String cacheTitle) {
        if (cacheManager == null || null == cacheManager.getCache(cacheTitle)) {
            return new ArrayList<>();
        }
        Iterator it = cacheManager.getCache(cacheTitle).iterator();
        List<T> list = new ArrayList<>();
        while (it.hasNext()) {
            Cache.Entry<K, T> entry = (Cache.Entry<K, T>) it.next();
            T t = entry.getValue();
            T value = t;
            if (t instanceof NulsCloneable) {
                value = (T) ((NulsCloneable) t).copy();
            }
            list.add(value);
        }
        return list;
    }


    @Override
    public void removeElement(String cacheTitle, K key) {
        if (null == cacheManager.getCache(cacheTitle)) {
            throw new NulsRuntimeException(ErrorCode.FAILED, "Cache not exist!");
        }
        if (containsKey(cacheTitle, key)) {
            cacheManager.getCache(cacheTitle).remove(key);
        }
    }

    @Override
    public void clearCache(String title) {
        if (null == cacheManager.getCache(title)) {
            throw new NulsRuntimeException(ErrorCode.FAILED, "Cache not exist!");
        }
        cacheManager.getCache(title).clear();
    }

    @Override
    public List<String> getCacheTitleList() {
        return cacheManager.getCacheTitleList();
    }

    @Override
    public boolean containsKey(String cacheTitle, K key) {
        boolean result = this.cacheManager.getCache(cacheTitle).containsKey(key);
        return result;
    }

    @Override
    public Set<K> keySet(String cacheTitle) {
        Iterator it = cacheManager.getCache(cacheTitle).iterator();
        Set<K> set = new HashSet<>();
        while (it.hasNext()) {
            Cache.Entry<K, T> entry = (Cache.Entry<K, T>) it.next();
            set.add((K) entry.getKey());
        }
        return set;
    }

}
