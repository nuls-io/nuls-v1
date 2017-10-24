package io.nuls.task;

import io.nuls.util.log.Log;

/**
 * Created by Niels on 2017/10/13.
 * nuls.io
 */
public class NulsModuleProxy extends NulsModule {

    private final NulsModule module;

    public NulsModuleProxy(NulsModule module) {
        super(module.getModuleName());
        this.module = module;
    }


    @Override
    public void start() {
        if (!module.getStatus().equals(ModuleStatus.RUNNING)) {
            Log.warn("The module is allready running");
        }
        module.setStatus(ModuleStatus.STARTING);
        try {
            module.start();
        } catch (Exception e) {
            module.setStatus(ModuleStatus.EXCEPTION);
            Log.error(e);
        }
        module.setStatus(ModuleStatus.UNSTARTED);
    }

    @Override
    public void shutdown() {
        if (!module.getStatus().equals(ModuleStatus.RUNNING)) {
            Log.warn("The module is allready stoped");
        }
        module.setStatus(ModuleStatus.STOPPING);
        try {
            module.shutdown();
        } catch (Exception e) {
            module.setStatus(ModuleStatus.EXCEPTION);
            Log.error(e);
        }
        module.setStatus(ModuleStatus.STOPED);
    }

    @Override
    public void destroy() {
        if (module.getStatus().equals(ModuleStatus.RUNNING)) {
            this.shutdown();
        }
        module.setStatus(ModuleStatus.DESTROYING);
        try {
            module.destroy();
        } catch (Exception e) {
            module.setStatus(ModuleStatus.EXCEPTION);
            Log.error(e);
        }
        ModuleManager.getInstance().removeService(this.getModuleName());
        ModuleManager.getInstance().remModule(this.getModuleName());
        ModuleManager.getInstance().remThreadsByModule(this.getModuleName());
        module.setStatus(ModuleStatus.DESTROYED);

    }

    @Override
    public String getInfo() {
        return module.getInfo();
    }

    @Override
    public String getVersion() {
        return module.getVersion();
    }
}
