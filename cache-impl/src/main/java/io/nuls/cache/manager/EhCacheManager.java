package io.nuls.cache.manager;

import io.nuls.cache.constant.EhCacheConstant;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.MemoryUnit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Niels on 2017/10/27.
 * nuls.io
 */
public class EhCacheManager {
    private static final EhCacheManager manager = new EhCacheManager();
    private static  final Map<String,Class> keyTypeMap = new HashMap<>();
    private static  final Map<String,Class> valueTypeMap = new HashMap<>();
    private CacheManager cacheManager;

    private EhCacheManager(){}

    public static EhCacheManager getInstance(){
        return manager;
    }

    public  void init(){
        cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true);
    }

    public void createCache(String title,Class keyType,Class valueType,int heapMb) {
        ResourcePoolsBuilder builder = ResourcePoolsBuilder.heap(Integer.MAX_VALUE);
        builder.heap(heapMb, MemoryUnit.MB);
        cacheManager.createCache(title,
                CacheConfigurationBuilder.newCacheConfigurationBuilder(keyType,valueType, builder));
        keyTypeMap.put(title,keyType);
        valueTypeMap.put(title,valueType);
    }

    public Cache getCache(String title){
        return cacheManager.getCache(title,keyTypeMap.get(title),valueTypeMap.get(title));
    };

    public void close(){
        cacheManager.close();
    }

    public void removeCache(String title) {
        cacheManager.removeCache(title);
    }

    public List<String> getCacheTitleList() {
        return new ArrayList<String>(keyTypeMap.keySet());
    }
}
