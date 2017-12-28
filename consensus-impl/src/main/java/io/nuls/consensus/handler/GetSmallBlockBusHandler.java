package io.nuls.consensus.handler;

import io.nuls.consensus.entity.AskSmallBlockData;
import io.nuls.consensus.entity.SmallBlockData;
import io.nuls.consensus.event.GetSmallBlockEvent;
import io.nuls.consensus.event.SmallBlockEvent;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.context.NulsContext;
import io.nuls.event.bus.bus.handler.AbstractEventBusHandler;
import io.nuls.event.bus.bus.service.intf.EventBroadcaster;
import io.nuls.ledger.service.intf.LedgerService;

import java.util.List;

/**
 * @author facjas
 * @date 2017/11/16
 */
public class GetSmallBlockBusHandler extends AbstractEventBusHandler<GetSmallBlockEvent> {

    private LedgerService ledgerService = NulsContext.getInstance().getService(LedgerService.class);
    private EventBroadcaster eventBroadcaster = NulsContext.getInstance().getService(EventBroadcaster.class);

    @Override
    public void onEvent(GetSmallBlockEvent event, String fromId) {
        AskSmallBlockData data = event.getEventBody();
        SmallBlockEvent blockEvent = new SmallBlockEvent();
        SmallBlockData blockData = new SmallBlockData();
        blockData.setBlockHash(data.getBlockHash());
        List<Transaction> txList = ledgerService.queryListByHashs(data.getTxHashList());
        blockData.setTxList(txList);
        blockEvent.setEventBody(blockData);
        eventBroadcaster.sendToPeer(blockEvent, fromId);
    }
}
