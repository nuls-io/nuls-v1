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
package io.nuls.kernel.utils.queue.entity;


import io.nuls.kernel.utils.queue.fqueue.entity.FQueue;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 用Fqueue实现的持久化队列
 * Created by Niels on 2017/9/20.
 */
public class PersistentQueue {

    private final String queueName;
    private final long maxSize;
    private FQueue queue = null;
    /**
     * Lock held by take, poll, etc
     */
    private final ReentrantLock takeLock = new ReentrantLock();

    /**
     * Wait queue for waiting takes
     */
    private final Condition notEmpty = takeLock.newCondition();

    /**
     * 创建队列
     *
     * @param queueName 队列名称
     * @param maxSize   单个文件最大大小fileLimitLength
     */
    public PersistentQueue(String queueName, long maxSize) throws Exception {
        this.queueName = PersistentQueue.class.getClassLoader().getResource("").getPath() + "/data/queue/" + queueName;
        this.maxSize = maxSize;
        this.queue = new FQueue(this.queueName, maxSize);
    }

    public void offer(byte[] t) {
        if (null == t || this.queue == null) {
            return;
        }
        this.queue.offer(t);
        if (size() > 0) {
            signalNotEmpty();
        }
    }

    @Deprecated
    public void remove(byte[] item) {
        this.queue.remove(item);
    }

    public byte[] poll() {
        return this.queue.poll();
    }

    public byte[] take() throws InterruptedException {
        byte[] x;
        final ReentrantLock takeLock = this.takeLock;
        takeLock.lockInterruptibly();
        try {
            while (size() == 0) {
                notEmpty.await();
            }
            x = poll();
            if (null == x) {
                return take();
            }
            if (size() > 0) {
                notEmpty.signal();
            }
        } finally {
            takeLock.unlock();
        }
        return x;
    }

    private void signalNotEmpty() {
        final ReentrantLock takeLock = this.takeLock;
        takeLock.lock();
        try {
            notEmpty.signal();
        } finally {
            takeLock.unlock();
        }
    }

    public long size() {
        return this.queue.size();
    }

    public void close() throws IOException {
        this.queue.close();
    }

    public void clear() {
        this.queue.clear();
    }

    public void distroy() throws IOException {
        if (null == queue) {
            return;
        }
        this.queue.close();
        File file = new File(this.queueName);
        this.deleteFile(file);
    }

    private void deleteFile(File file) {
        if (null != file && file.exists()) {//判断文件是否存在
            if (file.isFile()) {//判断是否是文件
                boolean b = file.delete();//删除文件
//                Log.debug("删除文件:" + file.getPath() + "，结果：" + b);
            } else if (file.isDirectory()) {//否则如果它是一个目录
                File[] files = file.listFiles();//声明目录下所有的文件 files[];
                for (File f : files) {//遍历目录下所有的文件
                    this.deleteFile(f);//把每个文件用这个方法进行迭代
                }
                boolean b = file.delete();//删除文件夹
//                Log.debug("删除文件:" + file.getPath() + "，结果：" + b);
            }
        }
    }

}
