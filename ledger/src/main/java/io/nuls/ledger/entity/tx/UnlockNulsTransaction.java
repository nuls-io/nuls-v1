package io.nuls.ledger.entity.tx;

import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.constant.TransactionConstant;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.ledger.entity.params.CoinTransferData;

/**
 * @author Niels
 * @date 2017/11/20
 */
public class UnlockNulsTransaction<T extends BaseNulsData> extends AbstractCoinTransaction<T> {
    public UnlockNulsTransaction() {
        this(TransactionConstant.TX_TYPE_UNLOCK);
    }

    public UnlockNulsTransaction(CoinTransferData params, String password) throws NulsException {
        this(TransactionConstant.TX_TYPE_UNLOCK, params, password);

    }

    protected UnlockNulsTransaction(int type, CoinTransferData params, String password) throws NulsException {
        super(type, params, password);
    }

    protected UnlockNulsTransaction(int type) {
        super(type);
    }

    @Override
    protected T parseTxData(NulsByteBuffer byteBuffer) throws NulsException {
        return null;
    }
}
