package io.nuls.consensus.event.handler;

import io.nuls.consensus.cache.manager.tx.ReceivedTxCacheManager;
import io.nuls.consensus.entity.RedPunishData;
import io.nuls.consensus.entity.TxGroup;
import io.nuls.consensus.event.TxGroupEvent;
import io.nuls.consensus.cache.manager.block.BlockCacheManager;
import io.nuls.consensus.thread.ConsensusMeetingRunner;
import io.nuls.core.chain.entity.*;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.constant.SeverityLevelEnum;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.validate.ValidateResult;
import io.nuls.event.bus.handler.AbstractEventHandler;
import io.nuls.network.service.NetworkService;

import java.util.ArrayList;
import java.util.List;

/**
 * @author facjas
 * @date 2017/11/16
 */
public class TxGroupHandler extends AbstractEventHandler<TxGroupEvent> {
    private BlockCacheManager blockCacheManager = BlockCacheManager.getInstance();
    private ReceivedTxCacheManager txCacheManager = ReceivedTxCacheManager.getInstance();
    private NetworkService networkService = NulsContext.getInstance().getService(NetworkService.class);
    @Override
    public void onEvent(TxGroupEvent event, String fromId) {
        TxGroup txGroup = event.getEventBody();
        BlockHeader header = blockCacheManager.getBlockHeader(event.getEventBody().getBlockHash().getDigestHex());
        if (header == null) {
            return;
        }
        SmallBlock smallBlock = blockCacheManager.getSmallBlock(header.getHash().getDigestHex());
        if(null==smallBlock){
            return;
        }
        Block block = new Block();
        block.setHeader(header);
        List<Transaction> txs = new ArrayList<>();
        for (NulsDigestData txHash : smallBlock.getTxHashList()) {
            Transaction tx = txCacheManager.getTx(txHash);
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
            if (vResult.getLevel() == SeverityLevelEnum.FLAGRANT_FOUL) {
                RedPunishData data = vResult.getObject();
                ConsensusMeetingRunner.putPunishData(data);
            }
            return;
        }
        blockCacheManager.cacheBlock(block);
    }
}
