package io.nuls.consensus.handler;

import io.nuls.consensus.event.ExitConsensusEvent;
import io.nuls.consensus.event.RegisterAgentEvent;
import io.nuls.core.exception.NulsException;
import io.nuls.event.bus.event.handler.AbstractNetworkNulsEventHandler;

/**
 *
 * @author facjas
 * @date 2017/11/16
 */
public class RegisterAgentHandler extends AbstractNetworkNulsEventHandler<RegisterAgentEvent> {

    @Override
    public void onEvent(RegisterAgentEvent event,String formId) throws NulsException {
        //todo

    }
}
