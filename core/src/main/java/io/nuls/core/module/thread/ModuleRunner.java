package io.nuls.core.module.thread;

import io.nuls.core.constant.ErrorCode;
import io.nuls.core.constant.ModuleStatusEnum;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.module.BaseNulsModule;
import io.nuls.core.module.manager.ModuleManager;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.str.StringUtils;

/**
 * @author Niels
 * @date 2017/11/24
 */
public class ModuleRunner implements Runnable {

    private final String moduleKey;
    private final String moduleClass;
    private  BaseNulsModule module;

    public ModuleRunner(String key, String moduleClass) {
        this.moduleKey = key;
        this.moduleClass = moduleClass;
    }

    @Override
    public void run() {
        try {
            module = this.loadModule();
            module.setStatus(ModuleStatusEnum.INITING);
            module.init();
            module.setStatus(ModuleStatusEnum.INITED);
            module.setStatus(ModuleStatusEnum.STARTING);
            module.start();
            module.setStatus(ModuleStatusEnum.RUNNING);
        } catch (ClassNotFoundException e) {
            module.setStatus(ModuleStatusEnum.EXCEPTION);
            Log.error(e);
            throw new NulsRuntimeException(ErrorCode.FAILED,e);
        } catch (IllegalAccessException e) {
            module.setStatus(ModuleStatusEnum.EXCEPTION);
            Log.error(e);
            throw new NulsRuntimeException(ErrorCode.FAILED,e);
        } catch (InstantiationException e) {
            module.setStatus(ModuleStatusEnum.EXCEPTION);
            Log.error(e);
            throw new NulsRuntimeException(ErrorCode.FAILED,e);
        }
    }

    private BaseNulsModule loadModule() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        BaseNulsModule module = null;
        do {
            if (StringUtils.isBlank(moduleClass)) {
                Log.warn("module cannot start:" + moduleClass);
                break;
            }
            Class clazz = Class.forName(moduleClass);
            module = (BaseNulsModule) clazz.newInstance();
            module.setModuleName(this.moduleKey);
            Log.info("load module:" + module.getInfo());
        } while (false);
        ModuleManager.getInstance().regModule(module);

        return module;
    }

    public String getModuleKey() {
        return moduleKey;
    }

    public BaseNulsModule getModule() {
        return module;
    }
}
