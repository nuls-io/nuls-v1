package io.nuls.network.intf;


import io.nuls.task.NulsModule;
import io.nuls.task.ModuleStatus;

public abstract class NetworkModule implements NulsModule{
    protected NetworkModule(){
        this.moduleName = this.getClass().getSimpleName();
        this.status = ModuleStatus.UNINITED;
    }
    private String moduleName;
    private ModuleStatus status;

    @Override
    public String getModuleName() {
        return this.moduleName;
    }

    @Override
    public void reboot() {
        this.shutdown();
        this.start();
    }

    @Override
    public ModuleStatus getStatus() {
        return status;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public void setStatus(ModuleStatus status) {
        this.status = status;
    }

}
