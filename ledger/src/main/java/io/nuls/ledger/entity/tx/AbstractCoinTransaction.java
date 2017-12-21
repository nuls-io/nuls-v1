package io.nuls.ledger.entity.tx;

import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.context.NulsContext;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;
import io.nuls.ledger.entity.CoinData;
import io.nuls.ledger.service.intf.CoinDataProvider;

import java.io.IOException;

/**
 * @author Niels
 * @date 2017/11/14
 */
public abstract class AbstractCoinTransaction<T extends BaseNulsData> extends Transaction {
    private CoinDataProvider coinDataProvider = NulsContext.getInstance().getService(CoinDataProvider.class);
    private T txData;

    protected  CoinData coinData;

    public AbstractCoinTransaction(int type) {
        super(type);
    }

    public T getTxData() {
        return txData;
    }

    public void setTxData(T txData) {
        this.txData = txData;
    }

    @Override
    public int size() {
        int size = super.size();
        size += this.coinData.size();
        if (null != txData) {
            size += txData.size();
        }
        return size;
    }

    @Override
    public void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        super.serializeToStream(stream);
        this.coinData.serializeToStream(stream);
        if (txData != null) {
            this.txData.serializeToStream(stream);
        }
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) {
        super.parse(byteBuffer);
        this.coinData = coinDataProvider.parse(byteBuffer);
        if (!byteBuffer.isFinished()) {
            txData = parseBody(byteBuffer);
        }
    }

    protected abstract T parseBody(NulsByteBuffer byteBuffer);


    protected CoinDataProvider getCoinDataProvider() {
        return coinDataProvider;
    }
}
