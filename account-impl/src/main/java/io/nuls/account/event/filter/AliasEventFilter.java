package io.nuls.account.event.filter;

import io.nuls.account.entity.event.AliasEvent;
import io.nuls.event.bus.event.filter.NulsEventFilter;
import io.nuls.event.bus.event.filter.NulsEventFilterChain;

/**
 * @author vivi
 * @date 2017/12/18.
 */
public class AliasEventFilter implements NulsEventFilter<AliasEvent> {

    private static AliasEventFilter filter = new AliasEventFilter();

    private AliasEventFilter() {

    }

    public static NulsEventFilter getInstance() {
        return filter;
    }

    @Override
    public void doFilter(AliasEvent event, NulsEventFilterChain chain) {

    }
}
