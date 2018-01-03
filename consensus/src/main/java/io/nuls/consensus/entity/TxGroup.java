package io.nuls.consensus.entity;

import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.chain.manager.TransactionManager;
import io.nuls.core.crypto.VarInt;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.crypto.Utils;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;
import io.nuls.core.utils.log.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Niels
 * @date 2017/12/18
 */
public class TxGroup extends BaseNulsData {

    private long blockHeight;

    private List<Transaction> txList;

    @Override
    public int size() {
        int size = 0;
        size += Utils.sizeOfSerialize(blockHeight);
        size += VarInt.sizeOf(txList.size());
        for (Transaction tx : txList) {
            size += Utils.sizeOfSerialize(tx);
        }
        return size;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeVarInt(blockHeight);
        stream.writeVarInt(txList.size());
        for (Transaction tx : txList) {
            stream.writeNulsData(tx);
        }
    }

    @Override
    protected void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.blockHeight=byteBuffer.readVarInt();
        long txCount = byteBuffer.readVarInt();
        this.txList = new ArrayList<>();
        for (int i = 0; i < txCount; i++) {
            try {
                txList.add(TransactionManager.getInstance(byteBuffer));
            } catch (IllegalAccessException e) {
                Log.error(e);
                throw new NulsRuntimeException(e);
            } catch (InstantiationException e) {
                Log.error(e);
                throw new NulsRuntimeException(e);
            }

        }

    }

    public long getBlockHeight() {
        return blockHeight;
    }

    public void setBlockHeight(long blockHeight) {
        this.blockHeight = blockHeight;
    }

    public List<Transaction> getTxList() {
        return txList;
    }

    public void setTxList(List<Transaction> txList) {
        this.txList = txList;
    }
}
