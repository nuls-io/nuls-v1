package io.nuls.event.bus.handler.intf;

import io.nuls.core.event.BaseEvent;
import io.nuls.event.bus.filter.NulsEventFilter;
import io.nuls.event.bus.filter.NulsEventFilterChain;

/**
 * @author Niels
 * @date 2017/11/15
 */
public interface NulsEventHandler<T extends BaseEvent> {

    void addFilter(NulsEventFilter<T> filter);

    NulsEventFilterChain getFilterChain();

    /**
     * @param event
     * @param fromId hash of the peer who send this event!
     */
    void onEvent(T event, String fromId);
}
