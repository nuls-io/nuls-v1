package io.nuls.cache.intf;

import io.nuls.cache.entity.CacheElement;

/**
 * Created by Niels on 2017/10/18.
 * nuls.io
 */
public interface ICacheEventListenner {

    void onAdd(CacheElement element);

    void onModfiy(CacheElement element);

    void onRemove(CacheElement element);

    void onClear();

}
