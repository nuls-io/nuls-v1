package io.nuls.consensus.module;

import io.nuls.consensus.constant.ConsensusConstant;
import io.nuls.consensus.constant.ConsensusEventType;
import io.nuls.consensus.event.*;
import io.nuls.consensus.tx.ExitConsensusTransaction;
import io.nuls.consensus.tx.JoinConsensusTransaction;
import io.nuls.core.constant.NulsConstant;
import io.nuls.core.module.BaseNulsModule;

/**
 * @author Niels
 * @date 2017/11/7
 */
public abstract class AbstractConsensusModule extends BaseNulsModule {
    public AbstractConsensusModule() {
        super(NulsConstant.MODULE_ID_CONSENSUS);
        this.registerEvent(ConsensusEventType.JOIN, JoinConsensusEvent.class);
        this.registerEvent(ConsensusEventType.EXIT, ExitConsensusEvent.class);
        this.registerEvent(ConsensusEventType.BLOCK, BlockEvent.class);
        this.registerEvent(ConsensusEventType.BLOCK_HEADER, BlockHeaderEvent.class);
        this.registerEvent(ConsensusEventType.SMALL_BLOCK, SmallBlockEvent.class);
        this.registerEvent(ConsensusEventType.GET_SMALL_BLOCK, GetSmallBlockEvent.class);
        this.registerEvent(ConsensusEventType.GET_BLOCK, GetBlockEvent.class);
        
        this.registerTransaction(ConsensusConstant.TX_TYPE_JOIN_CONSENSUS, JoinConsensusTransaction.class);
        this.registerTransaction(ConsensusConstant.TX_TYPE_EXIT_CONSENSUS, ExitConsensusTransaction.class);
    }

}
