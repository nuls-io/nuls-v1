package io.nuls.task;

import io.nuls.exception.NulsException;
import io.nuls.util.cfg.ConfigLoader;
import io.nuls.util.log.Log;

import java.util.List;
import java.util.Map;

/**
 * Created by Niels on 2017/9/26.
 * nuls.io
 */
public abstract class NulsModule {

    private final String moduleName;

    private ModuleStatus status;

    public NulsModule(String moduleName) {
        this.moduleName = moduleName;
        this.status = ModuleStatus.UNSTARTED;
        ModuleManager.getInstance().regModule(this.moduleName,this);
    }

    public abstract void start();

    public abstract void shutdown();

    public abstract void destroy();

    public abstract String getInfo();

    protected final List<NulsThread> getThreadList(){
        return ModuleManager.getInstance().getThreadsByModule(this.moduleName);
    }

    public final ModuleStatus getStatus() {
        return this.status;
    }

    public final String getModuleName() {
        return moduleName;
    }

    public void setStatus(ModuleStatus status) {
        this.status = status;
    }

    protected final String getCfgProperty(String section, String property) {
        try {
            return ConfigLoader.getCfgValue(section, property);
        } catch (NulsException e) {
            Log.error(e);
            return null;
        }
    }

    protected final void registerService(Object service){
        ModuleManager.getInstance().regService(this.moduleName,service);
    }
}
