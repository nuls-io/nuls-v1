package io.nuls.core.manager;


import io.nuls.core.constant.ErrorCode;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.module.BaseNulsModule;
import io.nuls.core.module.NulsModuleProxy;
import io.nuls.core.thread.BaseNulsThread;

import java.util.*;

/**
 * Created by Niels on 2017/9/26.
 *
 */
public class ModuleManager {

    private static final Map<String, BaseNulsThread> threadMap = new HashMap<>();

    private static final Map<String, NulsModuleProxy> moduleMap = new HashMap<>();

    private static final ModuleManager manager = new ModuleManager();

    private static final Map<Class, Object> intfMap = new HashMap<>();
    private static final Map<String, Set<Class>> moduleIntfMap = new HashMap<>();

    private ModuleManager() {
    }

    public <T> T getService(Class<T> tclass) {
        if(intfMap.get(tclass) == null) {
            return null;
        }
        return (T) intfMap.get(tclass);
    }

    public void regService(String moduleName, Object service) {
        Class serviceInterface = null;
        if (null == service.getClass().getInterfaces() || service.getClass().getInterfaces().length == 0 && !service.getClass().getSuperclass().equals(Object.class)) {
            serviceInterface = service.getClass().getSuperclass();
        } else if (null != service.getClass().getInterfaces() && service.getClass().getInterfaces().length == 1) {
            serviceInterface = service.getClass().getInterfaces()[0];
        } else if (null == service.getClass().getInterfaces() || service.getClass().getInterfaces().length > 1) {
            throw new NulsRuntimeException(ErrorCode.FAILED, "register service faild:interface is uncertain");
        } else {
            serviceInterface = service.getClass();
        }
        this.regService(moduleName, serviceInterface, service);
    }

    public void regService(String moduleName, Class serviceInterface, Object service) {
        if (serviceInterface == null) {
            serviceInterface = service.getClass();
        }
        if (intfMap.keySet().contains(serviceInterface)) {
            throw new NulsRuntimeException(ErrorCode.INTF_REPETITION);
        }
        intfMap.put(serviceInterface, service);
        Set<Class> set = moduleIntfMap.get(moduleName);
        if (null == set) {
            set = new HashSet<>();
        }
        set.add(serviceInterface);
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

    public BaseNulsModule getModule(String moduleName) {
        return moduleMap.get(moduleName);
    }

    public BaseNulsModule getModule(Class moduleClass) {
        for (BaseNulsModule module : moduleMap.values()) {
            if (module.getModuleClass().equals(moduleClass)) {
                return module;
            } else if (module.getModuleClass().getSuperclass().equals(moduleClass) && !BaseNulsModule.class.equals(module.getModuleClass().getSuperclass())) {
                return module;
            }
        }
        return null;
    }

    public BaseNulsModule getModuleById(int id) {
        for (BaseNulsModule module : moduleMap.values()) {
            if (module.getModuleId() == id) {
                return module;
            }
        }
        return null;
    }

    public String getInfo() {
        StringBuilder str = new StringBuilder();
        for (BaseNulsModule module : moduleMap.values()) {
            str.append(module.getInfo());
        }
        return str.toString();
    }

    public void regThread(String threadName, BaseNulsThread thread) {
        if (threadMap.keySet().contains(threadName)) {
            throw new NulsRuntimeException(ErrorCode.THREAD_REPETITION, "the name of thread is already exist(" + threadName + ")");
        }
        threadMap.put(threadName, thread);
    }

    private void cancelThread(String threadName) {
        threadMap.remove(threadName);
    }

    public void regModule(String moduleName, BaseNulsModule module) {
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


    public List<BaseNulsThread> getThreadsByModule(String moduleName) {
        List<BaseNulsThread> list = new ArrayList<>();
        for (BaseNulsThread t : threadMap.values()) {
            if (t.getModule().getModuleName().equals(moduleName)) {
                list.add(t);
            }
        }
        return list;
    }

    public void remThreadsByModule(String moduleName) {
        for (BaseNulsThread t : getThreadsByModule(moduleName)) {
            threadMap.remove(t.getName());
        }
    }

}
