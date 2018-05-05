/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.cache.service;

import io.nuls.cache.intf.NulsCacheListener;

import java.util.List;
import java.util.Set;

/**
 * @Desription:
 * @Author: PierreLuo
 * @Date: 2018/5/4
 */
public interface CacheService<K, V> {

    /**
     * remove a cache by title
     */
    void removeCache(String title);

    /**
     * put data to a cache
     */
    void putElement(String cacheTitle, K key, Object value);


    /**
     * get data from the cache named cacheTitle
     */
    V getElement(String cacheTitle, K key);

    List<V> getElementList(String cacheTitle);

    /**
     * remove an element from the cache named cacheTitle
     */
    void removeElement(String cacheTitle, K key);

    /**
     * @param title
     */
    void clearCache(String title);

    List<String> getCacheTitleList();


    boolean containsKey(String cacheTitle, K key);

    Set<K> keySet(String cacheTitle);

    void createCache(String cacheName, int heapMb, int timeToLiveSeconds, int timeToIdleSeconds, NulsCacheListener listener);
}