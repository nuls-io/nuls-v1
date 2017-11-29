package io.nuls.mq.module;

import io.nuls.core.constant.NulsConstant;
import io.nuls.core.module.BaseNulsModule;

/**
 * @author Niels
 * @date 2017/9/26
 */
public abstract class AbstractMQModule extends BaseNulsModule {

    protected AbstractMQModule() {
        super(NulsConstant.MODULE_ID_MQ);
    }
}
