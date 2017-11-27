package io.nuls.core.module;


import io.nuls.core.constant.ModuleStatusEnum;
import io.nuls.core.manager.ModuleManager;
import io.nuls.core.thread.manager.ThreadManager;
import io.nuls.core.utils.log.Log;

/**
 * Created by Niels on 2017/10/13.
 *
 */
public class NulsModuleProxy extends BaseNulsModule {

    private final BaseNulsModule module;

    public NulsModuleProxy(BaseNulsModule module) {
        super(module.getModuleName());
        this.module = module;
    }


    @Override
    public void start() {
        if (!module.getStatus().equals(ModuleStatusEnum.RUNNING)) {
            Log.warn("The module is allready running");
        }
        module.setStatus(ModuleStatusEnum.STARTING);
        try {
            module.start();
        } catch (Exception e) {
            module.setStatus(ModuleStatusEnum.EXCEPTION);
            Log.error(e);
        }
        module.setStatus(ModuleStatusEnum.UNSTARTED);
    }

    @Override
    public void shutdown() {
        if (!module.getStatus().equals(ModuleStatusEnum.RUNNING)) {
            Log.warn("The module is allready stoped");
        }
        module.setStatus(ModuleStatusEnum.STOPPING);
        try {
            module.shutdown();
        } catch (Exception e) {
            module.setStatus(ModuleStatusEnum.EXCEPTION);
            Log.error(e);
        }
        ThreadManager.shutdownByModuleId(this.getModuleId());
        module.setStatus(ModuleStatusEnum.STOPED);
    }

    @Override
    public void destroy() {
        if (module.getStatus().equals(ModuleStatusEnum.RUNNING)) {
            this.shutdown();
        }
        module.setStatus(ModuleStatusEnum.DESTROYING);
        try {
            module.destroy();
        } catch (Exception e) {
            module.setStatus(ModuleStatusEnum.EXCEPTION);
            Log.error(e);
        }
        ModuleManager.getInstance().removeService(this.getModuleName());
        ModuleManager.getInstance().remModule(this.getModuleName());
        ThreadManager.shutdownByModuleId(this.getModuleId());
        module.setStatus(ModuleStatusEnum.DESTROYED);
    }

    @Override
    public String getInfo() {
        return module.getInfo();
    }

    @Override
    public int getVersion() {
        return module.getVersion();
    }

    @Override
    public Class<? extends BaseNulsModule> getModuleClass() {
        if (null != module) {
            return module.getClass();
        }
        return null;
    }
}
