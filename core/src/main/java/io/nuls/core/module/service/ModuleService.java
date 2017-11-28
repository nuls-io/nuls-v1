package io.nuls.core.module.service;


import io.nuls.core.constant.ModuleStatusEnum;
import io.nuls.core.event.EventManager;
import io.nuls.core.module.BaseNulsModule;
import io.nuls.core.module.manager.ModuleManager;
import io.nuls.core.module.manager.ServiceManager;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.str.StringUtils;

/**
 * @author Niels
 * @date 2017/10/16
 */
public class ModuleService {
    private static final ModuleService INSTANCE = new ModuleService();

    private ModuleService() {
        ServiceManager.getInstance().regService((short) 0, ModuleService.class, this);
    }

    public static ModuleService getInstance() {
        return INSTANCE;
    }

    public Short getModuleId(Class<? extends BaseNulsModule> clazz) {
        return ModuleManager.getInstance().getModuleId(clazz);
    }

    private BaseNulsModule loadModule(short moduleId, String moduleClass) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        BaseNulsModule module = null;
        do {
            if (StringUtils.isBlank(moduleClass)) {
                Log.warn("module cannot start:" + moduleClass);
                break;
            }
            Class clazz = Class.forName(moduleClass);
            module = (BaseNulsModule) clazz.newInstance();
            module.setModuleId(moduleId);
            EventManager.refreshEvents();
            Log.info("load module:" + module.getInfo());
        } while (false);
        ModuleManager.getInstance().regModule(module);
        return module;
    }

    public void startModule(short moduleId, String moduleClass) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        BaseNulsModule module = this.loadModule(moduleId, moduleClass);
        ModuleManager.getInstance().startModule(module);
    }

    public ModuleStatusEnum getModuleState(short moduleId) {
        return ModuleManager.getInstance().getModuleState(moduleId);
    }

    public void shutdown(short moduleId) {
        ModuleManager.getInstance().stopModule(moduleId);
    }

    public void destroy(short moduleId) {
        ModuleManager.getInstance().destoryModule(moduleId);
    }

}
