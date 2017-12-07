package io.nuls.consensus.handler;

import io.nuls.consensus.event.GetBlockEvent;
import io.nuls.consensus.event.RedPunishConsensusEvent;
import io.nuls.core.exception.NulsException;
import io.nuls.event.bus.event.handler.AbstractNetworkNulsEventHandler;

/**
 *
 * @author facjas
 * @date 2017/11/16
 */
public class RedPunishHandler extends AbstractNetworkNulsEventHandler<RedPunishConsensusEvent> {

    @Override
    public void onEvent(RedPunishConsensusEvent event) throws NulsException {
        //todo

    }
}
