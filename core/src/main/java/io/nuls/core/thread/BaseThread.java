package io.nuls.core.thread;

import io.nuls.core.utils.log.Log;

/**
 * @author Niels
 */
public class BaseThread extends Thread {
    private short moduleId;
    private String poolName;

    public BaseThread() {
        super();
    }

    public BaseThread(Runnable target) {
        super(target);
    }

    public BaseThread(ThreadGroup group, Runnable target) {
        super(group, target);
    }

    public BaseThread(String name) {
        super(name);
    }

    public BaseThread(ThreadGroup group, String name) {
        super(group, name);
    }

    public BaseThread(Runnable target, String name) {
        super(target, name);
    }

    public BaseThread(ThreadGroup group, Runnable target, String name) {
        super(group, target, name);
    }

    public BaseThread(ThreadGroup group, Runnable target, String name, long stackSize) {
        super(group, target, name, stackSize);
    }

    @Override
    public synchronized final void start() {
        this.beforeStart();
        super.start();
        this.afterStart();
    }

    protected void beforeStart() {
        //default do nothing
    }

    @Override
    public final void run() {
        this.beforeRun();
        boolean ok = true;
        try {
            super.run();
        } catch (Exception e) {
            ok = false;
            runException(e);
        }
        if(ok){
            this.afterRun();
        }

    }

    protected void runException(Exception e) {
        Log.error(e);
    }

    @Override
    public final void interrupt() {
        this.beforeInterrupt();
        super.interrupt();
        this.afterInterrupt();
    }

    protected void afterStart() {
        //default do nothing
    }

    protected void afterRun() {
        //default do nothing
    }

    protected void beforeRun() {
        //default do nothing
    }

    protected void afterInterrupt() {
        //default do nothing
    }

    protected void beforeInterrupt() {
        //default do nothing
    }

    public short getModuleId() {
        return moduleId;
    }

    public void setModuleId(short moduleId) {
        this.moduleId = moduleId;
    }

    public String getPoolName() {
        return poolName;
    }

    public void setPoolName(String poolName) {
        this.poolName = poolName;
    }
}
