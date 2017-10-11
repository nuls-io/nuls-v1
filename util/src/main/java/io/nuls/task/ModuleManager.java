package io.nuls.task;

import io.nuls.exception.NulsRuntimeException;
import io.nuls.util.constant.ErrorCode;

import javax.management.monitor.StringMonitor;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Niels on 2017/9/26.
 * nuls.io
 */
public final class ModuleManager {

    private static final Map<String, NulsThread> threadMap = new HashMap<>();

    private static final Map<String, NulsModule> moduleMap = new HashMap<>();

    private static final ModuleManager manager = new ModuleManager();

    private ModuleManager(){}

    public static ModuleManager getInstance(){
        return manager;
    }


    public Map<String, NulsModule> getModules() {
        return moduleMap;
    }

    public NulsModule getModule(String moduleName) {
        return moduleMap.get(moduleName);
    }

    public String getInfo() {
        StringBuilder str = new StringBuilder();
        for (NulsModule module : moduleMap.values()) {
            str.append(module.getInfo());
        }
        return str.toString();
    }

    public void regThread(String threadName, NulsThread thread) {
        if (threadMap.keySet().contains(threadName)) {
            throw new NulsRuntimeException(ErrorCode.THREAD_REPETITION,"the name of thread is already exist(" + threadName + ")");
        }
        threadMap.put(threadName, thread);
    }

    public void cancelThread(String threadName) {
        threadMap.remove(threadName);
    }

    public void regModule(String moduleName, NulsModule module) {
        if (moduleMap.keySet().contains(moduleName)) {
            throw new NulsRuntimeException(ErrorCode.THREAD_REPETITION,"the name of Module is already exist(" + moduleName + ")");
        }
        moduleMap.put(moduleName, module);
    }

    public void remModule(String moduleName){
        moduleMap.remove(moduleName);
    }

    public NulsThread getThread(String threadName) {
        return threadMap.get(threadName);
    }

    public Map<String, NulsThread> getAllThreads() {
        return threadMap;
    }

    public Map<String, NulsThread> getThreadsByModule(String moduleName) {
        Map<String, NulsThread> map = new HashMap<>();
        for (NulsThread t : threadMap.values()) {
            if (t.getModule().getModuleName().equals(moduleName)) {
                map.put(t.getName(), t);
            }
        }
        return map;
    }

}
