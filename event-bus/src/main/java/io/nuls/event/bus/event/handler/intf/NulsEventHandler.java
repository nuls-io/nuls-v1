package io.nuls.event.bus.event.handler.intf;

import io.nuls.core.event.BaseNulsEvent;
import io.nuls.core.exception.NulsException;
import io.nuls.event.bus.event.filter.NulsEventFilter;
import io.nuls.event.bus.event.filter.NulsEventFilterChain;

/**
 *
 * @author Niels
 * @date 2017/11/15
 */
public interface NulsEventHandler<T extends BaseNulsEvent> {

    void addFilter(NulsEventFilter<T> filter);

    NulsEventFilterChain getFilterChain();

    /**
     * @param event
     * @param fromId hash of the peer who send this event!
     */
    void onEvent(T event,String fromId) ;
}
