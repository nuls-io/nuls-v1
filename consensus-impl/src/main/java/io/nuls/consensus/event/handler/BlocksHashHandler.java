package io.nuls.consensus.event.handler;

import io.nuls.consensus.event.BlocksHashEvent;
import io.nuls.consensus.utils.DistributedBlockInfoRequestUtils;
import io.nuls.event.bus.handler.AbstractEventHandler;

/**
 * @author Niels
 * @date 2018/1/16
 */
public class BlocksHashHandler extends AbstractEventHandler<BlocksHashEvent> {

    @Override
    public void onEvent(BlocksHashEvent event, String fromId) {
        DistributedBlockInfoRequestUtils.getInstance().addBlockHashResponse(fromId, event.getEventBody());
    }
}
