package io.nuls.cache.manager;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.MemoryUnit;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private CacheManager cacheManager;

    private EhCacheManager() {
    }

    public static EhCacheManager getInstance() {
        return INSTANCE;
    }

    public void init() {
        cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true);
    }

    public void createCache(String title, Class keyType, Class<? extends Serializable> valueType, int heapMb) {
        CacheConfigurationBuilder builder = CacheConfigurationBuilder.newCacheConfigurationBuilder(keyType, valueType,
                ResourcePoolsBuilder.newResourcePoolsBuilder()
                        .heap(heapMb, MemoryUnit.MB)
        );
        cacheManager.createCache(title, builder);
        KEY_TYPE_MAP.put(title, keyType);
        VALUE_TYPE_MAP.put(title, valueType);
    }

    public Cache getCache(String title) {
        return cacheManager.getCache(title, KEY_TYPE_MAP.get(title), VALUE_TYPE_MAP.get(title));
    }

    ;

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
