package io.nuls.network.netty.report;

import io.netty.util.internal.PlatformDependent;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.thread.manager.TaskManager;
import io.nuls.network.constant.NetworkConstant;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicLong;

public class PlatformDepedentReporter extends Thread {

    private AtomicLong directMemory = null;

    public void init() {
        Field[] fields = PlatformDependent.class.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {

            fields[i].setAccessible(true);

            String fieldName = fields[i].getName();
            if("DIRECT_MEMORY_COUNTER".equals(fieldName)) {
                try {
                    directMemory = (AtomicLong) fields[i].get(PlatformDependent.class);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void start() {
        TaskManager.createAndRunThread(NetworkConstant.NETWORK_MODULE_ID, "netty-memory-report", this);
    }

    @Override
    public void run() {
        while(true) {
            doReport();
            try {
                Thread.sleep(5000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(1!=1) {
                break;
            }
        }
    }

    private void doReport() {
        if(directMemory != null)
            Log.info("======== : " + directMemory.get());
    }
}
