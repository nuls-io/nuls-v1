package io.nuls.consensus.module;

import io.nuls.consensus.constant.ConsensusEventType;
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
        this.registerBusDataClass(ConsensusEventType.BLOCK, BlockEvent.class);
        this.registerBusDataClass(ConsensusEventType.BLOCK_HEADER, BlockHeaderEvent.class);
        this.registerBusDataClass(ConsensusEventType.SMALL_BLOCK, SmallBlockEvent.class);
        this.registerBusDataClass(ConsensusEventType.GET_SMALL_BLOCK, GetSmallBlockEvent.class);
        this.registerBusDataClass(ConsensusEventType.GET_BLOCK, GetBlockEvent.class);

    }

}
