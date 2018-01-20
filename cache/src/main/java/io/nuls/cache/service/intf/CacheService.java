/**
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
 */
package io.nuls.cache.service.intf;

import io.nuls.cache.entity.CacheElement;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Niels
 * @date 2017/10/18
 */
public interface CacheService<K, V> {

    /**
     * create a cache named title
     *
     * @param title
     */
    void createCache(String title,int heapMb);

    /**
     * create a cache named title by configurations
     *
     * @param title
     * @param initParams
     */
    void createCache(String title, Map<String, Object> initParams);

    void createCache(String title, int heapMb, int timeToLiveSeconds, int timeToIdleSeconds);


    /**
     * remove a cache by title
     *
     * @param title
     */
    void removeCache(String title);

    /**
     * put data to a cache
     *
     * @param cacheTitle
     * @param key
     * @param value
     */
    void putElement(String cacheTitle, K key, Object value);

    /**
     * put data to a cache
     *
     * @param element
     */
    void putElement(CacheElement element);

    /**
     * get data from the cache named cacheTitle
     *
     * @param cacheTitle
     * @param key
     * @return
     */
    V getElement(String cacheTitle, K key);

    List<V> getElementList(String cacheTitle);

    /**
     * remove an element from the cache named cacheTitle
     *
     * @param cacheTitle
     * @param key
     */
    void removeElement(String cacheTitle, K key);

    /**
     * @param title
     */
    void clearCache(String title);

    List<String> getCacheTitleList();


    boolean containsKey(String cacheTitle, K key);

    Set<K> keySet(String cacheTitle);
}
