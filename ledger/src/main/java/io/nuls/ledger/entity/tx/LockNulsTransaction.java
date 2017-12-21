package io.nuls.ledger.entity.tx;

import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.constant.TransactionConstant;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.ledger.entity.params.CoinTransferData;

/**
 * @author Niels
 * @date 2017/11/20
 */
public class LockNulsTransaction<T extends BaseNulsData>  extends AbstractCoinTransaction<T> {

    public LockNulsTransaction() {
        super(TransactionConstant.TX_TYPE_LOCK);
    }

    public LockNulsTransaction(CoinTransferData params, String password) {
        this(TransactionConstant.TX_TYPE_LOCK, params, password);

    }

    protected LockNulsTransaction(int type, CoinTransferData params, String password) {
        super(type,params,password);
    }

    protected LockNulsTransaction(int type) {
        super(type);
    }


    @Override
    protected T parseTxData(NulsByteBuffer byteBuffer) {
        return null;
    }

}
