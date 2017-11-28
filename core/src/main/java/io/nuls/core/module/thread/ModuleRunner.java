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

    private final short moduleId;
    private final String moduleClass;
    private  BaseNulsModule module;

    public ModuleRunner(short moduleId, String moduleClass) {
        this.moduleId = moduleId;
        this.moduleClass = moduleClass;
    }

    @Override
    public void run() {
        try {
            module = this.loadModule();
        } catch (ClassNotFoundException e) {
            Log.error(e);
            throw new NulsRuntimeException(ErrorCode.FAILED,e);
        } catch (IllegalAccessException e) {
            Log.error(e);
            throw new NulsRuntimeException(ErrorCode.FAILED,e);
        } catch (InstantiationException e) {
            Log.error(e);
            throw new NulsRuntimeException(ErrorCode.FAILED,e);
        }
        module.setStatus(ModuleStatusEnum.STARTING);
        module.start();
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
            Log.info("load module:" + module.getInfo());
        } while (false);
        ModuleManager.getInstance().regModule(module);
        return module;
    }

    public short getModuleId() {
        return moduleId;
    }

    public BaseNulsModule getModule() {
        return module;
    }
}
