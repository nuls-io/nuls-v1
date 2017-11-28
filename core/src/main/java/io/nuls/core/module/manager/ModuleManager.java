package io.nuls.core.module.manager;


import io.nuls.core.constant.ErrorCode;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.module.BaseNulsModule;
import io.nuls.core.module.thread.ModuleThreadPoolExecuter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Niels
 * @date 2017/9/26
 */
public class ModuleManager {

    private static final ModuleThreadPoolExecuter POOL = ModuleThreadPoolExecuter.getInstance();

    private static final Map<Short, BaseNulsModule> MODULE_MAP = new HashMap<>();

    private static final ModuleManager MANAGER = new ModuleManager();

    private ModuleManager() {
    }

    public static ModuleManager getInstance() {
        return MANAGER;
    }

    public BaseNulsModule getModule(short moduleId) {
        return MODULE_MAP.get(moduleId);
    }
    public Short getModuleId(Class<? extends BaseNulsModule> moduleClass) {
        if(null==moduleClass){
            return null;
        }
        for(BaseNulsModule module:MODULE_MAP.values()){
            if(moduleClass.equals(module.getClass())){
                return module.getModuleId();
            }
        }
        return null;
    }

    public void regModule(BaseNulsModule module) {
        short moduleId = module.getModuleId();
        if (MODULE_MAP.keySet().contains(moduleId)) {
            throw new NulsRuntimeException(ErrorCode.THREAD_REPETITION, "the id of Module is already exist(" + module.getModuleName() + ")");
        }
        MODULE_MAP.put(moduleId,module);
    }

    public void remModule(short moduleId) {
        MODULE_MAP.remove(moduleId);
    }


    public void startModule(BaseNulsModule module) {
        POOL.startModule(module);
    }

    public void stopModule(short moduleId) {
        POOL.stopModule(MODULE_MAP.get(moduleId));
    }

    public void destoryModule(short moduleId) {
        BaseNulsModule module = MODULE_MAP.get(moduleId);
        if (null == module) {
            return;
        }
        module.shutdown();
        module.destroy();
        POOL.stopModule(module);
    }

    public String getInfo() {
        //todo
        StringBuilder str = new StringBuilder();
        for (BaseNulsModule module : MODULE_MAP.values()) {
            str.append(module.getInfo());
        }
        return str.toString();
    }

    public Thread.State getModuleState(short moduleId) {
        return POOL.getProcessState(moduleId);
    }
}
