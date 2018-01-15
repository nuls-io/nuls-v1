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
public class TransferTransaction<T extends BaseNulsData> extends AbstractCoinTransaction<T> {

    public TransferTransaction() {
        this(TransactionConstant.TX_TYPE_TRANSFER);
    }

    public TransferTransaction(CoinTransferData params, String password) throws NulsException {
        this(TransactionConstant.TX_TYPE_TRANSFER, params, password);
    }

    protected TransferTransaction(int type, CoinTransferData params, String password) throws NulsException {
        super(type, params, password);
    }

    protected TransferTransaction(int type) {
        super(type);
    }

    @Override
    protected T parseTxData(NulsByteBuffer byteBuffer) {
        return null;
    }

}
