package io.nuls.consensus.event.filter;

import io.nuls.consensus.cache.manager.tx.ConfirmingTxCacheManager;
import io.nuls.consensus.cache.manager.tx.ReceivedTxCacheManager;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.event.bus.filter.NulsEventFilter;
import io.nuls.event.bus.filter.NulsEventFilterChain;
import io.nuls.ledger.event.TransactionEvent;

/**
 * @author Niels
 * @date 2018/1/8
 */
public class NewTxEventFilter implements NulsEventFilter<TransactionEvent> {

    private ReceivedTxCacheManager cacheManager = ReceivedTxCacheManager.getInstance();
    private ConfirmingTxCacheManager confirmingTxCacheManager = ConfirmingTxCacheManager.getInstance();

    @Override
    public void doFilter(TransactionEvent event, NulsEventFilterChain chain) {
        Transaction tx = event.getEventBody();
        if (null == tx||null == tx.getHash()) {
            return;
        }
        boolean exist = cacheManager.txExist(tx.getHash());
        if (exist) {
            return;
        }
        exist = null != confirmingTxCacheManager.getTx(tx.getHash());
        if (exist) {
            return;
        }
        chain.doFilter(event);
    }
}
