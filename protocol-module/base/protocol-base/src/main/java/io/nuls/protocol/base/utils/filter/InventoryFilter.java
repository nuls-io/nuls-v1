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

package io.nuls.protocol.base.utils.filter;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 向量清单过滤器
 *
 * @author ln
 */
public class InventoryFilter {

    private final int elements;
    private AtomicInteger size = new AtomicInteger(0);

    private BloomFilter<byte[]> filter;

//    private Lock lock = new ReentrantLock();

    public InventoryFilter(int elements) {
        this.elements = elements;
        filter = BloomFilter.create(Funnels.byteArrayFunnel(), elements, 0.00001);
    }

    public BloomFilter getFilter() {
        return filter;
    }

    public void insert(byte[] object) {
//        lock.lock();
//        try {
        filter.put(object);
        int count = size.incrementAndGet();
        if (count >= elements - 100) {
            this.clear();
        }
//        } finally {
//            lock.unlock();
//        }
    }

    public boolean contains(byte[] object) {
        return filter.mightContain(object);
    }

    public void clear() {
        filter = BloomFilter.create(Funnels.byteArrayFunnel(), elements, 0.00001);
    }
}
