package io.nuls.core.context;

import io.nuls.core.chain.entity.Block;
import io.nuls.core.manager.ModuleManager;
import io.nuls.core.module.NulsModule;
import io.nuls.core.utils.io.ByteBuffer;

public class NulsContext {

    private NulsContext() {
    }

    private static final NulsContext nc = new NulsContext();
    public static String DEFAULT_ENCODING ;

    /**
     * get zhe only instance of NulsContext
     *
     * @return
     */
    public static final NulsContext getInstance() {
        return nc;
    }

    /**
     * get NulsModule Object
     *
     * @param moduleName
     * @return
     */
    public NulsModule getModule(String moduleName) {
        return ModuleManager.getInstance().getModule(moduleName);
    }

    public NulsModule getModule(Class moduleClass) {
        return ModuleManager.getInstance().getModule(moduleClass);
    }

    public NulsModule getModule(int moduleId) {
        return ModuleManager.getInstance().getModuleById(moduleId);
    }

    /**
     * get Service by interface
     *
     * @param tClass
     * @param <T>
     * @return
     */
    public <T> T getService(Class<T> tClass) {
        return ModuleManager.getInstance().getService(tClass);
    }

    public int getVersion() {
        return 0;
    }

    public String getModuleVersion(String module) {
        return "";
    }
}
