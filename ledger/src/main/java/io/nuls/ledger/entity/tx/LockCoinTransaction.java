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
public class LockCoinTransaction<T extends BaseNulsData>  extends AbstractCoinTransaction<T> {

    public LockCoinTransaction() {
        this(TransactionConstant.TX_TYPE_LOCK,null,null);
    }

    public LockCoinTransaction(CoinTransferData params, String password) {
        this(TransactionConstant.TX_TYPE_LOCK, params, password);

    }

    protected LockCoinTransaction(int type, CoinTransferData params, String password) {
        super(type,params,password);
    }

    protected LockCoinTransaction(int type) {
        super(type);
    }


    @Override
    protected T parseTxData(NulsByteBuffer byteBuffer) throws NulsException {
        return null;
    }

}
