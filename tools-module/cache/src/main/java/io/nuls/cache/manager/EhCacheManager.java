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
import io.nuls.cache.model.CacheMapParams;
import io.nuls.cache.utils.CacheObjectSerializer;
import io.nuls.cache.utils.EhcacheListener;
import io.nuls.core.tools.param.AssertUtil;
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
     * @param title    容器标识,cache name
     * @param params   初始化参数,init parameters
     */
    public void createCache(String title, CacheMapParams params) {
        AssertUtil.canNotEmpty(params.getHeapMb());
        AssertUtil.canNotEmpty(params.getKeyType());
        AssertUtil.canNotEmpty(params.getValueType());
        CacheConfigurationBuilder builder = CacheConfigurationBuilder.newCacheConfigurationBuilder(params.getKeyType(), params.getValueType(),
                ResourcePoolsBuilder.newResourcePoolsBuilder().heap(params.getHeapMb(), MemoryUnit.MB)
        );
        if (params.getListener() != null) {
            Set<EventType> types = new HashSet<>();
            types.add(EventType.CREATED);
            types.add(EventType.UPDATED);
            types.add(EventType.EVICTED);
            types.add(EventType.EXPIRED);
            types.add(EventType.REMOVED);
            CacheEventListenerConfigurationBuilder cacheEventListenerConfiguration = CacheEventListenerConfigurationBuilder
                    .newEventListenerConfiguration(new EhcacheListener(params.getListener()), types)
                    .unordered().asynchronous();
            builder = builder.add(cacheEventListenerConfiguration);
        }
        builder = builder.withSizeOfMaxObjectGraph(MAX_SIZE_OF_CACHE_OBJ_GRAPH);
//        builder = builder.withValueSerializer(new CacheObjectSerializer(params.getValueType())).withKeySerializer(new CacheObjectSerializer(params.getKeyType()));
        if (params.getTimeToLiveSeconds() > 0) {
            builder = builder.withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(params.getTimeToLiveSeconds())));
        }
        if (params.getTimeToIdleSeconds() > 0) {
            builder = builder.withExpiry(ExpiryPolicyBuilder.timeToIdleExpiration(Duration.ofSeconds(params.getTimeToIdleSeconds())));
        }
        if (null != params.getValueCopier()) {
            builder = builder.withValueCopier( params.getValueCopier());
        }
        cacheManager.createCache(title, builder.build());
        KEY_TYPE_MAP.put(title, params.getKeyType());
        VALUE_TYPE_MAP.put(title, params.getValueType());
    }

    /**
     * 获取原始的ehcache的cache
     *
     * @param title 缓存标识，cache name
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
