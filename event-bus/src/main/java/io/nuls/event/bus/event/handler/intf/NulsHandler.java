package io.nuls.event.bus.event.handler.intf;

import io.nuls.core.bus.BaseBusData;
import io.nuls.core.event.BaseNulsEvent;
import io.nuls.event.bus.event.filter.NulsFilter;
import io.nuls.event.bus.event.filter.NulsFilterChain;

/**
 * @author Niels
 * @date 2017/11/15
 */
public interface NulsHandler<T extends BaseBusData> {

    void addFilter(NulsFilter<T> filter);

    NulsFilterChain getFilterChain();

    /**
     * @param event
     * @param fromId hash of the peer who send this event!
     */
    void onEvent(T event, String fromId);
}
