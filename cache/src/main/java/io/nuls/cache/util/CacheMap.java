package io.nuls.cache.util;

import io.nuls.cache.service.intf.CacheService;
import io.nuls.core.chain.intf.NulsCloneable;
import io.nuls.core.context.NulsContext;

import java.util.List;
import java.util.Set;

/**
 * @author Niels
 * @date 2017/12/20
 */
public class CacheMap<K, V> {

    private final String cacheName;
    private CacheService cacheService = NulsContext.getInstance().getService(CacheService.class);

    public CacheMap(String cacheName, int timeToLiveSeconds, int timeToIdleSeconds) {
        this.cacheService.createCache(cacheName, timeToLiveSeconds, timeToIdleSeconds);
        this.cacheName = cacheName;
    }

    public CacheMap(String cacheName) {
        this(cacheName, 0, 0);
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


    public <V extends NulsCloneable> void put(K key, V value) {
        this.cacheService.putElement(cacheName, key, value);
    }

    public void putWithOutClone(K key, V value) {
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
}
