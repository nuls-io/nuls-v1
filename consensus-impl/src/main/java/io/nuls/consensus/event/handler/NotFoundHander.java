package io.nuls.consensus.event.handler;

import io.nuls.consensus.download.DownloadCacheHandler;
import io.nuls.consensus.event.NotFoundEvent;
import io.nuls.core.exception.NulsException;
import io.nuls.event.bus.handler.AbstractEventHandler;

/**
 * @author: Niels Wang
 * @date: 2018/4/9
 */
public class NotFoundHander extends AbstractEventHandler<NotFoundEvent> {
    /**
     * @param fromId hash of the node who send this event!
     */
    @Override
    public void onEvent(NotFoundEvent event, String fromId) throws NulsException {
        DownloadCacheHandler.notFoundBlock(event.getEventBody());
    }
}
