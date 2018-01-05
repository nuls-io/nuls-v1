package io.nuls.consensus.module;

import io.nuls.consensus.constant.ConsensusEventType;
import io.nuls.consensus.entity.TxGroup;
import io.nuls.consensus.event.*;
import io.nuls.core.constant.NulsConstant;
import io.nuls.core.module.BaseModuleBootstrap;

/**
 * @author Niels
 * @date 2017/11/7
 */
public abstract class AbstractConsensusModule extends BaseModuleBootstrap {
    public AbstractConsensusModule() {
        super(NulsConstant.MODULE_ID_CONSENSUS);
    }

}
