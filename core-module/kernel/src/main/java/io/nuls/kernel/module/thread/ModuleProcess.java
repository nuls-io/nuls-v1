/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package io.nuls.kernel.module.thread;

import io.nuls.core.tools.log.Log;
import io.nuls.kernel.constant.ModuleStatusEnum;
import io.nuls.kernel.func.TimeService;
import io.nuls.kernel.thread.BaseThread;

/**
 * @author Niels
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
        while (running && ModuleStatusEnum.RUNNING.equals(runner.getModule().getStatus())) {
            try {
                Thread.sleep(10000L);
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
