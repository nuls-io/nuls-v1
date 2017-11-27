package io.nuls.core.module.thread;

import java.util.concurrent.ThreadFactory;

/**
 * @author Niels
 * @date 2017/11/27
 */
public class ModuleThreadFactory implements ThreadFactory {
    @Override
    public Thread newThread(Runnable r) {
        //todo
        ModuleProcess process = new ModuleProcess();
        process.setDaemon(false);
        return process;
    }
}
