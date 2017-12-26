package io.nuls.ledger.entity.tx;

import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.constant.TransactionConstant;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.ledger.entity.params.CoinTransferData;

/**
 * @author Niels
 * @date 2017/12/4
 */
public class CoinBaseTransaction<T extends BaseNulsData> extends AbstractCoinTransaction<T> {

    public CoinBaseTransaction() {
        this(TransactionConstant.TX_TYPE_COIN_BASE, null, null);
    }

    public CoinBaseTransaction(CoinTransferData params, String password) {
        this(TransactionConstant.TX_TYPE_TRANSFER, params, password);
    }

    protected CoinBaseTransaction(int type, CoinTransferData params, String password) {
        super(type, params, password);
    }

    protected CoinBaseTransaction(int type) {
        super(type);
    }

    @Override
    protected T parseTxData(NulsByteBuffer byteBuffer) {
        // todo auto-generated method stub(niels)
        return null;
    }
}
