package io.nuls.core.module.service;


import io.nuls.core.manager.ModuleManager;
import io.nuls.core.module.BaseNulsModule;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.str.StringUtils;

/**
 * Created by Niels on 2017/10/16.
 *
 */
public class ModuleService {

    private static final ModuleService service = new ModuleService();

    private ModuleService() {
        ModuleManager.getInstance().regService("system",null, this);
    }

    public static ModuleService getInstance() {
        return service;
    }

    public BaseNulsModule loadModule(String moduleClass) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        BaseNulsModule module = null;
        do {
            if (StringUtils.isBlank(moduleClass)) {
                Log.warn("module cannot start:" + moduleClass);
                break;
            }
            Class clazz = Class.forName(moduleClass);
            module = (BaseNulsModule) clazz.newInstance();
            Log.info("load module:" + module.getInfo());
        } while (false);
        return module;
    }
}
