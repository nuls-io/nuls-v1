package io.nuls.core.manager;


import io.nuls.core.constant.ErrorCode;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.module.BaseNulsModule;
import io.nuls.core.module.NulsModuleProxy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Niels
 * @date 2017/9/26
 *
 */
public class ModuleManager {

    private static final Map<String, NulsModuleProxy> MODULE_MAP = new HashMap<>();

    private static final ModuleManager MANAGER = new ModuleManager();

    private static final Map<Class, Object> INTF_MAP = new HashMap<>();
    private static final Map<String, Set<Class>> MODULE_INTF_MAP = new HashMap<>();

    private ModuleManager() {
    }

    public <T> T getService(Class<T> tclass) {
        if(INTF_MAP.get(tclass) == null) {
            return null;
        }
        return (T) INTF_MAP.get(tclass);
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
        if (INTF_MAP.keySet().contains(serviceInterface)) {
            throw new NulsRuntimeException(ErrorCode.INTF_REPETITION);
        }
        INTF_MAP.put(serviceInterface, service);
        Set<Class> set = MODULE_INTF_MAP.get(moduleName);
        if (null == set) {
            set = new HashSet<>();
        }
        set.add(serviceInterface);
        MODULE_INTF_MAP.put(moduleName, set);
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
        INTF_MAP.remove(clazz);
        Set<Class> set = MODULE_INTF_MAP.get(moduleName);
        if (null != set) {
            set.remove(clazz);
            MODULE_INTF_MAP.put(moduleName, set);
        }
    }

    public void removeService(String moduleName) {
        Set<Class> set = MODULE_INTF_MAP.get(moduleName);
        if (null == set) {
            return;
        }
        for (Class clazz : set) {
            removeService(moduleName, clazz);
        }
    }

    public static ModuleManager getInstance() {
        return MANAGER;
    }

    public BaseNulsModule getModule(String moduleName) {
        return MODULE_MAP.get(moduleName);
    }

    public BaseNulsModule getModule(Class moduleClass) {
        for (BaseNulsModule module : MODULE_MAP.values()) {
            if (module.getModuleClass().equals(moduleClass)) {
                return module;
            } else if (module.getModuleClass().getSuperclass().equals(moduleClass) && !BaseNulsModule.class.equals(module.getModuleClass().getSuperclass())) {
                return module;
            }
        }
        return null;
    }

    public BaseNulsModule getModuleById(int id) {
        for (BaseNulsModule module : MODULE_MAP.values()) {
            if (module.getModuleId() == id) {
                return module;
            }
        }
        return null;
    }

    public String getInfo() {
        StringBuilder str = new StringBuilder();
        for (BaseNulsModule module : MODULE_MAP.values()) {
            str.append(module.getInfo());
        }
        return str.toString();
    }

    public void regModule(String moduleName, BaseNulsModule module) {
        if (MODULE_MAP.keySet().contains(moduleName)) {
            throw new NulsRuntimeException(ErrorCode.THREAD_REPETITION, "the name of Module is already exist(" + moduleName + ")");
        }
        if (module instanceof NulsModuleProxy) {
            MODULE_MAP.put(moduleName, (NulsModuleProxy) module);
        } else {
            MODULE_MAP.put(moduleName, new NulsModuleProxy(module));
        }
    }

    public void remModule(String moduleName) {
        MODULE_MAP.remove(moduleName);
    }


}
