package io.nuls.consensus.handler;

import io.nuls.consensus.event.BlockEvent;
import io.nuls.consensus.event.RegisterAgentEvent;
import io.nuls.core.exception.NulsException;
import io.nuls.event.bus.event.handler.AbstractNetworkNulsEventHandler;

/**
 *
 * @author facjas
 * @date 2017/11/16
 */
public class BlockEventHandler extends AbstractNetworkNulsEventHandler<BlockEvent> {

    @Override
    public void onEvent(BlockEvent event) throws NulsException {
        //todo

    }
}
