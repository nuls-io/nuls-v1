package io.nuls.core.module.thread;

import io.nuls.core.module.BaseNulsModule;

/**
 * @author Niels
 * @date 2017/11/24
 */
public class ModuleRunner implements Runnable {

    private final BaseNulsModule module;

    public ModuleRunner(BaseNulsModule t) {
        this.module = t;
    }

    @Override
    public void run() {
        module.start();
    }

    public BaseNulsModule getModule() {
        return module;
    }
}
