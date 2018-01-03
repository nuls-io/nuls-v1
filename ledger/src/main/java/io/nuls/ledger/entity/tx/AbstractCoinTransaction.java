package io.nuls.ledger.entity.tx;

import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.crypto.Utils;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;
import io.nuls.ledger.entity.CoinData;
import io.nuls.ledger.entity.listener.CoinDataTxListener;
import io.nuls.ledger.entity.params.CoinTransferData;
import io.nuls.ledger.entity.validator.CoinDataValidator;
import io.nuls.ledger.service.intf.CoinDataProvider;

import java.io.IOException;

/**
 * @author Niels
 * @date 2017/11/14
 */
public abstract class AbstractCoinTransaction<T extends BaseNulsData> extends Transaction<T> {

    private CoinDataProvider coinDataProvider = NulsContext.getInstance().getService(CoinDataProvider.class);

    protected CoinData coinData;

    public AbstractCoinTransaction(int type) {
        this(type, null, null);
    }

    public AbstractCoinTransaction(int type, CoinTransferData coinParam, String password) {
        super(type);
        if (null != coinParam) {
            this.coinData = this.coinDataProvider.createTransferData(coinParam, password);
        }
        this.registerValidator(CoinDataValidator.getInstance());
        this.registerListener(CoinDataTxListener.getInstance());
    }

    @Override
    public int size() {
        int size = super.size();
        size += Utils.sizeOfSerialize(coinData);
        return size;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        super.serializeToStream(stream);
        stream.writeNulsData(coinData);
    }

    @Override
    protected void parse(NulsByteBuffer byteBuffer) throws NulsException {
        super.parse(byteBuffer);
        this.coinData = coinDataProvider.parse(byteBuffer);
    }

    public CoinDataProvider getCoinDataProvider() {
        return coinDataProvider;
    }


    public final CoinTransferData getCoinTransferData() {
        return this.getCoinDataProvider().getTransferData(this.coinData);
    }

    public final CoinData getCoinData() {
        return coinData;
    }


}
