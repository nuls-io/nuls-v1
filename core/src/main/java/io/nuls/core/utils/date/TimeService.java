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
package io.nuls.core.utils.date;

import com.fasterxml.jackson.databind.util.JSONPObject;
import io.nuls.core.thread.manager.TaskManager;
import io.nuls.core.utils.json.JSONUtils;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.network.RequestUtil;
import io.nuls.core.utils.str.StringUtils;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author vivi
 * @date 2017/11/21
 */
public class TimeService implements Runnable {

    private static TimeService INSTANCE = new TimeService();

    private TimeService() {
        webTimeUrl = "http://time.inchain.org/now";
        READ_WRITE_LOCK = new ReentrantReadWriteLock();
        start();
    }

    public static TimeService getInstance() {
        return INSTANCE;
    }

    private String webTimeUrl;

    /**
     * 时间偏移差距触发点，超过该值会导致本地时间重设，单位毫秒
     **/
    public static final long TIME_OFFSET_BOUNDARY = 1000L;

    private static final long NET_REFRESH_TIME = 10 * 60 * 1000L;   // 10 minutes;

    public static final long ONE_HOUR = 3600 * 1000L;

    /**
     * 网络时间偏移值
     */
    private static long netTimeOffset;

    private static long lastSyncTime;

    private boolean running;

    private static ReentrantReadWriteLock READ_WRITE_LOCK;

    public void start() {
        Log.info("----------- network timeService start -------------");
        syncWebTime();
        running = true;
        TaskManager.createAndRunThread((short) 1, "TimeService", this, true);
    }

    private void syncWebTime() {
        READ_WRITE_LOCK.writeLock().lock();
        try {
            long localBeforeTime = System.currentTimeMillis();

//            URL url = new URL(webTimeUrl);
//            URLConnection connection = url.openConnection();
//            connection.connect();
//            long netTime = connection.getDate();

            String response = RequestUtil.doGet(webTimeUrl, "utf-8");
            Map<String, Object> resMap = JSONUtils.json2map(response);
            long netTime = (long) resMap.get("time");

            long localEndTime = System.currentTimeMillis();

            netTimeOffset = (netTime + (localEndTime - localBeforeTime) / 2) - localEndTime;

            lastSyncTime = localEndTime;
        } catch (Exception e) {
            // 1 minute later try again
            lastSyncTime = lastSyncTime + 60000L;
        } finally {
            READ_WRITE_LOCK.writeLock().unlock();
        }
    }

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

    public static long currentTimeMillis() {
        READ_WRITE_LOCK.readLock().lock();
        try {
            return System.currentTimeMillis() + netTimeOffset;
        } finally {
            READ_WRITE_LOCK.readLock().unlock();
        }
    }

    public static long currentTimeSeconds() {
        return currentTimeMillis() / 1000;
    }

    public static long getNetTimeOffset() {
        return netTimeOffset;
    }

    public void shutdown() {
        this.running = false;
    }

}
