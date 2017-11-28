package io.nuls.core.module;


import io.nuls.core.constant.ModuleStatusEnum;
import io.nuls.core.module.manager.ModuleManager;
import io.nuls.core.module.manager.ServiceManager;
import io.nuls.core.module.thread.ModuleThreadPoolExecuter;
import io.nuls.core.thread.manager.ThreadManager;
import io.nuls.core.utils.log.Log;

/**
 * @author Niels
 * @date 2017/10/13
 */
public class NulsModuleProxy1 extends BaseNulsModule {

    private final BaseNulsModule module;

    public NulsModuleProxy1(BaseNulsModule module) {
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
        ModuleThreadPoolExecuter.getInstance().stopModule(this);
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
        ServiceManager.getInstance().removeService(this.getModuleId());
        ModuleManager.getInstance().remModule(this.getModuleId());
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
