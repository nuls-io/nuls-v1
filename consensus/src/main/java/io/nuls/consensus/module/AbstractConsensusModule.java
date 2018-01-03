package io.nuls.consensus.module;

import io.nuls.consensus.constant.ConsensusEventType;
import io.nuls.consensus.entity.TxGroup;
import io.nuls.consensus.event.*;
import io.nuls.core.constant.NulsConstant;
import io.nuls.core.module.BaseNulsModule;

/**
 * @author Niels
 * @date 2017/11/7
 */
public abstract class AbstractConsensusModule extends BaseNulsModule {
    public AbstractConsensusModule() {
        super(NulsConstant.MODULE_ID_CONSENSUS);
        this.publish(ConsensusEventType.BLOCK, BlockEvent.class);
        this.publish(ConsensusEventType.BLOCK_HEADER, BlockHeaderEvent.class);
        this.publish(ConsensusEventType.SMALL_BLOCK, SmallBlockEvent.class);
        this.publish(ConsensusEventType.TX_GROUP, TxGroupEvent.class);
        this.publish(ConsensusEventType.GET_SMALL_BLOCK, GetSmallBlockEvent.class);
        this.publish(ConsensusEventType.GET_BLOCK, GetBlockEvent.class);
        this.publish(ConsensusEventType.GET_TX_GROUP, GetTxGroupEvent.class);
        this.publish(ConsensusEventType.GET_BLOCK_HEADER, GetBlockHeaderEvent.class);

    }

}
