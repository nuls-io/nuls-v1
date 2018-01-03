package io.nuls.account.event.handler;

import io.nuls.account.entity.event.AliasEvent;
import io.nuls.account.entity.tx.AliasTransaction;
import io.nuls.core.constant.SeverityLevelEnum;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.log.Log;
import io.nuls.core.validate.ValidateResult;
import io.nuls.event.bus.handler.AbstractNetworkEventHandler;
import io.nuls.event.bus.service.intf.NetworkEventBroadcaster;
import io.nuls.ledger.service.intf.LedgerService;

/**
 * @author vivi
 * @date 2017/12/18.
 */
public class AliasEventHandler extends AbstractNetworkEventHandler<AliasEvent> {

    private static AliasEventHandler handler = new AliasEventHandler();

    private AliasEventHandler() {
    }

    public static AliasEventHandler getInstance() {
        return handler;
    }

    private NetworkEventBroadcaster networkEventBroadcaster;

    private LedgerService ledgerService;

    public void setNetworkEventBroadcaster(NetworkEventBroadcaster networkEventBroadcaster) {
        this.networkEventBroadcaster = networkEventBroadcaster;
    }

    public void setLedgerService(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    @Override
    public void onEvent(AliasEvent event, String fromId)   {
        AliasTransaction tx = event.getEventBody();
        ValidateResult result = tx.verify();
        if (null==result||result.isFailed()) {
            if (SeverityLevelEnum.FLAGRANT.equals(result.getLevel())) {
               //todo networkService.removePeer(fromId);
            }
            return;
        }

        try {
            ledgerService.verifyAndCacheTx(tx);
        } catch (NulsException e) {
            Log.error(e);
            throw new NulsRuntimeException(e);
        }
        networkEventBroadcaster.broadcastHashAndCache(event, fromId);
    }
}
