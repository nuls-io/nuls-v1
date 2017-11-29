package io.nuls.core.module.thread;

import io.nuls.core.module.BaseNulsModule;
import io.nuls.core.utils.log.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Niels
 * @date 2017/11/27
 */
public class ModuleThreadPoolExecuter {

    private static final ModuleThreadPoolExecuter POOL = new ModuleThreadPoolExecuter();

    private final Map<Short, ModuleProcess> PROCCESS_MAP = new HashMap<>();

    private ModuleThreadFactory factory = new ModuleThreadFactory();

    private ModuleThreadPoolExecuter() {
    }

    public static final ModuleThreadPoolExecuter getInstance() {
        return POOL;
    }

    public void startModule(String key, String moduleClass) {
        if(null==moduleClass){
            return;
        }
        try {
            ModuleRunner runner = new ModuleRunner(key, moduleClass);
            ModuleProcess moduleProcess = factory.newThread(runner);
            moduleProcess.start();
        }catch (Exception e){
            Log.error(e);
        }
    }

    public void stopModule(short moduleId) {
        ModuleProcess process = PROCCESS_MAP.get(moduleId);
        if (null != process && !process.isInterrupted()) {
            process.interrupt();
        }
    }

    public Thread.State getProcessState(short moduleId) {
        ModuleProcess process = PROCCESS_MAP.get(moduleId);
        if (null != process) {
            return process.getState();
        }
        return null;
    }

    public List<ModuleProcess> getProcessList() {
        return new ArrayList<>(PROCCESS_MAP.values());
    }

    public void removeProcess(short moduleId) {

    }
}
