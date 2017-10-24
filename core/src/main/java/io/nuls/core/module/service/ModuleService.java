package io.nuls.core.module.service;


import io.nuls.core.manager.ModuleManager;
import io.nuls.core.module.NulsModule;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.str.StringUtils;

/**
 * Created by Niels on 2017/10/16.
 * nuls.io
 */
public class ModuleService {

    private static final ModuleService service = new ModuleService();

    private ModuleService() {
        ModuleManager.getInstance().regService("system", this);
    }

    public static ModuleService getInstance() {
        return service;
    }

    public NulsModule loadModule(String moduleClass) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        NulsModule module = null;
        do {
            if (StringUtils.isBlank(moduleClass)) {
                Log.warn("module cannot start:" + moduleClass);
                break;
            }
            Class clazz = Class.forName(moduleClass);
            module = (NulsModule) clazz.newInstance();
            Log.info("load module:" + module.getInfo());
        } while (false);
        return module;
    }
}
