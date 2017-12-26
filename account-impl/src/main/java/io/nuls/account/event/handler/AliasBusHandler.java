package io.nuls.account.event.handler;

import io.nuls.account.entity.event.AliasEvent;
import io.nuls.account.entity.tx.AliasTransaction;
import io.nuls.core.constant.SeverityLevelEnum;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.log.Log;
import io.nuls.core.validate.ValidateResult;
import io.nuls.event.bus.bus.handler.AbstractEventBusHandler;
import io.nuls.event.bus.bus.service.intf.EventBroadcaster;
import io.nuls.ledger.service.intf.LedgerService;

/**
 * @author vivi
 * @date 2017/12/18.
 */
public class AliasBusHandler extends AbstractEventBusHandler<AliasEvent> {

    private static AliasBusHandler handler = new AliasBusHandler();

    private AliasBusHandler() {
    }

    public static AliasBusHandler getInstance() {
        return handler;
    }

    private EventBroadcaster eventBroadcaster;

    private LedgerService ledgerService;

    public void setEventBroadcaster(EventBroadcaster eventBroadcaster) {
        this.eventBroadcaster = eventBroadcaster;
    }

    public void setLedgerService(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    @Override
    public void onEvent(AliasEvent event, String fromId)   {
        AliasTransaction tx = event.getEventBody();
        ValidateResult result = tx.verify();
        if (result.isFailed()) {
            if (SeverityLevelEnum.FLAGRANT.equals(result.getLevel())) {
               // networkService.removePeer(fromId);
            }
            return;
        }

        try {
            ledgerService.verifyAndCacheTx(tx);
        } catch (NulsException e) {
            Log.error(e);
            throw new NulsRuntimeException(e);
        }
        eventBroadcaster.broadcastHashAndCache(event, fromId);
    }
}
