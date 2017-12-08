package io.nuls.consensus.handler;

import io.nuls.consensus.event.ExitConsensusEvent;
import io.nuls.core.exception.NulsException;
import io.nuls.event.bus.event.handler.AbstractNetworkNulsEventHandler;

/**
 *
 * @author facjas
 * @date 2017/11/16
 */
public class ExitConsensusHandler extends AbstractNetworkNulsEventHandler<ExitConsensusEvent> {

    @Override
    public void onEvent(ExitConsensusEvent event,String formId) throws NulsException {
        //todo
    }
}
