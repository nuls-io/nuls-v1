package io.nuls.event.bus.bus.filter;

import io.nuls.core.bus.BaseBusData;

/**
 * @author Niels
 * @date 2017/11/7
 */
public interface NulsBusFilter<T extends BaseBusData> {

    void doFilter(T data, NulsBusFilterChain chain);

}
