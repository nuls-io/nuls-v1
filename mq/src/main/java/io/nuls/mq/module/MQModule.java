package io.nuls.mq.module;

import io.nuls.core.module.NulsModule;

/**
 * Created by Niels on 2017/9/26.
 * nuls.io
 */
public abstract class MQModule extends NulsModule {

    protected MQModule() {
        super(MQModule.class.getSimpleName());
    }
}
