package io.nuls.consensus.handler.filter;

import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.consensus.event.JoinConsensusEvent;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.context.NulsContext;
import io.nuls.event.bus.bus.filter.NulsBusFilter;
import io.nuls.event.bus.bus.filter.NulsBusFilterChain;
import io.nuls.ledger.service.intf.LedgerService;

import java.util.List;

/**
 * @author Niels
 * @date 2017/12/19
 */
public class CreditThresholdBusFilter implements NulsBusFilter<JoinConsensusEvent> {

    private static final CreditThresholdBusFilter INSTANCE = new CreditThresholdBusFilter();
    private LedgerService ledgerService = NulsContext.getInstance().getService(LedgerService.class);

    private CreditThresholdBusFilter() {
    }

    public static final CreditThresholdBusFilter getInstance() {
        return INSTANCE;
    }

    @Override
    public void doFilter(JoinConsensusEvent event, NulsBusFilterChain chain) {
        String address = event.getEventBody().getTxData().getAddress();
        List<Transaction> list = ledgerService.queryListByAccount(address, PocConsensusConstant.TX_TYPE_RED_PUNISH, 0);
        if (null == list || list.isEmpty()) {
            chain.doFilter(event);
        }

    }
}
