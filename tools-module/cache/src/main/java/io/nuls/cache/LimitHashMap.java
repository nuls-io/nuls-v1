/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package io.nuls.cache;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * @author: Niels Wang
 * @date: 2018/7/5
 */
public class LimitHashMap<K, V> {

    private final int maxSize;
    private Map<K, V> map = new ConcurrentHashMap<>();

    private LinkedBlockingDeque<K> queue = new LinkedBlockingDeque<>();

    public LimitHashMap(int maxSize) {
        this.maxSize = maxSize;
    }

    public boolean put(K k, V v) {
        V other = map.put(k, v);
        if(other != null) {
            return false;
        }
        queue.offer(k);
        if (maxSize > queue.size()) {
            return true;
        }
        int count = maxSize / 2;
        for (int i = 0; i < count; i++) {
            K key = queue.poll();
            if (null == key) {
                return true;
            }
            map.remove(key);
            if (count % 100 == 0 && count > queue.size()) {
                break;
            }
        }
        return true;
    }

    public void remove(K k) {
        map.remove(k);
        queue.remove(k);
    }

    public V get(K k) {
        return map.get(k);
    }

    public void clear() {
        queue.clear();
        map.clear();
    }

    public int size() {
        return map.size();
    }

    public boolean containsKey(K key) {
        return map.containsKey(key);
    }

    public Collection<V> values() {
        return map.values();
    }

    public Map<K, V> getMap() {
        return map;
    }

    public LinkedBlockingDeque<K> getQueue() {
        return queue;
    }
}
