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
        this.beforeStart();
        super.start();
        this.afterStart();
    }

    protected void beforeStart() {
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

    protected void afterStart() {
        //todo
        System.out.println("afterStart");
    }

    protected void afterRun() {
        //todo
        System.out.println("afterRun");
    }

    protected void beforeRun() {
        //todo
        System.out.println("beforeRun");
    }

    protected void afterInterrupt() {
        //todo
        System.out.println("afterInterrupt");
    }

    protected void beforeInterrupt() {
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
