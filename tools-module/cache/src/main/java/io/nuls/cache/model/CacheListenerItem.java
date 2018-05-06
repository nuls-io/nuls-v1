package io.nuls.cache.model;

/**
 * @author Niels
 * @date 2018/1/23
 */
public class CacheListenerItem<K, V> {

    public CacheListenerItem() {
    }

    public CacheListenerItem(K k, V newValue, V oldValue) {
        this.key = k;
        this.newValue = newValue;
        this.oldValue = oldValue;
    }

    private K key;
    private V newValue;

    public K getKey() {
        return key;
    }

    public void setKey(K key) {
        this.key = key;
    }

    public V getNewValue() {
        return newValue;
    }

    public void setNewValue(V newValue) {
        this.newValue = newValue;
    }

    public V getOldValue() {
        return oldValue;
    }

    public void setOldValue(V oldValue) {
        this.oldValue = oldValue;
    }

    private V oldValue;
}
