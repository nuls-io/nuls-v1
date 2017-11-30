package io.nuls.network.service.impl;

import io.nuls.core.thread.manager.ThreadManager;
import io.nuls.core.utils.log.Log;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author vivi
 * @date 2017/11/21
 */
public class TimeService implements Runnable {

    private static TimeService INSTANCE = new TimeService();

    private TimeService() {
        webTimeUrl = "http://www.baidu.com";
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

    /**
     * 网络时间偏移值
     */
    private static long netTimeOffset;

    private static long lastSyncTime;

    private boolean running;


    public void start() {
        Log.info("----------- network timeService start -------------");
        syncWebTime();
        running = true;
        ThreadManager.createSingleThreadAndRun((short) 1, "TimeService", this, true);
    }

    private void syncWebTime() {
        try {
            long localBeforeTime = System.currentTimeMillis();
            URL url = new URL(webTimeUrl);
            URLConnection connection = url.openConnection();
            connection.connect();
            long netTime = connection.getDate();

            long localEndTime = System.currentTimeMillis();

            netTimeOffset = (netTime + (localEndTime - localBeforeTime) / 2) - localEndTime;

            lastSyncTime = localEndTime;
        } catch (IOException e) {
            // 1 minute later try again
            lastSyncTime = lastSyncTime + 60000L;
        }
    }

    @Override
    public void run() {
        long lastTime = System.currentTimeMillis();
        while (true) {
            long newTime = System.currentTimeMillis();

            if (Math.abs(newTime - lastTime) > TIME_OFFSET_BOUNDARY) {
                Log.info("local time changed ：{}", newTime - lastTime);
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
        return System.currentTimeMillis() + netTimeOffset;
    }

    public static long currentTimeSeconds() {
        return currentTimeMillis() / 1000;
    }


    public void shutdown() {
        this.running = false;
    }

}
