package io.nuls.core.module;

import io.nuls.core.constant.ModuleStatusEnum;
import io.nuls.core.event.BaseNulsEvent;
import io.nuls.core.event.EventManager;
import io.nuls.core.exception.NulsException;
import io.nuls.core.module.manager.ServiceManager;
import io.nuls.core.utils.cfg.ConfigLoader;
import io.nuls.core.utils.log.Log;

/**
 * @author Niels
 * @date 2017/9/26
 */
public abstract class BaseNulsModule {

    private final short moduleId;

    private final String moduleName;

    private ModuleStatusEnum status;

    public BaseNulsModule(short moduleId, String moduleName) {
        this.moduleId = moduleId;
        this.moduleName = moduleName;
        this.status = ModuleStatusEnum.UNSTARTED;
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
        ServiceManager.getInstance().regService(this.moduleId, service);
    }

    protected final void registerService(Class serviceInterface, Object service) {
        ServiceManager.getInstance().regService(this.moduleId, serviceInterface, service);
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
}
