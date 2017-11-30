package io.nuls.core.module.manager;

import io.nuls.core.constant.ErrorCode;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.log.Log;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Niels
 * @date 2017/11/28
 */
public class ServiceManager {
    private static final ServiceManager INSTANCE = new ServiceManager();
    private static final Map<Class, Object> INTF_MAP = new HashMap<>();
    private static final Map<Short, Set<Class>> MODULE_INTF_MAP = new HashMap<>();
    private static final Map<Class, Short> MODULE_ID_MAP = new HashMap<>();
    private static final int WAIT_TIMES = 10;

    private ServiceManager() {
    }

    public static final ServiceManager getInstance() {
        return INSTANCE;
    }

    public <T> T getService(Class<T> tclass) {
        dependencyCheck(tclass, 0);
        return (T) INTF_MAP.get(tclass);
    }

    private void dependencyCheck(Class tclass, int index) {
        Object service = INTF_MAP.get(tclass);
        if(service!=null){
            return;
        }
        if (index >= WAIT_TIMES) {
            throw new NulsRuntimeException(ErrorCode.FAILED, "dependency module is not ready!" + tclass);
        }
        sleepAndIncrement(tclass,index);
    }

    private void sleepAndIncrement(Class tclass, int index){
        try {
            Thread.sleep(1000L);
            this.dependencyCheck(tclass, index + 1);
        } catch (InterruptedException e) {
            Log.error(e);
        }
    }

    public void regService(short moduleId, Object service) {
        Class serviceInterface =  null;
        boolean useSuperClass = null == service.getClass().getInterfaces() || service.getClass().getInterfaces().length == 0 && !service.getClass().getSuperclass().equals(Object.class);
        if (useSuperClass) {
            serviceInterface = service.getClass().getSuperclass();
        } else if (null != service.getClass().getInterfaces() && service.getClass().getInterfaces().length == 1) {
            serviceInterface = service.getClass().getInterfaces()[0];
        } else if (null == service.getClass().getInterfaces() || service.getClass().getInterfaces().length > 1) {
            throw new NulsRuntimeException(ErrorCode.FAILED, "register service faild:interface is uncertain");
        } else {
            serviceInterface = service.getClass();
        }
        this.regService(moduleId, serviceInterface, service);
    }

    public void regService(short moduleId, Class serviceInterface, Object service) {
        if (serviceInterface == null) {
            serviceInterface = service.getClass();
        }
        if (INTF_MAP.keySet().contains(serviceInterface)) {
            throw new NulsRuntimeException(ErrorCode.INTF_REPETITION);
        }
        INTF_MAP.put(serviceInterface, service);
        Set<Class> set = MODULE_INTF_MAP.get(moduleId);
        if (null == set) {
            set = new HashSet<>();
        }
        set.add(serviceInterface);
        MODULE_INTF_MAP.put(moduleId, set);
        MODULE_ID_MAP.put(serviceInterface, moduleId);
    }

    public void removeService(short moduleId, Object service) {
        Class key = service.getClass().getSuperclass();
        if (key.equals(Object.class)) {
            key = service.getClass();
        }
        if (null == key || key.equals(Object.class)) {
            key = service.getClass();
        }
        removeService(moduleId, key);
    }

    public void removeService(short moduleId, Class clazz) {
        INTF_MAP.remove(clazz);
        Set<Class> set = MODULE_INTF_MAP.get(moduleId);
        if (null != set) {
            set.remove(clazz);
            MODULE_INTF_MAP.put(moduleId, set);
        }
    }

    public void removeService(short moduleId) {
        Set<Class> set = MODULE_INTF_MAP.get(moduleId);
        if (null == set) {
            return;
        }
        for (Class clazz : set) {
            removeService(moduleId, clazz);
        }
    }
}
