package io.nuls.contract.sdk;

import java.math.BigInteger;

public class Msg {

    /**
     * 剩余Gas
     * remaining gas
     *
     * @return
     */
    public static native long gasleft();

    /**
     * 消息发送者地址
     * sender of the message
     *
     * @return
     */
    public static native Address sender();

    /**
     * 随消息发送的Na数
     * number of na sent with the message
     *
     * @return
     */
    public static native BigInteger value();

    /**
     * Gas价格
     * gas price
     *
     * @return
     */
    public static native long gasprice();

    /**
     * 合约地址
     * contract address
     *
     * @return
     */
    public static native Address address();

}
