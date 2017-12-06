package io.nuls.consensus.handler;

import io.nuls.consensus.event.BlockEvent;
import io.nuls.consensus.event.GetBlockEvent;
import io.nuls.core.exception.NulsException;
import io.nuls.event.bus.event.handler.AbstractNetworkNulsEventHandler;

/**
 *
 * @author facjas
 * @date 2017/11/16
 */
public class GetBlockHandler extends AbstractNetworkNulsEventHandler<GetBlockEvent> {

    @Override
    public void onEvent(GetBlockEvent event) throws NulsException {
        //todo

    }
}
