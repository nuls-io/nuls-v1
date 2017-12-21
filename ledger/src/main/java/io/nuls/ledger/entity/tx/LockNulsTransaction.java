package io.nuls.ledger.entity.tx;

import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.chain.entity.Na;
import io.nuls.core.constant.TransactionConstant;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;
import io.nuls.core.utils.log.Log;
import io.nuls.ledger.entity.CoinData;
import io.nuls.ledger.entity.LockData;
import io.nuls.ledger.service.intf.CoinDataProvider;

import java.io.IOException;

/**
 * @author Niels
 * @date 2017/11/20
 */
public class LockNulsTransaction extends AbstractCoinTransaction<LockData> {

    private CoinDataProvider coinDataProvider = NulsContext.getInstance().getService(CoinDataProvider.class);

    private CoinData coinData;

    public LockNulsTransaction() {
        super(TransactionConstant.TX_TYPE_LOCK);
    }

    @Override
    public int size() {
        int size = super.size();
        size += this.coinData.size();
        return size;
    }

    @Override
    public void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        super.serializeToStream(stream);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) {
        //todo

    }

    @Override
    protected LockData parseBody(NulsByteBuffer byteBuffer) {
        LockData data = new LockData();
        try {
            data.parse(byteBuffer);
        } catch (NulsException e) {
            Log.error(e);
            throw new NulsRuntimeException(e);
        }
        return data;
    }

    @Override
    public void setCoinData() {
        coinData = coinDataProvider.getLockCoinData(this.getTxData());
    }

}
