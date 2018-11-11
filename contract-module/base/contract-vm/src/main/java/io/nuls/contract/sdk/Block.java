package io.nuls.contract.sdk;

public class Block {

    /**
     * 给定块的区块头
     *
     * @param blockNumber 区块高度
     * @return 给定块的区块头
     */
    public static native BlockHeader getBlockHeader(long blockNumber);

    /**
     * 当前块的区块头
     *
     * @return 当前块的区块头
     */
    public static native BlockHeader currentBlockHeader();

    /**
     * 最新块的区块头
     *
     * @return 最新块的区块头
     */
    public static native BlockHeader newestBlockHeader();

    /**
     * 给定块的哈希值
     * hash of the given block
     *
     * @param blockNumber
     * @return 给定块的哈希值
     */
    public static String blockhash(long blockNumber) {
        return getBlockHeader(blockNumber).getHash();
    }

    /**
     * 当前块矿工地址
     * current block miner’s address
     *
     * @return 地址
     */
    public static Address coinbase() {
        return currentBlockHeader().getPackingAddress();
    }

    /**
     * 当前块编号
     * current block number
     *
     * @return number
     */
    public static long number() {
        return currentBlockHeader().getHeight();
    }

    /**
     * 当前块时间戳
     * current block timestamp
     *
     * @return timestamp
     */
    public static long timestamp() {
        return currentBlockHeader().getTime();
    }

}
