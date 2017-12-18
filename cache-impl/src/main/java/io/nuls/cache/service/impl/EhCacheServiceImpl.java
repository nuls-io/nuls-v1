package io.nuls.cache.service.impl;

import io.nuls.cache.constant.EhCacheConstant;
import io.nuls.cache.entity.CacheElement;
import io.nuls.cache.manager.EhCacheManager;
import io.nuls.cache.service.intf.CacheService;
import io.nuls.core.chain.intf.NulsCloneable;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.exception.NulsRuntimeException;
import org.ehcache.Cache;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Niels
 * @date 2017/10/27
 */
public class EhCacheServiceImpl<K, T extends NulsCloneable> implements CacheService<K, T> {
    private final EhCacheManager cacheManager = EhCacheManager.getInstance();

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
    public void createCache(String title) {
        cacheManager.createCache(title, String.class, Serializable.class, EhCacheConstant.DEFAULT_MAX_SIZE, 0, 0);
    }

    @Override
    public void createCache(String title, int timeToLiveSeconds, int timeToIdleSeconds) {
        cacheManager.createCache(title, String.class, Serializable.class, EhCacheConstant.DEFAULT_MAX_SIZE, timeToLiveSeconds, timeToIdleSeconds);
    }


    @Override
    public void removeCache(String title) {
        cacheManager.removeCache(title);
    }

    @Override
    public void putElement(String cacheTitle, K key, T value) {
        this.putElementWithoutClone(cacheTitle, key, value.copy());
    }

    @Override
    public void putElementWithoutClone(String cacheTitle, K key, Object value) {
        if (null == cacheManager.getCache(cacheTitle)) {
            throw new NulsRuntimeException(ErrorCode.FAILED, "Cache not exist!");
        }
        cacheManager.getCache(cacheTitle).put(key, value);
    }

    @Override
    public void putElement(String cacheTitle, CacheElement element) {
        if (null == cacheManager.getCache(cacheTitle)) {
            throw new NulsRuntimeException(ErrorCode.FAILED, "Cache not exist!");
        }
        cacheManager.getCache(cacheTitle).put(element.getKey(), element.getValue().copy());
    }

    @Override
    public T getElementValue(String cacheTitle, K key) {
        if (null == cacheManager.getCache(cacheTitle) || null == key) {
            return null;
        }
        T t = this.getElementValueWithOutClone(cacheTitle, key);
        if (null == t) {
            return t;
        }
        return (T) t.copy();
    }

    @Override
    public T getElementValueWithOutClone(String cacheTitle, K key) {
        if (null == cacheManager.getCache(cacheTitle) || null == key) {
            return null;
        }
        return ((T) cacheManager.getCache(cacheTitle).get(key));
    }

    @Override
    public List<T> getElementValueList(String cacheTitle) {
        Iterator it = cacheManager.getCache(cacheTitle).iterator();
        List<T> list = new ArrayList<>();
        while (it.hasNext()) {
            Cache.Entry<K, T> entry = (Cache.Entry<K, T>) it.next();
            list.add((T) entry.getValue().copy());
        }
        return list;
    }

    @Override
    public void removeElement(String cacheTitle, K key) {
        if (null == cacheManager.getCache(cacheTitle)) {
            throw new NulsRuntimeException(ErrorCode.FAILED, "Cache not exist!");
        }
        cacheManager.getCache(cacheTitle).remove(key);
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
    public boolean containsKey(String cacheTitle, String key) {
        return this.cacheManager.getCache(cacheTitle).containsKey(key);
    }
}
