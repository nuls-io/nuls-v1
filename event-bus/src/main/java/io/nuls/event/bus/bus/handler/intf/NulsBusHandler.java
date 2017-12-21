package io.nuls.event.bus.bus.handler.intf;

import io.nuls.core.bus.BaseBusData;
import io.nuls.event.bus.bus.filter.NulsBusFilter;
import io.nuls.event.bus.bus.filter.NulsBusFilterChain;

/**
 * @author Niels
 * @date 2017/11/15
 */
public interface NulsBusHandler<T extends BaseBusData> {

    void addFilter(NulsBusFilter<T> filter);

    NulsBusFilterChain getFilterChain();

    /**
     * @param event
     * @param fromId hash of the peer who send this event!
     */
    void onEvent(T event, String fromId);
}
