package io.nuls.contract.vm;

import java.util.HashMap;
import java.util.Map;

public class HeapMap<K, V> extends HashMap<K, V> {

    private Map<K, V> cache = new HashMap<>();

    private int initialCapacity;

    public HeapMap(int initialCapacity) {
        super(initialCapacity);
        this.initialCapacity = initialCapacity;
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
    }

    public void clearCache() {
        this.cache = new HashMap<>(initialCapacity);
    }

}
