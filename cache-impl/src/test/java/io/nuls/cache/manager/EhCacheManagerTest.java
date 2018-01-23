package io.nuls.cache.manager;

import io.nuls.cache.service.impl.EhCacheServiceImpl;
import io.nuls.cache.service.intf.CacheService;
import org.junit.Test;

/**
 * @author Niels
 * @date 2018/1/23
 */
public class EhCacheManagerTest {
    EhCacheManager manager = EhCacheManager.getInstance();
    @Test
    public void test() throws Exception {
        manager.init();
        CacheService<String,String> cache = EhCacheServiceImpl.getInstance();
        String cacheName = "test1";
        cache.createCache(cacheName,100,0,0);
        cache.putElement(cacheName,"x","adsfasdglauwerg");

        System.out.println(cache.getElement(cacheName,"x"));

    }
}