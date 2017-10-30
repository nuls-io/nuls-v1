package io.nuls.network.service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Date;

//import io.nuls.util.RequestUtil;
//import io.nuls.util.json.JSONUtils;


public final class TimeService {


    /** 时间偏移差距触发点，超过该值会导致本地时间重设，单位毫秒 **/
    public static final long TIME_OFFSET_BOUNDARY = 1000l;

    /*
     * 网络时间刷新间隔
     */
    private static final long TIME_REFRESH_TIME = 600000l;

    /*
     * 模拟网络时钟
     */
    private volatile static Date mockTime;
    /*
     * 网络时间与本地时间的偏移
     */
    private static long netTimeOffset;
    //网络时间是否通过第一种方式进行了同步，如果没有，则通过第二种方式同步
    private static boolean netTimeHasSync;

    /*
     * 系统启动时间
     */
    private final static Date systemStartTime = new Date();

    private long lastInitTime;
    private boolean running;

    @PostConstruct
    public void start() {
        startSyn();
    }

    @PreDestroy
    public void stop() {
        running = false;
    }


    /**
     * 异步启动时间服务
     */
    private void startSyn() {
        /*
        initTime();
        Thread monitorThread = new Thread() {
            @Override
            public void run() {
                monitorTimeChange();
            }
        };
        monitorThread.setName("time change monitor");
        monitorThread.start();
        */
    }
    /*
        private void initTime() {

            String timeServiceUrl = "http://time.inchain.org/now";

            long nowTime = System.currentTimeMillis();

            String res = RequestUtil.doGet(timeServiceUrl, null);
            try {
                Map<String,Object> resultJson = JSONUtils.json2map(res);
                if(resultJson.get("success").equals("true")) {
                    long netTime = Long.parseLong(resultJson.get("time").toString());

                    long newLocalNowTime = System.currentTimeMillis();

                    netTimeOffset = (netTime + (newLocalNowTime - nowTime) / 2) - System.currentTimeMillis();

                    initSuccess();
                } else {
                    initError();
                }
            } catch (Exception e) {
                initError();
            }
        }

        private void initError() {
            //1分钟后重试
            lastInitTime = System.currentTimeMillis() - 60000l;

        }

        private void initSuccess() {
            lastInitTime = System.currentTimeMillis();
            if(!netTimeHasSync) {
                netTimeHasSync = true;
            }
        }

*/
    public void monitorTimeChange() {
    }
    /**
     * 以给定的秒数推进（或倒退）网络时间
     */
    public static Date rollMockClock(int seconds) {
        return rollMockClockMillis(seconds * 1000);
    }

    /**
     * 以给定的毫秒数推进（或倒退）网络时间
     */
    public static Date rollMockClockMillis(long millis) {
        if (mockTime == null)
            throw new IllegalStateException("You need to use setMockClock() first.");
        mockTime = new Date(mockTime.getTime() + millis);
        return mockTime;
    }

    /**
     * 将网络时间设置为当前时间
     */
    public static void setMockClock() {
        mockTime = new Date();
    }

    /**
     * 将网络时间设置为给定时间（以秒为单位）
     */
    public static void setMockClock(long mockClockSeconds) {
        mockTime = new Date(mockClockSeconds * 1000);
    }

    /**
     * 当前网络时间
     * @return long
     */
    public static Date now() {
        return mockTime != null ? mockTime : new Date();
    }

    /**
     * 当前毫秒时间
     * @return long
     */
    public static long currentTimeMillis() {
        return System.currentTimeMillis() + netTimeOffset;
    }

    /**
     * 当前时间秒数
     * @return long
     */
    public static long currentTimeSeconds() {
        return currentTimeMillis() / 1000;
    }

    /**
     * 获取系统运行时长（返回毫秒数）
     * @return long
     */
    public static long getSystemRuningTimeMillis() {
        return new Date().getTime() - systemStartTime.getTime();
    }

    /**
     * 获取系统运行时长（返回秒数）
     * @return long
     */
    public static long getSystemRuningTimeSeconds() {
        return getSystemRuningTimeMillis() / 1000;
    }

    public static void setNetTimeOffset(long netTimeOffset) {
        if(!netTimeHasSync) {
            TimeService.netTimeOffset = netTimeOffset;
        }
    }
    public static long getNetTimeOffset() {
        return netTimeOffset;
    }

}
