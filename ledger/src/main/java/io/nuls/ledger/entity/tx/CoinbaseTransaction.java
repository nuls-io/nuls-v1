package io.nuls.ledger.entity.tx;

import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.constant.TransactionConstant;

/**
 * @author Niels
 * @date 2017/12/4
 */
public class CoinbaseTransaction<T extends BaseNulsData> extends AbstractCoinTransaction<T> {
    public CoinbaseTransaction() {
        super(TransactionConstant.TX_TYPE_COIN_BASE);
    }

}
