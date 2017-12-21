package io.nuls.account.event.filter;

import io.nuls.account.entity.event.AliasEvent;
import io.nuls.event.bus.event.filter.NulsFilter;
import io.nuls.event.bus.event.filter.NulsFilterChain;

/**
 * @author vivi
 * @date 2017/12/18.
 */
public class AliasFilter implements NulsFilter<AliasEvent> {

    private static AliasFilter filter = new AliasFilter();

    private AliasFilter() {

    }

    public static NulsFilter getInstance() {
        return filter;
    }

    @Override
    public void doFilter(AliasEvent event, NulsFilterChain chain) {

    }
}
