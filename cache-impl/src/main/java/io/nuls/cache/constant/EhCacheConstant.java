package io.nuls.cache.constant;

/**
 *
 * @author Niels
 * @date 2017/10/27
 *
 */
public interface EhCacheConstant {
    //version
    int CACHE_MODULE_VERSION = 1111;
    //Minimum version supported
    int MINIMUM_VERSION_SUPPORTED = 0;

    String KEY_TYPE_FIELD="keyType";
    String VALUE_TYPE_FIELD="valueType";
    String POOL_HEAP_FIELD = "heap";
    String POOL_OFF_HEAP_FIELD = "offheap";
    String POOL_DISK_FIELD = "disk";
    String POOL_TIME_OF_LIVE_SECONDS = "timeToLiveSeconds";
    String POOL_TIME_OF_IDLE_SECONDS = "timeToIdleSeconds";
    int DEFAULT_MAX_SIZE = 0;
}
