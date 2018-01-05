package io.nuls.consensus.event.filter;

import io.nuls.consensus.event.JoinConsensusEvent;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.constant.TransactionConstant;
import io.nuls.core.context.NulsContext;
import io.nuls.event.bus.filter.NulsEventFilter;
import io.nuls.event.bus.filter.NulsEventFilterChain;
import io.nuls.ledger.service.intf.LedgerService;

import java.util.List;

/**
 * @author Niels
 * @date 2017/12/19
 */
public class CreditThresholdEventFilter implements NulsEventFilter<JoinConsensusEvent> {

    private static final CreditThresholdEventFilter INSTANCE = new CreditThresholdEventFilter();
    private LedgerService ledgerService = NulsContext.getInstance().getService(LedgerService.class);

    private CreditThresholdEventFilter() {
    }

    public static final CreditThresholdEventFilter getInstance() {
        return INSTANCE;
    }

    @Override
    public void doFilter(JoinConsensusEvent event, NulsEventFilterChain chain) {
        String address = event.getEventBody().getTxData().getAddress();
        List<Transaction> list = ledgerService.queryListByAccount(address, TransactionConstant.TX_TYPE_RED_PUNISH, 0);
        if (null == list || list.isEmpty()) {
            chain.doFilter(event);
        }

    }
}
