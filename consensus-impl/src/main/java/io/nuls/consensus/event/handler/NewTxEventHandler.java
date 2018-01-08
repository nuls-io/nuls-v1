package io.nuls.consensus.event.handler;

import io.nuls.consensus.cache.manager.tx.ReceivedTxCacheManager;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.constant.SeverityLevelEnum;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.tx.serivce.CommonTransactionService;
import io.nuls.core.utils.log.Log;
import io.nuls.core.validate.ValidateResult;
import io.nuls.event.bus.handler.AbstractEventHandler;
import io.nuls.ledger.event.TransactionEvent;
import io.nuls.network.service.NetworkService;

/**
 * @author Niels
 * @date 2018/1/8
 */
public class NewTxEventHandler extends AbstractEventHandler<TransactionEvent> {

    private static NewTxEventHandler INSTANCE = new NewTxEventHandler();

    private ReceivedTxCacheManager cacheManager = ReceivedTxCacheManager.getInstance();

    private CommonTransactionService txService = NulsContext.getInstance().getService(CommonTransactionService.class);

    private NetworkService networkService = NulsContext.getInstance().getService(NetworkService.class);

    private NewTxEventHandler() {
    }

    public static NewTxEventHandler getInstance() {
        return INSTANCE;
    }

    @Override
    public void onEvent(TransactionEvent event, String fromId) {
        // todo auto-generated method stub(niels)
        Transaction tx = event.getEventBody();
        if (null == tx) {
            return;
        }
        ValidateResult result = txService.verify(tx);
        if(result.isFailed()){
            if(result.getLevel()== SeverityLevelEnum.FOUL){
                networkService.removeNode(fromId);
            }else if(result.getLevel()==SeverityLevelEnum.FLAGRANT){
                networkService.removeNode(fromId);
            }
        }
        cacheManager.putTx(tx);
        try {
            txService.approval(tx);
        } catch (NulsException e) {
            Log.error(e);
        }
    }
}
