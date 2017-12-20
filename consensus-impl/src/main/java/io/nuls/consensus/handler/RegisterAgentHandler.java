package io.nuls.consensus.handler;

import io.nuls.consensus.entity.tx.RegisterAgentTransaction;
import io.nuls.consensus.event.RegisterAgentEvent;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.log.Log;
import io.nuls.event.bus.event.handler.AbstractEventHandler;
import io.nuls.event.bus.event.service.intf.EventService;
import io.nuls.ledger.service.intf.LedgerService;

/**
 *
 * @author facjas
 * @date 2017/11/16
 */
public class RegisterAgentHandler extends AbstractEventHandler<RegisterAgentEvent> {
    private LedgerService ledgerService = NulsContext.getInstance().getService(LedgerService.class);
    private EventService eventService = NulsContext.getInstance().getService(EventService.class);

    @Override
    public void onEvent(RegisterAgentEvent event,String fromId)   {
        RegisterAgentTransaction tx = event.getEventBody();
        try {
            ledgerService.verifyAndCacheTx(tx);
        } catch (NulsException e) {
            Log.error(e);
            return;
        }
        this.eventService.broadcastHashAndCache(event);
    }
}
