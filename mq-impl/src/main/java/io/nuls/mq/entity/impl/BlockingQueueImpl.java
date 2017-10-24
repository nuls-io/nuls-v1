package io.nuls.mq.entity.impl;

import io.nuls.mq.intf.NulsQueue;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Niels on 2017/10/10.
 * nuls.io
 */
public class BlockingQueueImpl<T> extends NulsQueue<T> {

    private BlockingQueue<T> queue;

    public BlockingQueueImpl(String queueName,int maxSize){
        this.queueName = queueName;
        this.maxSize = maxSize;
        this.queue = new LinkedBlockingQueue<T>( maxSize);
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
}
