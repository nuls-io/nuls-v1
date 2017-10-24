package io.nuls.core.context;

import io.nuls.core.manager.ModuleManager;
import io.nuls.core.module.NulsModule;

public class NulsContext {

    private NulsContext() {
        // single
    }

    private static final NulsContext nc = new NulsContext();

    /**
     * get zhe only instance of NulsContext
     * @return
     */
    public static final NulsContext getInstance() {
        return nc;
    }

    /**
     * get NulsModule Object
     * @param moduleName
     * @return
     */
    public NulsModule getModule(String moduleName) {
        return ModuleManager.getInstance().getModule(moduleName);
    }

    /**
     * get Service by interface
     * @param tClass
     * @param <T>
     * @return
     */
    public <T> T getService(Class<T> tClass) {
        return ModuleManager.getInstance().getService(tClass);
    }
}
