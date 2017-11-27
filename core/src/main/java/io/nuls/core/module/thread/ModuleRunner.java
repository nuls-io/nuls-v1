package io.nuls.core.module.thread;

import io.nuls.core.module.BaseNulsModule;

/**
 * @author Niels
 * @date 2017/11/24
 */
public class ModuleRunner<T extends BaseNulsModule> implements Runnable {

    private final T module;

    public ModuleRunner(T t){
        this.module = t;
    }

    @Override
    public void run() {
        module.start();
    }
}
