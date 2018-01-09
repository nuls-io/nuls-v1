package io.nuls.cache.manager;

import io.nuls.core.chain.entity.Block;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.UserManagedCache;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.builders.UserManagedCacheBuilder;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.expiry.Duration;
import org.ehcache.expiry.Expirations;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Niels
 * @date 2017/10/27
 *
 */
public class EhCacheManager {
    private static final EhCacheManager INSTANCE = new EhCacheManager();
    private static final Map<String, Class> KEY_TYPE_MAP = new HashMap<>();
    private static final Map<String, Class> VALUE_TYPE_MAP = new HashMap<>();
    private Map<String,Cache> cacheMap;

    private EhCacheManager() {
    }

    public static EhCacheManager getInstance() {
        return INSTANCE;
    }

    public void init() {
        cacheMap = new HashMap<>();
    }

    public void createCache(String title, Class keyType, Class<? extends Serializable> valueType, int heapMb,int timeToLiveSeconds,int timeToIdleSeconds) {
        UserManagedCacheBuilder cacheBuilder =
                UserManagedCacheBuilder.newUserManagedCacheBuilder(keyType, valueType) ;
        if(heapMb>0){
            cacheBuilder.withResourcePools(ResourcePoolsBuilder.newResourcePoolsBuilder()
                    .heap(heapMb, MemoryUnit.MB));
        }
        if(timeToLiveSeconds>0){
            cacheBuilder.withExpiry(Expirations.timeToLiveExpiration(Duration.of(timeToLiveSeconds, TimeUnit.SECONDS)));
        }
        if(timeToIdleSeconds>0){
            cacheBuilder.withExpiry(Expirations.timeToIdleExpiration(Duration.of(timeToIdleSeconds, TimeUnit.SECONDS)));
        }
        cacheMap.put(title,cacheBuilder.build(true));
        KEY_TYPE_MAP.put(title, keyType);
        VALUE_TYPE_MAP.put(title, valueType);
    }

    public Cache getCache(String title) {
        return getCache(title, KEY_TYPE_MAP.get(title), VALUE_TYPE_MAP.get(title));
    }

    private <K,V> Cache<K,V> getCache(String title, Class<? extends K> aClass, Class<? extends V> aClass1) {
        Cache<K,V> cache = cacheMap.get(title);
        return cache;
    }

    public void close() {
        cacheMap.clear();
    }

    public void removeCache(String title) {
        cacheMap.remove(title);
    }

    public List<String> getCacheTitleList() {
        return new ArrayList<>(KEY_TYPE_MAP.keySet());
    }
}
