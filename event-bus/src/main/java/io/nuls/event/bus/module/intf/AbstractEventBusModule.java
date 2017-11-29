package io.nuls.event.bus.module.intf;

import io.nuls.core.constant.NulsConstant;
import io.nuls.core.module.BaseNulsModule;

/**
 * Created by Niels on 2017/11/6.
 *
 */
public abstract class AbstractEventBusModule extends BaseNulsModule {
    public AbstractEventBusModule() {
        super(NulsConstant.MODULE_ID_EVENT_BUS);
    }
}
