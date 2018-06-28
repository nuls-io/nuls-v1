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

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 向量清单过滤器
 *
 * @author ln
 */
public class InventoryFilter {

    private final int maxCount;
    private final int elements;
    private AtomicInteger size = new AtomicInteger(0);

    private BloomFilter filter;

    public InventoryFilter(int maxCount, int elements) {
        this.maxCount = maxCount;
        this.elements = elements;
        filter = new BloomFilter(elements, 0.0001, randomLong());
    }

    public BloomFilter getFilter() {
        return filter;
    }

    public void insert(byte[] object) {
        filter.insert(object);
        int count = size.incrementAndGet();
        if (count >= maxCount) {
            this.clear();
        }
    }

    public boolean contains(byte[] object) {
        return filter.contains(object);
    }

    public void clear() {
        filter = new BloomFilter(elements, 0.0001, randomLong());
        this.size = new AtomicInteger(0);
    }

    private long randomLong() {
        return (long) (Math.random() * Long.MAX_VALUE);
    }
}
