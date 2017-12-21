package io.nuls.account.event.filter;

import io.nuls.account.entity.event.AliasEvent;
import io.nuls.event.bus.bus.filter.NulsBusFilter;
import io.nuls.event.bus.bus.filter.NulsBusFilterChain;

/**
 * @author vivi
 * @date 2017/12/18.
 */
public class AliasBusFilter implements NulsBusFilter<AliasEvent> {

    private static AliasBusFilter filter = new AliasBusFilter();

    private AliasBusFilter() {

    }

    public static NulsBusFilter getInstance() {
        return filter;
    }

    @Override
    public void doFilter(AliasEvent event, NulsBusFilterChain chain) {

    }
}
