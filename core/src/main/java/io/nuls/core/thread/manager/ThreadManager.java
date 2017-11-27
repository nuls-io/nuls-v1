package io.nuls.core.thread.manager;

import io.nuls.core.constant.ErrorCode;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.thread.NulsThreadFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Niels
 * @date 2017/11/24
 */
public class ThreadManager {
    private static final int DEFAULT_QUEUE_MAX_SIZE = Integer.MAX_VALUE;

    private static final Map<String, ThreadPoolExecutor> POOL_EXECUTOR_MAP = new HashMap<>();

    public static final ThreadPoolExecutor createThreadPool(int threadCount, int queueSize, NulsThreadFactory factory) {
        if (threadCount == 0) {
            throw new NulsRuntimeException(ErrorCode.FAILED, "thread count cannot be 0!");
        }
        if(factory == null){
            throw new NulsRuntimeException(ErrorCode.FAILED, "thread factory cannot be null!");
        }
        if (queueSize == 0) {
            queueSize = DEFAULT_QUEUE_MAX_SIZE;
        }
        ThreadPoolExecutor pool = new ThreadPoolExecutor(threadCount, threadCount, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(queueSize), factory);
        POOL_EXECUTOR_MAP.put(factory.getPoolName(), pool);
        return pool;
    }


}
