/**
 * MIT License
 * Copyright (c) 2017-2018 nuls.io
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.db.model;

import java.util.Comparator;
import java.util.Objects;

/**
 * 封装数据键值对
 *
 * @param <K>
 * @param <V>
 */
public class Entry<K, V> implements Comparable<K> {
    private final K key;
    private V value;
    private Comparator<K> comparator;

    public Entry(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public Entry(K key, V value, Comparator<K> comparator) {
        this.key = key;
        this.value = value;
        this.comparator = comparator;
    }

    public final K getKey() {
        return key;
    }

    public final V getValue() {
        return value;
    }

    @Override
    public final String toString() {
        return key + "=" + value;
    }

    @Override
    public final int hashCode() {
        return Objects.hashCode(key) ^ Objects.hashCode(value);
    }

    @Override
    public final boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof Entry) {
            Entry<?, ?> e = (Entry<?, ?>) o;
            if (Objects.equals(key, e.getKey()) && Objects.equals(value, e.getValue())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int compareTo(K thatKey) {
        return comparator.compare(key, thatKey);
    }
}
