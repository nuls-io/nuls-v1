package io.nuls.consensus.handler;

import io.nuls.account.service.intf.AccountService;
import io.nuls.consensus.entity.RedPunishData;
import io.nuls.consensus.entity.TxGroup;
import io.nuls.consensus.entity.TxHashData;
import io.nuls.consensus.event.GetSmallBlockEvent;
import io.nuls.consensus.event.TxGroupEvent;
import io.nuls.consensus.service.cache.BlockCacheService;
import io.nuls.consensus.service.cache.BlockHeaderCacheService;
import io.nuls.consensus.service.cache.SmallBlockCacheService;
import io.nuls.consensus.thread.ConsensusMeetingRunner;
import io.nuls.core.chain.entity.*;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.constant.NulsConstant;
import io.nuls.core.constant.SeverityLevelEnum;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.validate.ValidateResult;
import io.nuls.event.bus.handler.AbstractNetworkEventHandler;
import io.nuls.event.bus.service.intf.NetworkEventBroadcaster;
import io.nuls.ledger.service.intf.LedgerService;
import io.nuls.network.service.NetworkService;

import java.util.ArrayList;
import java.util.List;

/**
 * @author facjas
 * @date 2017/11/16
 */
public class TxGroupHandler extends AbstractNetworkEventHandler<TxGroupEvent> {
    private SmallBlockCacheService smallBlockCacheService = SmallBlockCacheService.getInstance();
    private BlockHeaderCacheService headerCacheService = BlockHeaderCacheService.getInstance();
    private BlockCacheService blockCacheService = BlockCacheService.getInstance();
    private LedgerService ledgerService = NulsContext.getInstance().getService(LedgerService.class);
    private NetworkService networkService = NulsContext.getInstance().getService(NetworkService.class);
    @Override
    public void onEvent(TxGroupEvent event, String fromId) {
        TxGroup txGroup = event.getEventBody();
        BlockHeader header = headerCacheService.getHeader(event.getEventBody().getBlockHash().getDigestHex());
        if (header == null) {
            return;
        }
        SmallBlock smallBlock = smallBlockCacheService.getSmallBlock(header.getHash().getDigestHex());
        if(null==smallBlock){
            return;
        }
        Block block = new Block();
        block.setHeader(header);
        List<Transaction> txs = new ArrayList<>();
        for (NulsDigestData txHash : smallBlock.getTxHashList()) {
            Transaction tx = ledgerService.getTxFromCache(txHash.getDigestHex());
            if(null==tx){
                tx = txGroup.getTx(txHash.getDigestHex());
            }
            if(null==tx){
                throw new NulsRuntimeException(ErrorCode.DATA_ERROR);
            }
            txs.add(tx);
        }
        block.setTxs(txs);
        ValidateResult<RedPunishData> vResult = block.verify();
        if (null==vResult||vResult.isFailed()) {
            networkService.removeNode(fromId);
            if (vResult.getLevel() == SeverityLevelEnum.FLAGRANT) {
                RedPunishData data = vResult.getObject();
                ConsensusMeetingRunner.putPunishData(data);
            }
            return;
        }
        blockCacheService.cacheBlock(block);
    }
}
