package io.nuls.core.module.thread;

import io.nuls.core.constant.ModuleStatusEnum;
import io.nuls.core.thread.BaseThread;
import io.nuls.core.utils.date.TimeService;
import io.nuls.core.utils.log.Log;

/**
 * @author Niels
 * @date 2017/11/27
 */
public class ModuleProcess extends BaseThread {
    private final ModuleRunner runner;
    private long startTime;
    private boolean running = true;

    public ModuleProcess(ModuleRunner target) {
        super(target);
        this.runner = target;
        Log.info("============init");
    }

    @Override
    public void beforeStart() {
        this.startTime = TimeService.currentTimeMillis();
        Log.info("============beforestart");
    }

    @Override
    protected void afterStart() {
        Log.info("============afterStart");

    }

    @Override
    protected void runException(Exception e) {
        Log.info("============runException");
        runner.getModule().setStatus(ModuleStatusEnum.EXCEPTION);
        running = false;
    }

    @Override
    protected void afterRun() {runner.getModule().setStatus(ModuleStatusEnum.RUNNING);
        Log.info("============afterRun");
        while (running) {
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                Log.error(e);
            }
        }
    }

    @Override
    protected void beforeRun() {
        Log.info("============beforeRun");
    }

    @Override
    protected void afterInterrupt() {
        runner.getModule().setStatus(ModuleStatusEnum.STOPED);
        Log.info("============afterInterrupt");

    }

    @Override
    protected void beforeInterrupt() {
        runner.getModule().setStatus(ModuleStatusEnum.STOPPING);
        running = false;
        Log.info("============beforeInterrupt");
    }

    public long getStartTime() {
        return startTime;
    }

    public ModuleRunner getRunner() {
        return runner;
    }
}
