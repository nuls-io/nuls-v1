package io.nuls.consensus.event.handler;

import io.nuls.consensus.event.GetSmallBlockRequest;
import io.nuls.consensus.event.SmallBlockEvent;
import io.nuls.consensus.service.intf.BlockService;
import io.nuls.core.chain.entity.*;
import io.nuls.core.context.NulsContext;
import io.nuls.event.bus.handler.AbstractEventHandler;
import io.nuls.event.bus.service.intf.EventBroadcaster;
import io.nuls.ledger.service.intf.LedgerService;

/**
 * @author facjas
 * @date 2017/11/16
 */
public class GetSmallBlockHandler extends AbstractEventHandler<GetSmallBlockRequest> {

    private EventBroadcaster eventBroadcaster = NulsContext.getInstance().getService(EventBroadcaster.class);
    private BlockService blockService = NulsContext.getInstance().getService(BlockService.class);

    @Override
    public void onEvent(GetSmallBlockRequest event, String fromId) {
        Block block = blockService.getBlock(event.getEventBody().getDigestHex());
        SmallBlockEvent smallBlockEvent = new SmallBlockEvent();
        SmallBlock smallBlock = new SmallBlock();
        smallBlock.setBlockHash(block.getHeader().getHash());
        smallBlock.setTxHashList(block.getTxHashList());
        smallBlock.setTxCount(block.getHeader().getTxCount());
        smallBlockEvent.setEventBody(smallBlock);
        eventBroadcaster.sendToNode(smallBlockEvent, fromId);
    }
}
