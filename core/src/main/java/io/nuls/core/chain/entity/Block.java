package io.nuls.core.chain.entity;

import io.nuls.core.utils.io.NulsByteBuffer;

import java.util.List;

/**
 *
 * @author win10
 * @date 2017/10/30
 */
public class Block extends BlockHeader {

    public static int MAX_SIZE = 2 * 1024 * 2014;   //2M

    private BlockHeader header;

    //交易列表
    private List<Transaction> txs;

    @Override
    public int size() {
        return 0;
    }

    @Override
    public byte[] serialize() {
        return new byte[0];
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) {
    }

    public BlockHeader getHeader() {
        return header;
    }

    public void setHeader(BlockHeader header) {
        this.header = header;
    }

    public List<Transaction> getTxs() {
        return txs;
    }

    public void setTxs(List<Transaction> txs) {
        this.txs = txs;
    }
}
