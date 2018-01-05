package io.nuls.consensus.event.filter;

import io.nuls.consensus.constant.ConsensusStatusEnum;
import io.nuls.consensus.entity.ConsensusStatusInfo;
import io.nuls.consensus.event.JoinConsensusEvent;
import io.nuls.consensus.service.impl.PocConsensusServiceImpl;
import io.nuls.consensus.service.intf.ConsensusService;
import io.nuls.event.bus.filter.NulsEventFilter;
import io.nuls.event.bus.filter.NulsEventFilterChain;

/**
 * @author Niels
 * @date 2017/12/6
 */
public class AllreadyJoinConsensusEventFilter implements NulsEventFilter<JoinConsensusEvent> {

    private static final AllreadyJoinConsensusEventFilter INSTANCE = new AllreadyJoinConsensusEventFilter();

    private ConsensusService consensusService = PocConsensusServiceImpl.getInstance();
    private AllreadyJoinConsensusEventFilter(){}
    public static final AllreadyJoinConsensusEventFilter getInstance(){
        return INSTANCE;
    }
    @Override
    public void doFilter(JoinConsensusEvent event, NulsEventFilterChain chain) {
        ConsensusStatusInfo info = consensusService.getConsensusInfo(event.getEventBody().getTxData().getAddress());
        if (info.getStatus() != ConsensusStatusEnum.NOT_IN.getCode()) {
            return;
        }
        chain.doFilter(event);
    }
}
