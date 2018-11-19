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

import io.nuls.core.tools.log.Log;
import io.nuls.kernel.thread.manager.TaskManager;
import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

import java.net.InetAddress;
import java.text.SimpleDateFormat;
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
//        urlList.add("sgp.ntp.org.cn");
        urlList.add("cn.ntp.org.cn");
        urlList.add("time1.apple.com");
        urlList.add("ntp3.aliyun.com");
        urlList.add("ntp5.aliyun.com");
        urlList.add("us.ntp.org.cn");
        urlList.add("kr.ntp.org.cn");
//        urlList.add("de.ntp.org.cn");
//        urlList.add("jp.ntp.org.cn");
        urlList.add("ntp7.aliyun.com");
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
        int count = 0;
        long sum = 0L;

        for (int i = 0; i < urlList.size(); i++) {
            long localBeforeTime = System.currentTimeMillis();
            long netTime = getWebTime(urlList.get(i));

            if (netTime == 0) {
                continue;
            }

            long localEndTime = System.currentTimeMillis();

            long value = (netTime + (localEndTime - localBeforeTime) / 2) - localEndTime;
            count++;
            sum += value;
        }

        if (count > 0) {
            netTimeOffset = sum / count;
        }
        lastSyncTime = currentTimeMillis();
    }

    /**
     * 获取网络时间
     * todo 可优化为哪个地址延迟小使用哪个
     *
     * @return long
     */
    private long getWebTime(String address) {
        try {
            NTPUDPClient client = new NTPUDPClient();
            client.open();
            client.setDefaultTimeout(1000);
            client.setSoTimeout(1000);
            InetAddress inetAddress = InetAddress.getByName(address);
//            Log.debug("start ask time....");
            TimeInfo timeInfo = client.getTime(inetAddress);
//            Log.debug("done!");
            return timeInfo.getMessage().getTransmitTimeStamp().getTime();
        } catch (Exception e) {
            return 0L;
        }
    }

    /**
     * 启动时间同步线程
     * Start the time synchronization thread.
     */
    public void start() {
        Log.debug("----------- TimeService start -------------");
        TaskManager.createAndRunThread((short) 1, "TimeService", this, true);
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
     * @return long
     */
    public static long currentTimeMillis() {
        return System.currentTimeMillis() + netTimeOffset;
    }

    /**
     * 获取网络时间偏移值
     * Gets the network time offset.
     *
     * @return long
     */
    public static long getNetTimeOffset() {
        return netTimeOffset;
    }
}
