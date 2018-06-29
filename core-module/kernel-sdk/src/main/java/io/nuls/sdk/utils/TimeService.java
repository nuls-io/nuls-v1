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

package io.nuls.sdk.utils;

import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * 时间服务类：用于同步网络标准时间
 * Time service class:Used to synchronize network standard time.
 *
 * @author vivi
 */
public class TimeService implements Runnable {

    private TimeService() {
        urlList.add("http://time.inchain.org");         //inchain
        urlList.add("https://www.baidu.com");           //baidu
        urlList.add("https://www.alibaba.com");         //alibaba
        urlList.add("https://github.com/");             //github
//        syncWebTime();
    }

    private static TimeService instance = new TimeService();

    public static TimeService getInstance() {
        return instance;
    }

    /**
     * 网站url集合，用于同步网络时间
     */
    private List<String> urlList = new ArrayList<>();

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
     * 同步网络时间
     */
    private void syncWebTime() {
        long localBeforeTime = System.currentTimeMillis();

        long netTime = getWebTime();

        long localEndTime = System.currentTimeMillis();

        netTimeOffset = (netTime + (localEndTime - localBeforeTime) / 2) - localEndTime;

        lastSyncTime = currentTimeMillis();
    }

    /**
     * 获取网络时间
     * 连接公用网站，获取对方的网络时间，
     * 有一个获取成功就立刻返回
     *
     * @return
     */
    private long getWebTime() {
        for (int i = 0; i < urlList.size(); i++) {
            try {
                URL url = new URL(urlList.get(i));
                URLConnection conn = url.openConnection();
                conn.connect();
                long time = conn.getDate();
                return time;
            } catch (Exception e) {
                // try to connect next
                continue;
            }
        }
        return 0;
    }

    /**
     * 启动时间同步线程
     * Start the time synchronization thread.
     */
    public void start() {
        Log.debug("----------- TimeService start -------------");
        Thread thread = new Thread(this);
        thread.run();
    }

    /**
     * 循环调用同步网络时间方法
     * Loop call synchronous network time method.
     */
    @Override
    public void run() {
        long lastTime = System.currentTimeMillis();
        syncWebTime();
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
     *
     * @return
     */
    public static long currentTimeMillis() {
        return System.currentTimeMillis() + netTimeOffset;
    }

    /**
     * 获取网络时间偏移值
     * Gets the network time offset.
     *
     * @return
     */
    public static long getNetTimeOffset() {
        return netTimeOffset;
    }
}
