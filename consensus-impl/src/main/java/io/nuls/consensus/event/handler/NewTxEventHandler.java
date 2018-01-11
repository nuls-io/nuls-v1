package io.nuls.consensus.event.handler;

import io.nuls.consensus.cache.manager.tx.ReceivedTxCacheManager;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.constant.SeverityLevelEnum;
import io.nuls.core.context.NulsContext;
import io.nuls.core.validate.ValidateResult;
import io.nuls.event.bus.handler.AbstractEventHandler;
import io.nuls.ledger.event.TransactionEvent;
import io.nuls.ledger.service.intf.LedgerService;
import io.nuls.network.service.NetworkService;

/**
 * @author Niels
 * @date 2018/1/8
 */
public class NewTxEventHandler extends AbstractEventHandler<TransactionEvent> {

    private static NewTxEventHandler INSTANCE = new NewTxEventHandler();

    private ReceivedTxCacheManager cacheManager = ReceivedTxCacheManager.getInstance();

    private LedgerService txService = NulsContext.getInstance().getService(LedgerService.class);

    private NetworkService networkService = NulsContext.getInstance().getService(NetworkService.class);

    private NewTxEventHandler() {
    }

    public static NewTxEventHandler getInstance() {
        return INSTANCE;
    }

    @Override
    public void onEvent(TransactionEvent event, String fromId) {
        Transaction tx = event.getEventBody();
        if (null == tx) {
            return;
        }
        ValidateResult result = txService.verifyTx(tx);
        if(result.isFailed()){
            if(result.getLevel()== SeverityLevelEnum.NORMAL_FOUL){
                networkService.removeNode(fromId);
            }else if(result.getLevel()==SeverityLevelEnum.FLAGRANT_FOUL){
                networkService.removeNode(fromId);
            }
        }
        cacheManager.putTx(tx);
    }
}
