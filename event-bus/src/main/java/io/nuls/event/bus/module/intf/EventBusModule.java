package io.nuls.event.bus.module.intf;

import io.nuls.core.module.NulsModule;

/**
 * Created by Niels on 2017/11/6.
 *
 */
public abstract class EventBusModule extends NulsModule {
    public EventBusModule() {
        super("event-bus");
    }
}
