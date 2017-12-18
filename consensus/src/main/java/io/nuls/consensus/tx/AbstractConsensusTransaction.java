package io.nuls.consensus.tx;

import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;

import java.io.IOException;

/**
 * @author Niels
 * @date 2017/12/4
 */
public abstract class AbstractConsensusTransaction<T extends BaseNulsData> extends Transaction {

    private T txData;

    public AbstractConsensusTransaction(int type) {
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
        if(null!=txData){
            size += txData.size();
        }
        return size;
    }

    @Override
    public void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        super.serializeToStream(stream);
        if(txData!=null){
            this.txData.serializeToStream(stream);
        }
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) {
        super.parse(byteBuffer);
        if(!byteBuffer.isFinished()){
            txData = parseBody(byteBuffer);
        }
    }

    protected abstract T parseBody(NulsByteBuffer byteBuffer);
}