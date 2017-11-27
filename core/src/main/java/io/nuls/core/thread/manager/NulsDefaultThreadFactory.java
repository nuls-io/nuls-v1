package io.nuls.core.thread.manager;

import io.nuls.core.thread.BaseThread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Niels
 * @date 2017/11/24
 */
public class NulsDefaultThreadFactory implements ThreadFactory {
    private AtomicInteger threadNo = new AtomicInteger(1);
    private final String poolName;

    public NulsDefaultThreadFactory(short moduleId,String poolName) {
        this.poolName = poolName;
        //todo 加入管理
    }

    @Override
    public final Thread newThread(Runnable r) {
        String threadName = "[" + poolName + "-" + threadNo.getAndIncrement() + "]";
        Thread newThread = new BaseThread(r, threadName);
        newThread.setDaemon(true);
        if (newThread.getPriority() != Thread.NORM_PRIORITY) {
            newThread.setPriority(Thread.NORM_PRIORITY);
        }
        return newThread;
    }

    public String getPoolName() {
        return poolName;
    }
}
