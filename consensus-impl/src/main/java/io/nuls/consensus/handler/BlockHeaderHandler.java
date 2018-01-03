package io.nuls.consensus.handler;

import io.nuls.consensus.event.BlockHeaderEvent;
import io.nuls.consensus.service.cache.BlockHeaderCacheService;
import io.nuls.consensus.utils.DistributedBlockInfoRequestUtils;
import io.nuls.core.chain.entity.BlockHeader;
import io.nuls.core.context.NulsContext;
import io.nuls.event.bus.handler.AbstractNetworkEventHandler;
import io.nuls.event.bus.service.intf.NetworkEventBroadcaster;
import io.nuls.ledger.service.intf.LedgerService;

/**
 * @author facjas
 * @date 2017/11/16
 */
public class BlockHeaderHandler extends AbstractNetworkEventHandler<BlockHeaderEvent> {

    private BlockHeaderCacheService headerCacheService = BlockHeaderCacheService.getInstance();

    private LedgerService ledgerService = NulsContext.getInstance().getService(LedgerService.class);
    private NetworkEventBroadcaster networkEventBroadcaster = NulsContext.getInstance().getService(NetworkEventBroadcaster.class);

    @Override
    public void onEvent(BlockHeaderEvent event, String fromId) {
        if (DistributedBlockInfoRequestUtils.getInstance().addBlockHeader(fromId, event.getEventBody())) {
            return;
        }
        BlockHeader header = event.getEventBody();
        //todo 分叉处理
        //todo 收到区块头后的处理变更
        header.verify();
//        headerCacheService.cacheHeader(header);
//        GetTxGroupEvent smallBlockEvent = new GetTxGroupEvent();
//        AskTxGroupData data = new AskTxGroupData();
//        data.setBlockHash(header.getHash());
//        List<NulsDigestData> txHashList = new ArrayList<>();
//        for (NulsDigestData txHash : header.getTxHashList()) {
//            boolean exist = ledgerService.txExist(txHash.getDigestHex());
//            if (!exist) {
//                txHashList.add(txHash);
//            }
//        }
//        data.setTxHashList(txHashList);
//        smallBlockEvent.setEventBody(data);
//        eventBroadcaster.sendToPeer(smallBlockEvent, fromId);
    }
}
