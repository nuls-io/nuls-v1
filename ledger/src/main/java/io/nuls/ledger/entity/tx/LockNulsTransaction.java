package io.nuls.ledger.entity.tx;

import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.constant.TransactionConstant;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.ledger.entity.params.LockData;

/**
 * @author Niels
 * @date 2017/11/20
 */
public class LockNulsTransaction extends AbstractCoinTransaction {

    public LockNulsTransaction() {
        super(TransactionConstant.TX_TYPE_LOCK);
    }

    public LockNulsTransaction(LockData params, String password) {
        this(TransactionConstant.TX_TYPE_LOCK, params, password);

    }

    protected LockNulsTransaction(int type, LockData params, String password) {
        super(type);
        this.coinData = this.getCoinDataProvider().createLockCoinData(params, password);
    }

    protected LockNulsTransaction(int type) {
        super(type);
    }


    @Override
    protected BaseNulsData parseBody(NulsByteBuffer byteBuffer) {
        return null;
    }

    public LockData getLockData(){
        return this.getCoinDataProvider().getLockData(this.coinData);
    }

}
