package io.nuls.core.manager;


import io.nuls.core.constant.ErrorCode;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.module.NulsModule;
import io.nuls.core.module.NulsModuleProxy;
import io.nuls.core.thread.NulsThread;

import java.util.*;

/**
 * Created by Niels on 2017/9/26.
 * nuls.io
 */
public class ModuleManager {

    private static final Map<String, NulsThread> threadMap = new HashMap<>();

    private static final Map<String, NulsModuleProxy> moduleMap = new HashMap<>();

    private static final ModuleManager manager = new ModuleManager();

    private static final Map<Class, Object> intfMap = new HashMap<>();
    private static final Map<String, Set<Class>> moduleIntfMap = new HashMap<>();

    private ModuleManager() {
    }

    public <T> T getService(Class<T> tclass) {
        return (T) intfMap.get(tclass);
    }

    public void regService(String moduleName, Object service) {
        Class key = service.getClass().getSuperclass();
        if (key.equals(Object.class)) {
            key = service.getClass();
        }
        if (null == key || key.equals(Object.class)) {
            key = service.getClass();
        }
        if (intfMap.keySet().contains(key)) {
            throw new NulsRuntimeException(ErrorCode.INTF_REPETITION);
        }
        intfMap.put(key, service);
        Set<Class> set = moduleIntfMap.get(moduleName);
        if (null == set) {
            set = new HashSet<>();
        }
        set.add(key);
        moduleIntfMap.put(moduleName, set);
    }

    public void removeService(String moduleName, Object service) {
        Class key = service.getClass().getSuperclass();
        if (key.equals(Object.class)) {
            key = service.getClass();
        }
        if (null == key || key.equals(Object.class)) {
            key = service.getClass();
        }
        removeService(moduleName, key);
    }

    public void removeService(String moduleName, Class clazz) {
        intfMap.remove(clazz);
        Set<Class> set = moduleIntfMap.get(moduleName);
        if (null != set) {
            set.remove(clazz);
            moduleIntfMap.put(moduleName, set);
        }
    }

    public void removeService(String moduleName) {
        Set<Class> set = moduleIntfMap.get(moduleName);
        if (null == set) {
            return;
        }
        for (Class clazz : set) {
            removeService(moduleName, clazz);
        }
    }

    public static ModuleManager getInstance() {
        return manager;
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
            throw new NulsRuntimeException(ErrorCode.THREAD_REPETITION, "the name of thread is already exist(" + threadName + ")");
        }
        threadMap.put(threadName, thread);
    }

    private void cancelThread(String threadName) {
        threadMap.remove(threadName);
    }

    public void regModule(String moduleName, NulsModule module) {
        if (moduleMap.keySet().contains(moduleName)) {
            throw new NulsRuntimeException(ErrorCode.THREAD_REPETITION, "the name of Module is already exist(" + moduleName + ")");
        }
        if (module instanceof NulsModuleProxy) {
            moduleMap.put(moduleName, (NulsModuleProxy) module);
        } else {
            moduleMap.put(moduleName, new NulsModuleProxy(module));
        }
    }

    public void remModule(String moduleName) {
        moduleMap.remove(moduleName);
    }


    public List<NulsThread> getThreadsByModule(String moduleName) {
        List<NulsThread> list = new ArrayList<>();
        for (NulsThread t : threadMap.values()) {
            if (t.getModule().getModuleName().equals(moduleName)) {
                list.add(t);
            }
        }
        return list;
    }
    public void remThreadsByModule(String moduleName) {
        for (NulsThread t : getThreadsByModule(moduleName) ) {
             threadMap.remove(t.getName());
        }
    }
}
