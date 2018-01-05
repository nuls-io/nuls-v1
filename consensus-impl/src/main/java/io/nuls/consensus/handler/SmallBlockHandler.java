package io.nuls.consensus.handler;

import io.nuls.consensus.entity.TxHashData;
import io.nuls.consensus.event.GetSmallBlockEvent;
import io.nuls.consensus.event.GetTxGroupEvent;
import io.nuls.consensus.event.SmallBlockEvent;
import io.nuls.consensus.service.impl.BlockServiceImpl;
import io.nuls.consensus.service.intf.BlockService;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.context.NulsContext;
import io.nuls.event.bus.handler.AbstractNetworkEventHandler;
import io.nuls.event.bus.service.intf.NetworkEventBroadcaster;
import io.nuls.ledger.service.intf.LedgerService;

import java.util.ArrayList;
import java.util.List;

/**
 * @author facjas
 * @date 2017/11/16
 */
public class SmallBlockHandler extends AbstractNetworkEventHandler<SmallBlockEvent> {
    private NetworkEventBroadcaster broadcaster = NulsContext.getInstance().getService(NetworkEventBroadcaster.class);
    private LedgerService ledgerService = NulsContext.getInstance().getService(LedgerService.class);

    @Override
    public void onEvent(SmallBlockEvent event, String fromId) {
        GetTxGroupEvent getTxGroupEvent = new GetTxGroupEvent();
        TxHashData data = new TxHashData();
        data.setBlockHash(event.getEventBody().getBlockHash());
        List<NulsDigestData> txHashList = new ArrayList<>();
        for (NulsDigestData txHash : event.getEventBody().getTxHashList()) {
            boolean exist = ledgerService.txExist(txHash.getDigestHex());
            if (!exist) {
                txHashList.add(txHash);
            }
        }
        data.setTxHashList(txHashList);
        getTxGroupEvent.setEventBody(data);
        broadcaster.sendToNode(getTxGroupEvent, fromId);
    }
}
