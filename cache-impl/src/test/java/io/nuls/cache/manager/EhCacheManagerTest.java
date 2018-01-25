package io.nuls.cache.manager;

import io.nuls.cache.service.impl.EhCacheServiceImpl;
import io.nuls.cache.service.intf.CacheService;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Niels
 * @date 2018/1/23
 */
public class EhCacheManagerTest {
    EhCacheManager manager = EhCacheManager.getInstance();
    @Test
    public void test() throws Exception {
        manager.init();
        CacheService<String,TT> cache = EhCacheServiceImpl.getInstance();

        CacheService<String,List<TT>> cacheList = EhCacheServiceImpl.getInstance();

        String cacheName = "test1";
        String cacheName2 = "list1";
        TT t1 = new TT(1,"t1");
        cache.createCache(cacheName,100,0,0);
        cache.putElement(cacheName,"x",t1);

        cacheList.createCache(cacheName2, 100, 0,0);
        List<TT> list = new ArrayList<>();
        list.add(t1);
        cacheList.putElement(cacheName2, "xx", list);

        System.out.println(cacheList.getElement(cacheName2,"xx").get(0));
        t1.setT(2);
        System.out.println(cache.getElement(cacheName,"x"));
        System.out.println(cacheList.getElement(cacheName2,"xx").get(0));

        cache.removeElement(cacheName, "x");

        System.out.println(cacheList.getElement(cacheName2,"xx").get(0));
    }
}