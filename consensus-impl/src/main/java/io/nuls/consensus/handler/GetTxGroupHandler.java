package io.nuls.consensus.handler;

import io.nuls.consensus.entity.AskTxGroupData;
import io.nuls.consensus.entity.TxGroup;
import io.nuls.consensus.event.GetTxGroupEvent;
import io.nuls.consensus.event.TxGroupEvent;
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
public class GetTxGroupHandler extends AbstractEventBusHandler<GetTxGroupEvent> {

    private LedgerService ledgerService = NulsContext.getInstance().getService(LedgerService.class);
    private EventBroadcaster eventBroadcaster = NulsContext.getInstance().getService(EventBroadcaster.class);

    @Override
    public void onEvent(GetTxGroupEvent event, String fromId) {
        AskTxGroupData data = event.getEventBody();
        TxGroupEvent blockEvent = new TxGroupEvent();
        TxGroup blockData = new TxGroup();
        blockData.setBlockHash(data.getBlockHash());
        List<Transaction> txList = ledgerService.queryListByHashs(data.getTxHashList());
        blockData.setTxList(txList);
        blockEvent.setEventBody(blockData);
        eventBroadcaster.sendToPeer(blockEvent, fromId);
    }
}
