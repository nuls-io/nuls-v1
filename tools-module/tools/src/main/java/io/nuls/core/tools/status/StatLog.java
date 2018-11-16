package io.nuls.core.tools.status;

import io.nuls.core.tools.log.MessageLog;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class StatLog {

    private static Map<String, AtomicInteger> countMap = new HashMap<>();
    private static Map<String, AtomicLong> timesMap = new HashMap<>();

    static {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(10000L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    for (String name : countMap.keySet()) {
                        MessageLog.debug("~~~~~~:" + name + "::::" + countMap.get(name) + "::::" + timesMap.get(name));
                    }
                }
            }
        });
        t.start();
    }

    private static void init(String name) {
        countMap.put(name, new AtomicInteger(0));
        timesMap.put(name, new AtomicLong(0));
    }

    public static void sum(String name, long times) {
        AtomicInteger count = countMap.get(name);
        if (null == count) {
            init(name);
            count = countMap.get(name);
        }
        AtomicLong time = timesMap.get(name);
        count.addAndGet(1);
        time.addAndGet(times);
    }


}
