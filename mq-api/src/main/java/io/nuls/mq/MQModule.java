package io.nuls.mq;

import io.nuls.task.NulsModule;
import io.nuls.task.ModuleStatus;

/**
 * Created by Niels on 2017/9/26.
 * nuls.io
 */
public abstract class MQModule extends NulsModule {

    protected MQModule() {
        super(MQModule.class.getSimpleName());
    }
}
