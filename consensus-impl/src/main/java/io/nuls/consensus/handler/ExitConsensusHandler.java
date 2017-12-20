package io.nuls.consensus.handler;

import io.nuls.consensus.entity.tx.PocExitConsensusTransaction;
import io.nuls.consensus.event.ExitConsensusEvent;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.log.Log;
import io.nuls.event.bus.event.handler.AbstractEventHandler;
import io.nuls.event.bus.event.service.intf.EventService;
import io.nuls.ledger.service.intf.LedgerService;

/**
 * @author facjas
 * @date 2017/11/16
 */
public class ExitConsensusHandler extends AbstractEventHandler<ExitConsensusEvent> {
    private LedgerService ledgerService = NulsContext.getInstance().getService(LedgerService.class);
    private EventService eventService = NulsContext.getInstance().getService(EventService.class);

    @Override
    public void onEvent(ExitConsensusEvent event, String fromId) {
        PocExitConsensusTransaction tx = (PocExitConsensusTransaction) event.getEventBody();
        try {
            ledgerService.verifyAndCacheTx(tx);
        } catch (NulsException e) {
            Log.error(e);
            return;
        }
        this.eventService.broadcastHashAndCache(event);
    }
}
