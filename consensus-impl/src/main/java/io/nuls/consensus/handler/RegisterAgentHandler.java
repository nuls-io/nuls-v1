package io.nuls.consensus.handler;

import io.nuls.consensus.entity.tx.RegisterAgentTransaction;
import io.nuls.consensus.event.RegisterAgentEvent;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.log.Log;
import io.nuls.event.bus.bus.handler.AbstractEventBusHandler;
import io.nuls.event.bus.bus.service.intf.EventBroadcaster;
import io.nuls.ledger.service.intf.LedgerService;

/**
 *
 * @author facjas
 * @date 2017/11/16
 */
public class RegisterAgentHandler extends AbstractEventBusHandler<RegisterAgentEvent> {
    private LedgerService ledgerService = NulsContext.getInstance().getService(LedgerService.class);
    private EventBroadcaster eventBroadcaster = NulsContext.getInstance().getService(EventBroadcaster.class);

    @Override
    public void onEvent(RegisterAgentEvent event,String fromId)   {
        RegisterAgentTransaction tx = event.getEventBody();
        try {
            ledgerService.verifyAndCacheTx(tx);
        } catch (NulsException e) {
            Log.error(e);
            return;
        }
        this.eventBroadcaster.broadcastHashAndCache(event);
    }
}
