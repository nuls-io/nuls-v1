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
package io.nuls.core.thread.cache;

import io.nuls.core.thread.BaseThread;

import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author Niels
 * @date 2017/11/27
 */
public class TaskTable {

    private static final TaskTable INSTANCE = new TaskTable();
    /**
     * key  :  poolName
     * value:  pool
     */
    private final Map<String, ThreadPoolExecutor> POOL_EXECUTOR_MAP = new HashMap<>();
    /**
     * key  :  moduleId
     * value:  poolName
     */
    private final Map<Short, Set<String>> MODULE_POOL_MAP = new HashMap<>();
    /**
     * key  :  threadName
     * value:  thread
     */
    private final Map<String, BaseThread> THREAD_MAP = new HashMap<>();
    /**
     * key  :  moduleId
     * value:  threadName
     */
    private final Map<Short, Set<String>> MODULE_THREAD_MAP = new HashMap<>();
    /**
     * key  :  poolName
     * value:  Set<threadName>
     */
    private final Map<String, Set<String>> POOL_THREAD_MAP = new HashMap<>();

    private TaskTable() {
    }

    public static final TaskTable getInstance() {
        return INSTANCE;
    }

    public final void putPool(short moduleId, String poolName, ThreadPoolExecutor pool) {
        POOL_EXECUTOR_MAP.put(poolName, pool);
        Set<String> set = MODULE_POOL_MAP.get(moduleId);
        if (null == set) {
            set = new HashSet<>();
        }
        set.add(poolName);
        MODULE_POOL_MAP.put(moduleId, set);
    }

    public final void putThread(short moduleId, String poolName, String threadName, BaseThread thread) {
        THREAD_MAP.put(threadName, thread);
        Set<String> set1 = MODULE_THREAD_MAP.get(moduleId);
        if (null == set1) {
            set1 = new HashSet<>();
        }
        set1.add(threadName);
        MODULE_THREAD_MAP.put(moduleId, set1);
        Set<String> set = POOL_THREAD_MAP.get(poolName);
        if (null == set) {
            set = new HashSet<>();
        }
        set.add(threadName);
        POOL_THREAD_MAP.put(poolName, set);
    }

    public final BaseThread getThread(String threadName) {
        return THREAD_MAP.get(threadName);
    }

    public final ThreadPoolExecutor getPool(String poolName) {
        return POOL_EXECUTOR_MAP.get(poolName);
    }

    public final List<BaseThread> getThreadList(short moduleId) {
        Set<String> set = MODULE_THREAD_MAP.get(moduleId);
        if (null == set) {
            return null;
        }
        List<BaseThread> list = new ArrayList<>();
        for (String threadName : set) {
            list.add(THREAD_MAP.get(threadName));
        }
        return list;
    }

    public final List<BaseThread> getThreadList(String poolName) {
        Set<String> set = POOL_THREAD_MAP.get(poolName);
        if (null == set) {
            return null;
        }
        List<BaseThread> list = new ArrayList<>();
        for (String threadName : set) {
            list.add(THREAD_MAP.get(threadName));
        }
        return list;
    }

    public final List<ThreadPoolExecutor> getPoolList(short moduleId) {
        Set<String> set = MODULE_POOL_MAP.get(moduleId);
        if (null == set) {
            return null;
        }
        List<ThreadPoolExecutor> list = new ArrayList<>();
        for (String threadName : set) {
            list.add(POOL_EXECUTOR_MAP.get(threadName));
        }
        return list;
    }

    public void remove(short moduleId) {
        Set<String> threadNameSet = MODULE_THREAD_MAP.get(moduleId);
        for (String name : threadNameSet) {
            BaseThread thread = THREAD_MAP.get(name);
            thread.interrupt();
            THREAD_MAP.remove(name);
        }
        MODULE_THREAD_MAP.remove(moduleId);
        Set<String> poolNameSet = MODULE_POOL_MAP.get(moduleId);
        for (String name : poolNameSet) {
            ThreadPoolExecutor pool = POOL_EXECUTOR_MAP.get(name);
            pool.shutdown();
            POOL_EXECUTOR_MAP.remove(name);
            POOL_THREAD_MAP.remove(name);
        }
        MODULE_POOL_MAP.remove(moduleId);

    }

    public void removeThread(short moduleId,String threadName) {
        THREAD_MAP.remove(threadName);
        Set<String> set = MODULE_THREAD_MAP.get(moduleId);
        set.remove(threadName);
        for(Set<String> tset:POOL_THREAD_MAP.values()){
            tset.remove(threadName);
        }
    }
}
