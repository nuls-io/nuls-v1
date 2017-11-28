package io.nuls.core.module.thread;

import io.nuls.core.thread.BaseThread;
import io.nuls.core.utils.date.TimeService;

/**
 * @author Niels
 * @date 2017/11/27
 */
public class ModuleProcess extends BaseThread {
    private final ModuleRunner runner;
    private long startTime;

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
        super.afterStart();
    }

    @Override
    protected void afterRun() {
        super.afterRun();
    }

    @Override
    protected void beforeRun() {
        super.beforeRun();
    }

    @Override
    protected void afterInterrupt() {
        super.afterInterrupt();
    }

    @Override
    protected void beforeInterrupt() {
        super.beforeInterrupt();
    }

    public long getStartTime() {
        return startTime;
    }
}
