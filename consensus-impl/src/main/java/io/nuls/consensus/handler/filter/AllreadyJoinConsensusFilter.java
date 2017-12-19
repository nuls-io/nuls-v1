package io.nuls.consensus.handler.filter;

import io.nuls.consensus.event.JoinConsensusEvent;
import io.nuls.consensus.service.impl.PocConsensusServiceImpl;
import io.nuls.consensus.service.intf.ConsensusService;
import io.nuls.event.bus.event.filter.NulsEventFilter;
import io.nuls.event.bus.event.filter.NulsEventFilterChain;

/**
 * @author Niels
 * @date 2017/12/6
 */
public class AllreadyJoinConsensusFilter implements NulsEventFilter<JoinConsensusEvent> {

    private ConsensusService consensusService = PocConsensusServiceImpl.getInstance();

    @Override
    public void doFilter(JoinConsensusEvent event, NulsEventFilterChain chain) {
        //todo


        chain.doFilter(event);
    }
}
