package io.nuls.consensus.cache.manager.tx;

import io.nuls.core.chain.entity.Transaction;

import java.util.List; /**
 * @author Niels
 * @date 2018/1/5
 */
public class ConfirmingTxCacheManager {
    private static ConfirmingTxCacheManager INSTANCE = new ConfirmingTxCacheManager();
    private ConfirmingTxCacheManager(){}
    public static ConfirmingTxCacheManager getInstance() {
        return INSTANCE;
    }

    public void putTxList(List<Transaction> txs) {
        // todo auto-generated method stub(niels)

    }
}
