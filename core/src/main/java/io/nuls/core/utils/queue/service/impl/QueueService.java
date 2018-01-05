package io.nuls.core.utils.queue.service.impl;

import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.queue.entity.BlockingQueueImpl;
import io.nuls.core.utils.queue.entity.PersistentQueue;
import io.nuls.core.utils.queue.entity.StatInfo;
import io.nuls.core.utils.queue.intf.AbstractNulsQueue;
import io.nuls.core.utils.queue.manager.QueueManager;

/**
 * 队列服务类
 * Created by Niels on 2017/9/20.
 */
public class QueueService<T> {

    private static final QueueService INSTANCE = new QueueService();

    private QueueService() {
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

    public boolean destroyQueue(String queueName) {
        try {
            QueueManager.destroyQueue(queueName);
        } catch (Exception e) {
            Log.error("", e);
            return false;
        }
        return true;
    }

    public T poll(String queueName) {
        Object data = QueueManager.poll(queueName);
        return (T) data;
    }

    public T take(String queueName) throws InterruptedException {
        Object data = QueueManager.take(queueName);
        return (T) data;
    }

    public void offer(String queueName, T item) {
        QueueManager.offer(queueName, item);
    }

    public long size(String queueName) {
        return QueueManager.size(queueName);
    }

    public long getMaxSize(String queueName) {
        return QueueManager.getMaxSize(queueName);
    }

    public void clear(String queueName) {
        QueueManager.clear(queueName);
    }

    public void close(String queueName) {
        QueueManager.close(queueName);
    }

    public StatInfo getStatInfo(String queueName) {
        return QueueManager.getStatInfo(queueName);
    }

    public void remove(String queueName, T item) {
        QueueManager.remove(queueName, item);

    }
}
