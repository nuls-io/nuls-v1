/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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
