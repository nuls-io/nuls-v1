/**
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.core.utils.queue.manager;

import io.nuls.core.constant.ErrorCode;
import io.nuls.core.constant.NulsConstant;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.module.manager.ServiceManager;
import io.nuls.core.thread.manager.NulsThreadFactory;
import io.nuls.core.thread.manager.TaskManager;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.queue.entity.StatInfo;
import io.nuls.core.utils.queue.fqueue.exception.FileFormatException;
import io.nuls.core.utils.queue.intf.AbstractNulsQueue;
import io.nuls.core.utils.queue.thread.StatusLogThread;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 持久化队列管理器
 *
 * @author Niels
 * @date 2017/9/20
 */
public final class QueueManager {
    private static final Map<String, AbstractNulsQueue> QUEUES_MAP = new HashMap<>();
    private static final Map<String, Lock> LOCK_MAP = new HashMap<>();
    //统计日志时间段
    private static final int LATELY_SECOND = 10;

    private static boolean Running = false;

    public static void logQueueStatus() {
        for (Map.Entry<String, AbstractNulsQueue> entry : QUEUES_MAP.entrySet()) {
            try {
                AbstractNulsQueue queue = entry.getValue();
                long nowIn = queue.getStatInfo().getInCount().get();
                long nowOut = queue.getStatInfo().getOutCount().get();
                long latelyInTps = (nowIn - queue.getStatInfo().getLastInCount()) / queue.getStatInfo().getLatelySecond();
                long latelyOutTps = (nowOut - queue.getStatInfo().getLastOutCount()) / queue.getStatInfo().getLatelySecond();
                queue.getStatInfo().setLatelyInTps(latelyInTps);
                queue.getStatInfo().setLatelyOutTps(latelyOutTps);
                queue.getStatInfo().setLastInCount(nowIn);
                queue.getStatInfo().setLastOutCount(nowOut);
                Log.info(queue.getStatInfo().toString());
            } catch (Exception e) {
            }
        }
    }

    public static final int getLatelySecond() {
        return LATELY_SECOND;
    }

    /**
     * 将队列加入管理中
     *
     * @param queueName 队列名称
     * @param queue     队列实例
     */
    public static void initQueue(String queueName, AbstractNulsQueue queue) {
        initQueue(queueName, queue, LATELY_SECOND);
    }

    /**
     * 将队列加入管理中
     *
     * @param queueName    队列名称
     * @param queue        队列实例
     * @param latelySecond 统计日志时间段
     */
    public static void initQueue(String queueName, AbstractNulsQueue queue, int latelySecond) {
        if (!Running) {
            throw new NulsRuntimeException(ErrorCode.FAILED, "The DBModule is not running!");
        }
        if (QUEUES_MAP.containsKey(queueName)) {
            throw new NulsRuntimeException(ErrorCode.FAILED, "queue name is allready exist");
        }
        if (latelySecond == 0) {
            latelySecond = LATELY_SECOND;
        }
        Log.debug("队列初始化，名称：{}，单个文件最大大小：{}", queue.getQueueName(), queue.getMaxSize());
        queue.setStatInfo(new StatInfo(queue.getQueueName(), queue.size(), latelySecond));
        QUEUES_MAP.put(queueName, queue);
        LOCK_MAP.put(queueName, new ReentrantLock());
    }

    public static void destroyQueue(String queueName) throws IOException, FileFormatException {
        if (!Running) {
            throw new NulsRuntimeException(ErrorCode.FAILED, "The DBModule is not running!");
        }
        AbstractNulsQueue queue = QUEUES_MAP.get(queueName);
        if (null == queue) {
            throw new NulsRuntimeException(ErrorCode.FAILED, "queue not exist");
        }
        queue.distroy();
        QUEUES_MAP.remove(queueName);
        Log.debug("队列销毁，名称：{}。", queueName);
    }

    public static Object take(String queueName) throws InterruptedException {
        if (!Running) {
            throw new NulsRuntimeException(ErrorCode.FAILED, "The DBModule is not running!");
        }
        AbstractNulsQueue queue = QUEUES_MAP.get(queueName);
        if (null == queue) {
            throw new NulsRuntimeException(ErrorCode.FAILED, "queue not exist");
        }
        Object value = queue.take();
        queue.getStatInfo().takeOne();
        Log.debug("从队列中取出数据，名称：{}，当前长度：{}。", queueName, queue.size());
        return value;
    }

    public static Object poll(String queueName) {
        if (!Running) {
            throw new NulsRuntimeException(ErrorCode.FAILED, "The DBModule is not running!");
        }
        AbstractNulsQueue queue = QUEUES_MAP.get(queueName);
        if (null == queue) {
            throw new NulsRuntimeException(ErrorCode.FAILED, "queue not exist");
        }
        Object obj = queue.poll();
        boolean notNull = null != obj;
        if (notNull) {
            queue.getStatInfo().takeOne();
            Log.debug("从队列中取出数据，名称：{}，当前长度：{}。", queueName, queue.size());
        }
        return obj;
    }

    public static void offer(String queueName, Object item) {
        if (!Running) {
            throw new NulsRuntimeException(ErrorCode.FAILED, "The DBModule is not running!");
        }
        AbstractNulsQueue queue = QUEUES_MAP.get(queueName);
        if (null == queue) {
            throw new NulsRuntimeException(ErrorCode.FAILED, "queue not exist");
        }

        queue.offer(item);
        queue.getStatInfo().putOne();
        Log.debug("向队列中加入数据，名称：{}，当前长度：{}。", queueName, queue.size());
    }

    public static void clear(String queueName) {
        if (!Running) {
            throw new NulsRuntimeException(ErrorCode.FAILED, "The DBModule is not running!");
        }
        AbstractNulsQueue queue = QUEUES_MAP.get(queueName);
        if (null == queue) {
            throw new NulsRuntimeException(ErrorCode.FAILED, "queue not exist");
        }
        Log.debug("清空队列数据，名称：{}，当前长度：{}。", queueName, queue.size());
        queue.clear();
    }

    public static void close(String queueName) throws NulsRuntimeException {
        if (!Running) {
            throw new NulsRuntimeException(ErrorCode.FAILED, "The DBModule is not running!");
        }
        AbstractNulsQueue queue = QUEUES_MAP.get(queueName);
        if (null == queue) {
            throw new NulsRuntimeException(ErrorCode.FAILED, "queue not exist");
        }
        try {
            queue.close();
        } catch (Exception e) {
            throw new NulsRuntimeException(e);
        }
        Log.debug("关闭队列实例，名称：{}，当前长度：{}。", queueName, queue.size());
    }

    public static long size(String queueName) {
        if (!Running) {
            throw new NulsRuntimeException(ErrorCode.FAILED, "The DBModule is not running!");
        }
        AbstractNulsQueue queue = QUEUES_MAP.get(queueName);
        if (null == queue) {
            throw new NulsRuntimeException(ErrorCode.FAILED, "queue not exist");
        }
        return queue.size();
    }

    public static long getMaxSize(String queueName) {
        if (!Running) {
            throw new NulsRuntimeException(ErrorCode.FAILED, "The DBModule is not running!");
        }
        AbstractNulsQueue queue = QUEUES_MAP.get(queueName);
        if (null == queue) {
            throw new NulsRuntimeException(ErrorCode.FAILED, "queue not exist");
        }
        return queue.getMaxSize();
    }

    public static StatInfo getStatInfo(String queueName) {
        if (!Running) {
            throw new NulsRuntimeException(ErrorCode.FAILED, "The DBModule is not running!");
        }
        AbstractNulsQueue queue = QUEUES_MAP.get(queueName);
        if (null == queue) {
            throw new NulsRuntimeException(ErrorCode.FAILED, "queue not exist");
        }
        return queue.getStatInfo();
    }

    public static List<StatInfo> getAllStatInfo() {
        List<StatInfo> list = new ArrayList<>();
        for (AbstractNulsQueue queue : QUEUES_MAP.values()) {
            list.add(queue.getStatInfo());
        }
        return list;
    }

    public static void shutdown() {
        Running = false;
    }

    public static <T> void remove(String queueName, T item) {
        if (!Running) {
            throw new NulsRuntimeException(ErrorCode.FAILED, "The DBModule is not running!");
        }
        AbstractNulsQueue queue = QUEUES_MAP.get(queueName);
        if (null == queue) {
            throw new NulsRuntimeException(ErrorCode.FAILED, "queue not exist");
        }

        queue.remove(item);
        queue.getStatInfo().putOne();
        Log.debug("从队列中删除数据，名称：{}，当前长度：{}。", queueName, queue.size());
    }

    public static void start() {
        ScheduledExecutorService service = TaskManager.createScheduledThreadPool(new NulsThreadFactory(NulsConstant.MODULE_ID_MICROKERNEL, "queueStatusLogPool"));
        service.scheduleAtFixedRate(new StatusLogThread(), 0, QueueManager.getLatelySecond(), TimeUnit.SECONDS);
        Running = true;
    }

    public static boolean exist(String queueName) {
        AbstractNulsQueue queue = QUEUES_MAP.get(queueName);
        return null != queue;
    }
}
