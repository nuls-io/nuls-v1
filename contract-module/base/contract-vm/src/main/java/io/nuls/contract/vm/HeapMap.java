package io.nuls.contract.vm;

import java.util.HashMap;
import java.util.Map;

public class HeapMap<K, V> extends HashMap<K, V> {

    private final Map<K, V> cache;

    public HeapMap(int initialCapacity) {
        super(initialCapacity);
        cache = new HashMap<>(initialCapacity);
    }

    @Override
    public V get(Object key) {
        V v = cache.get(key);
        if (v == null) {
            v = super.get(key);
        }
        return v;
    }

    @Override
    public V put(K key, V value) {
        return cache.put(key, value);
    }

    public void commit() {
        super.putAll(cache);
        cache.clear();
    }

    public void clearCache() {
        cache.clear();
    }

}
