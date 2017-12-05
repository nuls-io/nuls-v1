package io.nuls.core.chain.entity;

import io.nuls.core.crypto.Sha256Hash;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;

import java.io.IOException;
import java.util.List;

/**
 * @author win10
 * @date 2017/10/30
 */
public class Block extends BlockHeader {
    /**
     * 2M
     */
    public static int MAX_SIZE = 2 * 1024 * 2014;

    private List<Transaction> txs;

    public Block(long height, long time) {
        super(height, time);
        this.height = height;
        this.time = time;
    }

    public Block(long height, long time, NulsDigestData preHash) {
        super(height, time, preHash);
        this.height = height;
        this.time = time;
        this.preHash = preHash;
    }


    @Override
    public int size() {
        int size = super.size();
        for (Transaction tx : txs) {
            size += tx.size();
        }
        return size;
    }

    @Override
    public void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        super.serializeToStream(stream);
        for (Transaction tx : txs) {
            stream.write(tx.serialize());
        }
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) {
        super.parse(byteBuffer);
        while(byteBuffer.isFinished()){
            //todo
        }
    }

    public List<Transaction> getTxs() {
        return txs;
    }

    public void setTxs(List<Transaction> txs) {
        this.txs = txs;
    }
}
