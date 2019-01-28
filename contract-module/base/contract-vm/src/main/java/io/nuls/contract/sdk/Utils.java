package io.nuls.contract.sdk;

import java.math.BigInteger;

public class Utils {

    private Utils() {
    }

    /**
     * 检查条件，如果条件不满足则回滚
     *
     * @param expression 检查条件
     */
    public static void require(boolean expression) {
        if (!expression) {
            revert();
        }
    }

    /**
     * 检查条件，如果条件不满足则回滚
     *
     * @param expression 检查条件
     * @param errorMessage 错误信息
     */
    public static void require(boolean expression, String errorMessage) {
        if (!expression) {
            revert(errorMessage);
        }
    }

    /**
     * 终止执行并还原改变的状态
     */
    public static void revert() {
        revert(null);
    }

    /**
     * 终止执行并还原改变的状态
     *
     * @param errorMessage 错误信息
     */
    public static native void revert(String errorMessage);

    /**
     * 发送事件
     *
     * @param event 事件
     */
    public static native void emit(Event event);

    /**
     * @param seed a private seed
     * @return pseudo random number (0 ~ 1)
     */
    public static float pseudoRandom(long seed) {
        int hash1 = Block.currentBlockHeader().getPackingAddress().toString().substring(2).hashCode();
        int hash2 = Msg.address().toString().substring(2).hashCode();
        int hash3 = Msg.sender() != null ? Msg.sender().toString().substring(2).hashCode() : 0;
        int hash4 = Long.valueOf(Block.timestamp()).toString().hashCode();

        long hash = seed ^ hash1 ^ hash2 ^ hash3 ^ hash4;

        seed = (hash * 0x5DEECE66DL + 0xBL) & ((1L << 48) - 1);
        return ((int) (seed >>> 24) / (float) (1 << 24));
    }

    /**
     * @return pseudo random number (0 ~ 1)
     */
    public static float pseudoRandom() {
        return pseudoRandom(0x5DEECE66DL);
    }

    /**
     *
     * Please note that this is the SHA-3 FIPS 202 standard, not Keccak-256.
     *
     * @param src source string (hex encoding string)
     * @return sha3-256 hash (hex encoding string)
     */
    public static native String sha3(String hexString);

    /**
     *
     * Please note that this is the SHA-3 FIPS 202 standard, not Keccak-256.
     *
     * @param bytes source byte array
     * @return sha3-256 hash (hex encoding string)
     */
    public static native String sha3(byte[] bytes);

    /**
     * verify signature data(ECDSA)
     *
     * @param data(hex encoding string)
     * @param signature(hex encoding string)
     * @param pubkey(hex encoding string)
     * @return verify result
     */
    public static native boolean verifySignatureData(String data, String signature, String pubkey);

    /**
     * @param endHeight
     * @param count
     * @param algorithm
     * @return
     */
    public static native BigInteger getRandomSeedByCount(long endHeight, int count, String algorithm);

    /**
     * @param endHeight
     * @param count
     * @return
     */
    public static BigInteger getRandomSeedByCount(long endHeight, int count) {
        return getRandomSeedByCount(endHeight, count, "SHA3");
    }

    /**
     * @param startHeight
     * @param endHeight
     * @param algorithm
     * @return
     */
    public static native BigInteger getRandomSeedByHeight(long startHeight, long endHeight, String algorithm);

    /**
     * @param startHeight
     * @param endHeight
     * @return
     */
    public static BigInteger getRandomSeedByHeight(long startHeight, long endHeight){
        return getRandomSeedByHeight(startHeight, endHeight, "SHA3");
    }
}
