package io.nuls.message.bus.module;

import io.nuls.kernel.constant.NulsConstant;
import io.nuls.kernel.module.BaseModuleBootstrap;

/**
 * @author: Charlie
 * @date: 2018/5/6
 */
public abstract class AbstractMessageBusModule extends BaseModuleBootstrap {

    public AbstractMessageBusModule(){
        super(NulsConstant.MODULE_ID_EVENT_BUS);
    }
}
