package io.nuls.global;

import io.nuls.task.ModuleManager;
import io.nuls.task.NulsModule;

public class NulsContext {

    private NulsContext() {
        // single
    }

    private static final NulsContext nc = new NulsContext();

    public static final NulsContext getInstance() {
        return nc;
    }

    public NulsModule getModule(String moduleName) {
        return ModuleManager.getInstance().getModule(moduleName);
    }

    public <T> T getService(Class<T> tClass) {
        return ModuleManager.getInstance().getService(tClass);
    }
}
