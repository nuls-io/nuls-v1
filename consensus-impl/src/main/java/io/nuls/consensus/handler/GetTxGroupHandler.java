package io.nuls.consensus.handler;

import io.nuls.consensus.entity.TxGroup;
import io.nuls.consensus.entity.TxHashData;
import io.nuls.consensus.event.GetTxGroupEvent;
import io.nuls.consensus.event.TxGroupEvent;
import io.nuls.consensus.service.impl.BlockServiceImpl;
import io.nuls.consensus.service.intf.BlockService;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.event.bus.handler.AbstractNetworkEventHandler;
import io.nuls.event.bus.service.intf.NetworkEventBroadcaster;
import io.nuls.ledger.service.intf.LedgerService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author facjas
 * @date 2017/11/16
 */
public class GetTxGroupHandler extends AbstractNetworkEventHandler<GetTxGroupEvent> {

    private NetworkEventBroadcaster networkEventBroadcaster = NulsContext.getInstance().getService(NetworkEventBroadcaster.class);
    private BlockService blockService = BlockServiceImpl.getInstance();
    private LedgerService ledgerService = NulsContext.getInstance().getService(LedgerService.class);

    @Override
    public void onEvent(GetTxGroupEvent event, String fromId) {

        TxHashData eventBody = event.getEventBody();
        Block block = blockService.getBlock(eventBody.getBlockHash().getDigestHex());
        if (null == block) {
            return;
        }

        TxGroupEvent txGroupEvent = new TxGroupEvent();
        TxGroup txGroup = new TxGroup();
        txGroup.setBlockHash(block.getHeader().getHash());
        List<Transaction> txList = getTxList(block,eventBody.getTxHashList());
        txGroup.setTxList(txList);
        txGroupEvent.setEventBody(txGroup);
        networkEventBroadcaster.sendToPeer(txGroupEvent, fromId);
    }

    private List<Transaction> getTxList(Block block, List<NulsDigestData> txHashList) {
        List<Transaction> txList = new ArrayList<>();
        Map<String, Integer> allTxMap = new HashMap<>();
        for (int i = 0; i < block.getHeader().getTxCount(); i++) {
            Transaction tx = block.getTxs().get(i);
            allTxMap.put(tx.getHash().getDigestHex(), i);
        }
        for (NulsDigestData hash : txHashList) {
            txList.add(block.getTxs().get(allTxMap.get(hash.getDigestHex())));
        }
        if (txList.size() != txHashList.size()) {
            throw new NulsRuntimeException(ErrorCode.DATA_ERROR);
        }
        return txList;
    }
}
