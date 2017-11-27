package io.nuls.core.thread.manager;

import io.nuls.core.thread.BaseThread;
import io.nuls.core.utils.aop.AopUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @author Niels
 */
public class ThreadManager {
    private static final int DEFAULT_QUEUE_MAX_SIZE = Integer.MAX_VALUE;

    private static final Map<String, ThreadPoolExecutor> POOL_EXECUTOR_MAP = new HashMap<>();
    private static final String TEMPORARY_THREAD_POOL_NAME = "temporary";
    private static final int TEMPORARY_THREAD_POOL_COUNT = 10;
    private static final int TEMPORARY_THREAD_POOL_QUEUE_SIZE = 1000;

    /**
     * Initializing a temporary thread pool
     */
    static {
        createThreadPool(TEMPORARY_THREAD_POOL_COUNT, TEMPORARY_THREAD_POOL_QUEUE_SIZE, new NulsDefaultThreadFactory((short) 0,TEMPORARY_THREAD_POOL_NAME));
    }

    public static final ThreadPoolExecutor createThreadPool(int threadCount, int queueSize, NulsDefaultThreadFactory factory) {
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
        POOL_EXECUTOR_MAP.put(factory.getPoolName(), pool);
        return pool;
    }

    public static final ScheduledThreadPoolExecutor createScheduledThreadPool(int threadCount, NulsDefaultThreadFactory factory) {
        if (factory == null) {
            throw new RuntimeException("thread factory cannot be null!");
        }
        ScheduledThreadPoolExecutor pool = AopUtils.createProxy(ScheduledThreadPoolExecutor.class, new Class[]{int.class, ThreadFactory.class}, new Object[]{threadCount, factory}, new ThreadPoolInterceiptor());
        POOL_EXECUTOR_MAP.put(factory.getPoolName(), pool);
        return pool;
    }

    public static final ScheduledThreadPoolExecutor createScheduledThreadPool(NulsDefaultThreadFactory factory) {
        return createScheduledThreadPool(1, factory);
    }

    public static final void asynExecuteRunnable(Runnable runnable) {
        if(null==runnable){
            throw new RuntimeException("runnable is null");
        }
        ThreadPoolExecutor pool = POOL_EXECUTOR_MAP.get(TEMPORARY_THREAD_POOL_NAME);
        if (pool == null) {
            throw new RuntimeException("temporary thread pool not initialized yet");
        }
        pool.execute(runnable);
    }

    public static final void createSingleThreadAndRun(short moduleId,String threadName,Runnable runnable){
        NulsDefaultThreadFactory factory = new NulsDefaultThreadFactory(moduleId,threadName);
        Thread thread = factory.newThread(runnable);
        thread.start();
    }

    public static final void createSingleThreadAndRun(short moduleId,String threadName,Runnable runnable,boolean deamon){
        NulsDefaultThreadFactory factory = new NulsDefaultThreadFactory(moduleId,threadName);
        Thread thread = factory.newThread(runnable);
        thread.setDaemon(deamon);
        thread.start();
    }

    public static final List<BaseThread> getThreadList(short moduleId){
        //todo
        return null;
    }

    public static final String getThreadPoolsInfoString(short moduleId){
        //todo
        return null;
    }

    public static final Thread.State getThreadStateByName(String name){
        //todo
        return null;
    }

    public static void shutdownByModuleId(short moduleId){
        //todo 判断当前状态、停止、清除
    }
}
