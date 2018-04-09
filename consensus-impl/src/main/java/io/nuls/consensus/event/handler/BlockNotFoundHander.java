package io.nuls.consensus.event.handler;

import io.nuls.consensus.download.DownloadCacheHandler;
import io.nuls.consensus.event.BlockNotFoundEvent;
import io.nuls.core.exception.NulsException;
import io.nuls.event.bus.handler.AbstractEventHandler;

/**
 * @author: Niels Wang
 * @date: 2018/4/9
 */
public class BlockNotFoundHander extends AbstractEventHandler<BlockNotFoundEvent> {
    /**
     * @param fromId hash of the node who send this event!
     */
    @Override
    public void onEvent(BlockNotFoundEvent event, String fromId) throws NulsException {
        DownloadCacheHandler.notFoundBlock(event.getEventBody().getDigestHex());
    }
}
