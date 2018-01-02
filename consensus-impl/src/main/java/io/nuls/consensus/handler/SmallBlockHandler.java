package io.nuls.consensus.handler;

import io.nuls.account.service.intf.AccountService;
import io.nuls.consensus.entity.RedPunishData;
import io.nuls.consensus.event.SmallBlockEvent;
import io.nuls.consensus.service.cache.BlockCacheService;
import io.nuls.consensus.service.cache.BlockHeaderCacheService;
import io.nuls.consensus.thread.ConsensusMeetingRunner;
import io.nuls.core.chain.entity.*;
import io.nuls.core.constant.SeverityLevelEnum;
import io.nuls.core.context.NulsContext;
import io.nuls.core.validate.ValidateResult;
import io.nuls.event.bus.bus.handler.AbstractEventBusHandler;
import io.nuls.ledger.service.intf.LedgerService;
import io.nuls.network.service.NetworkService;

import java.util.ArrayList;
import java.util.List;

/**
 * @author facjas
 * @date 2017/11/16
 */
public class SmallBlockHandler extends AbstractEventBusHandler<SmallBlockEvent> {
    private BlockHeaderCacheService headerCacheService = BlockHeaderCacheService.getInstance();
    private BlockCacheService blockCacheService = BlockCacheService.getInstance();
    private LedgerService ledgerService = NulsContext.getInstance().getService(LedgerService.class);
    private NetworkService networkService = NulsContext.getInstance().getService(NetworkService.class);
    private AccountService accountService = NulsContext.getInstance().getService(AccountService.class);

    @Override
    public void onEvent(SmallBlockEvent event, String fromId) {
        BlockHeader header = headerCacheService.getHeader(event.getEventBody().getBlockHash().getDigestHex());
        if (header == null) {
            return;
        }
        Block block = new Block();
        block.setHeader(header);
        List<Transaction> txs = new ArrayList<>();
        for (NulsDigestData txHash : header.getTxHashList()) {
            Transaction tx = ledgerService.getTxFromCache(txHash.getDigestHex());
            txs.add(tx);
        }
        block.setTxs(txs);
        ValidateResult<RedPunishData> vResult = block.verify();
        if (null==vResult||vResult.isFailed()) {
            networkService.removePeer(fromId);
            if (vResult.getLevel() == SeverityLevelEnum.FLAGRANT) {
                RedPunishData data = vResult.getObject();
                ConsensusMeetingRunner.putPunishData(data);
            }
            return;
        }
        blockCacheService.cacheBlock(block);
    }
}
