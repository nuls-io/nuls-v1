package io.nuls.consensus.handler.filter;

import io.nuls.consensus.event.YellowPunishConsensusEvent;
import io.nuls.event.bus.event.filter.NulsFilter;
import io.nuls.event.bus.event.filter.NulsFilterChain;

/**
 * @author Niels
 * @date 2017/12/6
 */
public class YellowPunishFilter implements NulsFilter<YellowPunishConsensusEvent> {
    @Override
    public void doFilter(YellowPunishConsensusEvent event, NulsFilterChain chain) {
        //todo
        chain.doFilter(event);
    }
}
