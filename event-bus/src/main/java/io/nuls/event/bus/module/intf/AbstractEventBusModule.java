package io.nuls.event.bus.module.intf;

import io.nuls.core.constant.NulsConstant;
import io.nuls.core.module.BaseModuleBootstrap;

/**
 *
 * @author Niels
 * @date 2017/11/6
 */
public abstract class AbstractEventBusModule extends BaseModuleBootstrap {
    public AbstractEventBusModule() {
        super(NulsConstant.MODULE_ID_EVENT_BUS);
    }
}
