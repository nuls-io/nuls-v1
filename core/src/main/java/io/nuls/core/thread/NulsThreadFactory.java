package io.nuls.core.thread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Niels
 * @date 2017/11/24
 */
public abstract class NulsThreadFactory implements ThreadFactory {
    private AtomicInteger threadNo = new AtomicInteger(1);
    private final String poolName;

    public NulsThreadFactory(String poolName) {
        this.poolName = poolName;
    }

    @Override
    public final Thread newThread(Runnable r) {
        String threadName = "[" + poolName + "-" + threadNo.getAndIncrement() + "]";
        Thread newThread = new Thread(r, threadName);
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
