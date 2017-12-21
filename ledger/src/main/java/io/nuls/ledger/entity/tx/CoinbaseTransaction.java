package io.nuls.ledger.entity.tx;

import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.constant.TransactionConstant;
import io.nuls.core.utils.io.NulsByteBuffer;

/**
 * @author Niels
 * @date 2017/12/4
 */
public class CoinbaseTransaction<T extends BaseNulsData> extends AbstractCoinTransaction<T> {
    public CoinbaseTransaction() {
        super(TransactionConstant.TX_TYPE_COIN_BASE);
    }

    @Override
    protected T parseBody(NulsByteBuffer byteBuffer) {
        // todo auto-generated method stub(niels)
        return null;
    }
}
