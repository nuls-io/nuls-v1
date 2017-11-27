package io.nuls.core.thread.manager;

import io.nuls.core.thread.BaseThread;
import io.nuls.core.thread.data.ThreadData;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Niels
 * @date 2017/11/24
 */
public class NulsThreadFactory implements ThreadFactory {
    private final short moduleId;
    private final String poolName;
    private AtomicInteger threadNo = new AtomicInteger(1);
    private static final ThreadData THREAD_DATA = ThreadData.getInstance();
    public NulsThreadFactory(short moduleId, String poolName) {
        this.poolName = poolName;
        this.moduleId = moduleId;
    }

    @Override
    public final Thread newThread(Runnable r) {
        String threadName = "[" + poolName + "-" + threadNo.getAndIncrement() + "]";
        BaseThread newThread = new BaseThread(r, threadName);
        newThread.setModuleId(moduleId);
        newThread.setPoolName(poolName);
        newThread.setDaemon(true);
        if (newThread.getPriority() != Thread.NORM_PRIORITY) {
            newThread.setPriority(Thread.NORM_PRIORITY);
        }
        THREAD_DATA.putThread(moduleId,poolName,threadName,newThread);
        return newThread;
    }

    public String getPoolName() {
        return poolName;
    }

    public short getModuleId() {
        return moduleId;
    }
}
