package io.nuls.core.module.thread;

import io.nuls.core.module.BaseNulsModule;

/**
 * @author Niels
 * @date 2017/11/24
 */
public class ModuleRunner implements Runnable {

    private final BaseNulsModule module;

//    private boolean running = false;

    public ModuleRunner(BaseNulsModule t) {
        this.module = t;
    }

    @Override
    public void run() {
//        running = true;
        module.start();
//        while (running) {
//            try {
//                Thread.sleep(5000L);
//            } catch (InterruptedException e) {
//                Log.error(e);
//            }
//        }
    }

//    public void stop() {
//        running = false;
//    }

    public BaseNulsModule getModule() {
        return module;
    }
}
