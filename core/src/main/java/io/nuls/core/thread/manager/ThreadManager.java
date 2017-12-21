package io.nuls.core.thread.manager;

import io.nuls.core.thread.BaseThread;
import io.nuls.core.thread.cache.TaskManager;
import io.nuls.core.utils.aop.AopUtils;

import java.util.List;
import java.util.concurrent.*;

/**
 * @author Niels
 */
public class ThreadManager {
    private static final int DEFAULT_QUEUE_MAX_SIZE = Integer.MAX_VALUE;


    private static final TaskManager THREAD_DATA_CACHE = TaskManager.getInstance();

    private static final String TEMPORARY_THREAD_POOL_NAME = "temporary";
    private static final int TEMPORARY_THREAD_POOL_COUNT = 10;
    private static final int TEMPORARY_THREAD_POOL_QUEUE_SIZE = 1000;
    private static final ThreadPoolExecutor TEMPORARY_THREAD_POOL;

    /**
     * Initializing a temporary thread pool
     */
    static {
        TEMPORARY_THREAD_POOL = createThreadPool(TEMPORARY_THREAD_POOL_COUNT, TEMPORARY_THREAD_POOL_QUEUE_SIZE, new NulsThreadFactory((short) 0, TEMPORARY_THREAD_POOL_NAME));
    }

    public static final ThreadPoolExecutor createThreadPool(int threadCount, int queueSize, NulsThreadFactory factory) {
        if (threadCount == 0) {
            throw new RuntimeException("thread count cannot be 0!");
        }
        if (factory == null) {
            throw new RuntimeException("thread factory cannot be null!");
        }
        if (queueSize == 0) {
            queueSize = DEFAULT_QUEUE_MAX_SIZE;
        }

        Class[] paramClasses = new Class[]{int.class, int.class, long.class, TimeUnit.class, BlockingQueue.class, ThreadFactory.class};
        Object[] paramArgs = new Object[]{threadCount, threadCount, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(queueSize), factory};
        ThreadPoolExecutor pool = AopUtils.createProxy(ThreadPoolExecutor.class, paramClasses, paramArgs, new ThreadPoolInterceiptor());
        THREAD_DATA_CACHE.putPool(factory.getModuleId(), factory.getPoolName(), pool);
        return pool;
    }

    public static final ScheduledThreadPoolExecutor createScheduledThreadPool(int threadCount, NulsThreadFactory factory) {
        if (factory == null) {
            throw new RuntimeException("thread factory cannot be null!");
        }
        ScheduledThreadPoolExecutor pool = AopUtils.createProxy(ScheduledThreadPoolExecutor.class, new Class[]{int.class, ThreadFactory.class}, new Object[]{threadCount, factory}, new ThreadPoolInterceiptor());
        THREAD_DATA_CACHE.putPool(factory.getModuleId(), factory.getPoolName(), pool);
        return pool;
    }

    public static final ScheduledThreadPoolExecutor createScheduledThreadPool(NulsThreadFactory factory) {
        return createScheduledThreadPool(1, factory);
    }

    public static final void asynExecuteRunnable(Runnable runnable) {
        if (null == runnable) {
            throw new RuntimeException("runnable is null");
        }
        if (TEMPORARY_THREAD_POOL == null) {
            throw new RuntimeException("temporary thread pool not initialized yet");
        }
        TEMPORARY_THREAD_POOL.execute(runnable);
    }

    public static final void createSingleThreadAndRun(short moduleId, String threadName, Runnable runnable) {
        createSingleThreadAndRun(moduleId,threadName,runnable,true);
    }

    public static final void createSingleThreadAndRun(short moduleId, String threadName, Runnable runnable, boolean deamon) {
        NulsThreadFactory factory = new NulsThreadFactory(moduleId, threadName);
        Thread thread = factory.newThread(runnable);
        thread.setDaemon(deamon);
        thread.start();
    }

    public static final List<BaseThread> getThreadList(short moduleId) {
        return THREAD_DATA_CACHE.getThreadList(moduleId);
    }

    public static final String getThreadPoolsInfoString(short moduleId) {
        //todo
        return null;
    }

    public static final Thread.State getThreadStateByName(String name) {
        //todo
        return null;
    }

    public static void shutdownByModuleId(short moduleId) {
        List<ThreadPoolExecutor> poolList = THREAD_DATA_CACHE.getPoolList(moduleId);
        if (null != poolList) {
            for (ThreadPoolExecutor pool : poolList) {
                pool.shutdown();
            }
        }
        List<BaseThread> threadList = THREAD_DATA_CACHE.getThreadList(moduleId);
        if (null != threadList) {
            for (Thread thread : threadList) {
                if (thread.getState() == Thread.State.RUNNABLE) {
                    thread.interrupt();
                }
            }
        }
        THREAD_DATA_CACHE.remove(moduleId);
    }
}
