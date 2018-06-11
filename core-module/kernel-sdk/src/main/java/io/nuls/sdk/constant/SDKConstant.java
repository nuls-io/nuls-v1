package io.nuls.sdk.constant;

/**
 * @author: Charlie
 * @date: 2018/6/11
 */
public interface SDKConstant {

    public static String DEFAULT_ENCODING = "UTF-8";

    /**
     * 默认链id（nuls主链）,链id会影响地址的生成，当前地址以“Ns”开头
     * The default chain id (nuls main chain), the chain id affects the generation of the address,
     * and the current address begins with "Ns".16402.
     */
    public static short DEFAULT_CHAIN_ID = 1;

    /**
     * 空值占位符
     * Null placeholder.
     */
    byte[] PLACE_HOLDER = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};

    /**
     * 48位整型数据长度
     * 48 bit integer data length.
     */
    int INT48_VALUE_LENGTH = 6;
}
