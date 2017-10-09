package io.nuls.global;

import io.nuls.exception.NulsRuntimeException;
import io.nuls.task.ModuleManager;
import io.nuls.task.NulsModule;
import io.nuls.util.constant.ErrorCode;

import java.util.HashMap;
import java.util.Map;

public class NulsContext {

    private NulsContext() {
        // single
    }

    private static final NulsContext nc = new NulsContext();
    private static final Map<Class, Object> intfMap = new HashMap<>();
    private static ModuleManager moduleManager = ModuleManager.getInstance();

    public static final NulsContext getInstance() {
        return nc;
    }

    public <T> T getService(Class<T> tclass) {
        return (T) intfMap.get(tclass);
    }

    public void regService(Object service) {
        if (intfMap.keySet().contains(service.getClass().getSuperclass())) {
            throw new NulsRuntimeException(ErrorCode.INTF_REPETITION);
        }
        intfMap.put(service.getClass().getSuperclass(), service);
    }

    public static ModuleManager getModuleManager() {
        return moduleManager;
    }

    public NulsModule getModule(String moduleName) {
        return moduleManager.getModule(moduleName);
    }
}
