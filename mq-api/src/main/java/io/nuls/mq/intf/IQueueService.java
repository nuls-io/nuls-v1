package io.nuls.mq.intf;


import io.nuls.exception.NulsRuntimeException;
import io.nuls.mq.exception.QueueException;

import java.io.IOException;

/**
 * 提供给外部使用的接口
 * Created by Niels on 2017/9/20.
 */
public interface IQueueService<T> {
    /**
     * 创建一个持久化队列
     *
     * @param queueName 队列名称
     * @param maxSize   单个文件最大大小fileLimitLength/非持久化时是队列的最大长度
     * @param persist   是否持久化
     * @return 是否创建成功
     */
    public boolean createQueue(String queueName, Long maxSize, boolean persist);

    /**
     * 创建一个持久化队列
     *
     * @param queueName    队列名称
     * @param maxSize      单个文件最大大小fileLimitLength/非持久化时是队列的最大长度
     * @param persist      是否持久化
     * @param latelySecond 统计日志时间段
     * @return 是否创建成功
     */
    public boolean createQueue(String queueName, Long maxSize, boolean persist, int latelySecond);

    /**
     * 销毁该队列，并删除磁盘文件
     *
     * @param queueName 队列名称
     * @return
     */
    public boolean destroyQueue(String queueName);

    /**
     * 从队列中取出一个数据
     *
     * @param queueName 队列名称
     * @return
     */
    T poll(String queueName);

    T take(String queueName) throws InterruptedException;

    /**
     * 向队列中加入一条数据
     *
     * @param queueName 队列名称
     * @param item      数据对象
     */
    void offer(String queueName, T item);

    /**
     * @return 当前长度
     */
    public long size(String queueName);

    /**
     * @return 初始化时设置的单个文件最大大小
     */
    public long getMaxSize(String queueName);

    /**
     * 清空队列
     *
     * @param queueName
     */
    void clear(String queueName);

    /**
     * 关闭队列
     *
     * @param queueName
     * @throws IOException
     * @throws NulsRuntimeException
     */
    void close(String queueName) throws QueueException;

    /**
     * @param queueName
     * @return 状态描述对象
     */
    StatInfo getStatInfo(String queueName);
}
