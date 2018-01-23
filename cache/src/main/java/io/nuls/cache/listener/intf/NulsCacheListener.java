package io.nuls.cache.listener.intf;

import io.nuls.cache.entity.CacheListenerItem;

/**
 * @author Niels
 * @date 2018/1/23
 */
public interface NulsCacheListener<K, V> {

    void onCreate(CacheListenerItem<K, V> item);

    void onEvict(CacheListenerItem<K, V> item);

    void onRemove(CacheListenerItem<K, V> item);

    void onUpdate(CacheListenerItem<K, V> item);

    void onExpire(CacheListenerItem<K, V> item);

}
