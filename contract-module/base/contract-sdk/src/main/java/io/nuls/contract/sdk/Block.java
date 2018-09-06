package io.nuls.contract.sdk;

public class Block {

    /**
     * 给定块的区块头
     *
     * @param blockNumber
     * @return
     */
    public static native BlockHeader getBlockHeader(long blockNumber);

    /**
     * 当前块的区块头
     *
     * @return
     */
    public static native BlockHeader currentBlockHeader();

    /**
     * 给定块的哈希值
     * hash of the given block
     *
     * @param blockNumber
     * @return
     */
    public static String blockhash(long blockNumber) {
        return getBlockHeader(blockNumber).getHash();
    }

    /**
     * 当前块矿工地址
     * current block miner’s address
     *
     * @return
     */
    public static Address coinbase() {
        return currentBlockHeader().getPackingAddress();
    }

    /**
     * 当前块编号
     * current block number
     *
     * @return
     */
    public static long number() {
        return currentBlockHeader().getHeight();
    }

    /**
     * 当前块时间戳
     * current block timestamp
     *
     * @return
     */
    public static long timestamp() {
        return currentBlockHeader().getTime();
    }

}
