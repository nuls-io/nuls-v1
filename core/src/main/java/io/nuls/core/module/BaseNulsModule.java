package io.nuls.core.module;

import io.nuls.core.constant.ModuleStatusEnum;
import io.nuls.core.event.EventManager;
import io.nuls.core.event.BaseNulsEvent;
import io.nuls.core.exception.NulsException;
import io.nuls.core.manager.ModuleManager;
import io.nuls.core.thread.BaseThread;
import io.nuls.core.utils.cfg.ConfigLoader;
import io.nuls.core.utils.log.Log;

import java.util.List;

/**
 * Created by Niels on 2017/9/26.
 *
 */
public abstract class BaseNulsModule {

    private short moduleId;

    private final String moduleName;

    private ModuleStatusEnum status;

    public BaseNulsModule(String moduleName) {
        this.moduleName = moduleName;
        this.status = ModuleStatusEnum.UNSTARTED;
        ModuleManager.getInstance().regModule(this.moduleName, this);
    }

    /**
     * start the module
     */
    public abstract void start();

    /**
     * stop the module
     */
    public abstract void shutdown();

    /**
     * destroy the module
     */
    public abstract void destroy();

    /**
     * get all info of the module
     *
     * @return
     */
    public abstract String getInfo();

    /**
     * get the version of the module
     *
     * @return
     */
    public abstract int getVersion();

    /**
     * get the status of the module
     *
     * @return
     */
    public final ModuleStatusEnum getStatus() {
        return this.status;
    }

    public final String getModuleName() {
        return moduleName;
    }

    public void setStatus(ModuleStatusEnum status) {
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

    /**
     * register the service to ModuleManager
     *
     * @param service
     */
    protected final void registerService(Object service) {
        ModuleManager.getInstance().regService(this.moduleName, service);
    }

    protected final void registerService(Class serviceInterface, Object service) {
        ModuleManager.getInstance().regService(this.moduleName, serviceInterface, service);
    }

    protected final void registerEvent(short eventType, Class<? extends BaseNulsEvent> eventClass) {
        if (this.getModuleId() == 0) {
            EventManager.putEvent(this, eventType, eventClass);
        } else {
            EventManager.putEvent(this.getModuleId(), eventType, eventClass);
        }
    }

    public Class<? extends BaseNulsModule> getModuleClass() {
        return this.getClass();
    }

    public short getModuleId() {
        return moduleId;
    }

    public void setModuleId(short moduleId) {
        this.moduleId = moduleId;
    }
}
