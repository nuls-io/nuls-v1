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
package io.nuls.core.utils.queue.entity;


import io.nuls.core.utils.queue.intf.AbstractNulsQueue;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Niels on 2017/10/10.
 */
public class BlockingQueueImpl<T> extends AbstractNulsQueue<T> {

    private BlockingQueue<T> queue;

    public BlockingQueueImpl(String queueName, int maxSize) {
        this.queueName = queueName;
        this.maxSize = maxSize;
        this.queue = new LinkedBlockingQueue<T>(maxSize);
    }

    @Override
    public void offer(T t) {
        this.queue.offer(t);
    }

    @Override
    public T poll() {
        return this.queue.poll();
    }

    @Override
    public T take() throws InterruptedException {
        return this.queue.take();
    }

    @Override
    public long size() {
        return this.queue.size();
    }

    @Override
    public void distroy() throws IOException {
        this.queue.clear();
    }

    @Override
    public void close() throws IOException {
        this.queue.clear();
    }

    @Override
    public void clear() {
        this.queue.clear();
    }

    @Override
    public void remove(T t) {
        this.queue.remove(t);
    }
}
