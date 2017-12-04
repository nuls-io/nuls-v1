package io.nuls.core.chain.entity;

import io.nuls.core.crypto.Sha256Hash;
import io.nuls.core.utils.io.NulsByteBuffer;

import java.util.List;

/**
 * @author win10
 * @date 2017/10/30
 */
public class Block extends BlockHeader {

    public static int MAX_SIZE = 2 * 1024 * 2014;   //2M


    public Block(long height, long time) {
        super(height, time);
        this.height = height;
        this.time = time;
    }

    public Block(long height, long time, Sha256Hash preHash) {
        super(height, time, preHash);
        this.height = height;
        this.time = time;
        this.preHash = preHash;
    }

    //交易列表
    private List<Transaction> txs;

    @Override
    protected int dataSize() {
        return 0;
    }

    @Override
    protected void parseObject(NulsByteBuffer byteBuffer) {
    }

    public List<Transaction> getTxs() {
        return txs;
    }

    public void setTxs(List<Transaction> txs) {
        this.txs = txs;
    }
}
