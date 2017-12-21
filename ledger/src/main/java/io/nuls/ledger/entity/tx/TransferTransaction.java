package io.nuls.ledger.entity.tx;

import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.constant.TransactionConstant;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;
import io.nuls.ledger.entity.CoinData;
import io.nuls.ledger.entity.params.TransferData;

import java.io.IOException;

/**
 * @author Niels
 * @date 2017/11/20
 */
public class TransferTransaction<T extends BaseNulsData> extends AbstractCoinTransaction<T> {

    public TransferTransaction() {
        super(TransactionConstant.TX_TYPE_TRANSFER);
    }

    public TransferTransaction(TransferData params, String password) {
        this(TransactionConstant.TX_TYPE_TRANSFER, params, password);
    }

    protected TransferTransaction(int type, TransferData params, String password) {
        super(type);
        this.coinData = this.getCoinDataProvider().createTransferCoinData(params, password);
    }

    protected TransferTransaction(int type) {
        super(type);
    }

    @Override
    protected T parseBody(NulsByteBuffer byteBuffer) {
        return null;
    }

    public TransferData getTransferData() {
        return this.getCoinDataProvider().getTransferData(this.coinData);
    }

}
