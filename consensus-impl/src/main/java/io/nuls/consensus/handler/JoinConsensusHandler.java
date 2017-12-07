package io.nuls.consensus.handler;

import io.nuls.consensus.event.JoinConsensusEvent;
import io.nuls.core.event.BaseNulsEvent;
import io.nuls.core.exception.NulsException;
import io.nuls.event.bus.event.handler.AbstractNetworkNulsEventHandler;

/**
 *
 * @author facjas
 * @date 2017/11/16
 */
public class JoinConsensusHandler extends AbstractNetworkNulsEventHandler<JoinConsensusEvent> {

    @Override
    public void onEvent(JoinConsensusEvent event) throws NulsException {
        //todo
    }
}
