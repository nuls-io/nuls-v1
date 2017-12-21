package io.nuls.consensus.handler.filter;

import io.nuls.consensus.constant.ConsensusStatusEnum;
import io.nuls.consensus.entity.ConsensusStatusInfo;
import io.nuls.consensus.event.JoinConsensusEvent;
import io.nuls.consensus.service.impl.PocConsensusServiceImpl;
import io.nuls.consensus.service.intf.ConsensusService;
import io.nuls.event.bus.bus.filter.NulsBusFilter;
import io.nuls.event.bus.bus.filter.NulsBusFilterChain;

/**
 * @author Niels
 * @date 2017/12/6
 */
public class AllreadyJoinConsensusBusFilter implements NulsBusFilter<JoinConsensusEvent> {

    private static final AllreadyJoinConsensusBusFilter INSTANCE = new AllreadyJoinConsensusBusFilter();

    private ConsensusService consensusService = PocConsensusServiceImpl.getInstance();
    private AllreadyJoinConsensusBusFilter(){}
    public static final AllreadyJoinConsensusBusFilter getInstance(){
        return INSTANCE;
    }
    @Override
    public void doFilter(JoinConsensusEvent event, NulsBusFilterChain chain) {
        ConsensusStatusInfo info = consensusService.getConsensusInfo(event.getEventBody().getTxData().getAddress());
        if (info.getStatus() != ConsensusStatusEnum.NOT_IN.getCode()) {
            return;
        }
        chain.doFilter(event);
    }
}
