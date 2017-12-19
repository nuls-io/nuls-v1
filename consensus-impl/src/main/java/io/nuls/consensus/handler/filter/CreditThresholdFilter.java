package io.nuls.consensus.handler.filter;

import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.consensus.event.JoinConsensusEvent;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.context.NulsContext;
import io.nuls.event.bus.event.filter.NulsEventFilter;
import io.nuls.event.bus.event.filter.NulsEventFilterChain;
import io.nuls.ledger.service.intf.LedgerService;

import java.util.List;

/**
 * @author Niels
 * @date 2017/12/19
 */
public class CreditThresholdFilter implements NulsEventFilter<JoinConsensusEvent> {

    private static final CreditThresholdFilter INSTANCE = new CreditThresholdFilter();
    private LedgerService ledgerService = NulsContext.getInstance().getService(LedgerService.class);

    private CreditThresholdFilter() {
    }

    public static final CreditThresholdFilter getInstance() {
        return INSTANCE;
    }

    @Override
    public void doFilter(JoinConsensusEvent event, NulsEventFilterChain chain) {
        String address = event.getEventBody().getTxData().getAddress();
        List<Transaction> list = ledgerService.queryListByAccount(address, PocConsensusConstant.TX_TYPE_RED_PUNISH, 0);
        if (null == list || list.isEmpty()) {
            chain.doFilter(event);
        }

    }
}
