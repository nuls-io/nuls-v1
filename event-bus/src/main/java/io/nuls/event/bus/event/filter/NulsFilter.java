package io.nuls.event.bus.event.filter;

import io.nuls.core.bus.BaseBusData;

/**
 * @author Niels
 * @date 2017/11/7
 */
public interface NulsFilter<T extends BaseBusData> {

    void doFilter(T data, NulsFilterChain chain);

}
