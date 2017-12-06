package io.nuls.consensus.handler;

import io.nuls.consensus.event.BlockEvent;
import io.nuls.consensus.event.BlockHeaderEvent;
import io.nuls.core.exception.NulsException;
import io.nuls.event.bus.event.handler.AbstractNetworkNulsEventHandler;

/**
 *
 * @author facjas
 * @date 2017/11/16
 */
public class BlockHeaderHandler extends AbstractNetworkNulsEventHandler<BlockHeaderEvent> {

    @Override
    public void onEvent(BlockHeaderEvent event) throws NulsException {
        //todo

    }
}
