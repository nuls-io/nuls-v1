/*
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
 *
 */
package io.nuls.kernel.func;

import io.nuls.core.tools.json.JSONUtils;
import io.nuls.core.tools.log.Log;
import io.nuls.core.tools.network.RequestUtil;
import io.nuls.kernel.thread.manager.TaskManager;

import java.util.Map;

/**
 * 时间服务类：用于同步网络标准时间
 * Time service class:Used to synchronize network standard time.
 *
 * @author vivi
 * @date 2017/11/21
 */
public class TimeService implements Runnable {

    private static TimeService INSTANCE = new TimeService();

    /**
     * 初始化时间服务器列表
     * Initialize the time server list.
     */
    //todo
    private TimeService() {
        webTimeUrl = "http://time.inchain.org/now";
        start();
    }

    public static TimeService getInstance() {
        return INSTANCE;
    }

    /**
     * 选择的时间服务器访问地址
     * Select the time server to access the address.
     */
    private String webTimeUrl;

    /**
     * 时间偏移差距触发点，超过该值会导致本地时间重设，单位毫秒
     * Time migration gap trigger point, which can cause local time reset, unit milliseconds.
     **/
    public static final long TIME_OFFSET_BOUNDARY = 3000L;

    /**
     * 重新同步时间间隔
     * Resynchronize the interval.
     */
    private static final long NET_REFRESH_TIME = 10 * 60 * 1000L;   // 10 minutes;

    /**
     * 网络时间偏移值
     */
    private static long netTimeOffset;

    /**
     * 上次同步时间点
     * The last synchronization point.
     */
    private static long lastSyncTime;

    /**
     * 启动时间同步线程
     * Start the time synchronization thread.
     */
    public void start() {
        Log.debug("----------- network timeService start -------------");
        syncWebTime();
        TaskManager.createAndRunThread((short) 1, "TimeService", this, true);
    }

    /**
     * 同步网络时间
     * Synchronous network time
     */
    private void syncWebTime() {
        try {
            long localBeforeTime = System.currentTimeMillis();

            String response = RequestUtil.doGet(webTimeUrl, "utf-8");
            Map<String, Object> resMap = JSONUtils.json2map(response);
            long netTime = (long) resMap.get("time");

            long localEndTime = System.currentTimeMillis();

            netTimeOffset = (netTime + (localEndTime - localBeforeTime) / 2) - localEndTime;

            lastSyncTime = currentTimeMillis();
        } catch (Exception e) {
            // 1 minute later try again
            Log.error("sync net time error : " + e.getMessage());
            lastSyncTime = lastSyncTime + 60000L;
        }
    }

    /**
     * 循环调用同步网络时间方法
     * Loop call synchronous network time method.
     */
    @Override
    public void run() {
        long lastTime = System.currentTimeMillis();
        while (true) {
            long newTime = System.currentTimeMillis();

            if (Math.abs(newTime - lastTime) > TIME_OFFSET_BOUNDARY) {
                Log.debug("local time changed ：{}", newTime - lastTime);
                syncWebTime();

            } else if (currentTimeMillis() - lastSyncTime > NET_REFRESH_TIME) {
                //每隔一段时间更新网络时间
                syncWebTime();
            }
            lastTime = newTime;
            try {
                Thread.sleep(500L);
            } catch (InterruptedException e) {

            }
        }
    }

    /**
     * 获取当前网络时间毫秒数
     * Gets the current network time in milliseconds.
     * @return
     */
    public static long currentTimeMillis() {
        return System.currentTimeMillis() + netTimeOffset;
    }
    /**
     * 获取当前网络时间秒数
     * Gets the current network time in seconds.
     * @return
     */
    public static long currentTimeSeconds() {
        return currentTimeMillis() / 1000;
    }

    /**
     * 获取网络时间偏移值
     * Gets the network time offset.
     * @return
     */
    public static long getNetTimeOffset() {
        return netTimeOffset;
    }


    /**
     * 停止同步网络时间、并且停止服务
     * Stop synchronizing network time and stop service.
     */
    public void shutdown() {
       //todo
    }

}
