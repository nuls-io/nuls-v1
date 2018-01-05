package io.nuls.consensus.event.filter;

import io.nuls.consensus.event.YellowPunishConsensusEvent;
import io.nuls.event.bus.filter.NulsEventFilter;
import io.nuls.event.bus.filter.NulsEventFilterChain;

/**
 * @author Niels
 * @date 2017/12/6
 */
public class YellowPunishEventFilter implements NulsEventFilter<YellowPunishConsensusEvent> {
    @Override
    public void doFilter(YellowPunishConsensusEvent event, NulsEventFilterChain chain) {
        //todo
        chain.doFilter(event);
    }
}
