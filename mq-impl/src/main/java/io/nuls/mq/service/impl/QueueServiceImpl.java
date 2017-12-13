package io.nuls.mq.service.impl;

import io.nuls.core.utils.log.Log;
import io.nuls.mq.entity.StatInfo;
import io.nuls.mq.entity.impl.BlockingQueueImpl;
import io.nuls.mq.entity.impl.PersistentQueue;
import io.nuls.mq.intf.QueueService;
import io.nuls.mq.intf.AbstractNulsQueue;
import io.nuls.mq.manager.QueueManager;

/**
 * 队列服务类
 * Created by Niels on 2017/9/20.
 */
public class QueueServiceImpl<T> implements QueueService<T> {

    private static final QueueServiceImpl INSTANCE = new QueueServiceImpl();

    private QueueServiceImpl() {
    }

    public static QueueService getInstance() {
        return INSTANCE;
    }


    /**
     * 创建一个持久化队列
     *
     * @param queueName 队列名称
     * @param maxSize   单个文件最大大小fileLimitLength/非持久化时是队列的最大长度
     * @return 是否创建成功
     */
    @Override
    public boolean createQueue(String queueName, Long maxSize, boolean persist) {
        return createQueue(queueName, maxSize, persist, 0);
    }

    /**
     * 创建一个持久化队列
     *
     * @param queueName    队列名称
     * @param maxSize      单个文件最大大小fileLimitLength/非持久化时是队列的最大长度
     * @param latelySecond 统计日志打印时间段
     * @return 是否创建成功
     */
    @Override
    public boolean createQueue(String queueName, Long maxSize, boolean persist, int latelySecond) {
        try {
            AbstractNulsQueue queue = null;
            if (persist) {
                queue = new PersistentQueue(queueName, maxSize);
            } else {
                queue = new BlockingQueueImpl(queueName, Integer.parseInt(maxSize + ""));
            }
            QueueManager.initQueue(queueName, queue, latelySecond);
            return true;
        } catch (Exception e) {
            Log.error("", e);
            return false;
        }
    }

    @Override
    public boolean destroyQueue(String queueName) {
        try {
            QueueManager.destroyQueue(queueName);
        } catch (Exception e) {
            Log.error("", e);
            return false;
        }
        return true;
    }

    @Override
    public T poll(String queueName) {
        Object data = QueueManager.poll(queueName);
        return (T) data;
    }

    @Override
    public T take(String queueName) throws InterruptedException {
        Object data = QueueManager.take(queueName);
        return (T) data;
    }

    @Override
    public void offer(String queueName, T item) {
        QueueManager.offer(queueName, item);
    }

    @Override
    public long size(String queueName) {
        return QueueManager.size(queueName);
    }

    @Override
    public long getMaxSize(String queueName) {
        return QueueManager.getMaxSize(queueName);
    }

    @Override
    public void clear(String queueName) {
        QueueManager.clear(queueName);
    }

    @Override
    public void close(String queueName) {
        QueueManager.close(queueName);
    }

    @Override
    public StatInfo getStatInfo(String queueName) {
        return QueueManager.getStatInfo(queueName);
    }

    @Override
    public void remove(String queueName, T item) {
        QueueManager.remove(queueName,item);

    }
}
