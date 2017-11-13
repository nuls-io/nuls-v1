package io.nuls.consensus.module;

import io.nuls.consensus.event.*;
import io.nuls.core.module.NulsModule;

/**
 * Created by Niels on 2017/11/7.
 * nuls.io
 */
public abstract class ConsensusModule extends NulsModule {
    public ConsensusModule() {
        super("consensus");
        this.registerEvent((short) 1, BaseConsensusEvent.class);
        this.registerEvent((short) 2, JoinConsensusEvent.class);
        this.registerEvent((short) 3, ExitConsensusEvent.class);
        this.registerEvent((short) 4, BlockEvent.class);
        this.registerEvent((short) 5, BlockHeaderEvent.class);
        this.registerEvent((short) 6, GetBlockEvent.class);
        this.registerEvent((short) 7, RedPunishConsensusEvent.class);
        this.registerEvent((short) 8, YellowPunishConsensusEvent.class);
    }

}
