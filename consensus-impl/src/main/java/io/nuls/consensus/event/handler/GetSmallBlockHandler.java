package io.nuls.consensus.event.handler;

import io.nuls.consensus.event.GetSmallBlockEvent;
import io.nuls.consensus.event.SmallBlockEvent;
import io.nuls.consensus.service.impl.BlockServiceImpl;
import io.nuls.consensus.service.intf.BlockService;
import io.nuls.core.chain.entity.*;
import io.nuls.core.context.NulsContext;
import io.nuls.event.bus.handler.AbstractNetworkEventHandler;
import io.nuls.event.bus.service.intf.NetworkEventBroadcaster;
import io.nuls.ledger.service.intf.LedgerService;

/**
 * @author facjas
 * @date 2017/11/16
 */
public class GetSmallBlockHandler extends AbstractNetworkEventHandler<GetSmallBlockEvent> {

    private NetworkEventBroadcaster networkEventBroadcaster = NulsContext.getInstance().getService(NetworkEventBroadcaster.class);
    private BlockService blockService = BlockServiceImpl.getInstance();
    private LedgerService ledgerService = NulsContext.getInstance().getService(LedgerService.class);

    @Override
    public void onEvent(GetSmallBlockEvent event, String fromId) {
        long height = event.getEventBody().getVal();
        Block block = blockService.getBlock(height);
        SmallBlockEvent smallBlockEvent = new SmallBlockEvent();
        SmallBlock smallBlock = new SmallBlock();
        smallBlock.setBlockHash(block.getHeader().getHash());
        smallBlock.setTxHashList(block.getTxHashList());
        smallBlock.setTxCount(block.getHeader().getTxCount());
        smallBlockEvent.setEventBody(smallBlock);
        networkEventBroadcaster.sendToPeer(smallBlockEvent, fromId);
//        TxHashData eventBody = event.getEventBody();
//        Block block = blockService.getBlock(eventBody.getBlockHeight());
//        if (null == block) {
//            return;
//        }
//
//        SmallBlockEvent blockEvent = new SmallBlockEvent();
//        SmallBlock smallBlock = new SmallBlock();
//        smallBlock.setBlockHash(block.getHeader().getHash());
//        List<Transaction> txList = getTxList(block,eventBody.getTxHashList());
//        smallBlock.setTxList(txList);
//        smallBlock.setTxCount(txList.size());
//        blockEvent.setEventBody(smallBlock);
//        networkEventBroadcaster.sendToPeer(blockEvent, fromId);
    }

//    private List<Transaction> getTxList(Block block, List<NulsDigestData> txHashList) {
//        List<Transaction> txList = new ArrayList<>();
//        Map<String, Integer> allTxMap = new HashMap<>();
//        for (int i = 0; i < block.getHeader().getTxCount(); i++) {
//            Transaction tx = block.getTxs().get(i);
//            allTxMap.put(tx.getHash().getDigestHex(), i);
//        }
//        for (NulsDigestData hash : txHashList) {
//            txList.add(block.getTxs().get(allTxMap.get(hash.getDigestHex())));
//        }
//        if (txList.size() != txHashList.size()) {
//            throw new NulsRuntimeException(ErrorCode.DATA_ERROR);
//        }
//        return txList;
//    }
}
