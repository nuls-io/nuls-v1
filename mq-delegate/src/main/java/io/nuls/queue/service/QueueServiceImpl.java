package io.nuls.queue.service;

import io.nuls.global.NulsContext;
import io.nuls.mq.exception.QueueException;
import io.nuls.mq.intf.IQueueService;
import io.nuls.mq.intf.NulsQueue;
import io.nuls.mq.intf.StatInfo;
import io.nuls.queue.impl.BlockingQueueImpl;
import io.nuls.queue.impl.PersistentQueue;
import io.nuls.queue.impl.manager.QueueManager;
import io.nuls.util.log.Log;

/**
 * 队列服务类
 * Created by Niels on 2017/9/20.
 */
public class QueueServiceImpl<T> implements IQueueService<T> {

    private static final QueueServiceImpl service = new QueueServiceImpl();

    private QueueServiceImpl() {
        NulsContext.getInstance().regService(this);
    }

    public static IQueueService getInstance() {
        return service;
    }


    /**
     * 创建一个持久化队列
     *
     * @param queueName 队列名称
     * @param maxSize   单个文件最大大小fileLimitLength/非持久化时是队列的最大长度
     * @return 是否创建成功
     */
    public boolean createQueue(String queueName, Long maxSize,boolean persist) {
        return createQueue(queueName, maxSize,persist, 0);
    }

    /**
     * 创建一个持久化队列
     *
     * @param queueName    队列名称
     * @param maxSize      单个文件最大大小fileLimitLength/非持久化时是队列的最大长度
     * @param latelySecond 统计日志打印时间段
     * @return 是否创建成功
     */
    public boolean createQueue(String queueName, Long maxSize,boolean persist, int latelySecond) {
        try {
            NulsQueue queue = null;
            if(persist){
                queue = new PersistentQueue(queueName, maxSize);
            }else{
                queue = new BlockingQueueImpl(queueName,Integer.parseInt(maxSize+""));
            }
            QueueManager.initQueue(queueName, queue,latelySecond);
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
    public void close(String queueName) throws QueueException {
        QueueManager.close(queueName);
    }

    @Override
    public StatInfo getStatInfo(String queueName) {
        return QueueManager.getStatInfo(queueName);
    }
}
