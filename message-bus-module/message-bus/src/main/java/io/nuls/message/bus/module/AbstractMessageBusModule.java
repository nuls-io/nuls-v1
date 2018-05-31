package io.nuls.message.bus.module;

import io.nuls.kernel.constant.NulsConstant;
import io.nuls.kernel.module.BaseModuleBootstrap;
import io.nuls.message.bus.constant.MessageBusConstant;

/**
 * @author: Charlie
 * @date: 2018/5/6
 */
public abstract class AbstractMessageBusModule extends BaseModuleBootstrap {

    public AbstractMessageBusModule(){
        super(MessageBusConstant.MODULE_ID_MESSAGE_BUS);
    }
}
