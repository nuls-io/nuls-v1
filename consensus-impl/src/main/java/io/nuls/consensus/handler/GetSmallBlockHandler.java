package io.nuls.consensus.handler;

import io.nuls.consensus.entity.AskSmallBlockData;
import io.nuls.consensus.entity.SmallBlockData;
import io.nuls.consensus.event.GetSmallBlockEvent;
import io.nuls.consensus.event.SmallBlockEvent;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsException;
import io.nuls.event.bus.event.handler.AbstractNetworkNulsEventHandler;
import io.nuls.event.bus.event.service.intf.EventService;
import io.nuls.ledger.service.intf.LedgerService;

import java.util.List;

/**
 *
 * @author facjas
 * @date 2017/11/16
 */
public class GetSmallBlockHandler extends AbstractNetworkNulsEventHandler<GetSmallBlockEvent> {

    private LedgerService ledgerService = NulsContext.getInstance().getService(LedgerService.class);
    private EventService eventService = NulsContext.getInstance().getService(EventService.class);
    @Override
    public void onEvent(GetSmallBlockEvent event,String fromId) throws NulsException {
        AskSmallBlockData data = event.getEventBody();
        SmallBlockEvent blockEvent = new SmallBlockEvent();
        SmallBlockData blockData = new SmallBlockData();
        blockData.setHeight(data.getHeight());
        List<Transaction> txList = ledgerService.queryListByHashs(data.getTxHashList());
        blockData.setTxList(txList);
        blockEvent.setEventBody(blockData);
        eventService.sendToPeer(blockEvent,fromId);
    }
}
