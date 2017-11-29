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
    }

    @Override
    public void beforeStart() {
        this.startTime = TimeService.currentTimeMillis();
    }

    @Override
    protected void afterStart() {

    }

    @Override
    protected void runException(Exception e) {
        Log.error(e);
        runner.getModule().setStatus(ModuleStatusEnum.EXCEPTION);
        running = false;
    }

    @Override
    protected void afterRun() {
        while (running&&ModuleStatusEnum.RUNNING.equals(runner.getModule().getStatus())) {
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                Log.error(e);
            }
        }
    }

    @Override
    protected void beforeRun() {
    }

    @Override
    protected void afterInterrupt() {
        runner.getModule().setStatus(ModuleStatusEnum.STOPED);

    }

    @Override
    protected void beforeInterrupt() {
        runner.getModule().setStatus(ModuleStatusEnum.STOPPING);
        running = false;
    }

    public long getStartTime() {
        return startTime;
    }

    public ModuleRunner getRunner() {
        return runner;
    }
}
