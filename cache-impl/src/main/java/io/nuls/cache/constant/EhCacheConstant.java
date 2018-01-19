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
package io.nuls.cache.constant;

/**
 *
 * @author Niels
 * @date 2017/10/27
 *
 */
public interface EhCacheConstant {
    //version
    int CACHE_MODULE_VERSION = 1111;
    //Minimum version supported
    int MINIMUM_VERSION_SUPPORTED = 0;

    String KEY_TYPE_FIELD="keyType";
    String VALUE_TYPE_FIELD="valueType";
    String POOL_HEAP_FIELD = "heap";
    String POOL_OFF_HEAP_FIELD = "offheap";
    String POOL_DISK_FIELD = "disk";
    String POOL_TIME_OF_LIVE_SECONDS = "timeToLiveSeconds";
    String POOL_TIME_OF_IDLE_SECONDS = "timeToIdleSeconds";
    int DEFAULT_MAX_SIZE = 16;
}
