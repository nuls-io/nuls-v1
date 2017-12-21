package io.nuls.consensus.handler.filter;

import io.nuls.consensus.constant.ConsensusStatusEnum;
import io.nuls.consensus.entity.ConsensusStatusInfo;
import io.nuls.consensus.event.JoinConsensusEvent;
import io.nuls.consensus.service.impl.PocConsensusServiceImpl;
import io.nuls.consensus.service.intf.ConsensusService;
import io.nuls.event.bus.event.filter.NulsFilter;
import io.nuls.event.bus.event.filter.NulsFilterChain;

/**
 * @author Niels
 * @date 2017/12/6
 */
public class AllreadyJoinConsensusFilter implements NulsFilter<JoinConsensusEvent> {

    private static final AllreadyJoinConsensusFilter INSTANCE = new AllreadyJoinConsensusFilter();

    private ConsensusService consensusService = PocConsensusServiceImpl.getInstance();
    private AllreadyJoinConsensusFilter(){}
    public static final AllreadyJoinConsensusFilter getInstance(){
        return INSTANCE;
    }
    @Override
    public void doFilter(JoinConsensusEvent event, NulsFilterChain chain) {
        ConsensusStatusInfo info = consensusService.getConsensusInfo(event.getEventBody().getTxData().getAddress());
        if (info.getStatus() != ConsensusStatusEnum.NOT_IN.getCode()) {
            return;
        }
        chain.doFilter(event);
    }
}
