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
package io.nuls.cache.manager;

import io.nuls.cache.listener.intf.NulsCacheListener;
import io.nuls.cache.utils.EhcacheListener;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.*;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.event.EventType;
import org.ehcache.spi.copy.Copier;

import java.io.Serializable;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author Niels
 * @date 2017/10/27
 */
public class EhCacheManager {
    private static final EhCacheManager INSTANCE = new EhCacheManager();
    private static final Map<String, Class> KEY_TYPE_MAP = new ConcurrentHashMap<>();
    private static final Map<String, Class> VALUE_TYPE_MAP = new ConcurrentHashMap<>();
    private static final long MAX_SIZE_OF_CACHE_OBJ_GRAPH = 5 * 1024 * 1024;

    private CacheManager cacheManager;

    private EhCacheManager() {
        init();
    }

    public static EhCacheManager getInstance() {
        return INSTANCE;
    }

    /**
     * 初始化方法，创建ehcache的管理器
     * Initialize method, to create the ehcache manager.
     */
    private void init() {
        cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true);
    }

    /**
     * 创建一个缓存容器
     * Create a cache container.
     *
     * @param title 容器标识，cache name
     * @param keyType 索引类型, the type of Key
     * @param valueType 对象类型 the type of value
     * @param heapMb 最大使用空间,Maximum usable space
     * @param timeToLiveSeconds 最长存活时间，为0时不超时,Maximum survival time, no timeout at 0.
     * @param timeToIdleSeconds 最长空闲时间，为0时不超时,Maximum idle time, no timeout at 0.
     * @param listener 缓存监听器, cacheListener
     * @param valueCopier 对象复制器, value object copier
     */
    public void createCache(String title, Class keyType, Class<? extends Serializable> valueType, int heapMb, int timeToLiveSeconds, int timeToIdleSeconds, NulsCacheListener listener, Copier valueCopier) {
        CacheConfigurationBuilder builder = CacheConfigurationBuilder.newCacheConfigurationBuilder(keyType, valueType,
                ResourcePoolsBuilder.newResourcePoolsBuilder().heap(heapMb, MemoryUnit.MB)
        );
        builder = builder.withSizeOfMaxObjectGraph(MAX_SIZE_OF_CACHE_OBJ_GRAPH);
        if (timeToLiveSeconds > 0) {
            builder = builder.withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(timeToLiveSeconds)));
//            builder = builder.withExpiry(Expirations.timeToLiveExpiration(Duration.of(timeToLiveSeconds, TimeUnit.SECONDS)));
        }
        if (timeToIdleSeconds > 0) {
            builder = builder.withExpiry(ExpiryPolicyBuilder.timeToIdleExpiration(Duration.ofSeconds(timeToIdleSeconds)));
//            builder = builder.withExpiry(Expirations.timeToIdleExpiration(Duration.of(timeToIdleSeconds, TimeUnit.SECONDS)));
        }
        if (null != valueCopier) {
            builder = builder.withValueCopier(valueCopier);
        }
        if (listener != null) {
            Set<EventType> types = new HashSet<>();
            types.add(EventType.CREATED);
            types.add(EventType.UPDATED);
            types.add(EventType.EVICTED);
            types.add(EventType.EXPIRED);
            types.add(EventType.REMOVED);
            CacheEventListenerConfigurationBuilder cacheEventListenerConfiguration = CacheEventListenerConfigurationBuilder
                    .newEventListenerConfiguration(new EhcacheListener(listener), types)
                    .unordered().asynchronous();
            builder = builder.add(cacheEventListenerConfiguration);
        }
        cacheManager.createCache(title, builder.build());
        KEY_TYPE_MAP.put(title, keyType);
        VALUE_TYPE_MAP.put(title, valueType);
    }

    /**
     * 获取原始的ehcache的cache
     * @param title 缓存标识，cache name
     * @return
     */
    public Cache getCache(String title) {
        Class keyType = KEY_TYPE_MAP.get(title);
        Class valueType = VALUE_TYPE_MAP.get(title);
        if (null == cacheManager || null == keyType || valueType == null) {
            return null;
        }
        return cacheManager.getCache(title, keyType, valueType);
    }

    public void close() {
        cacheManager.close();
    }

    public void removeCache(String title) {
        cacheManager.removeCache(title);
    }

    public List<String> getCacheTitleList() {
        return new ArrayList<String>(KEY_TYPE_MAP.keySet());
    }

}
