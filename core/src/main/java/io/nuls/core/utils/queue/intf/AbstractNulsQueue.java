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
package io.nuls.core.utils.queue.intf;

import io.nuls.core.utils.queue.entity.StatInfo;

import java.io.IOException;

/**
 * 队列定义
 *
 * @author Niels
 * @date 2017/9/20
 */
public abstract class AbstractNulsQueue<T> {

    protected String queueName;
    protected long maxSize;
    private StatInfo statInfo;

    public final String getQueueName() {
        return queueName;
    }

    /**
     * @return 初始化时设置的最大长度
     */
    public final long getMaxSize() {
        return maxSize;
    }


    /**
     * 向队列中加入数据
     *
     * @param t 数据对象
     */
    public abstract void offer(T t);

    /**
     * 从队列中取出数据，若没有数据则阻塞
     *
     * @return 数据对象
     */
    public abstract T poll();

    public abstract T take() throws InterruptedException;

    /**
     * @return 队列当前长度
     */
    public abstract long size();

    /**
     * 销毁改队列，包括删除磁盘文件
     */
    public abstract void distroy() throws IOException;

    /**
     * 关闭队列
     *
     * @throws IOException
     */
    public abstract void close() throws IOException;

    /**
     * 清空队列
     */
    public abstract void clear();

    public void setStatInfo(StatInfo statInfo) {
        this.statInfo = statInfo;
    }

    public StatInfo getStatInfo() {
        return statInfo;
    }

    public abstract void remove(T item) ;
}
