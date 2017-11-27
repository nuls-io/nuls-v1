package io.nuls.core.thread;

public  class BaseThread extends Thread{
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
        super.start();
        this.afterStart();
    }

    @Override
    public final void run() {
        this.beforeRun();
        try{
            super.run();
        }finally {
            this.afterRun();
        }
    }

    @Override
    public final void interrupt() {
        this.beforeInterrupt();
        super.interrupt();
        this.afterInterrupt();
    }

    private void afterStart() {
        //todo
        System.out.println("afterStart");
    }

    private void afterRun() {
        //todo
        System.out.println("afterRun");
    }

    private void beforeRun() {
        //todo
        System.out.println("beforeRun");
    }

    private void afterInterrupt() {
        //todo
        System.out.println("afterInterrupt");
    }

    private void beforeInterrupt() {
        //todo
        System.out.println("beforeInterrupt");
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
