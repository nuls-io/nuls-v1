package io.nuls.cache.service.impl;

import io.nuls.cache.constant.EhCacheConstant;
import io.nuls.cache.entity.CacheElement;
import io.nuls.cache.manager.EhCacheManager;
import io.nuls.cache.service.intf.CacheService;
import org.ehcache.Cache;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Niels on 2017/10/27.
 */
public class EhCacheServiceImpl<K, T> implements CacheService<K, T> {
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
        cacheManager.createCache(title, keyType, valueType, heap);
    }

    @Override
    public void createCache(String title) {
        cacheManager.createCache(title, String.class, Serializable.class, EhCacheConstant.DEFAULT_MAX_SIZE);
    }


    @Override
    public void removeCache(String title) {
        cacheManager.removeCache(title);
    }

    @Override
    public void putElement(String cacheTitle, K key, T value) {
        cacheManager.getCache(cacheTitle).put(key, value);
    }

    @Override
    public void putElement(String cacheTitle, CacheElement element) {
        cacheManager.getCache(cacheTitle).put(element.getKey(), element.getValue());
    }

    @Override
    public T getElementValue(String cacheTitle, K key) {
        return (T) cacheManager.getCache(cacheTitle).get(cacheTitle);
    }

    @Override
    public List<T> getElementValueList(String cacheTitle) {
        Iterator it = cacheManager.getCache(cacheTitle).iterator();
        List<T> list = new ArrayList<>();
        while (it.hasNext()) {
            Cache.Entry<K,T> entry = (Cache.Entry<K,T>) it.next();
            list.add(entry.getValue());
        }
        return list;
    }

    @Override
    public void removeElement(String cacheTitle, K key) {
        cacheManager.getCache(cacheTitle).remove(key);
    }

    @Override
    public void clearCache(String title) {
        cacheManager.getCache(title).clear();
    }

    @Override
    public List<String> getCacheTitleList() {
        return cacheManager.getCacheTitleList();
    }

    @Override
    public void putElements(String cacheTitle, Map<K, T> map) {
        cacheManager.getCache(cacheTitle).putAll(map);
    }

}
